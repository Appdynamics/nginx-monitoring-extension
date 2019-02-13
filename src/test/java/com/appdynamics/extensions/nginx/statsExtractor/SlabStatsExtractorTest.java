/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.nginx.statsExtractor;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.nginx.Config.Stat;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class SlabStatsExtractorTest {

    private InputStream inputStream;
    private JSONObject responseBody;
    private SlabStatsExtractor slabStatsExtractor  = new SlabStatsExtractor();
    private String metricPrefix;
    private MonitorContextConfiguration contextConfiguration = new MonitorContextConfiguration("NginxMonitor", "Custom Metrics|Nginx|", Mockito.mock(File.class), Mockito.mock(AMonitorJob.class));
    private Stat stat;

    @Before
    public void setUp() throws Exception {
        String testString;
        inputStream = new FileInputStream("src/test/resources/TestJSON.txt");
        try {
            testString = IOUtils.toString(inputStream, "UTF-8");
        } finally {
            inputStream.close();
        }
        responseBody = (JSONObject) new JSONArray(testString).get(2);
        metricPrefix = "Custom Metrics|NGinX|";
        contextConfiguration.setMetricXml("src/test/resources/metricJson.xml", Stat.Stats.class);
        Stat[] stats = ((Stat.Stats) contextConfiguration.getMetricsXml()).getStats();
        stat = getAptStat(stats);
    }
    @Test
    public void extractSlabsStats() {
        if(stat != null) {
            List<Metric> slabMetricsList = slabStatsExtractor.extractStats(responseBody, stat, metricPrefix);
            Assert.assertTrue(slabMetricsList.size() == 2);
        }
    }

    private Stat getAptStat(Stat[] stats){
        for(Stat statItr : stats){
            if(statItr.getName().equals("Slab-Status")){
                return statItr;
            }
        }
        return null;
    }
}