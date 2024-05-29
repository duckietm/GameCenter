package com.gamecenter.games.battleball.skils;

import com.eu.habbo.Emulator;
import com.gamecenter.games.battleball.BattleBallGame;
import com.gamecenter.games.battleball.BattleBallGamePlayer;
import com.gamecenter.websocket.client.WebSocketClient;

public class DrillSkill extends BattleBallSkills{
    public DrillSkill() {
        super(SkillsType.DRILL, 103987);
    }

    public static String SKILL_DRILL_ATTRIBUTE = "SKILL_DRILL_ATTRIBUTE";

    @Override
    public void execute(WebSocketClient wsClient, BattleBallGamePlayer playerGame, BattleBallGame game) {
        playerGame.getHabbo().getHabboStats().cache.put(SKILL_DRILL_ATTRIBUTE, playerGame.getTeamColor());
        playerGame.setSkillsType(null);

        Emulator.getThreading().run(new Runnable() {
            @Override
            public void run() {
                playerGame.getHabbo().getHabboStats().cache.remove(SKILL_DRILL_ATTRIBUTE);
            }
        }, 5000);
    }
}
