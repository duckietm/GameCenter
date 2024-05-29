package com.gamecenter.websocket.outgoing.common.games.gamecenter;

import java.util.ArrayList;

import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.gamecenter.games.battleball.BattleBallGame;
import com.gamecenter.games.battlebuild.BattleBuildGame;
import com.gamecenter.games.classes.GamePlayerScore;
import com.gamecenter.games.deliveryfood.DeliveryFoodGame;
import com.gamecenter.games.snowstorm.SnowTeam;
import com.gamecenter.websocket.outgoing.OutgoingWebMessage;

public class WinnerInfo extends OutgoingWebMessage {

    public WinnerInfo(BattleBuildGame game, int maxScore) {
        super("winnerInfo");

        if (game != null) {

            ArrayList<GamePlayer> list = new ArrayList<>();

            for(GamePlayerScore score : game.gamePlayerScores){
                if(score.score == maxScore){
                    HabboInfo info = score.player.getHabbo().getHabboInfo();
                    list.add(new GamePlayer(info.getUsername(), info.getLook(), score.score));
                }
            }

            this.data.add("scores", new JsonPrimitive(new Gson().toJson(list)));
        }
    }

    public WinnerInfo(DeliveryFoodGame game, int maxScore) {
        super("winnerInfo");

        if (game != null) {

            ArrayList<GamePlayer> list = new ArrayList<>();

            for(GamePlayerScore score : game.gamePlayerScores){
                if(score.score == maxScore){
                    HabboInfo info = score.player.getHabbo().getHabboInfo();
                    list.add(new GamePlayer(info.getUsername(), info.getLook(), score.score));
                }
            }

            this.data.add("scores", new JsonPrimitive(new Gson().toJson(list)));
        }
    }

    public WinnerInfo(BattleBallGame game, GameTeam teamWinner) {
        super("winnerInfo");

        if (game != null) {

            ArrayList<GamePlayer> list = new ArrayList<>();

            for(com.eu.habbo.habbohotel.games.GamePlayer p : teamWinner.getMembers()){
                HabboInfo info = p.getHabbo().getHabboInfo();
                list.add(new GamePlayer(info.getUsername(), info.getLook(), p.getScore()));
            }

            this.data.add("scores", new JsonPrimitive(new Gson().toJson(list)));
        }
    }

    public WinnerInfo(SnowTeam team) {
        super("winnerInfo");

        if (team != null) {

            ArrayList<GamePlayer> list = new ArrayList<>();

            if(team.reference.equals("a")){
                list.add(new GamePlayer(team.reference, "hd-0-0.ch-20000-0", team.points));
            }else {
                list.add(new GamePlayer(team.reference, "hd-0-0.ch-20001-0", team.points));
            }


            this.data.add("scores", new JsonPrimitive(new Gson().toJson(list)));
        }
    }
}
