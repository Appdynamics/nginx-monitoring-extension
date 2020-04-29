/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.nginx;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.nginx.Config.Stat;
import com.appdynamics.extensions.util.AssertUtils;
import com.google.common.collect.Maps;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created by adityajagtiani on 8/31/16.
 */

public class NginxMonitor extends ABaseMonitor {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(NginxMonitor.class);
    private MonitorContextConfiguration monitorContextConfiguration;
    private Map<String, ?> configYml = Maps.newHashMap();

    @Override
    protected String getDefaultMetricPrefix() {
        return Constant.METRIC_PREFIX;
    }

    @Override
    public String getMonitorName() {
        return Constant.MonitorName;
    }

    @Override
    protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
        try {
            List<Map<String, ?>> nginxServers = (List<Map<String, ?>>) configYml.get("servers");
            AssertUtils.assertNotNull(monitorContextConfiguration.getMetricsXml(), "Metrics xml not available");
            for (Map<String, ?> server : nginxServers) {
                AssertUtils.assertNotNull(server, "the server arguments are empty");
                AssertUtils.assertNotNull(server.get("displayName"), "The displayName can not be null");
                logger.info("Starting the Nginx Monitoring Task for server : " + server.get("displayName"));
                NginxMonitorTask task = new NginxMonitorTask(monitorContextConfiguration, tasksExecutionServiceProvider.getMetricWriteHelper(), server);
                tasksExecutionServiceProvider.submit((String) server.get("displayName"), task);
            }
        } catch (Exception e) {
            logger.error("Nginx servers Metrics collection failed", e);
        }
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        return (List<Map<String, ?>>) configYml.get("servers");
    }

    @Override
    protected void initializeMoreStuff(Map<String, String> args) {
        monitorContextConfiguration = getContextConfiguration();
        configYml = monitorContextConfiguration.getConfigYml();
        AssertUtils.assertNotNull(configYml, "The config.yml is not available");
        logger.info("initializing metric.xml file");
        monitorContextConfiguration.setMetricXml(args.get("metric-file"), Stat.Stats.class);
    }

//    public static void main(String[] args) throws TaskExecutionException {
//
//        NginxMonitor monitor = new NginxMonitor();
//        final Map<String, String> taskArgs = new HashMap<>();
//        taskArgs.put("config-file", "src/main/resources/config.yml");
//        taskArgs.put("metric-file", "src/main/resources/metrics.xml");
//
//        monitor.execute(taskArgs, null);
//
//    }
}
