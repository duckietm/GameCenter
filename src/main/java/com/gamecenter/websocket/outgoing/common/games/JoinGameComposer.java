package com.gamecenter.websocket.outgoing.common.games;

import com.google.gson.JsonPrimitive;
import com.gamecenter.websocket.outgoing.OutgoingWebMessage;

public class JoinGameComposer extends OutgoingWebMessage {
    public JoinGameComposer(String gameName) {
        super("joinGame");
        this.data.add("game", new JsonPrimitive(gameName));
    }

}
