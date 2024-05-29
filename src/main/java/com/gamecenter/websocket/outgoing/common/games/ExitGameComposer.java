package com.gamecenter.websocket.outgoing.common.games;

import com.gamecenter.websocket.outgoing.OutgoingWebMessage;

public class ExitGameComposer extends OutgoingWebMessage {
    public ExitGameComposer(String name) {
        super("exitGame");
    }
}
