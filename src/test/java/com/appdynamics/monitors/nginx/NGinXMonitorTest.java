package com.appdynamics.monitors.nginx;

import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Test;

import java.util.Map;

public class NGinXMonitorTest {

    @Test
    public void test() throws TaskExecutionException {
        NGinXMonitor monitor = new NGinXMonitor();
        Map<String, String> taskArgs = Maps.newHashMap();
        taskArgs.put("config-file", "src/test/resources/config.yml");
        taskArgs.put("metric-file", "src/test/resources/metricJson.xml");
        monitor.execute(taskArgs, null);
    }
}