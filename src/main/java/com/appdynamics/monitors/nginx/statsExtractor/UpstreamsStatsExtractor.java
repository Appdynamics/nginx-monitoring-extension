/**
 * Copyright 2014 AppDynamics, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.monitors.nginx.statsExtractor;

import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.monitors.nginx.Config.MetricConfig;
import com.appdynamics.monitors.nginx.Config.Stat;
import com.google.common.collect.Lists;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class UpstreamsStatsExtractor extends StatsExtractor {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Metric> extractStats(JSONObject upstreams, Stat stat, String metricPrefix) {
        List<Metric> upstreamsStatsMetrics = Lists.newArrayList();
        try {
            Set<String> serverGroupNames = upstreams.keySet();

            for (String serverGroupName : serverGroupNames) {
                JSONObject jsonObject = upstreams.getJSONObject(serverGroupName);

                Set<String> keys = jsonObject.keySet();
                for (String key : keys) {
                    Object element = jsonObject.get(key);

                    if (element instanceof JSONArray) {
                        JSONArray serverGroups = (JSONArray) element;

                        for (int i = 0; i < serverGroups.length(); i++) {
                            JSONObject server = serverGroups.getJSONObject(i);
                            upstreamsStatsMetrics.addAll(collectUpStreamMetrics(serverGroupName, server, stat, metricPrefix));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error while collecting metrics for upstream stat", e);
        }
        return upstreamsStatsMetrics;
    }

    private List<Metric> collectUpStreamMetrics(String serverGroupName, JSONObject server, Stat stat, String metricPrefix) {
        Map<String, MetricConfig> configMetricsMap = getmetricConfigMap(stat.getMetricConfig());
        List<Metric> upStreamMetrics = Lists.newArrayList();
        String serverIp = server.getString("server");
        Set<String> keySet = server.keySet();
        if (configMetricsMap.containsKey("backup") && server.has("backup")) {
            MetricConfig config = configMetricsMap.get("backup");
            Map<String, String> propertiesMap = objectMapper.convertValue(config, Map.class);
            boolean backup = server.getBoolean("backup");
            Metric metric = new Metric(config.getAlias(), backup ? "1" : "0", metricPrefix + serverGroupName + "|" + serverIp + "|backup", propertiesMap);
            upStreamMetrics.add(metric);
        }

        if (configMetricsMap.containsKey("responses") && server.has("responses"))
            upStreamMetrics.addAll(collectUpStreamResponseMetrics(serverGroupName, server, metricPrefix, configMetricsMap.get("responses")));

        if (configMetricsMap.containsKey("health_checks") && server.has("health_checks"))
            upStreamMetrics.addAll(collectUpStreamHealthCheckMetrics(serverGroupName, server, metricPrefix, configMetricsMap.get("health_checks")));

        keySet.removeAll(Arrays.asList("backup", "responses", "health_checks", "downstart"));
        String metricValue;
        for (String key : keySet) {
            if (configMetricsMap.containsKey(key) && server.has(key)) {
                MetricConfig config = configMetricsMap.get(key);
                if (config.getMetricConverter() != null)
                    metricValue = getConvertedStatus(config.getMetricConverter(), server.getString(key));
                else
                    metricValue = String.valueOf(server.getLong(key));
                Map<String, String> propertiesMap = objectMapper.convertValue(config, Map.class);
                Metric metric = new Metric(config.getAlias(), metricValue, metricPrefix + serverGroupName + "|" + serverIp + "|" + config.getAttr(), propertiesMap);
                upStreamMetrics.add(metric);
            }
        }
        return upStreamMetrics;
    }

    private List<Metric> collectUpStreamResponseMetrics(String serverGroupName, JSONObject server, String metricPrefix, MetricConfig config) {
        List<Metric> responseMetricsList = Lists.newArrayList();
        Map<String, String> propertiesMap = objectMapper.convertValue(config, Map.class);
        String serverIp = server.getString("server");
        JSONObject responses = server.getJSONObject("responses");

        Set<String> keySet = responses.keySet();
        for (String key : keySet) {
            long resp1xx = responses.getLong(key);
            Metric metric = new Metric(key, String.valueOf(resp1xx), metricPrefix + serverGroupName + "|" + serverIp + "|responses|" + key, propertiesMap);
            responseMetricsList.add(metric);
        }
        return responseMetricsList;
    }

    private List<Metric> collectUpStreamHealthCheckMetrics(String serverGroupName, JSONObject server, String metricPrefix, MetricConfig config) {
        List<Metric> healthCheckMetricsList = Lists.newArrayList();
        String serverIp = server.getString("server");
        Map<String, String> propertiesMap = objectMapper.convertValue(config, Map.class);
        JSONObject healthChecks = server.getJSONObject("health_checks");
        long checks = healthChecks.getLong("checks");
        Metric metric = new Metric("Checks", String.valueOf(checks), metricPrefix +  serverGroupName + "|" + serverIp + "|health_checks|checks", propertiesMap);
        healthCheckMetricsList.add(metric);

        long healthCheckFails = healthChecks.getLong("fails");
        metric = new Metric("Fails", String.valueOf(healthCheckFails), metricPrefix +serverGroupName + "|" + serverIp + "|health_checks|fails", propertiesMap);
        healthCheckMetricsList.add(metric);

        long unhealthy = healthChecks.getLong("unhealthy");
        metric = new Metric("Unhealthy", String.valueOf(unhealthy), metricPrefix +serverGroupName + "|" + serverIp + "|health_checks|unhealthy", propertiesMap);
        healthCheckMetricsList.add(metric);

        if (server.has("last_passed")) {
            boolean lastPassed = healthChecks.getBoolean("last_passed");
            metric = new Metric("Last Passed", String.valueOf(lastPassed ? 0 : 1), metricPrefix +serverGroupName + "|" + serverIp + "|health_checks|last_passed", propertiesMap);
            healthCheckMetricsList.add(metric);
        }
        return healthCheckMetricsList;
    }
}