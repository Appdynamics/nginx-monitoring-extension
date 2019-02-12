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