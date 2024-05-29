package com.gamecenter.websocket.incoming.common.misc;

import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.incoming.IncomingWebMessage;
import com.gamecenter.websocket.outgoing.common.misc.PongComposer;

public class PingEvent extends IncomingWebMessage<Object> {
    public PingEvent(){
        super(Object.class);
    }
    @Override
    public void handle(WebSocketClient client, Object message) throws InterruptedException {
        client.sendMessage(new PongComposer());
    }
}