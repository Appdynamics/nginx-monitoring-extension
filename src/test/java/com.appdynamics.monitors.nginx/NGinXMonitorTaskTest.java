/*
 * Copyright 2013. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.nginx;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.monitors.nginx.util.NGinXMonitorUtils;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
//*
 //Created by adityajagtiani on 9/30/16.


@RunWith(PowerMockRunner.class)
@PrepareForTest({EntityUtils.class, NGinXMonitorUtils.class})
public class NGinXMonitorTaskTest {
    private NGinXMonitorTask nGinXMonitorTask;
    private MonitorConfiguration configuration;
    private Map server;
    private CloseableHttpClient client;
    private CloseableHttpResponse response;
    private HttpEntity entity;
    private Header header;
    private MetricWriteHelper metricWriter;
    private String headerTxt, responseBody;
    private FileInputStream inputStream;

    @Before
    public void setup() throws IOException {
        configuration = mock(MonitorConfiguration.class);
        server = new HashMap<String, String>();
        server.put("uri", "http://testuri.com/status");
        nGinXMonitorTask = new NGinXMonitorTask(configuration, server);

        client = mock(CloseableHttpClient.class);
        response = mock(CloseableHttpResponse.class);
        entity = mock(HttpEntity.class);
        header = mock(Header.class);
        metricWriter = mock(MetricWriteHelper.class);

        when(configuration.getHttpClient()).thenReturn(client);
        when(client.execute(Mockito.any(HttpGet.class))).thenReturn(response);
        when(response.getEntity()).thenReturn(entity);
        when(response.getFirstHeader(Mockito.anyString())).thenReturn(header);
        when(configuration.getMetricPrefix()).thenReturn("prefix");
        PowerMockito.mockStatic(EntityUtils.class);
        when(configuration.getMetricWriter()).thenReturn(metricWriter);

        PowerMockito.mockStatic(NGinXMonitorUtils.class);
        when(NGinXMonitorUtils.getEnvDataForUrl(anyMap())).thenReturn(server);
    }

    @Test
    public void runTest_jsonResponse() throws IOException {
        headerTxt = "application/json";
        inputStream = new FileInputStream("src/test/resources/TestJSON.txt");
        try {
            responseBody = IOUtils.toString(inputStream);
        } finally {
            inputStream.close();
        }

        when(header.getValue()).thenReturn(headerTxt);
        when(EntityUtils.toString(entity, "UTF-8")).thenReturn(responseBody);
        nGinXMonitorTask.run();
        Mockito.verify(metricWriter, times(200)).printMetric(Mockito.anyString(), Mockito.anyString(), eq(MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION), eq(MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE), eq(MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE));
    }

    @Test
    public void runTest_plainTextResponse() throws IOException {
        headerTxt = "text/plain";
        inputStream = new FileInputStream("src/test/resources/TestPlainText.txt");
        try {
            responseBody = IOUtils.toString(inputStream);
        } finally {
            inputStream.close();
        }

        when(header.getValue()).thenReturn(headerTxt);
        when(EntityUtils.toString(entity, "UTF-8")).thenReturn(responseBody);
        nGinXMonitorTask.run();
        Mockito.verify(metricWriter, times(7)).printMetric(Mockito.anyString(), Mockito.anyString(), eq(MetricWriter.METRIC_AGGREGATION_TYPE_OBSERVATION), eq(MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE), eq(MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE));
    }

    @Test
    public void runTest_invalidContentType()throws IOException {
        headerTxt = "invalid/content";
        inputStream = new FileInputStream("src/test/resources/TestPlainText.txt");
        try {
            responseBody = IOUtils.toString(inputStream);
        } finally {
            inputStream.close();
        }

        when(header.getValue()).thenReturn(headerTxt);
        when(EntityUtils.toString(entity, "UTF-8")).thenReturn(responseBody);
        try {
            nGinXMonitorTask.run();
        }
        catch (Exception ex) {
            Assert.assertEquals("Invalid content type [ " + headerTxt + " ] for URL " + server.get("uri"), ex.getMessage());
        }
    }






















































































    /*
    @Test
    public void parseStubStatsResultTest() throws IOException{
        NGinXMonitorTask task = new NGinXMonitorTask();
        String responseBody = "Active connections: 1 \n server accepts handled requests\n 28 28 26 \nReading: 0 Writing: 1 Waiting: 5770848640 \n";
        Map<String, String> test = task.parseStubStatsResults(responseBody);
        Set<Map.Entry<String, String>> set = test.entrySet();
        Assert.assertEquals(7, test.size());
    }*/


















/*    @Test
    public void populateTest() throws IOException, TaskExecutionException {
        MonitorConfiguration configuration = Mockito.mock(MonitorConfiguration.class);
        Map map = Mockito.mock(Map.class);
        String responseBody = "response body";
        Map<String, String> testMap = new HashMap<String, String>();
        testMap.put("testKey", "testValue");
        CloseableHttpClient testClient = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse testResponse = Mockito.mock(CloseableHttpResponse.class);
        Mockito.when(configuration.getHttpClient()).thenReturn(testClient);
        Mockito.when(testClient.execute(Mockito.any(HttpGet.class))).thenReturn(testResponse);
        HttpEntity testEntity = Mockito.mock(HttpEntity.class);
        Mockito.when(testResponse.getEntity()).thenReturn(testEntity);
        PowerMockito.mockStatic(EntityUtils.class);
        PowerMockito.when(EntityUtils.toString(testEntity, "UTF-8")).thenReturn(responseBody);
        Header header = Mockito.mock(Header.class);
        String testHeaderValue = "application/json";
        Mockito.when(testResponse.getFirstHeader(Mockito.anyString())).thenReturn(header);
        Mockito.when(header.getValue()).thenReturn(testHeaderValue);
        NGinXMonitorTask testTask = Mockito.spy(new NGinXMonitorTask(configuration, map));
        Mockito.when(testTask.parsePlusStatsResult(responseBody)).thenReturn(testMap);

        NGinXMonitorTask task = new NGinXMonitorTask(configuration, map);
        Map<String, String> results =  task.populate();
        Assert.assertEquals(testMap.entrySet().iterator().next().getKey(), results.entrySet().iterator().next().getKey());
        Assert.assertEquals(testMap.entrySet().iterator().next().getValue(), results.entrySet().iterator().next().getValue());
    }

    @Test
    public void printAllMetricsTest() {
        Map <String, String> testMap = new HashMap<String, String>();
        testMap.put("testKey", "testValue");
        MonitorConfiguration configuration = Mockito.mock(MonitorConfiguration.class);
        Map map = Mockito.mock(Map.class);
        NGinXMonitorTask task = new NGinXMonitorTask(configuration, map);
        MetricWriteHelper metricWriteHelper = Mockito.mock(MetricWriteHelper.class);
        Mockito.when(configuration.getMetricWriter()).thenReturn(metricWriteHelper);
        Mockito.verify(metricWriteHelper).printMetric(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        task.printAllMetrics("prefix", testMap);

    }*/
}
