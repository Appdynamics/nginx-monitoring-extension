/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.nginx;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.nginx.Config.MetricConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by adityajagtiani on 10/10/16.
 */
public class PlainTextResponseParser {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(PlainTextResponseParser.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static List<Metric> parseResponse(String responseBody, MetricConfig[] metricConfigs, String metricPrefix) throws IOException {
        List<Metric> resultList = Lists.newArrayList();
        Pattern numPattern = Pattern.compile("\\d+");
        BufferedReader reader = new BufferedReader(new StringReader(responseBody));
        String line = "", whiteSpaceRegex = "\\s";
        String currAttr;
        try {
            while ((line = reader.readLine()) != null) {
                currAttr = "Active connections";
                if (line.contains(currAttr)) {
                    Matcher numMatcher = numPattern.matcher(line);
                    if (numMatcher.find()) {
                        addValueToMetricList(numMatcher.group(), metricConfigs, currAttr, metricPrefix, resultList);
                    }
                } else if (line.contains("server")) {
                    line = reader.readLine();
                    String[] results = line.trim().split(whiteSpaceRegex);
                    currAttr = "Server|Accepts";
                    addValueToMetricList(results[0], metricConfigs, currAttr, metricPrefix, resultList);
                    currAttr = "Server|Handled";
                    addValueToMetricList(results[1], metricConfigs, currAttr, metricPrefix, resultList);
                    currAttr = "Server|Requests";
                    addValueToMetricList(results[2], metricConfigs, currAttr, metricPrefix, resultList);
                } else if (line.contains("Reading")) {
                    String[] results = line.trim().split(whiteSpaceRegex);
                    currAttr = "Reading";
                    addValueToMetricList(results[1], metricConfigs, currAttr, metricPrefix, resultList);
                    currAttr = "Writing";
                    addValueToMetricList(results[3], metricConfigs, currAttr, metricPrefix, resultList);
                    currAttr = "Waiting";
                    addValueToMetricList(results[5], metricConfigs, currAttr, metricPrefix, resultList);
                }
            }
        }catch (Exception e){
            logger.error("error is parsing plain text", e);
        }
        return resultList;
    }

    private static MetricConfig getMatchedConfig(String key, MetricConfig[] configs) {
        for (MetricConfig config : configs) {
            if (config.getAttr().equals(key))
                return config;
        }
        return null;
    }
    private static void addValueToMetricList(String metricValue,  MetricConfig[] metricConfigs,String metricAttr, String metricPrefix, List<Metric> resultList){
        MetricConfig config = getMatchedConfig(metricAttr, metricConfigs);
        if(config != null) {
            Map<String, ?> propertiesMap = objectMapper.convertValue(config, Map.class);
            Metric metric = new Metric(metricAttr, metricValue, metricPrefix + metricAttr, propertiesMap);
            resultList.add(metric);
        }
    }
}
