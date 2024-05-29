package com.gamecenter.websocket.outgoing.common.games.gamecenter;

import java.util.ArrayList;

import com.eu.habbo.habbohotel.users.HabboInfo;
import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.gamecenter.games.battleball.BattleBallRoom;
import com.gamecenter.games.battlebuild.BattleBuildGame;
import com.gamecenter.games.classes.GamePlayerScore;
import com.gamecenter.games.deliveryfood.DeliveryFoodGame;
import com.gamecenter.games.snowstorm.SnowStormGame;
import com.gamecenter.websocket.outgoing.OutgoingWebMessage;

public class GameInfo extends OutgoingWebMessage {

    public GameInfo(BattleBuildGame game) {
        super("gameInfo");

        if (game != null) {

            ArrayList<GamePlayer> list = new ArrayList<>();

            for(GamePlayerScore score : game.gamePlayerScores){
                HabboInfo info = score.player.getHabbo().getHabboInfo();
                list.add(new GamePlayer(info.getUsername(), info.getLook(), score.score));
            }

            this.data.add("scores", new JsonPrimitive(new Gson().toJson(list)));
            this.data.add("theme", new JsonPrimitive(game.theme));
            this.data.add("end", new JsonPrimitive(game.timeToFinish));
        }
    }

    public GameInfo(DeliveryFoodGame game) {
        super("gameInfo");

        if (game != null) {

            ArrayList<GamePlayer> list = new ArrayList<>();

            for(GamePlayerScore score : game.gamePlayerScores){
                HabboInfo info = score.player.getHabbo().getHabboInfo();
                list.add(new GamePlayer(info.getUsername(), info.getLook(), score.score));
            }

            this.data.add("scores", new JsonPrimitive(new Gson().toJson(list)));
            this.data.add("end", new JsonPrimitive(game.timeToFinish));
        }
    }

    public GameInfo(BattleBallRoom game) {
        super("gameInfo");

        if (game != null) {
            ArrayList<GamePlayer> list = new ArrayList<>();

            this.data.add("scores", new JsonPrimitive(new Gson().toJson(list)));
            this.data.add("end", new JsonPrimitive(game.timeToFinish));
        }
    }

    public GameInfo(SnowStormGame game) {
        super("gameInfo");

        if (game != null) {
            ArrayList<GamePlayer> list = new ArrayList<>();

            this.data.add("scores", new JsonPrimitive(new Gson().toJson(list)));
            this.data.add("snow", new JsonPrimitive(true));
            this.data.add("end", new JsonPrimitive(game.timeToFinish));
        }
    }
}
