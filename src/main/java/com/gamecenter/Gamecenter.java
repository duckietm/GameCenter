package com.gamecenter;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.PacketManager;
import com.eu.habbo.messages.incoming.Incoming;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.incoming.users.RequestWearingBadgesEvent;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.HabboPlugin;
import com.gamecenter.events.EmulatorEvents;
import com.gamecenter.events.UserEvents;
import com.gamecenter.websocket.WebSocketManager;
import gnu.trove.map.hash.THashMap;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class Gamecenter extends HabboPlugin implements EventListener {
    public static Gamecenter PLUGIN = null;

    @Override
    public void onEnable() throws Exception {
        PLUGIN = this;

        Emulator.getPluginManager().registerEvents(this, new EmulatorEvents());
        Emulator.getPluginManager().registerEvents(this, new UserEvents());

        Logger("Plugin loaded!");

    }

    @Override
    public void onDisable() throws Exception {

        // PacketManager
        Field f = PacketManager.class.getDeclaredField("incoming");
        f.setAccessible(true);

        THashMap<Integer, Class<? extends MessageHandler>> incoming = (THashMap<Integer, Class<? extends MessageHandler>>)f.get(Emulator.getGameServer().getPacketManager());

        // Remove Packets
        incoming.remove(Incoming.RequestWearingBadgesEvent);

        // Add Packets
        Emulator.getGameServer().getPacketManager().registerHandler(Incoming.RequestWearingBadgesEvent, RequestWearingBadgesEvent.class);

        WebSocketManager.getInstance().stop();
        WebSocketManager.getInstance().Dispose();

        Logger("Plugin disable!");
    }

    @Override
    public boolean hasPermission(Habbo habbo, String s) {
        return false;
    }

    public static void Logger(String message) {
        LoggerFactory.getLogger("GameCenter").info(message);
    }
}
