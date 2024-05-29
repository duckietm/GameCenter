package com.gamecenter.games.classes;


public class GameDelivery {
    public RoomPosition chair;
  
    public RoomPosition table;
    
    public GameDelivery(RoomPosition chair, RoomPosition table) {
        this.chair = chair;
        this.table = table;
    }
}