/*
 * Copyright 2013. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.nginx;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.monitors.nginx.Config.Stat;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
//*
 //Created by adityajagtiani on 9/30/16.


@RunWith(PowerMockRunner.class)
@PrepareForTest(EntityUtils.class)
@PowerMockIgnore("javax.net.ssl.*")
public class NGinXMonitorTaskTest {
    @Mock
    private TasksExecutionServiceProvider serviceProvider;
    @Mock
    private MetricWriteHelper metricWriter;
    @Mock
    private MonitorContext context;
    @Mock
    private MonitorContextConfiguration configuration;
    @Mock
    private CloseableHttpClient client;
    @Mock
    private CloseableHttpResponse response;
    @Mock
    private HttpEntity entity;
    @Mock
    private Header header;
    private String testString;
    private Stat[] stats;
    Map config;
    private ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);

    @Before
    public void setup() throws IOException {
        MonitorContextConfiguration contextConfiguration = ConfigTestUtil.getContextConfiguration("src/test/resources/metricsPlainText.xml", "src/test/resources/config.yml");
        config = contextConfiguration.getConfigYml();
        stats = ((Stat.Stats) contextConfiguration.getMetricsXml()).getStats();
        PowerMockito.mockStatic(EntityUtils.class);
        Object metricsXml = contextConfiguration.getMetricsXml();
        when(configuration.getContext()).thenReturn(context);
        when(configuration.getConfigYml()).thenReturn(config);
        when(configuration.getMetricsXml()).thenReturn(metricsXml);
        when(context.getHttpClient()).thenReturn(client);
        when(client.execute(Mockito.any(HttpGet.class))).thenReturn(response);
        when(response.getEntity()).thenReturn(entity);
        when(response.getFirstHeader(Mockito.anyString())).thenReturn(header);
        when(header.getValue()).thenReturn("text/plain");
    }

    @Test
    public void checkNginxPlainTextCollectedMetricsCount() throws IOException {
        FileInputStream inputStream;
        String responseBody;
        inputStream = new FileInputStream("src/test/resources/TestPlainText.txt");
        try {
            responseBody = IOUtils.toString(inputStream, "UTF-8");
        } finally {
            inputStream.close();
        }
        inputStream = new FileInputStream("src/test/resources/TestJSON.txt");
        try {
            testString = IOUtils.toString(inputStream, "UTF-8");
        } finally {
            inputStream.close();
        }
        when(EntityUtils.toString(entity, "UTF-8")).thenReturn(responseBody);
        List servers = (List) config.get("servers");
        Map<String, String> server = (Map<String, String>) servers.get(0);
        server.put("nginx_plus", "false");

        NGinXMonitorTask nGinXMonitorTask = new NGinXMonitorTask(configuration, metricWriter, server);
        nGinXMonitorTask.run();

        verify(metricWriter).transformAndPrintMetrics(pathCaptor.capture());
        Assert.assertEquals(((List<Metric>)pathCaptor.getValue()).size(), 8);
    }
}