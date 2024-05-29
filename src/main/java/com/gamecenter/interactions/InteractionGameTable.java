package com.gamecenter.interactions;

import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.users.Habbo;
import com.gamecenter.games.Globals;
import com.gamecenter.games.classes.GameUserDelivery;
import com.gamecenter.games.deliveryfood.DeliveryFoodGame;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionGameTable extends InteractionDefault {
    public InteractionGameTable(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionGameTable(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
        if (this.canToggle(client.getHabbo(), room)){
            
            for(DeliveryFoodGame currentGame : Globals.gameDeliveryFood){
                if(currentGame.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == client.getHabbo().getHabboInfo().getId()).count() > 0){
                    GameUserDelivery delivery = currentGame.deliveries.stream().filter(x -> x.player.getHabbo().getHabboInfo().getId() == client.getHabbo().getHabboInfo().getId()).findFirst().get();
                    if(delivery != null){
                        if(this.getX() == delivery.delivery.table.x && this.getY() == delivery.delivery.table.y){
                            client.getHabbo().getHabboInfo().getCurrentRoom().giveEffect(client.getHabbo(), 0, -1);
                            currentGame.deliveries.remove(delivery);

                            for(int i : room.getCurrentBots().keys()){
                                Bot bot = room.getCurrentBots().get(i);
                                if(bot.getName().equals(client.getHabbo().getHabboInfo().getUsername())){
                                    room.removeBot(bot);
                                }
                            }

                            currentGame.addScore(client);
                        }
                    }
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
