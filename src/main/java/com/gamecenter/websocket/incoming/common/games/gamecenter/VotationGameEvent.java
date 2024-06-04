package com.gamecenter.websocket.incoming.common.games.gamecenter;

import com.gamecenter.games.Globals;
import com.gamecenter.games.battlebuild.BattleBuildGame;
import com.gamecenter.games.classes.GamePlayerScore;
import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.incoming.IncomingWebMessage;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class VotationGameEvent extends IncomingWebMessage<VotationGameEvent.JSONJoinGameEvent> {
    private static final Set<Integer> voters = ConcurrentHashMap.newKeySet();
    private static final Set<Integer> voteLocks = ConcurrentHashMap.newKeySet();

    public VotationGameEvent() {
        super(JSONJoinGameEvent.class);
    }

    public static void resetVoters() {
        synchronized (voters) {
            voters.clear();
        }
    }

    @Override
    public void handle(WebSocketClient client, JSONJoinGameEvent message) throws InterruptedException {
        if (client.getHabbo() != null) {
            int clientId = client.getHabbo().getHabboInfo().getId();

            if (!voteLocks.add(clientId)) {
                return;
            }

            for (BattleBuildGame game : Globals.gameBattleBuild) {
                boolean userFound = game.players.stream()
                        .anyMatch(x -> x.getHabbo().getHabboInfo().getId() == clientId);

                if (userFound) {
                    if (clientId == message.participantId) {
                        voteLocks.remove(clientId); // Remove the lock since the vote was not valid
                        return;
                    }

                    synchronized (voters) { // Synchronize access to the voters set
                        if (voters.contains(clientId)) {
                            voteLocks.remove(clientId); // Remove the lock since the vote was not valid
                            return;
                        }

                        GamePlayerScore score = game.gamePlayerScores.stream()
                                .filter(x -> x.player.getHabbo().getHabboInfo().getId() == message.participantId)
                                .findAny().orElse(null);
                        if (score != null) {
                            score.score += message.votationNumber;
                            game.broadcastScores();
                            voters.add(clientId); // Mark user as having voted
                        }
                    }
                }
            }
            voteLocks.remove(clientId);
        }
    }

    static class JSONJoinGameEvent {
        int participantId;
        int votationNumber;
    }
}
