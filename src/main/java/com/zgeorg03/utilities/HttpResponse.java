package com.zgeorg03.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by zgeorg03 on 4/13/17.
 */
public class HttpResponse {
    private final String id;
    private final int status;
    private final String body;
    private final long duration;

    public HttpResponse(String id, int status, String body, long duration) {
        this.id = id;
        this.status = status;
        this.body = body;
        this.duration = duration;
    }

    @Override
    public String toString() {
        return status+"\n"+body +"\n"+duration;
    }

    public int getStatus() {
        return status;
    }

    public long getDuration() {
        return duration;
    }

    public String getId() {
        return id;
    }

    public JsonNode getJsonReply() {
        ObjectMapper parser = new ObjectMapper();
        try {
            return parser.readTree(body);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
