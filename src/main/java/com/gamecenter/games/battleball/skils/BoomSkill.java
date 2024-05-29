package com.gamecenter.games.battleball.skils;

import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.gamecenter.games.battleball.BattleBallGame;
import com.gamecenter.games.battleball.BattleBallGamePlayer;
import com.gamecenter.interactions.battleball.InteractionBattleBallTile;
import com.gamecenter.websocket.client.WebSocketClient;
import gnu.trove.set.hash.THashSet;

import java.util.List;

public class BoomSkill extends BattleBallSkills{
    public BoomSkill() {
        super(SkillsType.BOOM, 103990);
    }

    @Override
    public void execute(WebSocketClient wsClient, BattleBallGamePlayer playerGame, BattleBallGame game) {
        Habbo habbo = playerGame.getHabbo();
        List<RoomTile> aroundTiles = game.getRoom().getLayout().getTilesAround(habbo.getRoomUnit().getCurrentLocation(), habbo.getRoomUnit().getBodyRotation().getValue(), true);

        if (aroundTiles.size() != 0){
            for (RoomTile tile: aroundTiles) {
                InteractionBattleBallTile item = (InteractionBattleBallTile) game.getRoom().getItemsAt(tile).stream().filter(x -> x.getBaseItem().getInteractionType().getType().equals(InteractionBattleBallTile.class)).findAny().orElse(null);

                if(item != null){

                    for (GameTeam t : game.getTeams().values()){
                        for (THashSet<HabboItem> i : game.getLockedTiles().values()){
                            i.stream().filter(x -> x.getX() == tile.x && x.getY() == tile.y).findAny().ifPresent(i::remove);
                        }
                    }

                    item.setExtradata("1");
                    game.getRoom().updateItem(item);
                }
            }
        }

        HabboItem itemCurrentLocation = game.getRoom().getItemsAt(habbo.getRoomUnit().getCurrentLocation()).stream().filter(x -> x.getBaseItem().getInteractionType().getType().equals(InteractionBattleBallTile.class)).findAny().orElse(null);

        if(itemCurrentLocation != null){
            for (GameTeam t : game.getTeams().values()){
                for (THashSet<HabboItem> i : game.getLockedTiles().values()){
                    i.stream().filter(x -> x.getX() == habbo.getRoomUnit().getCurrentLocation().x && x.getY() == habbo.getRoomUnit().getCurrentLocation().y).findAny().ifPresent(i::remove);
                }
            }

            itemCurrentLocation.setExtradata("1");
            game.getRoom().updateItem(itemCurrentLocation);
        }



        playerGame.setSkillsType(null);
    }
}
