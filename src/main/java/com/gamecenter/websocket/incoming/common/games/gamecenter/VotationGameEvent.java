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

    @Override
    public void handle(WebSocketClient client, JSONJoinGameEvent message) throws InterruptedException {
        if (client.getHabbo() != null) {
            int clientId = client.getHabbo().getHabboInfo().getId();
            System.out.println("VotationGameEvent triggered for participantId: " + message.participantId);

            // Debounce: Check and set the vote lock to prevent rapid duplicate votes
            if (!voteLocks.add(clientId)) {
                System.out.println("Duplicate vote attempt ignored for clientId: " + clientId);
                return;
            }

            for (BattleBuildGame game : Globals.gameBattleBuild) {
                game.players.forEach(player -> System.out.println("Player ID: " + player.getHabbo().getHabboInfo().getId()));
                game.gamePlayerScores.forEach(score -> System.out.println("Score Participant ID: " + score.player.getHabbo().getHabboInfo().getId()));

                boolean userFound = game.players.stream()
                        .anyMatch(x -> x.getHabbo().getHabboInfo().getId() == clientId);

                if (userFound) {
                    // Prevent users from voting for themselves
                    if (clientId == message.participantId) {
                        System.out.println("User cannot vote for themselves: " + message.participantId);
                        voteLocks.remove(clientId); // Remove the lock since the vote was not valid
                        return;
                    }

                    // Check if the user has already voted
                    synchronized (voters) { // Synchronize access to the voters set
                        if (voters.contains(clientId)) {
                            System.out.println("User has already voted: " + clientId);
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
                            System.out.println("Vote recorded for participantId: " + message.participantId + " with votationNumber: " + message.votationNumber);
                        } else {
                            System.out.println("Score not found for participantId: " + message.participantId);
                        }
                    }
                }
            }

            // Remove the vote lock after processing
            voteLocks.remove(clientId);
        } else {
            System.out.println("Client or Habbo is null");
        }
    }

    static class JSONJoinGameEvent {
        int participantId;
        int votationNumber;
    }
}
