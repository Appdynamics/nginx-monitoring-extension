package com.appdynamics.monitors.nginx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import com.singularity.ee.util.httpclient.HttpClientWrapper;
import com.singularity.ee.util.httpclient.HttpExecutionRequest;
import com.singularity.ee.util.httpclient.HttpExecutionResponse;
import com.singularity.ee.util.httpclient.HttpOperation;
import com.singularity.ee.util.httpclient.IHttpClientWrapper;
import com.singularity.ee.util.log4j.Log4JLogger;

/**
 * NGinXStatusMonitor is a class that provides metrics on NGinX server by using the 
 * NGinX status stub.
 */
public class NGinXMonitor extends AManagedMonitor
{
	/**
	 * The metric can be found in Application Infrastructure Performance|{@literal <}Node{@literal >}|Custom Metrics|WebServer|NGinX|Status
	 */
	private final static String metricPrefix = "Custom Metrics|WebServer|NGinX|Status|";

	protected volatile String host;
	protected volatile String port;
	protected volatile String location;

	private Map<String,String> resultMap = new HashMap<String,String>();

	protected final Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Main execution method that uploads the metrics to the AppDynamics Controller
	 * @see com.singularity.ee.agent.systemagent.api.ITask#execute(java.util.Map, com.singularity.ee.agent.systemagent.api.TaskExecutionContext)
	 */
	public TaskOutput execute(Map<String, String> arg0, TaskExecutionContext arg1)
			throws TaskExecutionException
	{
		try 
		{
			host = arg0.get("host");
			port = arg0.get("port");
			location = arg0.get("location");

			populate();

			printMetric("Activity|up", 1,
				MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION, 
				MetricWriter.METRIC_TIME_ROLLUP_TYPE_SUM,
				MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);

			printMetric("Activity|Active Connections", resultMap.get("ACTIVE_CONNECTIONS"),
				MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
				MetricWriter.METRIC_TIME_ROLLUP_TYPE_CURRENT,
				MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL
			);

			printMetric("Activity|Server|Accepts", resultMap.get("SERVER_ACCEPTS"),
				MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
				MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
				MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
			);

			printMetric("Activity|Server|Handled", resultMap.get("SERVER_HANDLED"),
				MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
				MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
				MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
			);

			printMetric("Activity|Server|Requests", resultMap.get("SERVER_REQUESTS"),
				MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
				MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
				MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
			);

			printMetric("Activity|Reading", resultMap.get("READING"),
				MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
				MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
				MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
			);

			printMetric("Activity|Writing", resultMap.get("WRITING"),
				MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
				MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
				MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
			);

			printMetric("Activity|Waiting", resultMap.get("WAITING"),
				MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
				MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
				MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
			);

			return new TaskOutput("NGinX Metric Upload Complete");
		}
		catch (Exception e)
		{
			return new TaskOutput("Error: " + e);
		}
	}

	/**
	 * Fetches Statistics from NGinX Server
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	protected void populate() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, IOException
	{
		IHttpClientWrapper httpClient = HttpClientWrapper.getInstance();

		HttpExecutionRequest request = new HttpExecutionRequest(getConnectionURL(), "", HttpOperation.GET);
		HttpExecutionResponse response = httpClient.executeHttpOperation(request, new Log4JLogger(logger));

		Pattern numPattern = Pattern.compile("\\d+");
		Matcher numMatcher;

		BufferedReader reader = new BufferedReader(new StringReader(response.getResponseBody()));
		String line, whiteSpaceRegex = "\\s";

		while ((line=reader.readLine()) != null)
		{
			if (line.matches("Active connections"))
			{
				numMatcher = numPattern.matcher(line);
				numMatcher.find();
				resultMap.put("ACTIVE_CONNECTIONS", numMatcher.group());
			}
			else if (line.matches("server"))
			{
				line = reader.readLine();

				String[] results = line.trim().split(whiteSpaceRegex);

				resultMap.put("SERVER_ACCEPTS", results[0]);
				resultMap.put("SERVER_HANDLED", results[1]);
				resultMap.put("SERVER_REQUESTS", results[2]);
			}
			else if (line.contains("Reading"))
			{
				String[] results = line.trim().split(whiteSpaceRegex);
				resultMap.put("READING", results[1]);
				resultMap.put("WRITING", results[3]);
				resultMap.put("WAITING", results[5]);
			}
		}
	}

	/**
	 * Returns the metric to the AppDynamics Controller.
	 * @param 	metricName		Name of the Metric
	 * @param 	metricValue		Value of the Metric
	 * @param 	aggregation		Average OR Observation OR Sum
	 * @param 	timeRollup		Average OR Current OR Sum
	 * @param 	cluster			Collective OR Individual
	 */
	public void printMetric(String metricName, Object metricValue, String aggregation, String timeRollup, String cluster)
	{
		MetricWriter metricWriter = getMetricWriter(getMetricPrefix() + metricName, 
			aggregation,
			timeRollup,
			cluster
		);

		metricWriter.printMetric(String.valueOf(metricValue));
	}

	protected String getConnectionURL()
	{
		return "http://" + host + ":" + port + "/" + location;
	}

	protected String getMetricPrefix()
	{
		return metricPrefix;
	}
}