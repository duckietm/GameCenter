package com.gamecenter.games.snowstorm;

import java.util.ArrayList;

public class SnowTeam {
    public ArrayList<SnowPlayer> players = new ArrayList<>();
    public String reference;
    public int points;

    public SnowTeam(String reference){
        this.reference = reference;
        this.points = 0;
    }
}
