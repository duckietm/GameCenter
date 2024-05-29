package com.gamecenter.games.battleball;

import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.users.Habbo;
import com.gamecenter.games.battleball.skils.SkillsType;
import com.gamecenter.websocket.client.WebSocketClient;

public class BattleBallGamePlayer extends GamePlayer {
    public SkillsType skillsType = null;
    public int skillTime = 0;
    public BattleBallGamePlayer(Habbo habbo, GameTeamColors teamColor) {
        super(habbo, teamColor);
    }

    public void setSkillsType(SkillsType type){
        this.skillsType = type;
    }
    public void setSkillTime(int seg){
        this.skillTime = seg + 3;
    }

    public void updateSkillTime(){
        this.skillTime -= 1;

        if(this.skillTime == 0){
            setSkillsType(null);
        }
    }
}

