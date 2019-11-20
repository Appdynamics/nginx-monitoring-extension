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
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;

import java.io.OutputStreamWriter;
import java.util.HashMap;
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

            Map<String, ?> kubernetesConfig = (Map<String, ?>) configYml.get("kubernetes");

            Boolean kubernetesMode = Boolean.valueOf(kubernetesConfig.get("useKubernetes").toString());

            for (Map<String, ?> server : nginxServers) {
                AssertUtils.assertNotNull(server, "the server arguments are empty");
                if (nginxServers.size() > 1 && !kubernetesMode) {
                    AssertUtils.assertNotNull(server.get("displayName"), "The displayName can not be null");
                    logger.info("Starting the Nginx Monitoring Task for server : " + server.get("displayName"));
                } else {
                    logger.info("Starting the Nginx Monitoring Task");
                }
                NginxMonitorTask task = new NginxMonitorTask(monitorContextConfiguration, tasksExecutionServiceProvider.getMetricWriteHelper(), server);
                tasksExecutionServiceProvider.submit((String) server.get("displayName"), task);
            }
        } catch (Exception e) {
            logger.error("Nginx servers Metrics collection failed", e);
        }
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        List<Map<String, ?>> servers = (List<Map<String, ?>>) configYml.get("servers");
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        return servers;
    }

    @Override
    protected void initializeMoreStuff(Map<String, String> args) {
        monitorContextConfiguration = getContextConfiguration();
        configYml = monitorContextConfiguration.getConfigYml();
        AssertUtils.assertNotNull(configYml, "The config.yml is not available");
        logger.info("initializing metric.xml file");
        monitorContextConfiguration.loadMetricXml(args.get("metric-file"), Stat.Stats.class);
    }

    public static void main(String[] args) throws TaskExecutionException {


        ConsoleAppender ca = new ConsoleAppender();
        ca.setWriter(new OutputStreamWriter(System.out));
        ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
        ca.setThreshold(Level.DEBUG);
        org.apache.log4j.Logger.getRootLogger().addAppender(ca);

        NginxMonitor monitor = new NginxMonitor();

        final Map<String, String> taskArgs = new HashMap<>();
        taskArgs.put("config-file", "src/main/resources/config.yml");
        taskArgs.put("metric-file", "src/main/resources/metrics.xml");

        monitor.execute(taskArgs, null);

    }
}
