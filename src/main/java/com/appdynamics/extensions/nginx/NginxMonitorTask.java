/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.nginx;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.nginx.Config.MetricConfig;
import com.appdynamics.extensions.nginx.Config.Stat;
import com.appdynamics.extensions.util.AssertUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by adityajagtiani on 8/31/16.
 */
class NginxMonitorTask implements AMonitorTaskRunnable {

    public static final Logger logger = Logger.getLogger(NginxMonitorTask.class);
    private static final String METRIC_SEPARATOR = Constant.METRIC_SEPARATOR;
    private Map server;
    private MonitorContextConfiguration configuration;
    private MetricWriteHelper metricWriteHelper;
    private String metricPrefix = "Custom Metrics|Nginx|";
    private AtomicInteger heartBeat = new AtomicInteger(0);

    public NginxMonitorTask(MonitorContextConfiguration monitorContextConfiguration, MetricWriteHelper metricWriteHelper, Map server) {
        this.configuration = monitorContextConfiguration;
        this.server = server;
        this.metricWriteHelper = metricWriteHelper;
        this.metricPrefix = configuration.getMetricPrefix() + METRIC_SEPARATOR + this.server.get("displayName");
    }

    public void run() {
        List<Metric> metricList = Lists.newArrayList();
        try {
            Map<String, String> requestMap = buildRequestMap();
            populateMetrics(requestMap, metricList);
            logger.info("Completed the Nginx Monitoring task");
        } catch (Exception e) {
            logger.error("Error while running the task " + server.get("displayName") + e);
        } finally {
            String prefix = metricPrefix + METRIC_SEPARATOR + "HeartBeat";
            Metric heartBeatMetric = new Metric("HeartBeat", String.valueOf(heartBeat.get()), prefix);
            metricList.add(heartBeatMetric);
            metricWriteHelper.transformAndPrintMetrics(metricList);
        }
    }

    private void populateMetrics(Map<String, String> requestMap, List<Metric> metricList) throws IOException, TaskExecutionException {
        try {
            String url = UrlBuilder.builder(requestMap).build();
            if (server.get("nginx_plus").equals("false")) {
                logger.debug("nginx_plus is false");
                metricList.addAll(populatePlainTextMetrics(url));
            } else {
                logger.debug("nginx_plus is true");
                Stat[] stats = ((Stat.Stats) configuration.getMetricsXml()).getStats();
                url = url + ((Stat.Stats) configuration.getMetricsXml()).getUrl() + "/";
                Phaser phaser = new Phaser();
                phaser.register();
                for (Stat stat : stats) {
                    if (stat.getStats() == null) {
                        if (!Strings.isNullOrEmpty(stat.getSubUrl())) {
                            JSONResponseCollector jsonResponseCollector = new JSONResponseCollector(stat, configuration, metricWriteHelper, metricPrefix, url + stat.getSubUrl(), heartBeat, phaser);
                            configuration.getContext().getExecutorService().execute("MetricCollector", jsonResponseCollector);
                            logger.debug("Registering MetricCollectorTask for " + server.get("displayName") + "for stats " + stat.getSubUrl());
                        }
                    } else {
                        Stat[] substats = stat.getStats();
                        for (Stat subStat : substats) {
                            JSONResponseCollector jsonResponseCollector = new JSONResponseCollector(subStat, configuration, metricWriteHelper, metricPrefix + METRIC_SEPARATOR + stat.getSubUrl(), url + stat.getSubUrl() + "/" + subStat.getSubUrl(), heartBeat, phaser);
                            configuration.getContext().getExecutorService().execute("MetricCollector", jsonResponseCollector);
                            logger.debug("Starting MetricCollectorTask for " + server.get("displayName") + "for stats " + subStat.getSubUrl());
                        }
                    }
                }
                phaser.arriveAndAwaitAdvance();
            }
        } catch (Exception e) {
            logger.error("Failed to complete nginx Monitor task{}", e);
        }
    }

    private List<Metric> populatePlainTextMetrics(String url) {
        CloseableHttpResponse response = null;
        List<Metric> metricsResultList = Lists.newArrayList();
        try {
            CloseableHttpClient httpClient = configuration.getContext().getHttpClient();
            HttpGet get = new HttpGet(url);
            response = httpClient.execute(get);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity, "UTF-8");

            heartBeat.incrementAndGet();
            AssertUtils.assertNotNull(responseBody, "response of the request is empty");
            logger.debug("response collected for text/plain: " + responseBody);
            String header = response.getFirstHeader("Content-Type").getValue();
            if (header != null && header.contains("text/plain")) {
                MetricConfig[] metricConfigs = getPlainTestConfigs(((Stat.Stats) configuration.getMetricsXml()).getStats());
                metricsResultList = PlainTextResponseParser.parseResponse(responseBody, metricConfigs, metricPrefix + METRIC_SEPARATOR);
            }
        } catch (Exception e) {
            logger.error("Failed to collect nginx plain-text metrics", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return metricsResultList;
        }
    }

    private MetricConfig[] getPlainTestConfigs(Stat[] stats) {
        for (Stat stat : stats) {
            if (stat.getName().equals("plain-text"))
                return stat.getMetricConfig();
        }
        return null;
    }

    /*
     * Builds a Map of the nginx server from the read config file.
     * @param haServer
     * @return
     */

    private Map<String, String> buildRequestMap() {
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put(Constant.URI, (String) server.get(Constant.URI));
        requestMap.put(Constant.USER_NAME, (String) server.get(Constant.USER_NAME));
        requestMap.put(Constant.PASSWORD, getPassword());
        return requestMap;
    }

    private String getPassword() {
        String password = (String) server.get(Constant.PASSWORD);
        String encryptedPassword = (String) server.get(Constant.ENCRYPTED_PASSWORD);
        Map<String, ?> configMap = configuration.getConfigYml();
        String encryptionKey = (String) configMap.get(Constant.ENCRYPTION_KEY);
        if (!Strings.isNullOrEmpty(password)) {
            return password;
        }
        if (!Strings.isNullOrEmpty(encryptedPassword)) {
            Map<String, String> cryptoMap = Maps.newHashMap();
            cryptoMap.put("encryptedPassword", encryptedPassword);
            cryptoMap.put("encryptionKey", encryptionKey);
            logger.debug("Decrypting the encrypted password........");
            return CryptoUtil.getPassword(cryptoMap);
        }
        return "";
    }

    @Override
    public void onTaskComplete() {
        logger.info("Completed the Nginx Monitoring task for log : " + server.get("displayName"));
    }
}

