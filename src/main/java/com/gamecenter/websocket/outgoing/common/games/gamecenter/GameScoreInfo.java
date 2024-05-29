package com.gamecenter.websocket.outgoing.common.games.gamecenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.eu.habbo.habbohotel.users.HabboInfo;
import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.gamecenter.games.battlebuild.BattleBuildGame;
import com.gamecenter.games.classes.GamePlayerScore;
import com.gamecenter.games.deliveryfood.DeliveryFoodGame;
import com.gamecenter.games.snowstorm.SnowStormGame;
import com.gamecenter.games.snowstorm.SnowTeam;
import com.gamecenter.websocket.outgoing.OutgoingWebMessage;

public class GameScoreInfo extends OutgoingWebMessage {

    public GameScoreInfo(BattleBuildGame game) {
        super("gameScoreInfo");

        if (game != null) {

            ArrayList<GamePlayer> list = new ArrayList<>();

            for(GamePlayerScore score : game.gamePlayerScores){
                HabboInfo info = score.player.getHabbo().getHabboInfo();
                list.add(new GamePlayer(info.getUsername(), info.getLook(), score.score));
            }

            Collections.sort(list, new Comparator<GamePlayer>() {
                @Override
                public int compare(GamePlayer o1, GamePlayer o2) {
                    return o1.score + o2.score;
                }
            });

            int ranking = 1;
            ArrayList<GamePlayerPos> finalList = new ArrayList<>();
            for(GamePlayer p : list){
                finalList.add(new GamePlayerPos(ranking, p.username, p.figure, p.score));
                ranking++;
            }

            this.data.add("scores", new JsonPrimitive(new Gson().toJson(finalList)));
        }
    }

    public GameScoreInfo(DeliveryFoodGame game) {
        super("gameScoreInfo");

        if (game != null) {

            ArrayList<GamePlayer> list = new ArrayList<>();

            for(GamePlayerScore score : game.gamePlayerScores){
                HabboInfo info = score.player.getHabbo().getHabboInfo();
                list.add(new GamePlayer(info.getUsername(), info.getLook(), score.score));
            }

            Collections.sort(list, new Comparator<GamePlayer>() {
                @Override
                public int compare(GamePlayer o1, GamePlayer o2) {
                    return o1.score + o2.score;
                }
            });

            int ranking = 1;
            ArrayList<GamePlayerPos> finalList = new ArrayList<>();
            for(GamePlayer p : list){
                finalList.add(new GamePlayerPos(ranking, p.username, p.figure, p.score));
                ranking++;
            }

            this.data.add("scores", new JsonPrimitive(new Gson().toJson(finalList)));
        }
    }

    public GameScoreInfo(SnowStormGame game) {
        super("gameScoreInfo");

        if (game != null) {

            ArrayList<GamePlayer> list = new ArrayList<>();

            for(SnowTeam team : game.teams){
                list.add(new GamePlayer(team.reference, "", team.points));
            }

            Collections.sort(list, new Comparator<GamePlayer>() {
                @Override
                public int compare(GamePlayer o1, GamePlayer o2) {
                    return o1.score + o2.score;
                }
            });

            int ranking = 1;
            ArrayList<GamePlayerPos> finalList = new ArrayList<>();
            for(GamePlayer p : list){
                finalList.add(new GamePlayerPos(ranking, p.username, p.figure, p.score));
                ranking++;
            }

            this.data.add("scores", new JsonPrimitive(new Gson().toJson(list)));
        }
    }
}
