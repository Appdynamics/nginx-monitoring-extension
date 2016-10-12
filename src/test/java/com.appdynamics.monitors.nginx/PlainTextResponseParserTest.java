package com.appdynamics.monitors.nginx;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.*;

import java.io.IOException;

/**
 * Created by adityajagtiani on 10/10/16.
 */
public class PlainTextResponseParserTest {
    PlainTextResponseParser parser;
    String responseBody;
    Map<String, String> results;

    @Before
    public void initialize() {
        parser = new PlainTextResponseParser();
        responseBody = "Active connections: 1 \n server accepts handled requests\n 28 28 26 \nReading: 0 Writing: 1 Waiting: 5770848640 \n";
        results = new HashMap<String, String>();
    }

    @Test
    public void parseResponseTest_SizeOfMap() throws IOException {
        Assert.assertTrue(parser.parseResponse(responseBody).size() == 7);
    }

    @Test
    public void parseResponseTest_Keys() throws IOException {
        results = parser.parseResponse(responseBody);
        Assert.assertTrue(results.containsKey("Active Connections"));
        Assert.assertTrue(results.containsKey("Reading"));
        Assert.assertTrue(results.containsKey("Writing"));
        Assert.assertTrue(results.containsKey("Waiting"));
        Assert.assertTrue(results.containsKey("Server|Requests"));
        Assert.assertTrue(results.containsKey("Server|Handled"));
        Assert.assertTrue(results.containsKey("Server|Accepts"));
    }

    @Test
    public void parseResponseTest_Values() throws IOException {
        results = parser.parseResponse(responseBody);
        Assert.assertTrue(results.get("Active Connections").equals("1"));
        Assert.assertTrue(results.get("Reading").equals("0"));
        Assert.assertTrue(results.get("Writing").equals("1"));
        Assert.assertTrue(results.get("Waiting").equals("5770848640"));
        Assert.assertTrue(results.get("Server|Handled").equals("28"));
        Assert.assertTrue(results.get("Server|Accepts").equals("28"));
        Assert.assertTrue(results.get("Server|Requests").equals("26"));
    }
}
