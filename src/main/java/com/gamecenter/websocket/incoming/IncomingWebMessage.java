package com.gamecenter.websocket.incoming;

import com.google.gson.JsonObject;
import com.gamecenter.websocket.client.WebSocketClient;

public abstract class IncomingWebMessage<T> {
    public final Class<T> type;

    public IncomingWebMessage(Class<T> type) {
        this.type = type;
    }

    public abstract void handle(WebSocketClient client, T message) throws InterruptedException;

    public static class JSONIncomingEvent {
        public String header;
        public JsonObject data;

    }
}