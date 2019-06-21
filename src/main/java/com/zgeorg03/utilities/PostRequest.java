package com.zgeorg03.utilities;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by zgeorg03 on 4/13/17.
 */
public class PostRequest extends HttpRequest {
    private HttpPost httpPost;
    public PostRequest(){
        super();
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setUrl(String url) throws URISyntaxException {
        super.setUrl(url);
    }

    public PostRequest(String id, int weight, String url, List<NameValuePair> params,int timeout) throws UnsupportedEncodingException {
        super(id,weight, url,timeout);
        httpPost  = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(params));
    }

    public HttpResponse call() throws Exception {
        long start = System.currentTimeMillis();
        URIBuilder riBuilder = new URIBuilder(url);
        httpPost = new HttpPost(riBuilder.build());
        CloseableHttpResponse httpResponse = client.execute(httpPost);
        HttpEntity entity = httpResponse.getEntity();
        String content = readInputStream(entity.getContent());
        String type = httpResponse.getEntity().getContentType().getValue().split(";")[0];
        int status = httpResponse.getStatusLine().getStatusCode();

        long duration = System.currentTimeMillis()-start;
        httpResponse.close();
        if (type.equals("application/json")) {
            String json = toPrettyJson(content);
            return new HttpResponse(id, status, json, duration);
        }
        return new HttpResponse(id, status, content, duration);

    }
}
