package com.gamecenter.websocket.security;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.File;

public class SSLCertificateLoader {
    private static final String filePath = "ssl";

    public static SslContext getContext(String password) {
        SslContext context;
        try {
            context = SslContextBuilder.forServer(new File( filePath + File.separator + "cert.pem" ), new File( filePath + File.separator + "privkey.pem" )).build();
        } catch ( Exception e ) {
            context = null;
        }
        return context;
    }
}
