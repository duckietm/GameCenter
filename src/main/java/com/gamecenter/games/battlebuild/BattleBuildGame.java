package com.gamecenter.games.battlebuild;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomState;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;

import com.gamecenter.games.Globals;
import com.gamecenter.games.classes.GamePlayerScore;
import com.gamecenter.games.classes.RoomPlayerBattleBuild;
import com.gamecenter.thread.ThreadDeleteBattleBuildRoom;
import com.gamecenter.utils.models.RoomObject;
import com.gamecenter.websocket.WebSocketManager;
import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.CloseGameQueue;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.CloseVotation;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.GameInfo;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.GamePlayerQueue;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.GameScoreInfo;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.ShowVotation;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.WinnerInfo;

public class BattleBuildGame {

    public ArrayList<GameClient> players = new ArrayList<>();
    public ArrayList<RoomPlayerBattleBuild> rooms = new ArrayList<>();
    public ArrayList<GamePlayerScore> gamePlayerScores = new ArrayList<>();

    public long timeToStart = 0L;

    public boolean hasStarted = false;

    public boolean hasFinished = false;

    public long timeToFinish = 0;

    public int MAX_PLAYERS = 8;

    public int MIN_PLAYERS = 2;
    public int TEMPLATE_ROOM_ID = Emulator.getConfig().getInt("hotel.battleball.room");

    public int MINUTES_DURATION = Emulator.getConfig().getInt("hotel.battleball.time");
    public String theme = null;

    public BattleBuildGame() {
        gameChecker();
        System.out.println("Game created successfully");
    }

    public void gameChecker() {
        Emulator.getThreading().run(() -> {
            try {
                run();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }, 100);
    }

    public void broadcastQueue() {
        for (GameClient s : this.players) {
            try {
                WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(s.getHabbo().getHabboInfo().getId());
                wsClient.sendMessage(new GamePlayerQueue(this));
            } catch (Exception exception) {
            }
        }
    }

    public void broadcastScores() {
        for (GameClient s : this.players) {
            try {
                WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(s.getHabbo().getHabboInfo().getId());
                wsClient.sendMessage(new GameScoreInfo(this));
            } catch (Exception exception) {
            }
        }
    }

    public void broadcastWinner(int maxScore) {
        for (GameClient s : this.players) {
            try {
                WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(s.getHabbo().getHabboInfo().getId());
                wsClient.sendMessage(new WinnerInfo(this, maxScore));
            } catch (Exception exception) {
            }
        }
    }

    public void broadcastTimeToFinish() {
        for (GameClient s : this.players) {
            try {
                WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(s.getHabbo().getHabboInfo().getId());
                wsClient.sendMessage(new GameInfo(this));
            } catch (Exception exception) {
            }
        }
    }

    public boolean joinPlayer(GameClient player) {
        if (this.players.stream().count() >= this.MAX_PLAYERS)
            return false;

        HabboInfo info = player.getHabbo().getHabboInfo();

        if(this.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == info.getId()).count() > 0) return false;
        this.players.add(player);

        if (this.players.stream().count() >= this.MIN_PLAYERS)
            this.timeToStart = Emulator.getIntUnixTimestamp();

        System.out.println("Player added to game: " + info.getUsername());

        broadcastQueue();

        return true;
    }

    public void leftPlayer(GameClient player) {
        player.getHabbo().roomBypass = false;
        this.players.remove(player);

        RoomPlayerBattleBuild pr = this.rooms.stream().filter(x -> (x.player == player)).findAny().orElse(null);

        if(pr != null) this.destroyRoom(pr.room);

        player.getHabbo().goToRoom(Emulator.getConfig().getInt("hotel.home.room"));

        broadcastQueue();
    }

    public void createRoomForPlayer(GameClient player) {
        Room roomCopy = Emulator.getGameEnvironment().getRoomManager().loadRoom(this.TEMPLATE_ROOM_ID, true);

        if (roomCopy == null) {
            System.err.println("Error: Failed to load template room with ID " + this.TEMPLATE_ROOM_ID);
        }
		
		System.err.println("Loaded: load template room with ID " + this.TEMPLATE_ROOM_ID);

        roomCopy.loadData();

        HabboInfo info = player.getHabbo().getHabboInfo();

        RoomObject roomObject = new RoomObject(roomCopy);
        roomObject.insertBattleRoom(info);
        roomObject.insertFurniture();

        int id = roomObject.getNewRoomId();
        Room room = Emulator.getGameEnvironment().getRoomManager().loadRoom(id, true);

        if (room == null) {
            System.err.println("Error: Failed to load new room with ID " + id);
            return;
        }
		
		System.err.println("Loaded: Load new room with ID " + id);

        room.loadData();
        room.setState(RoomState.INVISIBLE);
        room.setHideWall(true);
        room.setNeedsUpdate(true);

        this.rooms.add(new RoomPlayerBattleBuild(player, room));
    }

    public void destroyRoom(Room room) {
        Emulator.getThreading().run(new ThreadDeleteBattleBuildRoom(room));
    }

    private void run() throws InterruptedException {
        while (!this.hasStarted && !this.hasFinished) {

            if (this.players.stream().count() >= this.MIN_PLAYERS && this.timeToStart != 0L && Emulator.getIntUnixTimestamp() > this.timeToStart + 6L) {

                this.theme = String.valueOf((new Random()).nextInt(50));

                for (GameClient player : this.players){
                    player.getHabbo().roomBypass = true;
                    this.createRoomForPlayer(player);
                    this.gamePlayerScores.add(new GamePlayerScore(player));
                }

                Thread.sleep(4000L);

                for (RoomPlayerBattleBuild rb : this.rooms) {
                    rb.player.sendResponse(new ForwardToRoomComposer(rb.room.getId()));
                    WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(rb.player.getHabbo().getHabboInfo().getId());
                    wsClient.sendMessage(new CloseGameQueue());
                }

                this.hasStarted = true;
                this.timeToFinish = Emulator.getIntUnixTimestamp() + (60 * this.MINUTES_DURATION);
                broadcastTimeToFinish();
                Thread.sleep(1000L);

                while (Emulator.getIntUnixTimestamp() < this.timeToFinish && this.players.stream().count() > 0) {
                    Thread.sleep(2000L);
                }

                for (RoomPlayerBattleBuild rb : this.rooms) {
                    for(GameClient player : this.players){
                        player.sendResponse(new ForwardToRoomComposer(rb.room.getId()));
                        
                        WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(player.getHabbo().getHabboInfo().getId());
                        wsClient.sendMessage(new ShowVotation(rb.player.getHabbo().getHabboInfo()));
                    }

                    Thread.sleep(15000);

                    for(GameClient player : this.players){
                        WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(player.getHabbo().getHabboInfo().getId());
                        wsClient.sendMessage(new CloseVotation());
                    }
                }

                int maxScore = 0;

                for (GamePlayerScore score : this.gamePlayerScores) {
                    if (score.score > maxScore)
                        maxScore = score.score;
                }

                this.broadcastWinner(maxScore);

                for (Iterator<GameClient> iterator = this.players.iterator(); iterator.hasNext();) {
                    GameClient value = iterator.next();
                    iterator.remove();
                    leftPlayer(value);
                }
            }

            Thread.sleep(1500L);

            if (this.players.stream().count() == 0L) {
                Globals.gameBattleBuild.remove(this);
                this.hasFinished = true;

                for (Iterator<GameClient> iterator = this.players.iterator(); iterator.hasNext();) {
                    GameClient value = iterator.next();
                    iterator.remove();
                    leftPlayer(value);
                }
            }

        }

        Globals.gameBattleBuild.remove(this);
        this.hasFinished = true;
    }
}
