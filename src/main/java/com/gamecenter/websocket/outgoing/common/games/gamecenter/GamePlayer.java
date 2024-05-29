package com.gamecenter.websocket.outgoing.common.games.gamecenter;

public class GamePlayer {
    public String username;
    public String figure;
    public int score;

    public GamePlayer(String username, String figure, int score){
        this.username = username;
        this.figure = figure;
        this.score = score;
    }
}
