/**
 * Copyright 2014 AppDynamics, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.monitors.nginx.statsExtractor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class UpstreamsStatsExtractor implements StatsExtractor {

    @Override
    public Map<String, String> extractStats(JSONObject respJson) {

        JSONObject upstreams = respJson.getJSONObject("upstreams");

        int version = respJson.getInt("version");

        Map<String, String> upstreamsStats = new HashMap<String, String>();
        if(version == 6) {
            upstreamsStats = getUpstreamsStatsV6(upstreams);
        } else if(version == 5) {
            upstreamsStats = getUpstreamsStatsV5(upstreams);
        }
        return upstreamsStats;
    }

    private Map<String, String> getUpstreamsStatsV5(JSONObject upstreams) {
        Map<String, String> upstreamsStats = new HashMap<String, String>();
        Set<String> serverGroupNames = upstreams.keySet();

        for(String serverGroupName : serverGroupNames) {
            JSONArray serverGroups = upstreams.getJSONArray(serverGroupName);

            for (int i = 0; i < serverGroups.length(); i++) {
                JSONObject server = serverGroups.getJSONObject(i);
                collectMetrics(upstreamsStats, serverGroupName, server);
            }

        }

        return upstreamsStats;
    }

    private Map<String, String> getUpstreamsStatsV6(JSONObject upstreams) {
        Map<String, String> upstreamsStats = new HashMap<String, String>();
        Set<String> serverGroupNames = upstreams.keySet();

        for (String serverGroupName : serverGroupNames) {
            JSONObject jsonObject = upstreams.getJSONObject(serverGroupName);

            Set<String> keys = jsonObject.keySet();
            for (String key : keys) {
                Object element = jsonObject.get(key);

                if (element instanceof JSONArray) {
                    JSONArray serverGroups = (JSONArray)element;

                    for (int i = 0; i < serverGroups.length(); i++) {
                        JSONObject server = serverGroups.getJSONObject(i);
                        collectMetrics(upstreamsStats, serverGroupName, server);
                    }
                }
            }
        }
        return upstreamsStats;
    }

    private void collectMetrics(Map<String, String> upstreamsStats, String serverGroupName, JSONObject server) {


        String serverIp = server.getString("server");

        boolean backup = server.getBoolean("backup");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|backup", backup ? "1" : "0");

        long weight = server.getLong("weight");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|weight", String.valueOf(weight));

        // “up”, “down”, “unavail”, or “unhealthy”.
        String state = server.getString("state");
        int stateInt = -1;
        if ("up".equals(state)) {
            stateInt = 0;
        } else if ("down".equals(state)) {
            stateInt = 1;
        } else if ("unavail".equals(state)) {
            stateInt = 2;
        } else if ("unhealthy".equals(state)) {
            stateInt = 3;
        }
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|state", String.valueOf(stateInt));

        long active = server.getLong("active");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|active", String.valueOf(active));

        if (server.has("max_conns")) {
            long maxConns = server.getLong("max_conns");
            upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|max_conns", String.valueOf(maxConns));
        }

        long requests = server.getLong("requests");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|requests", String.valueOf(requests));

        JSONObject responses = server.getJSONObject("responses");
        long resp1xx = responses.getLong("1xx");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|responses|1xx", String.valueOf(resp1xx));

        long resp2xx = responses.getLong("2xx");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|responses|2xx", String.valueOf(resp2xx));

        long resp3xx = responses.getLong("3xx");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|responses|3xx", String.valueOf(resp3xx));

        long resp4xx = responses.getLong("4xx");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|responses|4xx", String.valueOf(resp4xx));

        long resp5xx = responses.getLong("5xx");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|responses|5xx", String.valueOf(resp5xx));

        long respTotal = responses.getLong("total");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|responses|total", String.valueOf(respTotal));

        long sent = server.getLong("sent");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|sent", String.valueOf(sent));

        long received = server.getLong("received");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|received", String.valueOf(received));

        long upstreamServerFails = server.getLong("fails");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|fails", String.valueOf(upstreamServerFails));

        long unavail = server.getLong("unavail");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|unavail", String.valueOf(unavail));

        JSONObject healthChecks = server.getJSONObject("health_checks");
        long checks = healthChecks.getLong("checks");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|health_checks|checks", String.valueOf(checks));

        long healthCheckFails = healthChecks.getLong("fails");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|health_checks|fails", String.valueOf(healthCheckFails));

        long unhealthy = healthChecks.getLong("unhealthy");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|health_checks|unhealthy", String.valueOf(unhealthy));

        if (server.has("last_passed")) {
            boolean lastPassed = healthChecks.getBoolean("last_passed");
            upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|health_checks|last_passed", String.valueOf(lastPassed ? 0 : 1));
        }

        long downtime = server.getLong("downtime");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|downtime", String.valueOf(downtime));

        long downstart = server.getLong("downstart");
        upstreamsStats.put("upstreams|" + serverGroupName + "|" + serverIp + "|downstart", String.valueOf(downstart));
    }
}
