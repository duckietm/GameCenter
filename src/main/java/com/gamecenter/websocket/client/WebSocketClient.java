package com.gamecenter.websocket.client;

import com.eu.habbo.habbohotel.users.Habbo;
import com.gamecenter.Gamecenter;
import com.gamecenter.utils.JsonFactory;
import com.gamecenter.utils.UtilityMethods;
import com.gamecenter.websocket.outgoing.OutgoingWebMessage;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebSocketClient {
    private final Channel channel;
    private Habbo habbo;
    private boolean isAuthenticated;

    public WebSocketClient(Channel channel) {
        this.channel = channel;
        this.isAuthenticated = false;
    }

    public void sendMessage(OutgoingWebMessage message) {
        if (this.channel.isOpen()) {
            if (message == null)
                return;

            this.channel.write(new TextWebSocketFrame(JsonFactory.getInstance().toJson(message)), this.channel.voidPromise());
            this.channel.flush();
        }
    }

    public boolean tryAuthenticate(String authTicket) {
        Habbo user = UtilityMethods.getUserWithSSO(authTicket);
        if (user == null)
            return false;

        this.habbo = user;
        this.isAuthenticated = true;
        return true;
    }

    public Channel getChannel() { return this.channel; }

    public Habbo getHabbo() {
        return this.habbo;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void dispose() {
        try {
            this.channel.close();
            this.habbo = null;
        } catch (Exception ex) {
            Gamecenter.Logger("dispose client \n" + ex.getMessage());
        }
    }
}
