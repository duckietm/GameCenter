package com.gamecenter.websocket.incoming.common.misc;

import ch.qos.logback.core.util.FileUtil;
import com.eu.habbo.Emulator;
import com.eu.habbo.plugin.HabboPlugin;
import com.eu.habbo.plugin.HabboPluginConfiguration;
import com.eu.habbo.threading.runnables.ShutdownEmulator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gamecenter.websocket.WebSocketManager;
import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.incoming.IncomingWebMessage;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class SSOTicketEvent extends IncomingWebMessage<SSOTicketEvent.JSONSSOTicketEvent> {
    public SSOTicketEvent() {
        super(JSONSSOTicketEvent.class);
    }

    @Override
    public void handle(WebSocketClient client, JSONSSOTicketEvent message) {

        if (!client.tryAuthenticate(message.ticket)) {
            client.dispose();
        }
    }
    static class JSONSSOTicketEvent {
        String ticket;
    }
}
