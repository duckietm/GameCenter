package com.gamecenter.websocket.outgoing.common.games.gamecenter;

import com.gamecenter.websocket.outgoing.OutgoingWebMessage;

public class CloseVotation extends OutgoingWebMessage {

    public CloseVotation() {
        super("closeVotationBattleBuild");
    }
}
