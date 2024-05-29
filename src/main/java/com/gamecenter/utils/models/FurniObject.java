package com.gamecenter.utils.models;

import com.eu.habbo.habbohotel.items.interactions.InteractionWired;
import com.eu.habbo.habbohotel.users.HabboItem;

public class FurniObject {
    public final int id;

    public final String name;

    public final String wallPosition;

    public final int x;

    public final int y;

    public final double z;

    public final String extradata;

    public final int rotation;

    public final boolean isWired;

    public String wiredData;

    public final String limitedData;

    public final String customData;

    public FurniObject(HabboItem item) {
        this.id = item.getId();
        this.name = item.getBaseItem().getName();
        this.wallPosition = item.getWallPosition();
        this.x = item.getX();
        this.y = item.getY();
        this.z = item.getZ();
        this.extradata = item.getExtradata();
        this.rotation = item.getRotation();
        this.isWired = item instanceof InteractionWired;
        this.customData = "";
        this.limitedData = item.isLimited() ? (item.getLimitedStack() + ":" + item.getLimitedSells()) : "";
        this.wiredData = (item instanceof InteractionWired) ? ((InteractionWired)item).getWiredData() : "";
    }
}