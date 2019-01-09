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

package com.appdynamics.monitors.nginx.statsExtractor;


import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.monitors.nginx.Config.MetricConfig;
import com.appdynamics.monitors.nginx.Config.MetricConverter;
import com.appdynamics.monitors.nginx.Config.Stat;
import com.appdynamics.monitors.nginx.Constant;
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