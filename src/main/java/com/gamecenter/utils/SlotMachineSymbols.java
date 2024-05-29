package com.gamecenter.utils;

public enum SlotMachineSymbols {
    APPLE("apple", "🍎"),
    BELL("bell", "🔔"),
    ORANGE("orange", "🍊"),
    WATERMELON("watermelon", "🍉");

    public final String symbol;
    public final String icon;

    SlotMachineSymbols(String symbol, String icon){
        this.symbol = symbol;
        this.icon = icon;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getIcon() {
        return icon;
    }
}
