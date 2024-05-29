package com.gamecenter.websocket.incoming.common.games.gamecenter;

import com.gamecenter.games.Globals;
import com.gamecenter.games.battlebuild.BattleBuildGame;
import com.gamecenter.games.classes.GamePlayerScore;
import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.incoming.IncomingWebMessage;

public class VotationGameEvent extends IncomingWebMessage<VotationGameEvent.JSONJoinGameEvent> {
    public VotationGameEvent(){
        super(JSONJoinGameEvent.class);
    }

    @Override
    public void handle(WebSocketClient client, JSONJoinGameEvent message) throws InterruptedException {
        if (client.getHabbo() != null){

            for(BattleBuildGame game : Globals.gameBattleBuild){
                if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == client.getHabbo().getHabboInfo().getId()).count() > 0){
                    GamePlayerScore score = game.gamePlayerScores.stream().filter(x -> x.player.getHabbo().getHabboInfo().getId() == message.participantId).findAny().orElse(null);
                    if(score != null){
                        score.score = score.score + message.votationNumber;
                        game.broadcastScores();
                    }
                }
            }
        }
    }

    static class JSONJoinGameEvent {
        int participantId;
        int votationNumber;
    }
}
