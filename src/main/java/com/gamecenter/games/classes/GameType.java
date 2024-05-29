package com.gamecenter.games.classes;

public enum GameType {
    DELIVERY_FOOD("delivery_food"),
    PICK_CREDITS("pick_credits");
  
    private final String type;
  
    GameType(String type) {
        this.type = type;
    }
  
    public String getType() {
        return this.type;
    }
}
