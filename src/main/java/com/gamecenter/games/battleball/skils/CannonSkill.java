package com.gamecenter.games.battleball.skils;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.gamecenter.games.battleball.BattleBallGame;
import com.gamecenter.games.battleball.BattleBallGamePlayer;
import com.gamecenter.interactions.battleball.InteractionBattleBallTile;
import com.gamecenter.websocket.client.WebSocketClient;

import java.util.List;

public class CannonSkill extends BattleBallSkills{
    public CannonSkill() {
        super(SkillsType.CANNON, 103993);
    }

    @Override
    public void execute(WebSocketClient wsClient, BattleBallGamePlayer playerGame, BattleBallGame game) {
        int distance = 4;
        List<RoomTile> DistanceTiles = game.getRoom().getLayout().getTilesInFront(playerGame.getHabbo().getRoomUnit().getCurrentLocation(), playerGame.getHabbo().getRoomUnit().getBodyRotation().getValue(), distance);
        playerGame.setSkillsType(null);

        if (DistanceTiles.size() != 0){
            boolean success = false;

            for (RoomTile tile: DistanceTiles) {
                if(success) return;

                if(tile.hasUnits()){
                    for (RoomUnit unit : tile.getUnits()){
                        Habbo h = game.getRoom().getHabbo(unit);

                        if(h != null){
                            game.stunPlayer(h);
                            success = true;
                        }
                    }
                }
            }
        }


    }
}
