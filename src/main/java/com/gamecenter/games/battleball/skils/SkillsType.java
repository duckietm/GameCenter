package com.gamecenter.games.battleball.skils;

public enum SkillsType {
    BOOM("boom"),
    CANNON("cannon"),
    BULB("bulb"),
    DRILL("drill"),
    FLASH("flash"),
    PINS("pins"),
    HARLE("harle"),
    SPRIN("sprin"),
    RANDOM("random");

    public final String type;
    SkillsType(String type){
        this.type = type;
    }
}
