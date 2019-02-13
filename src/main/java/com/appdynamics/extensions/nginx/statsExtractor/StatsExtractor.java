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
import com.appdynamics.extensions.nginx.Config.MetricConverter;
import com.appdynamics.extensions.nginx.Config.Stat;
import com.appdynamics.extensions.nginx.Constant;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StatsExtractor {

    public static final Logger logger = LoggerFactory.getLogger(StatsExtractor.class);

    public abstract List<Metric> extractStats(JSONObject respJson, Stat stat, String metricPrefix);

    public static final String METRIC_SEPARATOR = Constant.METRIC_SEPARATOR;

    public Map<String, MetricConfig> getmetricConfigMap(MetricConfig[] metricConfigs) {
        Map<String, MetricConfig> configMap = new HashMap<>();
        for (MetricConfig config : metricConfigs) {
            configMap.put(config.getAttr(), config);
        }
        return configMap;
    }

    /**
     * @param converters
     * @param status
     * @return
     */
    public String getConvertedStatus(MetricConverter[] converters, String status) {
        for (MetricConverter converter : converters) {
            if (converter.getLabel().equals(status))
                return converter.getValue();
        }
        return "";
    }
}