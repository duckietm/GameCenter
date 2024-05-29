package com.gamecenter.games.classes;

import com.eu.habbo.habbohotel.gameclients.GameClient;

public class GameUserDelivery {
    public GameClient player;
    public GameDelivery delivery;
    
    public GameUserDelivery(GameClient player, GameDelivery delivery){
        this.player = player;
        this.delivery = delivery;
    }
}
