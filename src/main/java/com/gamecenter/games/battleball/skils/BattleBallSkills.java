package com.gamecenter.games.battleball.skils;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.gamecenter.games.battleball.BattleBallGame;
import com.gamecenter.games.battleball.BattleBallGamePlayer;
import com.gamecenter.websocket.client.WebSocketClient;

public abstract class BattleBallSkills {
    public final SkillsType type;
    public final int itemId;

    public BattleBallSkills(SkillsType type, int itemId){
        this.type = type;
        this.itemId = itemId;
    }
    public abstract void execute(WebSocketClient wsClient, BattleBallGamePlayer playerGame, BattleBallGame game);
}
