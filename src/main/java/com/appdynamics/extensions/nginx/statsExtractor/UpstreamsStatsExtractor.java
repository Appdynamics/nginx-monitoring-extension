/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.nginx.statsExtractor;

import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.nginx.Config.MetricConfig;
import com.appdynamics.extensions.nginx.Config.Stat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
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
            Metric metric = new Metric(config.getAlias(), backup ? "1" : "0", metricPrefix + serverGroupName + METRIC_SEPARATOR + serverIp + "|backup", propertiesMap);
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
                Metric metric = new Metric(config.getAlias(), metricValue, metricPrefix + serverGroupName + METRIC_SEPARATOR + serverIp + METRIC_SEPARATOR + config.getAttr(), propertiesMap);
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
            Metric metric = new Metric(key, String.valueOf(resp1xx), metricPrefix + serverGroupName + METRIC_SEPARATOR + serverIp + "|responses|" + key, propertiesMap);
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
        Metric metric = new Metric("Checks", String.valueOf(checks), metricPrefix +  serverGroupName + METRIC_SEPARATOR + serverIp + "|health_checks|checks", propertiesMap);
        healthCheckMetricsList.add(metric);

        long healthCheckFails = healthChecks.getLong("fails");
        metric = new Metric("Fails", String.valueOf(healthCheckFails), metricPrefix +serverGroupName + METRIC_SEPARATOR + serverIp + "|health_checks|fails", propertiesMap);
        healthCheckMetricsList.add(metric);

        long unhealthy = healthChecks.getLong("unhealthy");
        metric = new Metric("Unhealthy", String.valueOf(unhealthy), metricPrefix +serverGroupName + METRIC_SEPARATOR + serverIp + "|health_checks|unhealthy", propertiesMap);
        healthCheckMetricsList.add(metric);

        if (server.has("last_passed")) {
            boolean lastPassed = healthChecks.getBoolean("last_passed");
            metric = new Metric("Last Passed", String.valueOf(lastPassed ? 0 : 1), metricPrefix +serverGroupName + METRIC_SEPARATOR + serverIp + "|health_checks|last_passed", propertiesMap);
            healthCheckMetricsList.add(metric);
        }
        return healthCheckMetricsList;
    }
}