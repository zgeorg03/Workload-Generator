package com.zgeorg03.utils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zgeorg03 on 4/13/17.
 */
public class GetRequest extends HttpRequest  {
    private HttpGet httpGet;

    private final List<NameValuePair> params;

    public GetRequest(String id, String url, List<NameValuePair> params) throws URISyntaxException {
        super(id);
        this.params = params;
        URIBuilder uriBuilder = new URIBuilder().addParameters(params);
        httpGet = new HttpGet(uriBuilder.build());
    }


    public GetRequest(String url, List<NameValuePair> params) throws URISyntaxException {
        super("");
        this.params = params;
        URIBuilder uriBuilder = new URIBuilder(url).addParameters(params);
        httpGet = new HttpGet(uriBuilder.build());
    }
    public GetRequest(String url, int connectionTimeout, List<NameValuePair> params) throws URISyntaxException {
        super("",connectionTimeout);
        this.params = params;
        URIBuilder uriBuilder = new URIBuilder(url).addParameters(params);
        httpGet = new HttpGet(uriBuilder.build());
    }
    public GetRequest(String id, String url, int connectionTimeout, List<NameValuePair> params) throws URISyntaxException {
        super(id,connectionTimeout);
        this.params = params;
        if(params==null)
            params = new LinkedList<>();
        URIBuilder uriBuilder = new URIBuilder(url).addParameters(params);
        httpGet = new HttpGet(uriBuilder.build());
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
