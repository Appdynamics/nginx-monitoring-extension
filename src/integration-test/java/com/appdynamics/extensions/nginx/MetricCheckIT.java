/*
 * Copyright 2020 AppDynamics LLC and its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.appdynamics.extensions.nginx;

import com.appdynamics.extensions.controller.apiservices.CustomDashboardAPIService;
import com.appdynamics.extensions.controller.apiservices.MetricAPIService;
import com.appdynamics.extensions.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MetricCheckIT {
    private MetricAPIService metricAPIService;
    private CustomDashboardAPIService customDashboardAPIService;

    @Before
    public void setup() {
        metricAPIService = IntegrationTestUtils.initializeMetricAPIService();
        customDashboardAPIService = IntegrationTestUtils.initializeCustomDashboardAPIService();
    }

    @Test
    public void testAPICallsMetric() {
        JsonNode jsonNode = null;
        if (metricAPIService != null) {
            jsonNode = metricAPIService.getMetricData("",
                    "Server%20&%20Infrastructure%20Monitoring/metric-data?metric-path=Application%20Infrastructure%20Performance%7CRoot%7CCustom%20Metrics%7CNginx%7CNginx%20Server%7CHeartBeat&time-range-type=BEFORE_NOW&duration-in-mins=60000&output=JSON");
        }
        Assert.assertNotNull("Cannot connect to controller API", jsonNode);
        JsonNode valueNode = JsonUtils.getNestedObject(jsonNode, "*", "metricId");
        Assert.assertTrue("Nginx Metrics Uploaded", valueNode.get(0).asInt() > 0);

    }

    @Test
    public void checkDashboardsUploaded() {
        boolean dashboardPresent = false;
        if (customDashboardAPIService != null) {
            JsonNode allDashboardsNode = customDashboardAPIService.getAllDashboards();
            dashboardPresent = IntegrationTestUtils.isDashboardPresent("Nginx Dashboard", allDashboardsNode);
            Assert.assertTrue(dashboardPresent);
        } else {
            Assert.assertTrue(false);
        }
    }

}
