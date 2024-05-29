package com.gamecenter.websocket.outgoing.common.misc;

import com.eu.habbo.Emulator;
import com.google.gson.JsonPrimitive;
import com.gamecenter.websocket.outgoing.OutgoingWebMessage;

public class PongComposer extends OutgoingWebMessage {
    public PongComposer() {
        super("pong");
        this.data.add("status", new JsonPrimitive(Emulator.getGameEnvironment().getHabboManager().getOnlineCount()));
    }
}
