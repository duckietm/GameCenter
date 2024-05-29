package com.gamecenter.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.util.pathfinding.Rotation;
import com.gamecenter.games.Globals;

import com.gamecenter.games.deliveryfood.DeliveryFoodGame;
import com.gamecenter.games.snowstorm.SnowStormGame;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionSnowMachine extends InteractionDefault {
    public InteractionSnowMachine(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionSnowMachine(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
        if (this.canToggle(client.getHabbo(), room)){
            for(SnowStormGame currentGame : Globals.gameSnowStorm){
                if(currentGame.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == client.getHabbo().getHabboInfo().getId()).count() > 0){
                    client.getHabbo().getRoomUnit().setRotation(RoomUserRotation.values()[Rotation.Calculate(client.getHabbo().getRoomUnit().getX(), client.getHabbo().getRoomUnit().getY(), this.getX(), this.getY())]);
                    currentGame.processNewBall(client.getHabbo(), this);
                    return;
                }
            }

        }
    }

    @Override
    public boolean canToggle(Habbo habbo, Room room) {
        return RoomLayout.tilesAdjecent(room.getLayout().getTile(this.getX(), this.getY()), habbo.getRoomUnit().getCurrentLocation());
    }

    @Override
    public boolean isUsable() {
        return true;
    }
}
