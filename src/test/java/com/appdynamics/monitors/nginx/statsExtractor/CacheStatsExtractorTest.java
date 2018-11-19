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

public class CacheStatsExtractorTest {
    private InputStream inputStream;
    private JSONObject responseBody;
    private CacheStatsExtractor cacheStatsExtractor = new CacheStatsExtractor();
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
        responseBody = (JSONObject) new JSONArray(testString).get(1);
        metricPrefix = "Custom Metrics|NGinX|";
        contextConfiguration.setMetricXml("src/test/resources/metricJson.xml", Stat.Stats.class);
        Stat[] stats = ((Stat.Stats) contextConfiguration.getMetricsXml()).getStats();
        stat = getAptStat(stats);
    }
    @Test
    public void extractCacheStats() {
        if(stat != null) {
            List<Metric> cacheMetricList = cacheStatsExtractor.extractStats(responseBody, stat, metricPrefix);
            Assert.assertTrue(cacheMetricList.size() == 21);
        }
    }

    private Stat getAptStat(Stat[] stats){
        for(Stat statItr : stats){
            if(statItr.getName().equals("HTTP-Endpoints")){
                return getAptSubStat(statItr.getStats());
            }
        }
        return null;
    }

    private Stat getAptSubStat(Stat[] stats){
        for(Stat subStatItr : stats){
            if(subStatItr.getName().equals("Caches"))
                return subStatItr;
        }
        return null;
    }
}