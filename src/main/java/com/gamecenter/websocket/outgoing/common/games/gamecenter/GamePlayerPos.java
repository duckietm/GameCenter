package com.gamecenter.websocket.outgoing.common.games.gamecenter;

public class GamePlayerPos {
    public int ranking;
    public String username;
    public String figure;
    public int score;

    public GamePlayerPos(int ranking, String username, String figure, int score){
        this.ranking = ranking;
        this.username = username;
        this.figure = figure;
        this.score = score;
    }
}
