package com.gamecenter.websocket.outgoing.common.games.gamecenter;

import java.util.ArrayList;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.gamecenter.games.battleball.BattleBallRoom;
import com.gamecenter.games.battlebuild.BattleBuildGame;
import com.gamecenter.games.deliveryfood.DeliveryFoodGame;
import com.gamecenter.games.snowstorm.SnowStormGame;
import com.gamecenter.websocket.outgoing.OutgoingWebMessage;

public class GamePlayerQueue extends OutgoingWebMessage {

    public GamePlayerQueue(BattleBuildGame game) {
        super("gamequeue");

        if (game != null) {

            ArrayList<GamePlayer> list = new ArrayList<>();

            for(GameClient player : game.players){
                HabboInfo info = player.getHabbo().getHabboInfo();
                list.add(new GamePlayer(info.getUsername(), info.getLook(), 0));
            }
            this.data.add("players", new JsonPrimitive(new Gson().toJson(list)));
        }
    }

    public GamePlayerQueue(DeliveryFoodGame game) {
        super("gamequeue");

        if (game != null) {

            ArrayList<GamePlayer> list = new ArrayList<>();

            for(GameClient player : game.players){
                HabboInfo info = player.getHabbo().getHabboInfo();
                list.add(new GamePlayer(info.getUsername(), info.getLook(), 0));
            }
            this.data.add("players", new JsonPrimitive(new Gson().toJson(list)));
        }
    }

    public GamePlayerQueue(BattleBallRoom game) {
        super("gamequeue");

        if (game != null) {

            ArrayList<GamePlayer> list = new ArrayList<>();

            for(GameClient player : game.players){
                HabboInfo info = player.getHabbo().getHabboInfo();
                list.add(new GamePlayer(info.getUsername(), info.getLook(), 0));
            }
            this.data.add("players", new JsonPrimitive(new Gson().toJson(list)));
        }
    }

    public GamePlayerQueue(SnowStormGame game) {
        super("gamequeue");

        if (game != null) {

            ArrayList<GamePlayer> list = new ArrayList<>();

            for(GameClient player : game.players){
                HabboInfo info = player.getHabbo().getHabboInfo();
                list.add(new GamePlayer(info.getUsername(), info.getLook(), 0));
            }
            this.data.add("players", new JsonPrimitive(new Gson().toJson(list)));
        }
    }
}
