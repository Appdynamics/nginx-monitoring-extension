/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.nginx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by adityajagtiani on 10/10/16.
 */
public class PlainTextResponseParser {

    public PlainTextResponseParser() {}

    public Map<String, String> parseResponse (String responseBody) throws IOException {
        Map <String, String> resultMap = new HashMap<String, String>();
        Pattern numPattern = Pattern.compile("\\d+");
        Matcher numMatcher;
        BufferedReader reader = new BufferedReader(new StringReader(responseBody));
        String line, whiteSpaceRegex = "\\s";

        while ((line = reader.readLine()) != null) {
            if (line.contains("Active connections")) {
                numMatcher = numPattern.matcher(line);
                if(numMatcher.find()) {
                    resultMap.put("Active Connections", numMatcher.group());
                }
            } else if (line.contains("server")) {
                line = reader.readLine();
                String[] results = line.trim().split(whiteSpaceRegex);
                resultMap.put("Server|Accepts", results[0]);
                resultMap.put("Server|Handled", results[1]);
                resultMap.put("Server|Requests", results[2]);
            } else if (line.contains("Reading")) {
                String[] results = line.trim().split(whiteSpaceRegex);
                resultMap.put("Reading", results[1]);
                resultMap.put("Writing", results[3]);
                resultMap.put("Waiting", results[5]);
            }
        }
        return resultMap;
    }
}
