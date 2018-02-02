package com.zgeorg03.models;

import org.apache.http.NameValuePair;

import java.util.List;

/**
 * Created by zgeorg03 on 4/14/17.
 */
public class Operation {
    private final String operationId;
    private final int weight;
    private final String url;
    private final String method;
    private final List<NameValuePair> data;

    public Operation(String operationId, int weight, String url, String method, List<NameValuePair> data) {
        this.operationId = operationId;
        this.weight = weight;
        this.url = url;
        this.method = method;
        this.data = data;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public List<NameValuePair> getData() {
        return data;
    }

    public int getWeight() {
        return weight;
    }
}
