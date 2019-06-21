package com.zgeorg03.utilities;

import com.zgeorg03.models.Operation;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

/**
 * Created by zgeorg03 on 4/13/17.
 */
public abstract  class HttpRequest  extends Operation implements Callable<HttpResponse> {
    protected CloseableHttpClient client;
    protected  String url;
    protected  String id;


    protected HttpRequest() {
        super();

    }

    protected HttpRequest(String id, int weight , String url,int timeout) {
        super(id,weight);
        this.id = id;
        this.url = url;
        //RequestConfig config = RequestConfig.custom().setTim(2*1000).build();
        //client = HttpClientBuilder.create().build();
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder.setConnectTimeout(timeout);
        requestBuilder.setConnectionRequestTimeout(timeout);
        requestBuilder.setSocketTimeout(timeout);
         client = HttpClientBuilder.create()
                .setMaxConnPerRoute(100)
                .setMaxConnTotal(100)
                .setDefaultRequestConfig(requestBuilder.build())
                .build();
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

    public String getUrl() {
        return url;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setUrl(String url) throws URISyntaxException {
        this.url = url;
    }

    protected String toPrettyJson(String rawJson){
        return rawJson;
    }
}
