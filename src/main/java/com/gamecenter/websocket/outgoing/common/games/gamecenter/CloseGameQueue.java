package com.gamecenter.websocket.outgoing.common.games.gamecenter;

import com.gamecenter.websocket.outgoing.OutgoingWebMessage;

public class CloseGameQueue extends OutgoingWebMessage {

    public CloseGameQueue() {
        super("closegamequeue");
    }
}
