/**
 * Copyright 2014 AppDynamics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.monitors.nginx.statsExtractor;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerZoneStatsExtractor implements StatsExtractor {

    @Override
    public Map<String, String> extractStats(JSONObject respJson) {
        JSONObject serverZones = respJson.getJSONObject("server_zones");
        Map<String, String> serverZonesStats = getServerZonesStats(serverZones);
        return serverZonesStats;
    }

    private Map<String, String> getServerZonesStats(JSONObject serverZones) {
        Map<String, String> serverZonesStats = new HashMap<String, String>();

        Set<String> serverZoneNames = serverZones.keySet();
        for (String serverZoneName : serverZoneNames) {
            JSONObject serverZone = serverZones.getJSONObject(serverZoneName);

            long processing = serverZone.getLong("processing");
            serverZonesStats.put("server_zones|" + serverZoneName + "|processing", String.valueOf(processing));

            long requests = serverZone.getLong("requests");
            serverZonesStats.put("server_zones|" + serverZoneName + "|requests", String.valueOf(requests));

            JSONObject responses = serverZone.getJSONObject("responses");

            long resp1xx = responses.getLong("1xx");
            serverZonesStats.put("server_zones|" + serverZoneName + "|responses|1xx", String.valueOf(resp1xx));

            long resp2xx = responses.getLong("2xx");
            serverZonesStats.put("server_zones|" + serverZoneName + "|responses|2xx", String.valueOf(resp2xx));

            long resp3xx = responses.getLong("3xx");
            serverZonesStats.put("server_zones|" + serverZoneName + "|responses|3xx", String.valueOf(resp3xx));

            long resp4xx = responses.getLong("4xx");
            serverZonesStats.put("server_zones|" + serverZoneName + "|responses|4xx", String.valueOf(resp4xx));

            long resp5xx = responses.getLong("5xx");
            serverZonesStats.put("server_zones|" + serverZoneName + "|responses|5xx", String.valueOf(resp5xx));

            long respTotal = responses.getLong("total");
            serverZonesStats.put("server_zones|" + serverZoneName + "|responses|total", String.valueOf(respTotal));

            long received = serverZone.getLong("received");
            serverZonesStats.put("server_zones|" + serverZoneName + "|received", String.valueOf(received));

            long sent = serverZone.getLong("sent");
            serverZonesStats.put("server_zones|" + serverZoneName + "|sent", String.valueOf(sent));
        }

        return serverZonesStats;
    }

}
