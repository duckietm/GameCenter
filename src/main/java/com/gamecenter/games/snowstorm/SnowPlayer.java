package com.gamecenter.games.snowstorm;

import com.eu.habbo.habbohotel.users.Habbo;

public class SnowPlayer {
    public Habbo habbo;
    public int actualBalls;
    public int actualHp;
    public boolean isDeath;
    public SnowTeam team;
    public String figure;

    public SnowPlayer(Habbo habbo, SnowTeam team){
        this.habbo = habbo;
        this.team = team;
        this.actualBalls = 6;
        this.actualHp = 5;
        this.isDeath = false;
        this.figure = habbo.getHabboInfo().getLook();
    }
}
