package com.gamecenter.websocket.incoming.common.battleball;

import com.gamecenter.games.battleball.BattleBallGame;
import com.gamecenter.games.battleball.BattleBallGamePlayer;
import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.incoming.IncomingWebMessage;
import com.gamecenter.websocket.outgoing.common.games.battleball.PowerInfoComposer;

public class SkillEvent extends IncomingWebMessage<Object> {

    public SkillEvent() {
        super(Object.class);
    }

    @Override
    public void handle(WebSocketClient client, Object message) throws InterruptedException {
        if(client.getHabbo() != null){
            BattleBallGame game = (BattleBallGame) client.getHabbo().getHabboInfo().getCurrentRoom().getGame(BattleBallGame.class);

            if(game != null){
                BattleBallGamePlayer player = (BattleBallGamePlayer) client.getHabbo().getHabboInfo().getGamePlayer();
                if(player != null && player.skillsType != null && player.skillTime != 0){
                    BattleBallGame.skills.get(player.skillsType).execute(client, player, game);
                    client.sendMessage(new PowerInfoComposer(player));
                }
            }
        }
    }
}
