package com.gamecenter.events;

import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.users.*;
import com.gamecenter.games.Globals;
import com.gamecenter.games.battleball.BattleBallRoom;
import com.gamecenter.games.battlebuild.BattleBuildGame;
import com.gamecenter.games.classes.RoomPlayerBattleBuild;
import com.gamecenter.games.deliveryfood.DeliveryFoodGame;
import com.gamecenter.games.snowstorm.SnowStormGame;

public class UserEvents implements EventListener {

    @EventHandler
    public static void onLogout(UserDisconnectEvent event){

        for(BattleBuildGame game : Globals.gameBattleBuild){
            if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == event.habbo.getHabboInfo().getId()).count() > 0){
                game.leftPlayer(event.habbo.getClient());
            }
        }

        for(DeliveryFoodGame game : Globals.gameDeliveryFood){
            if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == event.habbo.getHabboInfo().getId()).count() > 0){
                game.leftPlayer(event.habbo.getClient());
            }
        }

        for(SnowStormGame game : Globals.gameSnowStorm){
            if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == event.habbo.getHabboInfo().getId()).count() > 0){
                game.leftPlayer(event.habbo.getClient());
            }
        }

        for(BattleBallRoom game : Globals.gameBattleBall){
            if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == event.habbo.getHabboInfo().getId()).count() > 0){
                game.leftPlayer(event.habbo.getClient());
            }
        }
    }

    @EventHandler
    public static void onUserEnterRoomEvent(UserEnterRoomEvent event){
        boolean removePlayer = false;

        for(BattleBuildGame game : Globals.gameBattleBuild){
            if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == event.habbo.getHabboInfo().getId()).count() > 0){

                for (RoomPlayerBattleBuild r : game.rooms){
                    if (r.room.getId() == event.room.getId()){
                        removePlayer = false;
                        break;
                    }

                    removePlayer = true;
                }

                if(removePlayer){
                    game.leftPlayer(event.habbo.getClient());
                }
            }
        }

        for(DeliveryFoodGame game : Globals.gameDeliveryFood){
            if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == event.habbo.getHabboInfo().getId()).count() > 0){
                if(game.roomId != event.room.getId()){
                    game.leftPlayer(event.habbo.getClient());
                }
            }
        }

        for(SnowStormGame game : Globals.gameSnowStorm){
            if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == event.habbo.getHabboInfo().getId()).count() > 0){
                if(game.roomId != event.room.getId()){
                    game.leftPlayer(event.habbo.getClient());
                }
            }
        }

        for(BattleBallRoom game : Globals.gameBattleBall){
            if(game.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == event.habbo.getHabboInfo().getId()).count() > 0){
                if(game.roomGame.getId() != event.room.getId()){
                    game.leftPlayer(event.habbo.getClient());
                }
            }
        }
    }
}
