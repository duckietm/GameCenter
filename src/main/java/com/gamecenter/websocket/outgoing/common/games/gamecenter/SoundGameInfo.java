package com.gamecenter.websocket.outgoing.common.games.gamecenter;

import com.google.gson.JsonPrimitive;
import com.gamecenter.websocket.outgoing.OutgoingWebMessage;

public class SoundGameInfo extends OutgoingWebMessage {

    public SoundGameInfo(String link) {
        super("soundGameInfo");

        this.data.add("link", new JsonPrimitive(link));
    }
}
