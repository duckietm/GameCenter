package com.gamecenter.packets.incoming;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.users.inventory.BadgesComponent;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.rooms.items.AddFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;
import com.eu.habbo.messages.outgoing.users.UserBadgesComposer;
import com.gamecenter.games.Globals;
import com.gamecenter.games.deliveryfood.DeliveryFoodGame;
import com.gamecenter.games.snowstorm.SnowStormGame;

import java.util.Random;

public class RequestWearingBadgesEvent2 extends MessageHandler {
    @Override
    public void handle() throws Exception {
        int userId = this.packet.readInt();
        Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(userId);

        if (habbo == null || habbo.getHabboInfo() == null || habbo.getInventory() == null || habbo.getInventory().getBadgesComponent() == null)
            this.client.sendResponse(new UserBadgesComposer(BadgesComponent.getBadgesOfflineHabbo(userId), userId));
        else
            this.client.sendResponse(new UserBadgesComposer(habbo.getInventory().getBadgesComponent().getWearingBadges(), habbo.getHabboInfo().getId()));

        for(SnowStormGame currentGame : Globals.gameSnowStorm){
            if(currentGame.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == client.getHabbo().getHabboInfo().getId()).count() > 0 && currentGame.hasStarted){
                currentGame.processSnowBall(this.client.getHabbo(), habbo);
                return;
            }
        }
    }
}

