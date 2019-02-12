/*
 * Copyright 2013. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.nginx;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.nginx.Config.Stat;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by adityajagtiani on 10/10/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(EntityUtils.class)
public class JSONResponseCollectorTest {
    JSONResponseCollector parser;
    private InputStream inputStream;
    private String testString;
    private String metricPrefix;
    private Stat[] stats;
    @Mock
    private MonitorContextConfiguration contextConfiguration;
    @Mock
    private MonitorContext context;
    @Mock
    private CloseableHttpClient client;
    @Mock
    private CloseableHttpResponse response;
    @Mock
    private HttpEntity entity;
    @Mock
    private Header header;
    @Mock
    private MetricWriteHelper metricWriter;
    private ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
    Map<String, String> expectedValueMap;

    @Before
    public void initialize() throws Exception {
        inputStream = new FileInputStream("src/test/resources/TestJSON.txt");
        try {
            testString = IOUtils.toString(inputStream, "UTF-8");
        } finally {
            inputStream.close();
        }
        metricPrefix = "Custom Metrics|NGinX";
        MonitorContextConfiguration configuration = ConfigTestUtil.getContextConfiguration("src/test/resources/metricJson.xml", null);
        stats = ((Stat.Stats) configuration.getMetricsXml()).getStats();

        when(contextConfiguration.getContext()).thenReturn(context);
        when(context.getHttpClient()).thenReturn(client);
        when(client.execute(Mockito.any(HttpGet.class))).thenReturn(response);
        when(response.getEntity()).thenReturn(entity);
        when(response.getFirstHeader(Mockito.anyString())).thenReturn(header);
        when(header.getValue()).thenReturn("application/json");
        inputStream = new FileInputStream("src/test/resources/TestJSON.txt");
        try {
            testString = IOUtils.toString(inputStream, "UTF-8");
        } finally {
            inputStream.close();
        }
        PowerMockito.mockStatic(EntityUtils.class);
    }

    @Test
    public void extractProcessStats() throws IOException {
        String responseBody = (new JSONArray(testString).get(4)).toString();
        Stat stat = ConfigTestUtil.getAptStat(stats, "Processes-Status", null);
        when(EntityUtils.toString(entity, "UTF-8")).thenReturn(responseBody);
        if(stat != null) {
            parser = new JSONResponseCollector(stat, contextConfiguration, metricWriter, metricPrefix, "url" );
            parser.run();
            expectedValueMap = getExpectedProcessStatusMap();
            assertActualAndExpectedMetrics();
            Assert.assertTrue("The expected values were not sent. The missing values are " + expectedValueMap, expectedValueMap.isEmpty());

        }
    }

    @Test
    public void extractConnectionsStats() throws IOException {
        String responseBody = (new JSONArray(testString).get(5)).toString();
        Stat stat = ConfigTestUtil.getAptStat(stats, "Connections-Statistics", null);
        when(EntityUtils.toString(entity, "UTF-8")).thenReturn(responseBody);
        if(stat != null) {
            parser = new JSONResponseCollector(stat, contextConfiguration, metricWriter, metricPrefix, "url" );
            parser.run();
            expectedValueMap = getExpectedConnectionsStatusMap();
            assertActualAndExpectedMetrics();
            Assert.assertTrue("The expected values were not sent. The missing values are " + expectedValueMap, expectedValueMap.isEmpty());
        }
    }

    @Test
    public void extractSslStats() throws IOException{
        String responseBody = (new JSONArray(testString).get(6)).toString();
        Stat stat = ConfigTestUtil.getAptStat(stats, "SSL-Statistics", null);
        when(EntityUtils.toString(entity, "UTF-8")).thenReturn(responseBody);
        if(stat != null) {
            parser = new JSONResponseCollector(stat, contextConfiguration, metricWriter, metricPrefix, "url" );
            parser.run();
            expectedValueMap = getExpectedSslStatusMap();
            assertActualAndExpectedMetrics();
            Assert.assertTrue("The expected values were not sent. The missing values are " + expectedValueMap, expectedValueMap.isEmpty());
        }
    }
    @Test
    public void extractHttpRequestsStats() throws IOException{
        String responseBody = (new JSONArray(testString).get(7)).toString();
        Stat stat = ConfigTestUtil.getAptStat(stats, "HTTP-Endpoints", "Requests");
        when(EntityUtils.toString(entity, "UTF-8")).thenReturn(responseBody);
        if(stat != null) {
            parser = new JSONResponseCollector(stat, contextConfiguration, metricWriter, metricPrefix + "|http" , "url" );
            parser.run();
            expectedValueMap = getExpectedRequestStatusMap();
            assertActualAndExpectedMetrics();
            Assert.assertTrue("The expected values were not sent. The missing values are " + expectedValueMap, expectedValueMap.isEmpty());
        }
    }
    private void assertActualAndExpectedMetrics(){
        verify(metricWriter).transformAndPrintMetrics(pathCaptor.capture());
        for (Metric metric : (List<Metric>) pathCaptor.getValue()) {
            String actualValue = metric.getMetricValue();
            String metricName = metric.getMetricPath();
            if (expectedValueMap.containsKey(metricName)) {
                String expectedValue = expectedValueMap.get(metricName);
                Assert.assertEquals("The value of the metric " + metricName + " failed", expectedValue, actualValue);
                expectedValueMap.remove(metricName);
            } else {
                System.out.println("\"" + metricName + "\",\"" + actualValue + "\"");
                Assert.fail("Unknown Metric " + metricName);
            }
        }
    }


    private Map<String,String> getExpectedProcessStatusMap() {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("Custom Metrics|NGinX|processes|Respawned", "0");
        return resultMap;
    }

    private Map<String,String> getExpectedConnectionsStatusMap() {
        Map<String, String > resultMap = new HashMap<>();
        resultMap.put("Custom Metrics|NGinX|connections|Accepted", "195170424");
        resultMap.put("Custom Metrics|NGinX|connections|Dropped", "0");
        resultMap.put("Custom Metrics|NGinX|connections|Active", "2");
        resultMap.put("Custom Metrics|NGinX|connections|Idle", "42");
        return resultMap;
    }
    private Map<String,String> getExpectedSslStatusMap() {
        Map<String, String > resultMap = new HashMap<>();
        resultMap.put("Custom Metrics|NGinX|ssl|Handshakes", "2006921");
        resultMap.put("Custom Metrics|NGinX|ssl|Handshakes Failed", "160739");
        resultMap.put("Custom Metrics|NGinX|ssl|Session Reuses", "1562047");
        return resultMap;
    }
    private Map<String,String> getExpectedRequestStatusMap() {
        Map<String, String > resultMap = new HashMap<>();
        resultMap.put("Custom Metrics|NGinX|http|requests|Total", "308534178");
        resultMap.put("Custom Metrics|NGinX|http|requests|Current", "3");
        return resultMap;
    }

}
