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
