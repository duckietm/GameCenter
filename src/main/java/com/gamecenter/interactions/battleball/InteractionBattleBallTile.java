package com.gamecenter.interactions.battleball;

import com.eu.habbo.habbohotel.games.GameState;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.gamecenter.games.battleball.BattleBallGame;
import com.gamecenter.games.battleball.skils.DrillSkill;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.math3.util.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class InteractionBattleBallTile extends HabboItem {
    public InteractionBattleBallTile(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.setExtradata("0");
    }

    public InteractionBattleBallTile(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.setExtradata("0");
    }


    @Override
    public void serializeExtradata(ServerMessage serverMessage) {
        serverMessage.appendInt((this.isLimited() ? 256 : 0));
        serverMessage.appendString(this.getExtradata());

        super.serializeExtradata(serverMessage);
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

        if (this.getExtradata().isEmpty())
            this.setExtradata("0");

        int state = Integer.valueOf(this.getExtradata());

        Habbo habbo = room.getHabbo(roomUnit);

        if (habbo == null)
            return;

        if(habbo.getHabboInfo().getCurrentGame() != null){
            GameTeamColors checkDrillSkill = (GameTeamColors) habbo.getHabboStats().cache.get(DrillSkill.SKILL_DRILL_ATTRIBUTE);

            if (this.isLocked() && checkDrillSkill == null)
                return;

            if (habbo.getHabboInfo().getCurrentGame().equals(BattleBallGame.class)) {
                BattleBallGame game = ((BattleBallGame) room.getGame(BattleBallGame.class));

                if (game == null)
                    return;

                if (!game.state.equals(GameState.RUNNING))
                    return;

                game.markTile(habbo, this, state);
            }
        }
    }

    public boolean isLocked() {
        if (this.getExtradata().isEmpty())
            return false;

        return Integer.valueOf(this.getExtradata()) % 3 == 2;
    }

    @Override
    public boolean canStackAt(Room room, List<Pair<RoomTile, THashSet<HabboItem>>> itemsAtLocation) {
        /*for (Pair<RoomTile, THashSet<HabboItem>> set : itemsAtLocation) {
            if (set.getValue() != null && !set.getValue().isEmpty()) return false;
        }*/

        return super.canStackAt(room, itemsAtLocation);
    }

    @Override
    public void onPickUp(Room room) {
        super.onPickUp(room);

        this.setExtradata("0");
        room.updateItem(this);
    }

    @Override
    public void onPlace(Room room) {
        super.onPlace(room);

        BattleBallGame game = (BattleBallGame) room.getGame(BattleBallGame.class);

        if (game != null && game.getState() != GameState.IDLE) {
            this.setExtradata("1");
        }
    }
}
