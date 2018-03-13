/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.nginx.statsExtractor;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequestsStatsExtractor implements StatsExtractor {

    @Override
    public Map<String, String> extractStats(JSONObject respJson) {
        JSONObject requests = respJson.getJSONObject("requests");
        Map<String, String> requestStats = getRequestStats(requests);
        return requestStats;
    }

    private Map<String, String> getRequestStats(JSONObject requests) {
        Map<String, String> requestStats = new HashMap<String, String>();
        long total = requests.getLong("total");
        requestStats.put("requests|total", String.valueOf(total));
        long current = requests.getLong("current");
        requestStats.put("requests|current", String.valueOf(current));
        return requestStats;
    }
}
