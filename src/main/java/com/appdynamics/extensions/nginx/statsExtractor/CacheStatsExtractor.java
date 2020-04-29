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

public class CacheStatsExtractor extends StatsExtractor {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Metric> extractStats(JSONObject caches, Stat stat, String metricPrefix) {
        List<Metric> cacheMetrics = new ArrayList<>();
        try {
            Set<String> cacheNames = caches.keySet();
            Map<String, MetricConfig> metricConfigMap = getmetricConfigMap(stat.getMetricConfig());
            for (String cacheName : cacheNames) {
                JSONObject cache = caches.getJSONObject(cacheName);
                if (metricConfigMap.containsKey("size")) {
                    cacheMetrics.add(getLongMetric(metricConfigMap, cache, "size", metricPrefix, cacheName));
                }
                if (metricConfigMap.containsKey("max_size")) {
                    cacheMetrics.add(getLongMetric(metricConfigMap, cache, "max_size", metricPrefix, cacheName));
                }
                if (metricConfigMap.containsKey("cold")) {
                    cacheMetrics.add(getBooleanMetric(metricConfigMap, cache, "cold", metricPrefix, cacheName));
                }
                String[] cacheAttr = {"hit", "stale", "updating", "revalidated"};
                List<Metric> cachePerfMetrics = getCachePerfMetrics(metricConfigMap, cache, cacheAttr, metricPrefix, cacheName);
                if (cachePerfMetrics != null)
                    cacheMetrics.addAll(cachePerfMetrics);

                String[] cacheResultAttr = {"miss", "expired", "bypass"};
                cachePerfMetrics = getCacheHitMetrics(metricConfigMap, cache, cacheResultAttr, metricPrefix, cacheName);
                if (cachePerfMetrics != null)
                    cacheMetrics.addAll(cachePerfMetrics);
            }
        } catch (Exception e) {
            logger.error("Unexpected error while collecting metrics for cache stat", e);
        }
        return cacheMetrics;
    }

    private Metric getLongMetric(Map<String, MetricConfig> metricConfigMap, JSONObject cache, String key, String metricPrefix, String cacheName) {
        MetricConfig config = metricConfigMap.get(key);
        Map<String, String> propertiesMap = objectMapper.convertValue(config, Map.class);
        long size = cache.getLong(key);
        return new Metric(config.getAlias(), String.valueOf(size), metricPrefix + cacheName + METRIC_SEPARATOR + config.getAttr(), propertiesMap);
    }

    private Metric getBooleanMetric(Map<String, MetricConfig> metricConfigMap, JSONObject cache, String key, String metricPrefix, String cacheName) {
        MetricConfig config = metricConfigMap.get(key);
        Map<String, String> propertiesMap = objectMapper.convertValue(config, Map.class);
        boolean cold = cache.getBoolean(key);
        return new Metric(config.getAlias(), String.valueOf(cold ? 0 : 1), metricPrefix + cacheName + METRIC_SEPARATOR + config.getAttr(), propertiesMap);
    }

    private List<Metric> getCachePerfMetrics(Map<String, MetricConfig> metricConfigMap, JSONObject cache, String[] cacheAttr, String metricPrefix, String cacheName) {
        List<Metric> cachePerfMetricList = Lists.newArrayList();
        for (String str : cacheAttr) {
            if (metricConfigMap.containsKey(str)) {
                MetricConfig config = metricConfigMap.get(str);
                Map<String, String> propertiesMap = objectMapper.convertValue(config, Map.class);
                JSONObject jsonObject = cache.getJSONObject(str);
                long responses = jsonObject.getLong("responses");
                Metric metric = new Metric("Responses", String.valueOf(responses), metricPrefix + cacheName + METRIC_SEPARATOR + config.getAttr() + "|responses", propertiesMap);
                cachePerfMetricList.add(metric);

                long bytes = jsonObject.getLong("bytes");
                metric = new Metric("Bytes", String.valueOf(bytes), metricPrefix + "caches|" + cacheName + METRIC_SEPARATOR + config.getAttr() + "|bytes", propertiesMap);
                cachePerfMetricList.add(metric);
            }
        }
        return cachePerfMetricList;
    }

    private List<Metric> getCacheHitMetrics(Map<String, MetricConfig> metricConfigMap, JSONObject cache, String[] cacheResultAttr, String metricPrefix, String cacheName) {
        List<Metric> cacheHitMetricList = Lists.newArrayList();
        for (String str : cacheResultAttr) {
            if (metricConfigMap.containsKey(str)) {
                MetricConfig config = metricConfigMap.get(str);
                Map<String, String> propertiesMap = objectMapper.convertValue(config, Map.class);
                JSONObject jsonObject = cache.getJSONObject(str);
                long responses = jsonObject.getLong("responses");
                cacheHitMetricList.add(new Metric("Responses", String.valueOf(responses), metricPrefix + "caches|" + cacheName + METRIC_SEPARATOR + config.getAttr() + "|responses", propertiesMap));

                long bytes = jsonObject.getLong("bytes");
                cacheHitMetricList.add(new Metric("Bytes", String.valueOf(bytes), metricPrefix + "caches|" + cacheName + METRIC_SEPARATOR + config.getAttr() + "|bytes", propertiesMap));

                long responses_written = jsonObject.getLong("responses_written");
                cacheHitMetricList.add(new Metric("Responses written", String.valueOf(responses_written), metricPrefix + "caches|" + cacheName + METRIC_SEPARATOR + config.getAttr() + "|responses_written", propertiesMap));

                long bytes_written = jsonObject.getLong("bytes_written");
                cacheHitMetricList.add(new Metric("Bytes written", String.valueOf(bytes_written), metricPrefix + "caches|" + cacheName + METRIC_SEPARATOR + config.getAttr() + "|bytes_written", propertiesMap));
            }
        }
        return cacheHitMetricList;
    }
}
