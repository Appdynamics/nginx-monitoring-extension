/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.nginx;

import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Test;

import java.util.Map;

public class NginXMonitorTest {

    @Test
    public void test() throws TaskExecutionException {
        NginxMonitor monitor = new NginxMonitor();
        Map<String, String> taskArgs = Maps.newHashMap();
        taskArgs.put("config-file", "src/test/resources/config.yml");
        taskArgs.put("metric-file", "src/test/resources/metricJson.xml");
        monitor.execute(taskArgs, null);
    }
}