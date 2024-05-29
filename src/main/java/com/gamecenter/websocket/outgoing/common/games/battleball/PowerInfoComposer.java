package com.gamecenter.websocket.outgoing.common.games.battleball;

import com.google.gson.JsonPrimitive;
import com.gamecenter.games.battleball.BattleBallGamePlayer;
import com.gamecenter.websocket.outgoing.OutgoingWebMessage;

public class PowerInfoComposer extends OutgoingWebMessage {
    public PowerInfoComposer(BattleBallGamePlayer player) {
        super("battleBallPower");

        this.data.add("name", new JsonPrimitive(player.skillsType != null ? player.skillsType.type : "null"));
        this.data.add("time", new JsonPrimitive(player.skillTime));
    }
}
