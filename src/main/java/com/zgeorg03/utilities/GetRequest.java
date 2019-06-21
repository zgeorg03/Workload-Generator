package com.zgeorg03.utilities;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
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
    private URIBuilder uriBuilder;
    private List<NameValuePair> params = new LinkedList<>();

    public List<NameValuePair> getParams() {
        return params;
    }


    public GetRequest(String id, String url,int weight,int timeout) throws URISyntaxException {
        super(id,weight, url,timeout);
        this.params = new LinkedList<>();
        this.url = url;
    }

    public GetRequest(String id, String url,int weight, List<NameValuePair> params,int timeout) throws URISyntaxException {
        super(id,weight, url,timeout);
        this.params = params;
        if(params==null)
            params = new LinkedList<>();
        this.url = url;
        this.params = params;
    }


    public HttpResponse call() throws Exception {
        uriBuilder = new URIBuilder(url).addParameters(params);
        httpGet = new HttpGet(uriBuilder.build());
        long start = System.currentTimeMillis();
        CloseableHttpResponse httpResponse = client.execute(httpGet);
        HttpEntity entity = httpResponse.getEntity();
        String content = readInputStream(entity.getContent());
        String type = httpResponse.getEntity().getContentType().getValue().split(";")[0];
        int status = httpResponse.getStatusLine().getStatusCode();
        long duration = System.currentTimeMillis() - start;
        httpResponse.close();
        if (type.equals("application/json")) {
            String json = toPrettyJson(content);
            return new HttpResponse(id, status,json, duration);
        }
        return new HttpResponse(id, status, content, duration);
    }

    @Override
    public void setUrl(String url) throws URISyntaxException {
        this.url = url;
    }

    public void setParams(List<NameValuePair> params) {
        this.params = params;
    }

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    public void setId(String id) {
        super.setId(id);
        this.id = id;
    }

    @Override
    public void setWeight(int weight) {
        super.setWeight(weight);
        this.weight = weight;
    }
}
