package com.gamecenter.games.battleball.skils;

import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.gamecenter.games.battleball.BattleBallGame;
import com.gamecenter.games.battleball.BattleBallGamePlayer;
import com.gamecenter.interactions.battleball.InteractionBattleBallTile;
import com.gamecenter.websocket.client.WebSocketClient;
import gnu.trove.set.hash.THashSet;

import java.util.List;

public class FlashSkill extends BattleBallSkills{
    public FlashSkill() {
        super(SkillsType.FLASH, 103991);
    }

    @Override
    public void execute(WebSocketClient wsClient, BattleBallGamePlayer playerGame, BattleBallGame game) {
        int rango = 3;
        List<RoomTile> flashTiles = game.getRoom().getLayout().getTilesInFront(playerGame.getHabbo().getRoomUnit().getCurrentLocation(), playerGame.getHabbo().getRoomUnit().getBodyRotation().getValue(), rango);
        playerGame.setSkillsType(null);

        if (flashTiles.size() != 0){
            for (RoomTile tile: flashTiles) {
                InteractionBattleBallTile item = (InteractionBattleBallTile) game.getRoom().getItemsAt(tile).stream().filter(x -> x.getBaseItem().getInteractionType().getType().equals(InteractionBattleBallTile.class)).findAny().orElse(null);

                if(item != null){

                    for (GameTeam t : game.getTeams().values()){
                        for (THashSet<HabboItem> i : game.getLockedTiles().values()){
                            i.stream().filter(x -> x.getX() == tile.x && x.getY() == tile.y).findAny().ifPresent(i::remove);
                        }
                    }

                    playerGame.getHabbo().getHabboInfo().getGamePlayer().addScore(BattleBallGame.POINTS_LOCK_TILE);
                    game.tileLocked(playerGame.getHabbo().getHabboInfo().getGamePlayer().getTeamColor(), item, playerGame.getHabbo());
                    item.setExtradata(String.valueOf(2 + (playerGame.getTeamColor().type * 3)));
                    game.getRoom().updateItem(item);
                }
            }
        }


    }
}
