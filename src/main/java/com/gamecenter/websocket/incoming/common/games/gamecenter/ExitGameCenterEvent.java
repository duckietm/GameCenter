package com.gamecenter.websocket.incoming.common.games.gamecenter;

import com.gamecenter.games.Globals;
import com.gamecenter.games.battleball.BattleBallRoom;
import com.gamecenter.games.battlebuild.BattleBuildGame;
import com.gamecenter.games.deliveryfood.DeliveryFoodGame;
import com.gamecenter.games.snowstorm.SnowStormGame;
import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.incoming.IncomingWebMessage;

public class ExitGameCenterEvent extends IncomingWebMessage<ExitGameCenterEvent.JSONJoinExitEvent> {
    public ExitGameCenterEvent(){
        super(JSONJoinExitEvent.class);
    }

    @Override
    public void handle(WebSocketClient client, JSONJoinExitEvent message) throws InterruptedException {
        if (client.getHabbo() != null){

            for(BattleBuildGame game : Globals.gameBattleBuild){
                if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == client.getHabbo().getHabboInfo().getId()).count() > 0){
                    game.leftPlayer(client.getHabbo().getClient());
                    return;
                }
            }

            for(DeliveryFoodGame game : Globals.gameDeliveryFood){
                if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == client.getHabbo().getHabboInfo().getId()).count() > 0){
                    game.leftPlayer(client.getHabbo().getClient());
                    return;
                }
            }

            for(BattleBallRoom game : Globals.gameBattleBall){
                if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == client.getHabbo().getHabboInfo().getId()).count() > 0){
                    game.leftPlayer(client.getHabbo().getClient());
                    return;
                }
            }

            for(SnowStormGame game : Globals.gameSnowStorm){
                if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == client.getHabbo().getHabboInfo().getId()).count() > 0){
                    game.leftPlayer(client.getHabbo().getClient());
                    return;
                }
            }
        }
    }

    static class JSONJoinExitEvent {

    }
}
