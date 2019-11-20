/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.nginx;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.nginx.Config.MetricConfig;
import com.appdynamics.extensions.nginx.Config.Stat;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by adityajagtiani on 10/10/16.
 */

@RunWith(PowerMockRunner.class)
public class PlainTextResponseParserTest {
    private PlainTextResponseParser parser;
    private String responseBody;
    private List<Metric> results;
    private FileInputStream inputStream;
    private String metricPrefix;
    private MetricConfig[] metricConfigs;
    private MonitorContextConfiguration contextConfiguration = new MonitorContextConfiguration("NginxMonitor", "Custom Metrics|Nginx|", Mockito.mock(File.class));

    @Before
    public void initialize() throws IOException {
        parser = new PlainTextResponseParser();
        inputStream = new FileInputStream("src/test/resources/TestPlainText.txt");
        try {
            responseBody = IOUtils.toString(inputStream);
        } finally {
            inputStream.close();
        }
        contextConfiguration.loadMetricXml("src/test/resources/metricsPlainText.xml", Stat.Stats.class);
        Stat[] stats = ((Stat.Stats) contextConfiguration.getMetricsXml()).getStats();
        metricConfigs = stats[0].getMetricConfig();
        metricPrefix = "Custom Metrics|NGinX";
        results = Lists.newArrayList();
    }

    @Test
    public void parseResponseTest_SizeOfMap() throws IOException {
        Assert.assertTrue(parser.parseResponse(responseBody, metricConfigs, metricPrefix).size() == 7);
    }

    @Test
    public void parseResponseTest_Keys() throws IOException {
        results = parser.parseResponse(responseBody, metricConfigs, metricPrefix);
        Map<String, Boolean> resultsMap = getResultListToMapBoolean(results);
        Assert.assertTrue(resultsMap.get("Active connections"));
        Assert.assertTrue(resultsMap.get("Reading"));
        Assert.assertTrue(resultsMap.get("Writing"));
        Assert.assertTrue(resultsMap.get("Waiting"));
        Assert.assertTrue(resultsMap.get("Server|Requests"));
        Assert.assertTrue(resultsMap.get("Server|Handled"));
        Assert.assertTrue(resultsMap.get("Server|Accepts"));
    }

    @Test
    public void parseResponseTest_Values() throws IOException {
        Map<String, String> resultsValueMap = getResultListToMapValue(parser.parseResponse(responseBody, metricConfigs, metricPrefix));
        Assert.assertTrue(resultsValueMap.get("Active connections").equals("1"));
        Assert.assertTrue(resultsValueMap.get("Reading").equals("0"));
        Assert.assertTrue(resultsValueMap.get("Writing").equals("1"));
        Assert.assertTrue(resultsValueMap.get("Waiting").equals("5770848640"));
        Assert.assertTrue(resultsValueMap.get("Server|Handled").equals("28"));
        Assert.assertTrue(resultsValueMap.get("Server|Accepts").equals("28"));
        Assert.assertTrue(resultsValueMap.get("Server|Requests").equals("26"));
    }

    private Map<String, Boolean> getResultListToMapBoolean(List<Metric> result){
        Map<String, Boolean> resultMap = new HashMap<>();
        for(Metric metric : result)
            resultMap.put(metric.getMetricName(), true);
        return resultMap;
    }

    private Map<String, String> getResultListToMapValue(List<Metric> result){
        Map<String, String> resultMapVal = new HashMap<>();
        for(Metric metric : result)
            resultMapVal.put(metric.getMetricName(), metric.getMetricValue());
        return resultMapVal;
    }
}
