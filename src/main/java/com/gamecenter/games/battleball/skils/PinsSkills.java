package com.gamecenter.games.battleball.skils;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.items.AddFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import com.gamecenter.games.battleball.BattleBallGame;
import com.gamecenter.games.battleball.BattleBallGamePlayer;
import com.gamecenter.interactions.battleball.InteractionBattleBallTile;
import com.gamecenter.websocket.client.WebSocketClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PinsSkills extends BattleBallSkills{

    public int ITEMSID_PINS = 104010;
    public PinsSkills() {
        super(SkillsType.PINS, 103996);
    }

    @Override
    public void execute(WebSocketClient wsClient, BattleBallGamePlayer playerGame, BattleBallGame game) {
        Habbo habbo = playerGame.getHabbo();
        List<RoomTile> aroundTiles = game.getRoom().getLayout().getTilesAround(habbo.getRoomUnit().getCurrentLocation(), habbo.getRoomUnit().getBodyRotation().getValue(), true);
        playerGame.setSkillsType(null);

        if (aroundTiles.size() != 0){
            List<HabboItem> pins = new ArrayList<>();

            for (RoomTile tile: aroundTiles) {
                InteractionBattleBallTile item = (InteractionBattleBallTile) game.getRoom().getItemsAt(tile).stream().filter(x -> x.getBaseItem().getInteractionType().getType().equals(InteractionBattleBallTile.class)).findAny().orElse(null);

                if(item != null){
                    if(new Random().nextBoolean()){
                        Item itemPins = Emulator.getGameEnvironment().getItemManager().getItem(ITEMSID_PINS);
                        HabboItem newItem = Emulator.getGameEnvironment().getItemManager().createItem(12, itemPins, 0, 0, "");

                        HabboItem itemTile = game.getRoom().getTopItemAt(tile.x, tile.y);

                        newItem.setX(tile.x);
                        newItem.setY(tile.y);
                        newItem.setZ(itemTile.getZ() + 0.1);
                        newItem.setRoomId(game.getRoom().getId());
                        newItem.needsUpdate(true);

                        game.getRoom().addHabboItem(newItem);

                        game.getRoom().updateItem(newItem);
                        game.getRoom().updateTile(game.getRoom().getLayout().getTile(tile.x, tile.y));
                        game.getRoom().sendComposer(new AddFloorItemComposer(newItem, "BattleBall").compose());

                        pins.add(newItem);
                    }
                }
            }

            if(pins.size() != 0){
                Emulator.getThreading().run(new Runnable() {
                    @Override
                    public void run() {
                        for (HabboItem itemPin : pins) {
                            game.getRoom().removeHabboItem(itemPin);
                            new QueryDeleteHabboItem(itemPin.getId()).run();
                            game.getRoom().sendComposer(new RemoveFloorItemComposer(itemPin).compose());
                            game.getRoom().updateTile(game.getRoom().getLayout().getTile(itemPin.getX(), itemPin.getX()));
                        }
                    }
                }, 6000);
            }
        }


    }

}
