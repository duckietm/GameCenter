package com.gamecenter.websocket.outgoing.common.games.gamecenter;

import com.eu.habbo.habbohotel.users.HabboInfo;
import com.google.gson.JsonPrimitive;
import com.gamecenter.websocket.outgoing.OutgoingWebMessage;

public class ShowVotation extends OutgoingWebMessage {

    public ShowVotation(HabboInfo info) {
        super("showVotationBattleBuild");

        if (info != null) {
            this.data.add("name", new JsonPrimitive(info.getUsername()));
            this.data.add("figure", new JsonPrimitive(info.getLook()));
            this.data.add("id", new JsonPrimitive(info.getId()));
        }
    }
}
