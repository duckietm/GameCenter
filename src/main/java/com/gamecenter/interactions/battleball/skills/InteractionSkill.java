package com.gamecenter.interactions.battleball.skills;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import com.gamecenter.games.battleball.BattleBallGamePlayer;
import com.gamecenter.games.battleball.skils.SkillsType;
import com.gamecenter.websocket.WebSocketManager;
import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.outgoing.common.games.battleball.PowerInfoComposer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionSkill extends InteractionDefault {
    public SkillsType skillType;

    public InteractionSkill(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.setExtradata("0");
    }

    public InteractionSkill(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
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
            BattleBallGamePlayer player = (BattleBallGamePlayer) habbo.getHabboInfo().getGamePlayer();

            if(player != null && player.skillsType == null){

                player.setSkillsType(skillType);
                player.setSkillTime(10);

                this.setExtradata("1");
                room.updateItemState(this);

                room.removeHabboItem(this);
                HabboItem item = this;
                Emulator.getThreading().run(() -> {
                    new QueryDeleteHabboItem(item.getId()).run();
                    room.sendComposer(new RemoveFloorItemComposer(item).compose());
                    room.updateTile(room.getLayout().getTile(this.getX(), this.getY()));
                }, 500);

                WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(habbo.getHabboInfo().getId());

                if(wsClient != null){
                    wsClient.sendMessage(new PowerInfoComposer(player));
                }
            }
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
