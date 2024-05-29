package com.gamecenter.games.battleball.skils;

import com.eu.habbo.Emulator;
import com.gamecenter.games.battleball.BattleBallGame;
import com.gamecenter.games.battleball.BattleBallGamePlayer;
import com.gamecenter.websocket.client.WebSocketClient;

public class SprinSkill extends BattleBallSkills{

    public static String SKILL_SPRIN_ATTRIBUTE = "SKILL_SPRIN_ATTRIBUTE";
    public SprinSkill() {
        super(SkillsType.SPRIN, 103995);
    }
    @Override
    public void execute(WebSocketClient wsClient, BattleBallGamePlayer playerGame, BattleBallGame game) {
        playerGame.getHabbo().getHabboStats().cache.put(SKILL_SPRIN_ATTRIBUTE, true);
        playerGame.setSkillsType(null);

        Emulator.getThreading().run(new Runnable() {
            @Override
            public void run() {
                playerGame.getHabbo().getHabboStats().cache.remove(SKILL_SPRIN_ATTRIBUTE);
            }
        }, 5000);
    }
}
