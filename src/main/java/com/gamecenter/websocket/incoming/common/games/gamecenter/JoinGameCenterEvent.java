package com.gamecenter.websocket.incoming.common.games.gamecenter;

import com.gamecenter.games.Globals;
import com.gamecenter.games.battleball.BattleBallRoom;
import com.gamecenter.games.battlebuild.BattleBuildGame;
import com.gamecenter.games.deliveryfood.DeliveryFoodGame;
import com.gamecenter.games.snowstorm.SnowStormGame;
import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.incoming.IncomingWebMessage;

public class JoinGameCenterEvent extends IncomingWebMessage<JoinGameCenterEvent.JSONJoinGameEvent> {
    public JoinGameCenterEvent(){
        super(JSONJoinGameEvent.class);
    }

    @Override
    public void handle(WebSocketClient client, JSONJoinGameEvent message) throws InterruptedException {
        if (client.getHabbo() != null){

            for(BattleBuildGame game : Globals.gameBattleBuild){
                if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == client.getHabbo().getHabboInfo().getId()).count() > 0) return;
            }

            for(DeliveryFoodGame game : Globals.gameDeliveryFood){
                if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == client.getHabbo().getHabboInfo().getId()).count() > 0) return;
            }

            for(BattleBallRoom game : Globals.gameBattleBall){
                if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == client.getHabbo().getHabboInfo().getId()).count() > 0) return;
            }

            for(SnowStormGame game : Globals.gameSnowStorm){
                if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == client.getHabbo().getHabboInfo().getId()).count() > 0) return;
            }

            if(message.type.equalsIgnoreCase("battlebuild")){
                if(Globals.gameBattleBuild.stream().count() > 0){
                    for(BattleBuildGame game : Globals.gameBattleBuild){
                        if(!game.hasStarted && game.joinPlayer(client.getHabbo().getClient())) return;
                    }
                }

                BattleBuildGame game = new BattleBuildGame();
                Globals.gameBattleBuild.add(game);

                game.joinPlayer(client.getHabbo().getClient());
            }

            if(message.type.equalsIgnoreCase("hamburger")){
                if(Globals.gameDeliveryFood.stream().count() > 0){
                    for(DeliveryFoodGame game : Globals.gameDeliveryFood){
                        if(!game.hasStarted && game.joinPlayer(client.getHabbo().getClient())) return;
                    }
                }

                DeliveryFoodGame game = new DeliveryFoodGame();
                Globals.gameDeliveryFood.add(game);

                game.joinPlayer(client.getHabbo().getClient());
            }

            if(message.type.equalsIgnoreCase("battleball")){
                if(Globals.gameBattleBall.stream().count() > 0){
                    for(BattleBallRoom game : Globals.gameBattleBall){
                        if(!game.hasStarted && game.joinPlayer(client.getHabbo().getClient())) return;
                    }
                }

                BattleBallRoom game = new BattleBallRoom();
                Globals.gameBattleBall.add(game);

                game.joinPlayer(client.getHabbo().getClient());
            }

            if(message.type.equalsIgnoreCase("snowstorm")){
                if(Globals.gameSnowStorm.stream().count() > 0){
                    for(SnowStormGame game : Globals.gameSnowStorm){
                        if(!game.hasStarted && game.joinPlayer(client.getHabbo().getClient())) return;
                    }
                }

                SnowStormGame game = new SnowStormGame();
                Globals.gameSnowStorm.add(game);

                game.joinPlayer(client.getHabbo().getClient());
            }
        }
    }

    static class JSONJoinGameEvent {
        String type;
    }
}
