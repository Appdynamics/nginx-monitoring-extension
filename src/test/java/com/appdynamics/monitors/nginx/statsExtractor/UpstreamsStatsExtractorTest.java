package com.appdynamics.monitors.nginx.statsExtractor;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.monitors.nginx.Config.Stat;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientUtils.class)
@PowerMockIgnore("javax.net.ssl.*")
public class UpstreamsStatsExtractorTest {

    private InputStream inputStream;
    private String testString;
    private UpstreamsStatsExtractor upstreamsStatsExtractor = new UpstreamsStatsExtractor();
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
    public void extractHttpUpStreamStats() {
        Stat stat = getAptStat(stats, "HTTP-Endpoints", "Upstreams");
        if(stat != null) {
            JSONObject responseBody = (JSONObject) new JSONArray(testString).get(0);
            List<Metric> upstreamMetricList = upstreamsStatsExtractor.extractStats(responseBody, stat, metricPrefix);
            Assert.assertTrue(upstreamMetricList.size() == 40);
        }
    }

    @Test
    public void extractStreamUpStreamStats() {
        Stat stat = getAptStat(stats, "Stream", "Upstreams");
        if(stat != null) {
            JSONObject responseBody = (JSONObject) new JSONArray(testString).get(8);
            List<Metric> upstreamMetricList = upstreamsStatsExtractor.extractStats(responseBody, stat, metricPrefix);
            Assert.assertTrue(upstreamMetricList.size() == 107);
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