package com.gamecenter.websocket.outgoing.common.misc;

import com.google.gson.Gson;

import com.gamecenter.websocket.outgoing.OutgoingWebMessage;

public class ResponseDataComposer extends OutgoingWebMessage {

    public ResponseDataComposer(String name, Object data) {
        super(name);
        this.data.add("data", new Gson().toJsonTree(data));
    }
    
}
