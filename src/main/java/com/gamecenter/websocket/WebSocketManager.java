package com.gamecenter.websocket;

import com.eu.habbo.Emulator;
import com.eu.habbo.networking.Server;
import com.gamecenter.Gamecenter;
import com.gamecenter.websocket.client.WebSocketClientManager;
import com.gamecenter.websocket.incoming.IncomingWebMessage;
import com.gamecenter.websocket.incoming.common.battleball.SkillEvent;
import com.gamecenter.websocket.incoming.common.games.gamecenter.ExitGameCenterEvent;
import com.gamecenter.websocket.incoming.common.games.gamecenter.JoinGameCenterEvent;
import com.gamecenter.websocket.incoming.common.games.gamecenter.VotationGameEvent;
import com.gamecenter.websocket.incoming.common.misc.GoToRoomEvent;
import com.gamecenter.websocket.incoming.common.misc.PingEvent;
import com.gamecenter.websocket.incoming.common.misc.SSOTicketEvent;
import com.gamecenter.websocket.security.SSLCertificateLoader;
import gnu.trove.map.hash.THashMap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

public class WebSocketManager extends Server {
    public static WebSocketManager instance;
    private final WebSocketClientManager clientManager;
    private final THashMap<String, Class<? extends IncomingWebMessage>> incomingMessages;
    private final SslContext context;
    private final boolean SSL;

    private final WebSocketServerProtocolConfig config = WebSocketServerProtocolConfig.newBuilder()
            .websocketPath("/")
            .checkStartsWith(true)
            .maxFramePayloadLength(10485760)
            .build();


    public WebSocketManager() throws Exception {
        super(
                "BattlePass Ws",
                Emulator.getConfig().getValue("ws.host", "0.0.0.0"),
                Integer.parseInt(Emulator.getConfig().getValue("ws.port", "2053")),
                1,
                6
        );

        this.clientManager = new WebSocketClientManager();
        this.incomingMessages = new THashMap<>();
        context = SSLCertificateLoader.getContext(Emulator.getConfig().getValue("ws.cert.pass", ""));
        SSL = context != null;
        initializeMessages();
    }

    public static void Init() {
        try {
            instance = new WebSocketManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initializePipeline() {
        super.initializePipeline();
        this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                if (SSL)
                    ch.pipeline().addLast(context.newHandler(ch.alloc()));

                ch.pipeline().addLast(new HttpServerCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(10485760));
                ch.pipeline().addLast(new WebSocketServerProtocolHandler(config));
                ch.pipeline().addLast(new IdleStateHandler(60, 30, 0));
                ch.pipeline().addLast(new WebSocketMessageHandler());
            }
        });
    }

    public void initializeMessages() {
        this.registerMessage("sso", SSOTicketEvent.class);
        this.registerMessage("ping", PingEvent.class);
        this.registerMessage("goToRoom", GoToRoomEvent.class);

        this.registerMessage("joinGameCenter", JoinGameCenterEvent.class);
        this.registerMessage("leftGameCenter", ExitGameCenterEvent.class);
        this.registerMessage("votationGameCenter", VotationGameEvent.class);

        //BattleBall
        this.registerMessage("useSkill", SkillEvent.class);
    }

    public void registerMessage(String key, Class<? extends IncomingWebMessage> message) {
        this.incomingMessages.put(key, message);
    }

    public THashMap<String, Class<? extends IncomingWebMessage>> getIncomingMessages() {
        return this.incomingMessages;
    }

    public static WebSocketManager getInstance() {
        if (instance == null) {
            try {
                instance = new WebSocketManager();
            } catch (Exception ex) {
                Gamecenter.Logger("WebsocketInstance \n " + ex.getMessage());
            }
        }
        return instance;
    }

    public WebSocketClientManager getClientManager() {
        return this.clientManager;
    }

    public boolean isSSL() {
        return SSL;
    }

    public void Dispose() {
        clientManager.dispose();
        incomingMessages.clear();
        instance = null;
    }
}
