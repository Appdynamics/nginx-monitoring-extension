/*
 * Copyright 2013. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.nginx;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.util.AssertUtils;
import com.appdynamics.monitors.nginx.Config.Stat;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created by adityajagtiani on 8/31/16.
 */
public class NGinXMonitor extends ABaseMonitor {
    public static final Logger logger = Logger.getLogger(NGinXMonitor.class);
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
            AssertUtils.assertNotNull(configYml, "The config.yml is not available");
            AssertUtils.assertNotNull(this.getContextConfiguration().getMetricsXml(), "Metrics xml not available");

            for (Map<String, ?> server : nginxServers) {
                logger.info("Starting the Nginx Monitoring Task for log : " + server.get("displayName"));

                AssertUtils.assertNotNull(server, "the server arguments are empty");
                NGinXMonitorTask task = new NGinXMonitorTask(monitorContextConfiguration, tasksExecutionServiceProvider.getMetricWriteHelper(), server);
                AssertUtils.assertNotNull(server.get("displayName"), "The displayName can not be null");
                tasksExecutionServiceProvider.submit((String) server.get("displayName"), task);
            }
        } catch (Exception e) {
            logger.error("Nginx servers Metrics collection failed", e);
        }
    }

    @Override
    protected int getTaskCount() {
        List<Map<String, ?>> servers = (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get("servers");
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        return servers.size();
    }

    @Override
    protected void initializeMoreStuff(Map<String, String> args) {
        monitorContextConfiguration = getContextConfiguration();
        configYml = monitorContextConfiguration.getConfigYml();
        logger.info("initializing metric.xml file");
        this.getContextConfiguration().setMetricXml(args.get("metric-file"), Stat.Stats.class);
    }

    public static void main(String[] args) throws TaskExecutionException {

//        ConsoleAppender ca = new ConsoleAppender();
//        ca.setWriter(new OutputStreamWriter(System.out));
//        ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
//        ca.setThreshold(Level.DEBUG);
//        logger.getRootLogger().addAppender(ca);
//        NGinXMonitor monitor = new NGinXMonitor();
//
//        final Map<String, String> taskArgs = new HashMap<>();
//        taskArgs.put("config-file", "src/main/resources/conf/config.yml");
//        taskArgs.put("metric-file", "src/main/resources/conf/metrics.xml");
//
//        monitor.execute(taskArgs, null);

    }
}
