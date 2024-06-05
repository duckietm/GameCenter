package com.gamecenter.games.battleball.skils;

import com.eu.habbo.Emulator;
import com.gamecenter.games.battleball.BattleBallGame;
import com.gamecenter.games.battleball.BattleBallGamePlayer;
import com.gamecenter.websocket.client.WebSocketClient;

public class RandomSkill extends BattleBallSkills{
    public RandomSkill() {
        super(SkillsType.RANDOM, Emulator.getConfig().getInt("hotel.battleball.skill.randomitemid"));
    }

    @Override
    public void execute(WebSocketClient wsClient, BattleBallGamePlayer playerGame, BattleBallGame game) {
        playerGame.setSkillsType(game.getRandomSkill());
        playerGame.setSkillTime(10);
    }
}
