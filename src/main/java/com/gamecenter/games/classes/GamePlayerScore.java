package com.gamecenter.games.classes;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;

public class GamePlayerScore {
    public GameClient player;
    public int score;
    
    public GamePlayerScore(GameClient player) {
        this.player = player;
        this.score = 0;
    }
}

