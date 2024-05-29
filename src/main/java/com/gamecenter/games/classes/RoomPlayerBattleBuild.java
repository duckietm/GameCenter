package com.gamecenter.games.classes;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;

public class RoomPlayerBattleBuild {
    public GameClient player;
    public Room room;


    public RoomPlayerBattleBuild(GameClient player, Room room){
        this.player = player;
        this.room = room;
    }

}
