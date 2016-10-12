package com.appdynamics.monitors.nginx;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
/**
 * Created by adityajagtiani on 10/10/16.
 */
public class JSONResponseParserTest {
    JSONResponseParser parser;
    String responseBody;
    Map<String, String> results;

    @Before
    public void initialize() {
        parser = new JSONResponseParser();
        results = new HashMap<String, String>();
    }

    @Test
    public void parseResponseTest_NumberOfEntries() throws IOException {
        FileInputStream inputStream = new FileInputStream("src/test/resources/TestJSON.txt");
        try {
            responseBody = IOUtils.toString(inputStream);
        } finally {
            inputStream.close();
        }
        Assert.assertTrue(parser.parseResponse(responseBody).size() == 200);
    }
}
