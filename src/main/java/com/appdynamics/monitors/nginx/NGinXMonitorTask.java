/*
 * Copyright 2013. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.nginx;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.crypto.CryptoUtil;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.monitors.nginx.Config.MetricConfig;
import com.appdynamics.monitors.nginx.Config.Stat;
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
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by adityajagtiani on 8/31/16.
 */
public class NGinXMonitorTask implements Runnable, AMonitorTaskRunnable {

    public static final Logger logger = Logger.getLogger(NGinXMonitor.class);
    private static final String METRIC_SEPARATOR = "|";
    private Map server;
    private MonitorContextConfiguration configuration;
    private MetricWriteHelper metricWriteHelper;
    private String metricPrefix;
    private BigInteger heartBeatValue = BigInteger.ZERO;

    public NGinXMonitorTask(MonitorContextConfiguration monitorContextConfiguration, MetricWriteHelper metricWriteHelper, Map server) {
        this.configuration = monitorContextConfiguration;
        this.server = server;
        this.metricWriteHelper = metricWriteHelper;
        this.metricPrefix = configuration.getMetricPrefix() + METRIC_SEPARATOR + server.get("displayName");
    }

    public void run() {
        try {
            Map<String, String> requestMap = buildRequestMap();
            populateMetrics(requestMap);
            logger.info("Completed the Nginx Monitoring task");
        } catch (Exception e) {
            logger.error("Error while running the task " + server.get("displayName") + e);
        }finally {
            String prefix = metricPrefix + METRIC_SEPARATOR + "HeartBeat";
            Metric heartBeat = new Metric("HeartBeat", String.valueOf(heartBeatValue), prefix);
            metricWriteHelper.transformAndPrintMetrics(Arrays.asList(heartBeat));
        }
    }


    private void populateMetrics(Map<String, String> requestMap) throws IOException, TaskExecutionException {
        heartBeatValue = BigInteger.ONE;
        try {
            String url = UrlBuilder.builder(requestMap).build();
            if (server.get("nginx_plus").equals("false")) {
                populatePlainTextMetrics(url);
            } else {
                Stat[] stats = ((Stat.Stats) configuration.getMetricsXml()).getStats();
                url = url + ((Stat.Stats) configuration.getMetricsXml()).getUrl() + "/";
                for (Stat stat : stats) {
                    if (stat.getStats() == null) {
                        if(stat.getSubUrl() != "") {
                            JSONResponseParser jsonResponseParser = new JSONResponseParser(stat, configuration, metricWriteHelper, metricPrefix, url + stat.getSubUrl());
                            configuration.getContext().getExecutorService().execute("MetricCollector", jsonResponseParser);
                            logger.debug("Registering MetricCollectorTask for " + server.get("displayName") + "for stats " + stat.getSubUrl());
                        }
                    } else {
                        Stat[] substats = stat.getStats();
                        for (Stat subStat : substats) {
                            JSONResponseParser jsonResponseParser = new JSONResponseParser(subStat, configuration, metricWriteHelper, metricPrefix + "|" + stat.getSubUrl(), url + stat.getSubUrl() + "/" + subStat.getSubUrl());
                            configuration.getContext().getExecutorService().execute("MetricCollector", jsonResponseParser);
                            logger.debug("Starting MetricCollectorTask for " + server.get("displayName") + "for stats " + subStat.getSubUrl());
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Failed to complete nginx Monitor task{}", e);
        }
    }

    private void populatePlainTextMetrics(String url){
        CloseableHttpResponse response = null;
        List<Metric> metricsResultList = Lists.newArrayList();
        try {
            CloseableHttpClient httpClient = configuration.getContext().getHttpClient();
            HttpGet get = new HttpGet(url);
            response = httpClient.execute(get);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity, "UTF-8");
            AssertUtils.assertNotNull(responseBody, "response of the request is empty");
            String header = response.getFirstHeader("Content-Type").getValue();
            if (header != null && header.contains("text/plain")) {
                MetricConfig[] metricConfigs = getPainTestConfigs(((Stat.Stats) configuration.getMetricsXml()).getStats());
                PlainTextResponseParser plainTextParser = new PlainTextResponseParser();
                metricsResultList = plainTextParser.parseResponse(responseBody, metricConfigs, metricPrefix + "|");
            }
        }catch (Exception e){
            logger.error("Failed to collect nginx plain-text metrics", e);
        }
        finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            metricWriteHelper.transformAndPrintMetrics(metricsResultList);
        }
    }

    @Override
    public void onTaskComplete() {
        logger.info("Completed the Nginx Monitoring task for log : " + server.get("displayName"));
    }

    private MetricConfig[] getPainTestConfigs(Stat[] stats){
        for(Stat stat : stats){
            if(stat.getName().equals("plain-text"))
                return stat.getMetricConfig();
        }
        return null;
    }

    /*
     * Builds a hashMap of the nginx server from the read config file.
     *
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

    private String getPassword(){
        String password = (String) server.get("password");
        String encryptedPassword = (String) server.get("encryptedPassword");
        Map<String, ?> configMap = configuration.getConfigYml();
        String encryptionKey = configMap.get("encryptionKey").toString();
        if(!Strings.isNullOrEmpty(password)){
            return password;
        }
        if(!Strings.isNullOrEmpty(encryptedPassword) && !Strings.isNullOrEmpty(encryptionKey)){
            Map<String,String> cryptoMap = Maps.newHashMap();
            cryptoMap.put("encryptedPassword", encryptedPassword);
            cryptoMap.put("encryptionKey", encryptionKey);
            logger.debug("Decrypting the ecncrypted password........");
            return CryptoUtil.getPassword(cryptoMap);
        }
        return "";
    }

}

