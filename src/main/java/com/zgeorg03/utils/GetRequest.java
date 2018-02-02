package com.zgeorg03.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;

/**
 * Created by zgeorg03 on 4/13/17.
 */
public class GetRequest extends HttpRequest  {
    private HttpGet httpGet;

    public GetRequest(String id, String url) {
        super(id);
        httpGet  = new HttpGet(url);
    }

    public GetRequest(String url) {
        super("");
        httpGet  = new HttpGet(url);
    }
    public GetRequest(String url,int connectionTimeout) {
        super("",connectionTimeout);
        httpGet  = new HttpGet(url);
    }

    public HttpResponse call() throws Exception {
        long start = System.currentTimeMillis();
        org.apache.http.HttpResponse httpResponse = client.execute(httpGet);
        HttpEntity entity = httpResponse.getEntity();
        String content = readInputStream(entity.getContent());
        String type = httpResponse.getEntity().getContentType().getValue().split(";")[0];
        int status = httpResponse.getStatusLine().getStatusCode();

        httpGet.releaseConnection();
        long duration = System.currentTimeMillis() - start;
        if (type.equals("application/json")) {
            String json = toPrettyJson(content);
            httpGet.releaseConnection();
            return new HttpResponse(id, status,json, duration);
        }
        httpGet.releaseConnection();
        return new HttpResponse(id, status, content, duration);
    }
}
