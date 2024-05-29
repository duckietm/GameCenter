package com.gamecenter.interactions.battleball.skills;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.items.AddFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import com.gamecenter.games.battleball.BattleBallGame;
import com.gamecenter.games.battleball.BattleBallGamePlayer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionPins extends InteractionDefault {

    public InteractionPins(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.setExtradata("0");
    }

    public InteractionPins(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.setExtradata("0");
    }

    @Override
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
        return true;
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public void onWalk(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {

    }

    @Override
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOn(roomUnit, room, objects);

        Habbo habbo = room.getHabbo(roomUnit);

        if(habbo != null){
            if (habbo.getHabboInfo().getCurrentGame().equals(BattleBallGame.class)) {
                BattleBallGame game = ((BattleBallGame) room.getGame(BattleBallGame.class));
                BattleBallGamePlayer player = (BattleBallGamePlayer) habbo.getHabboInfo().getGamePlayer();

                if(player != null){
                    this.stunPlayer(habbo, room);

                    room.removeHabboItem(this);
                    HabboItem item = this;
                    Emulator.getThreading().run(() -> {
                        new QueryDeleteHabboItem(item.getId()).run();
                        room.sendComposer(new RemoveFloorItemComposer(item).compose());
                        room.updateTile(room.getLayout().getTile(item.getX(), item.getY()));
                    }, 500);

                }
            }
        }
    }

    public void stunPlayer(Habbo habbo, Room room){
        habbo.getRoomUnit().setCanWalk(false);

        Item item = Emulator.getGameEnvironment().getItemManager().getItem(BattleBallGame.ITEMID_STUN);
        HabboItem newItem = Emulator.getGameEnvironment().getItemManager().createItem(12, item, 0, 0, "");

        if(item != null && newItem != null){
            HabboItem itemTile = room.getTopItemAt(this.getX(), this.getY());

            newItem.setX(this.getX());
            newItem.setY(this.getY());
            newItem.setZ(itemTile.getZ() + 0.1);
            newItem.setRoomId(room.getId());
            newItem.needsUpdate(true);

            room.addHabboItem(newItem);

            room.updateItem(newItem);
            room.updateTile(room.getLayout().getTile(this.getX(), this.getY()));
            room.sendComposer(new AddFloorItemComposer(newItem, "BattleBall").compose());

            Emulator.getThreading().run(() -> {
                new QueryDeleteHabboItem(newItem.getId()).run();
                room.removeHabboItem(newItem);
                room.sendComposer(new RemoveFloorItemComposer(newItem).compose());
                room.updateTile(room.getLayout().getTile(this.getX(), this.getY()));
                habbo.getRoomUnit().setCanWalk(true);
            }, 4000);
        }



    }

    @Override
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOff(roomUnit, room, objects);
    }

    @Override
    public void onMove(Room room, RoomTile oldLocation, RoomTile newLocation) {
        super.onMove(room, oldLocation, newLocation);

        this.setExtradata("0");
        room.updateItemState(this);
    }

    @Override
    public void onPickUp(Room room) {
        this.setExtradata("0");
    }
}
