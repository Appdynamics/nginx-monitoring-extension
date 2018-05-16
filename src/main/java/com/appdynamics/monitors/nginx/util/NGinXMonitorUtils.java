/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.nginx.util;

import java.util.Map;

/**
 * Created by aditya.jagtiani on 5/15/18.
 */
public class NGinXMonitorUtils {

    public static Map<String, String> getEnvDataForUrl(Map<String, String> server) {
        String hostName = System.getenv(server.get("name"));
        server.put("name", hostName);

        String host = System.getenv(server.get("uri").split(":")[0]);
        String port = System.getenv(server.get("uri").split(":")[1]);
        server.put("uri", host + ":" + port);

        return server;
    }
}
