package com.zgeorg03.models;

import org.apache.http.NameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

    public Operation(String operationId, int weight, String url, String method, List<NameValuePair> data) throws UnsupportedEncodingException {
        this.operationId = operationId;
        this.weight = weight;
        String t[] = url.split("\\?");
        if(t.length>1) {
            String encode = URLEncoder.encode(t[1],"UTF-8");
            System.out.println(encode);
            this.url = t[0]+"?"+encode;
        }else
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
