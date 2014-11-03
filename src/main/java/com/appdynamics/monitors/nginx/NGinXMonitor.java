/**
 * Copyright 2014 AppDynamics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.monitors.nginx;

import com.appdynamics.extensions.ArgumentsValidator;
import com.appdynamics.extensions.http.Response;
import com.appdynamics.extensions.http.SimpleHttpClient;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.monitors.nginx.statsExtractor.CachesStatsExtractor;
import com.appdynamics.monitors.nginx.statsExtractor.ConnectionsStatsExtractor;
import com.appdynamics.monitors.nginx.statsExtractor.RequestsStatsExtractor;
import com.appdynamics.monitors.nginx.statsExtractor.ServerZoneStatsExtractor;
import com.appdynamics.monitors.nginx.statsExtractor.StatsExtractor;
import com.appdynamics.monitors.nginx.statsExtractor.UpstreamsStatsExtractor;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * NGinXStatusMonitor is a class that provides metrics on NGinX server by using the
 * NGinX status stub.
 */
public class NGinXMonitor extends AManagedMonitor {
    /**
     * The metric can be found in Application Infrastructure Performance|{@literal <}Node{@literal >}|Custom Metrics|WebServer|NGinX|Status
     */
    private static final Logger logger = Logger.getLogger(NGinXMonitor.class);

    private static final String METRIC_SEPARATOR = "|";

    private static final Map<String, String> DEFAULT_ARGS = new HashMap<String, String>() {{
        put("host", "localhost");
        put("port", "8080");
        put("location", "nginx_status");
        put("metric-prefix", "Custom Metrics|WebServer|NGinX");
    }};

    public NGinXMonitor() {
        String version = getClass().getPackage().getImplementationTitle();
        String msg = String.format("Using Monitor Version [%s]", version);
        logger.info(msg);
        System.out.println(msg);
    }

    /**
     * Main execution method that uploads the metrics to the AppDynamics Controller
     *
     * @see com.singularity.ee.agent.systemagent.api.ITask#execute(java.util.Map, com.singularity.ee.agent.systemagent.api.TaskExecutionContext)
     */
    public TaskOutput execute(Map<String, String> argsMap, TaskExecutionContext executionContext)
            throws TaskExecutionException {
        try {

            logger.debug("The args map before filling the default is:  " + argsMap);
            argsMap = ArgumentsValidator.validateArguments(argsMap, DEFAULT_ARGS);
            logger.debug("The args map after filling the default is: " + argsMap);

            Map<String, String> resultMap = populate(argsMap);

            String metricPrefix = argsMap.get("metric-prefix");

            printAllMetrics(metricPrefix, resultMap);

            logger.info("NGinX Metric Upload Complete");
            return new TaskOutput("NGinX Metric Upload Complete");
        } catch (Exception e) {
            e.printStackTrace();
            return new TaskOutput("Error: " + e);
        }
    }

    /**
     * Fetches Statistics from NGinX Server
     *
     * @param argsMap arguments passed
     */
    private Map<String, String> populate(Map<String, String> argsMap) throws IOException, TaskExecutionException {

        SimpleHttpClient httpClient = SimpleHttpClient.builder(argsMap).build();
        String url = UrlBuilder.builder(argsMap).path(argsMap.get("location")).build();
        Response response = httpClient.target(url).get();
        String responseBody = response.string();
        String header = response.getHeader("Content-Type");

        Map<String, String> resultMap = null;
        if ("application/json".equals(header)) {
            resultMap = parsePlusStatsResult(responseBody);
        } else if ("text/plain".equals(header)) {
            resultMap = parseStubStatsResults(responseBody);
        } else {
            logger.error("Invalid content type for URL " + url);
            throw new TaskExecutionException("Invalid content type for URL " + url);
        }
        return resultMap;
    }

    private Map<String, String> parsePlusStatsResult(String responseBody) {
        Map<String, String> resultMap = new HashMap<String, String>();
        JSONObject jsonObject = new JSONObject(responseBody);


        StatsExtractor connectionsStatsExtractor = new ConnectionsStatsExtractor();
        Map<String, String> connectionStats = connectionsStatsExtractor.extractStats(jsonObject);
        resultMap.putAll(connectionStats);

        StatsExtractor requestsStatsExtractor = new RequestsStatsExtractor();
        Map<String, String> requestStats = requestsStatsExtractor.extractStats(jsonObject);
        resultMap.putAll(requestStats);

        StatsExtractor serverZoneStatsExtractor = new ServerZoneStatsExtractor();
        Map<String, String> serverZonesStats = serverZoneStatsExtractor.extractStats(jsonObject);
        resultMap.putAll(serverZonesStats);

        StatsExtractor upstreamsStatsExtractor = new UpstreamsStatsExtractor();
        Map<String, String> upstreamsStats = upstreamsStatsExtractor.extractStats(jsonObject);
        resultMap.putAll(upstreamsStats);

        StatsExtractor cachesStatsExtractor = new CachesStatsExtractor();
        Map<String, String> cachesStats = cachesStatsExtractor.extractStats(jsonObject);
        resultMap.putAll(cachesStats);

        return resultMap;
    }


    private Map<String, String> parseStubStatsResults(String responseBody) throws IOException {

        Map<String, String> resultMap = new HashMap<String, String>();

        Pattern numPattern = Pattern.compile("\\d+");
        Matcher numMatcher;

        BufferedReader reader = new BufferedReader(new StringReader(responseBody));
        String line, whiteSpaceRegex = "\\s";

        while ((line = reader.readLine()) != null) {
            if (line.contains("Active connections")) {
                numMatcher = numPattern.matcher(line);
                numMatcher.find();
                resultMap.put("Active Connections", numMatcher.group());
            } else if (line.contains("server")) {
                line = reader.readLine();

                String[] results = line.trim().split(whiteSpaceRegex);

                resultMap.put("Server|Accepts", results[0]);
                resultMap.put("Server|Handled", results[1]);
                resultMap.put("Server|Requests", results[2]);
            } else if (line.contains("Reading")) {
                String[] results = line.trim().split(whiteSpaceRegex);
                resultMap.put("Reading", results[1]);
                resultMap.put("Writing", results[3]);
                resultMap.put("Waiting", results[5]);
            }
        }
        return resultMap;
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
        System.out.println(metricPrefix + METRIC_SEPARATOR + metricName+":::"+metricValue);
        MetricWriter metricWriter = getMetricWriter(metricPrefix + METRIC_SEPARATOR + metricName,
                MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE
        );

        metricWriter.printMetric(String.valueOf(metricValue));
    }

    public static void main(String[] args) {
        NGinXMonitor nGinXMonitor = new NGinXMonitor();
        try {
            nGinXMonitor.execute(new HashMap<String, String>(), null);
        } catch (TaskExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}