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

public class ConnectionsStatsExtractor implements StatsExtractor {

    @Override
    public Map<String, String> extractStats(JSONObject respJson) {
        JSONObject connections = respJson.getJSONObject("connections");
        Map<String, String> connectionStats = getConnectionStats(connections);
        return connectionStats;
    }

    private Map<String, String> getConnectionStats(JSONObject connections) {
        Map<String, String> connectionStats = new HashMap<String, String>();
        long accepted = connections.getLong("accepted");
        connectionStats.put("connections|accepted", String.valueOf(accepted));
        long dropped = connections.getLong("dropped");
        connectionStats.put("connections|dropped", String.valueOf(dropped));
        long active = connections.getLong("active");
        connectionStats.put("connections|active", String.valueOf(active));
        long idle = connections.getLong("idle");
        connectionStats.put("connections|idle", String.valueOf(idle));

        return connectionStats;
    }
}
