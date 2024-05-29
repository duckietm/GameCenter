package com.gamecenter.games.battleball;

import com.eu.habbo.habbohotel.games.Game;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.games.GameTeam;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.games.battlebanzai.BattleBanzaiGame;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameGate;
import com.eu.habbo.habbohotel.rooms.Room;

public class BattleBallGameTeam extends GameTeam {
    public BattleBallGameTeam(GameTeamColors teamColor) {
        super(teamColor);
    }

    @Override
    public void addMember(GamePlayer gamePlayer) {
        super.addMember(gamePlayer);
        int effect = 0;

        switch (this.teamColor){
            case RED:
                effect = 87;
                break;
            case BLUE:
                effect = 88;
                break;
            case YELLOW:
                effect = 89;
                break;
            case GREEN:
                effect = 86;
                break;
        }

        gamePlayer.getHabbo().getHabboInfo().getCurrentRoom().giveEffect(gamePlayer.getHabbo(), effect, -1);
    }

    @Override
    public void removeMember(GamePlayer gamePlayer) {
        Game game = gamePlayer.getHabbo().getHabboInfo().getCurrentRoom().getGame(gamePlayer.getHabbo().getHabboInfo().getCurrentGame());
        Room room = gamePlayer.getHabbo().getRoomUnit().getRoom();

        gamePlayer.getHabbo().getHabboInfo().getCurrentRoom().giveEffect(gamePlayer.getHabbo(), 0, -1);
        gamePlayer.getHabbo().getRoomUnit().setCanWalk(true);

        super.removeMember(gamePlayer);

    }
}
