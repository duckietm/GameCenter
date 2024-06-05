package com.gamecenter.games.battleball.skils;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.games.GameTeam;
import com.gamecenter.games.battleball.BattleBallGame;
import com.gamecenter.games.battleball.BattleBallGamePlayer;
import com.gamecenter.websocket.client.WebSocketClient;

public class HarleSkill extends BattleBallSkills{

    public static String SKILL_HARLE_ATTRIBUTE = "SKILL_HARLE_ATTRIBUTE";
    public HarleSkill() {
        super(SkillsType.HARLE, Emulator.getConfig().getInt("hotel.battleball.skill.harleitemid"));
    }
    @Override
    public void execute(WebSocketClient wsClient, BattleBallGamePlayer playerGame, BattleBallGame game) {
        for (GameTeam t : game.getTeams().values()){

            if (t.teamColor == playerGame.getTeamColor()) continue;

            for (GamePlayer p : t.getMembers()){
                BattleBallGamePlayer bPlayer = (BattleBallGamePlayer) p;

                bPlayer.getHabbo().getHabboStats().cache.put(SKILL_HARLE_ATTRIBUTE, playerGame);
            }
        }

        playerGame.setSkillsType(null);

        Emulator.getThreading().run(new Runnable() {
            @Override
            public void run() {
                for (GameTeam t : game.getTeams().values()){

                    if (t.teamColor == playerGame.getTeamColor()) continue;

                    for (GamePlayer p : t.getMembers()){
                        BattleBallGamePlayer bPlayer = (BattleBallGamePlayer) p;

                        bPlayer.getHabbo().getHabboStats().cache.remove(SKILL_HARLE_ATTRIBUTE);
                    }
                }
            }
        }, 5000);
    }
}
