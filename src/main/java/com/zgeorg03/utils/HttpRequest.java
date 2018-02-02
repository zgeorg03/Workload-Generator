package com.zgeorg03.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

/**
 * Created by zgeorg03 on 4/13/17.
 */
public abstract  class HttpRequest implements Callable<HttpResponse> {
    protected final HttpClient client;
    protected final String id;


    protected HttpRequest(String id) {
        this.id = id;
        client = HttpClientBuilder.create().build();
    }
    protected HttpRequest(String id, int connectionTimeout) {
        this.id=id;
        client = HttpClientBuilder.create().setDefaultRequestConfig(
                RequestConfig.custom().setConnectTimeout(connectionTimeout).build()
        ).build();
    }
    protected String readInputStream(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader( new InputStreamReader(inputStream));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        return response.toString();

    }
    protected String toPrettyJson(String rawJson){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser parser = new JsonParser();
        JsonElement object = parser.parse(rawJson).getAsJsonObject();
        return gson.toJson(object);
    }
}
