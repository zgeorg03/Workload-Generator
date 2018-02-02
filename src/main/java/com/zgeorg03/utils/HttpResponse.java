package com.zgeorg03.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

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

    public JsonObject getJsonReply() {
        JsonParser parser = new JsonParser();
        try {
            return parser.parse(body).getAsJsonObject();
        }catch (JsonParseException ex){
            return null;
        }
    }
}
