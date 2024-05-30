package com.gamecenter.events;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.ItemInteraction;
import com.eu.habbo.messages.PacketManager;
import com.eu.habbo.messages.incoming.Incoming;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.incoming.users.RequestWearingBadgesEvent;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadItemsManagerEvent;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadedEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gamecenter.Gamecenter;
import com.gamecenter.interactions.*;
import com.gamecenter.interactions.battleball.InteractionBattleBallTile;
import com.gamecenter.interactions.battleball.skills.*;
import com.gamecenter.packets.incoming.*;
import com.gamecenter.websocket.WebSocketManager;
import gnu.trove.map.hash.THashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;

public class EmulatorEvents implements EventListener {

    @EventHandler
    public static void onEmulatorLoaded(EmulatorLoadedEvent event) throws Exception  {

        // PacketManager
        Field f = PacketManager.class.getDeclaredField("incoming");
        f.setAccessible(true);

        THashMap<Integer, Class<? extends MessageHandler>> incoming = (THashMap<Integer, Class<? extends MessageHandler>>)f.get(Emulator.getGameServer().getPacketManager());

        // Remove Packets
        incoming.remove(Incoming.RequestWearingBadgesEvent);


        // Add Packets
        Emulator.getGameServer().getPacketManager().registerHandler(Incoming.RequestWearingBadgesEvent, RequestWearingBadgesEvent2.class);


        WebSocketManager.Init();
        WebSocketManager.getInstance().initializePipeline();
        WebSocketManager.getInstance().connect();

        Gamecenter.Logger("GameCenter Server on: " + (WebSocketManager.getInstance().isSSL() ? "wss" : "ws") + "://" + WebSocketManager.getInstance().getHost() + ":" + WebSocketManager.getInstance().getPort());
    }
    @EventHandler
    public static void onLoadItemsManager(EmulatorLoadItemsManagerEvent e) throws IllegalAccessException, NoSuchFieldException {
        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("game_delivery", InteractionGameDelivery.class));
        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("table_delivery", InteractionGameTable.class));

        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("snowst_machine", InteractionSnowMachine.class));

        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("battleball_tile", InteractionBattleBallTile.class));
        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("battleball_pins", InteractionPins.class));
        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("battleball_skill_boom", InteractionSkillBoom.class));
        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("battleball_skill_random", InteractionSkillRandom.class));
        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("battleball_skill_bulb", InteractionSkillBulb.class));
        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("battleball_skill_cannon", InteractionSkillCannon.class));
        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("battleball_skill_pins", InteractionSkillPins.class));
        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("battleball_skill_sprin", InteractionSkillSprin.class));
        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("battleball_skill_harle", InteractionSkillHarle.class));
        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("battleball_skill_flash", InteractionSkillFlash.class));
        Emulator.getGameEnvironment().getItemManager().addItemInteraction(new ItemInteraction("battleball_skill_drill", InteractionSkillDrill.class));

    }
}
