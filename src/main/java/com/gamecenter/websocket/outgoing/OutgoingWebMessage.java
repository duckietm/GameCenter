package com.gamecenter.websocket.outgoing;

import com.google.gson.JsonObject;

public class OutgoingWebMessage {
    public String header;
    public JsonObject data;

    public OutgoingWebMessage(String name) {
        this.header = name;
        this.data = new JsonObject();
    }
}
