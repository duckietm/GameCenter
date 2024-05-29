package com.gamecenter.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.users.Habbo;
import com.gamecenter.games.Globals;

import com.gamecenter.games.deliveryfood.DeliveryFoodGame;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionGameDelivery extends InteractionDefault {
    public InteractionGameDelivery(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionGameDelivery(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
        if (this.canToggle(client.getHabbo(), room)){

        if(client.getHabbo().getRoomUnit().getEffectId() != 0) return;
            
            for(DeliveryFoodGame currentGame : Globals.gameDeliveryFood){
                if(currentGame.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == client.getHabbo().getHabboInfo().getId()).count() > 0){
                    currentGame.processFurniDelivery(this, client);
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
