package com.appdynamics.monitors.nginx.statsExtractor;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.monitors.nginx.Config.Stat;
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

public class ServerZoneStatsExtractorTest {

    private InputStream inputStream;
    private String testString;
    private ServerZoneStatsExtractor serverZoneStatsExtractor = new ServerZoneStatsExtractor();
    private String metricPrefix;
    private MonitorContextConfiguration contextConfiguration = new MonitorContextConfiguration("NginxMonitor", "Custom Metrics|Nginx|", Mockito.mock(File.class), Mockito.mock(AMonitorJob.class));
    private Stat[] stats;

    @Before
    public void setUp() throws Exception {
        inputStream = new FileInputStream("src/test/resources/TestJSON.txt");
        try {
            testString = IOUtils.toString(inputStream, "UTF-8");
        } finally {
            inputStream.close();
        }
        metricPrefix = "Custom Metrics|NGinX|";
        contextConfiguration.setMetricXml("src/test/resources/metricJson.xml", Stat.Stats.class);
        stats = ((Stat.Stats) contextConfiguration.getMetricsXml()).getStats();

    }
    @Test
    public void extractHttpServerZoneStats() {
        Stat stat = getAptStat(stats, "HTTP-Endpoints", "Server-Zones");
        if(stat != null) {
            JSONObject responseBody = (JSONObject) new JSONArray(testString).get(3);
            List<Metric> serverZoneMetricsList = serverZoneStatsExtractor.extractStats(responseBody, stat, metricPrefix);
            Assert.assertTrue(serverZoneMetricsList.size() == 33);
        }
    }

    @Test
    public void extractStreamServerZoneStats() {
        Stat stat = getAptStat(stats, "Stream", "Server-Zones");
        if(stat != null) {
            JSONObject responseBody = (JSONObject) new JSONArray(testString).get(9);
            List<Metric> serverZoneMetricsList = serverZoneStatsExtractor.extractStats(responseBody, stat, metricPrefix);
            Assert.assertTrue(serverZoneMetricsList.size() == 18);
        }
    }
    private Stat getAptStat(Stat[] stats, String statName, String subStatName){
        for(Stat statItr : stats){
            if(statItr.getName().equals(statName)){
                return getAptSubStat(statItr.getStats(), subStatName);
            }
        }
        return null;
    }

    private Stat getAptSubStat(Stat[] stats, String subStatName){
        for(Stat subStatItr : stats){
            if(subStatItr.getName().equals(subStatName))
                return subStatItr;
        }
        return null;
    }
}