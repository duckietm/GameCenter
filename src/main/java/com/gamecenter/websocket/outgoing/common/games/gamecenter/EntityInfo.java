package com.gamecenter.websocket.outgoing.common.games.gamecenter;

import com.google.gson.JsonPrimitive;
import com.gamecenter.games.snowstorm.SnowPlayer;
import com.gamecenter.websocket.outgoing.OutgoingWebMessage;

public class EntityInfo extends OutgoingWebMessage {

    public EntityInfo(SnowPlayer player) {
        super("entityInfo");

        if (player != null) {
            this.data.add("balls", new JsonPrimitive(player.actualBalls));
            this.data.add("hp", new JsonPrimitive(player.actualHp));
            this.data.add("team", new JsonPrimitive(player.team.reference));
        }
    }
}
