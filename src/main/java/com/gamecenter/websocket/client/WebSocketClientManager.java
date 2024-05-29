package com.gamecenter.websocket.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.util.AttributeKey;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WebSocketClientManager {
    public static final AttributeKey<WebSocketClient> CLIENT = AttributeKey.valueOf("WebSocketClientBattlePass");
    private final ConcurrentMap<ChannelId, WebSocketClient> clients;

    public WebSocketClientManager() {
        this.clients = new ConcurrentHashMap<>();
    }

    public boolean addClient(ChannelHandlerContext ctx) {
        WebSocketClient client = new WebSocketClient(ctx.channel());
        ctx.channel().closeFuture().addListener((ChannelFutureListener) channelFuture ->
                this.disposeClient(ctx.channel())
        );

        ctx.channel().attr(CLIENT).set(client);
        ctx.fireChannelRegistered();

        return this.clients.putIfAbsent(ctx.channel().id(), client) == null;
    }

    public WebSocketClient getWebSocketClientForHabbo(int id) {
        for(WebSocketClient client : this.clients.values()) {
            if (client.getHabbo() == null)
                continue;
            if (client.getHabbo().getHabboInfo().getId() == id)
                return client;
        }
        return null;
    }

    public void disposeClient(WebSocketClient client) {
        this.disposeClient(client.getChannel());
    }

    private void disposeClient(Channel channel) {
        WebSocketClient client = channel.attr(CLIENT).get();

        if (client != null)
            client.dispose();

        channel.deregister();
        channel.attr(CLIENT).set(null);
        channel.closeFuture();
        channel.close();
        this.clients.remove(channel.id());
    }

    public void dispose() {
        clients.forEach((k,v) -> {
            v.dispose();
            v.getChannel().deregister();
            v.getChannel().attr(CLIENT).set(null);
            v.getChannel().closeFuture();
            v.getChannel().close();
        });

        clients.clear();
    }
}
