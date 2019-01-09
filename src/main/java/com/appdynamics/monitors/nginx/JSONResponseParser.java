/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.nginx;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.monitors.nginx.Config.MetricConfig;
import com.appdynamics.monitors.nginx.Config.Stat;
import static com.appdynamics.monitors.nginx.Constant.METRIC_SEPARATOR;
import com.appdynamics.monitors.nginx.statsExtractor.StatsExtractor;
import com.appdynamics.monitors.nginx.statsExtractor.StatsFactory;
import com.google.common.collect.Lists;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// #TODO Please change the name of the class. This is not just parsing response as PlainTextResponseParser class.
public class JSONResponseParser implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(JSONResponseParser.class);

    private Stat stat;

    private MonitorContextConfiguration configuration;

    private MetricWriteHelper metricWriteHelper;

    private String url;

    private static ObjectMapper objectMapper = new ObjectMapper();

    private String metricPrefix;


    public JSONResponseParser(Stat stat, MonitorContextConfiguration configuration, MetricWriteHelper metricWriteHelper, String metricPrefix, String url) {
        this.stat = stat;
        this.configuration = configuration;
        this.metricWriteHelper = metricWriteHelper;
        this.url = url;
        this.metricPrefix = metricPrefix + METRIC_SEPARATOR + stat.getSubUrl() + METRIC_SEPARATOR;
    }


    public void run() {
        CloseableHttpClient httpClient = configuration.getContext().getHttpClient();
        CloseableHttpResponse response = null;
        List<Metric> metricList = Lists.newArrayList();
        try {
            HttpGet get = new HttpGet(url);
            response = httpClient.execute(get);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity, "UTF-8");

            AssertUtils.assertNotNull(responseBody, "response of the request is empty");
            String header = response.getFirstHeader("Content-Type").getValue();
            if (header != null && header.contains("application/json")) {
                JSONObject jsonObject = new JSONObject(responseBody);
                StatsExtractor statsExtractor = new StatsFactory().getstatsExtractor(stat.getSubUrl());
                if (statsExtractor != null) {
                    metricList.addAll(statsExtractor.extractStats(jsonObject, stat, metricPrefix));
                } else if (stat.getMetricConfig() != null) {
                    metricList.addAll(collectMetrics(jsonObject, stat.getMetricConfig()));
                }

            }
            logger.debug("Successfully collected metrics for Stat {} {}", url,stat.getSubUrl());
        } catch (Exception e) {
            logger.error("Unexpected error while collecting metrics for stat", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            metricWriteHelper.transformAndPrintMetrics(metricList);
        }
    }

    private List<Metric> collectMetrics(JSONObject jsonObject, MetricConfig[] metricConfig) {
        List<Metric> metrics = new ArrayList<>();
        for (MetricConfig config : metricConfig) {
            Map<String, String> propertiesMap = objectMapper.convertValue(config, Map.class);
            Metric metric = new Metric(config.getAlias(), String.valueOf(jsonObject.get(config.getAttr())), metricPrefix + config.getAlias(), propertiesMap);
            metrics.add(metric);
        }
        return metrics;
    }
}

