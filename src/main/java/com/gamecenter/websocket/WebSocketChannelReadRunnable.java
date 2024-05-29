package com.gamecenter.websocket;

import com.gamecenter.Gamecenter;
import com.gamecenter.utils.JsonFactory;
import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.client.WebSocketClientManager;
import com.gamecenter.websocket.incoming.IncomingWebMessage;
import io.netty.channel.ChannelHandlerContext;

public class WebSocketChannelReadRunnable {
    private final ChannelHandlerContext ctx;
    private final String msg;

    public WebSocketChannelReadRunnable(ChannelHandlerContext ctx, String msg) {
        this.ctx = ctx;
        this.msg = msg;
    }

    public void run() {
        WebSocketClient client = this.ctx.channel().attr(WebSocketClientManager.CLIENT).get();
        if (client != null) {
            try {
                IncomingWebMessage.JSONIncomingEvent heading = JsonFactory.getInstance().fromJson(msg,  IncomingWebMessage.JSONIncomingEvent.class);
                Class<? extends IncomingWebMessage> message = WebSocketManager.getInstance().getIncomingMessages().get(heading.header);


                if (message == null)return;

                IncomingWebMessage webEvent = message.getDeclaredConstructor().newInstance();

                if (client.isAuthenticated() || webEvent.type.getName().contains("SSOTicketEvent") || webEvent.toString().contains("SSOTicketEvent"))
                    webEvent.handle(client, JsonFactory.getInstance().fromJson(heading.data.toString(), webEvent.type));
            } catch(Exception ex) {
                Gamecenter.Logger("WebSocketChannelReadRunnable \n" + ex.getMessage());
            }
        }
    }
}
