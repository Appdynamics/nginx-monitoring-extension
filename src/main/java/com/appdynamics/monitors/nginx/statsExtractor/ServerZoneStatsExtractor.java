/**
 * Copyright 2014 AppDynamics, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
                        Metric metric = new Metric(config.getAlias(), metricValue, metricPrefix + "upstreams|" + serverZoneName + "|" + config.getAttr(), propertiesMap);
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
            Metric metric = new Metric(key, String.valueOf(resp1xx), metricPrefix + "upstreams|" + serverZoneName + "|" + configStr + "|" + key, propertiesMap);
            responseMetricsList.add(metric);
        }
        return responseMetricsList;
    }
}