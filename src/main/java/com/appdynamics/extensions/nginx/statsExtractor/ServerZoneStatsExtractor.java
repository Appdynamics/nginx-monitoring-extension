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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//"http://192.168.56.101:8080/api/3/http/server_zones"
public class ServerZoneStatsExtractor extends StatsExtractor {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Metric> extractStats(JSONObject respJson, Stat stat, String metricPrefix) {
        List<Metric> serverZonesMetrics = new ArrayList<>();
        try {
            Map<String, MetricConfig> configMetricsMap = getmetricConfigMap(stat.getMetricConfig());
            Set<String> serverZoneNames = respJson.keySet();
            for (String serverZoneName : serverZoneNames) {
                JSONObject server = respJson.getJSONObject(serverZoneName);
                String configStr = configMetricsMap.containsKey("responses") ? "responses" : "sessions";
                if (server.has(configStr))
                    serverZonesMetrics.addAll(collectUpStreamResponseMetrics(serverZoneName, server.getJSONObject(configStr), configStr, metricPrefix, configMetricsMap.get(configStr)));
                Set<String> keySet = server.keySet();
                keySet.remove(configStr);
                String metricValue;
                for (String key : keySet) {
                    if (configMetricsMap.containsKey(key)) {
                        MetricConfig config = configMetricsMap.get(key);
                        Map<String, String> propertiesMap = objectMapper.convertValue(config, Map.class);
                        metricValue = String.valueOf(server.getLong(key));
                        Metric metric = new Metric(config.getAlias(), metricValue, metricPrefix + serverZoneName + METRIC_SEPARATOR + config.getAttr(), propertiesMap);
                        serverZonesMetrics.add(metric);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error while collecting metrics for server zones stat", e);
        }
        return serverZonesMetrics;
    }

    private List<Metric> collectUpStreamResponseMetrics(String serverZoneName, JSONObject responses, String configStr, String metricPrefix, MetricConfig config) {
        List<Metric> responseMetricsList = Lists.newArrayList();
        Map<String, String> propertiesMap = objectMapper.convertValue(config, Map.class);
        Set<String> keySet = responses.keySet();
        for (String key : keySet) {
            long resp1xx = responses.getLong(key);
            Metric metric = new Metric(key, String.valueOf(resp1xx), metricPrefix + serverZoneName + METRIC_SEPARATOR + configStr + METRIC_SEPARATOR + key, propertiesMap);
            responseMetricsList.add(metric);
        }
        return responseMetricsList;
    }
}