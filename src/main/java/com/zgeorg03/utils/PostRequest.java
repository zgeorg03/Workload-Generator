package com.zgeorg03.utils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by zgeorg03 on 4/13/17.
 */
public class PostRequest extends HttpRequest {
    private final HttpClient client;
    private HttpPost httpPost;

    public PostRequest(String id,String url, List<NameValuePair> params) throws UnsupportedEncodingException {
        super(id);
        client = HttpClientBuilder.create().build();
        httpPost  = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(params));
    }

    public HttpResponse call() throws Exception {
        long start = System.currentTimeMillis();
        org.apache.http.HttpResponse httpResponse = client.execute(httpPost);
        HttpEntity entity = httpResponse.getEntity();
        String content = readInputStream(entity.getContent());
        String type = httpResponse.getEntity().getContentType().getValue().split(";")[0];
        int status = httpResponse.getStatusLine().getStatusCode();

        long duration = System.currentTimeMillis()-start;
        if (type.equals("application/json")) {
            String json = toPrettyJson(content);
            httpPost.releaseConnection();
            return new HttpResponse(id, status, json, duration);
        }
        httpPost.releaseConnection();
        return new HttpResponse(id, status, content, duration);

    }
}
