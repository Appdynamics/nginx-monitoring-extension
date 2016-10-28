package com.appdynamics.monitors.nginx;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by adityajagtiani on 8/31/16.
 */
public class NGinXMonitorTask implements Runnable {

    public static final Logger logger = Logger.getLogger(NGinXMonitor.class);
    private static final String METRIC_SEPARATOR = "|";
    private Map server;
    private MonitorConfiguration configuration;

    public NGinXMonitorTask(MonitorConfiguration configuration, Map server) {
        this.configuration = configuration;
        this.server = server;
    }

    public void run() {
        try {
            Map<String, String> resultMap = populate();
            String metricPrefix = configuration.getMetricPrefix();
            printAllMetrics(metricPrefix, resultMap);
            logger.info("NGinX Metric Upload Complete");
        } catch (Exception e) {
            configuration.getMetricWriter().registerError(e.getMessage(), e);
            logger.error("Error while running the task", e);
        }
    }

    private Map<String, String> populate() throws IOException, TaskExecutionException {
        CloseableHttpClient httpClient = configuration.getHttpClient();
        CloseableHttpResponse response = null;
        try {
            String url = UrlBuilder.fromYmlServerConfig(server).build();
            HttpGet get = new HttpGet(url);
            response = httpClient.execute(get);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity, "UTF-8");
            Map<String, String> resultMap = new HashMap<String, String>();
            if(responseBody == null) {
                logger.error("Response body doesn't exist");
            }
            else {
                String header = response.getFirstHeader("Content-Type").getValue();
                if (header != null && header.contains("application/json")) {
                    JSONResponseParser jsonParser = new JSONResponseParser();
                    resultMap = jsonParser.parseResponse(responseBody);
                } else {
                    if (header != null && header.contains("text/plain")) {
                        PlainTextResponseParser plainTextParser = new PlainTextResponseParser();
                        resultMap = plainTextParser.parseResponse(responseBody);
                    } else {
                        logger.error("Invalid content type [ " + header + " ] for URL " + url);
                        throw new TaskExecutionException("Invalid content type [ " + header + " ] for URL " + url);
                    }
                }
            }
            return resultMap;
        }
        finally {
            if(response != null) {
                response.close();
            }
        }
    }

    /**
     * @param metricPrefix
     * @param resultMap
     */
    private void printAllMetrics(String metricPrefix, Map<String, String> resultMap) {
        for (Map.Entry<String, String> metricEntry : resultMap.entrySet()) {
            printMetric(metricPrefix, metricEntry.getKey(), metricEntry.getValue());
        }
    }

    /**
     * Returns the metric to the AppDynamics Controller.
     *
     * @param metricPrefix Metric prefix
     * @param metricName   Name of the Metric
     * @param metricValue  Value of the Metric
     */
    private void printMetric(String metricPrefix, String metricName, Object metricValue) {

        String aggregation = MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION;
        String timeRollup = MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE;
        String cluster = MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE;
        String serverName = (String) server.get("name");
        String metricPath = metricPrefix + METRIC_SEPARATOR + serverName + METRIC_SEPARATOR + metricName;
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        metricWriter.printMetric(metricPath, metricValue.toString(), aggregation, timeRollup, cluster);

        if (logger.isDebugEnabled()) {
            logger.debug("Metric [" + aggregation + "/" + timeRollup + "/" + cluster
                    + "] metric = " + metricPath + " = " + metricValue);
        }
    }
}