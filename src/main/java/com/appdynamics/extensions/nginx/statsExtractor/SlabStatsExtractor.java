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

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SlabStatsExtractor extends StatsExtractor {
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Metric> extractStats(JSONObject respJson, Stat stat, String metricPrefix) {
        List<Metric> slabMetricList = Lists.newArrayList();
        try {
            Map<String, MetricConfig> metricConfigMap = getmetricConfigMap(stat.getMetricConfig());
            Set<String> slabZoneNameSet = respJson.keySet();
            for (String slabZoneName : slabZoneNameSet) {
                JSONObject slabZoneObject = respJson.getJSONObject(slabZoneName);
                if (metricConfigMap.containsKey("pages")) {
                    MetricConfig config = metricConfigMap.get("pages");
                    Map<String, String> propertiesMap = objectMapper.convertValue(config, Map.class);
                    JSONObject slabZonePageObj = slabZoneObject.getJSONObject("pages");
                    for (String attr : new String[]{"used", "free"})
                        slabMetricList.add(new Metric(attr, String.valueOf(slabZonePageObj.getLong(attr)), metricPrefix + slabZoneName + "|pages|" + attr, propertiesMap));
                }
                if (metricConfigMap.containsKey("slots")) {
                    MetricConfig config = metricConfigMap.get("slots");
                    Map<String, String> propertiesMap = objectMapper.convertValue(config, Map.class);
                    JSONObject slabZoneSlotObj = slabZoneObject.getJSONObject("slots");
                    for (int pageSize = 8; pageSize <= 2048; pageSize = pageSize << 1) {
                        JSONObject slotPageSizeObj = slabZoneSlotObj.getJSONObject(String.valueOf(pageSize));
                        for (String pageAttr : new String[]{"used", "free", "reqs", "fails"}) {
                            slabMetricList.add(new Metric(pageAttr, String.valueOf(slotPageSizeObj.getLong(pageAttr)), metricPrefix + slabZoneName + "|slots|" + pageSize + METRIC_SEPARATOR + pageAttr, propertiesMap));
                        }
                    }
                }
            }
        }catch (Exception e) {
            logger.error("Unexpected error while collecting metrics for slab stat", e);
        }
        return slabMetricList;
    }
}
