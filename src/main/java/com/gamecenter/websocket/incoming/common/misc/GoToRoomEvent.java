package com.gamecenter.websocket.incoming.common.misc;

import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.incoming.IncomingWebMessage;

public class GoToRoomEvent extends IncomingWebMessage<GoToRoomEvent.GoToRoomEventJSON>{

    public GoToRoomEvent(){
        super(GoToRoomEventJSON.class);
    }

    @Override
    public void handle(WebSocketClient client, GoToRoomEventJSON message) throws InterruptedException {
        if(client.getHabbo() != null){
            client.getHabbo().goToRoom(message.roomId);
        }
    }

    static class GoToRoomEventJSON {
        int roomId;
    }
}
