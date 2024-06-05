package com.gamecenter.games.battleball;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomState;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;

import com.eu.habbo.messages.outgoing.rooms.users.RoomUnitOnRollerComposer;
import com.gamecenter.games.Globals;
import com.gamecenter.thread.ThreadDeleteBattleBuildRoom;
import com.gamecenter.utils.models.RoomObject;
import com.gamecenter.websocket.WebSocketManager;
import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.outgoing.common.games.battleball.PowerInfoComposer;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.CloseGameQueue;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.GameInfo;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.GamePlayerQueue;

public class BattleBallRoom {
    public Room roomGame = null;
    public BattleBallGame game = null;

    public ArrayList<GameClient> players = new ArrayList<>();
    public ArrayList<GameClient> teamA = new ArrayList<>();
    public ArrayList<GameClient> teamB = new ArrayList<>();
    public long timeToStart = 0L;

    public boolean hasStarted = false;

    public boolean hasFinished = false;

    public long timeToFinish = 0;

    public int MAX_PLAYERS = 8;

    public int MIN_PLAYERS = 2;
    public int TEMPLATE_ROOM_ID = 8577;
    public int MATCH_DURATION = 3;
    public BattleBallRoom() {
        gameChecker();
    }

    public void gameChecker() {
        Emulator.getThreading().run(() -> {
            try {
                run();
            } catch (InterruptedException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }, 100);
    }

    public boolean joinPlayer(GameClient player) {
        if (this.players.stream().count() >= this.MAX_PLAYERS)
            return false;

        HabboInfo info = player.getHabbo().getHabboInfo();

        if(this.players.stream().filter(x -> x.getHabbo().getHabboInfo().getId() == info.getId()).count() > 0) return false;

        this.players.add(player);

        if (this.players.stream().count() >= this.MIN_PLAYERS)
            this.timeToStart = Emulator.getIntUnixTimestamp();

        broadcastQueue();

        return true;
    }

    public void leftPlayer(GameClient player) {
        player.getHabbo().roomBypass = false;
        this.players.remove(player);
        player.getHabbo().goToRoom(Emulator.getConfig().getInt("hotel.home.room"));

        broadcastQueue();
    }

    public void createRoom() {
        Room roomCopy = Emulator.getGameEnvironment().getRoomManager().loadRoom(this.TEMPLATE_ROOM_ID,true);
        roomCopy.loadData();


        RoomObject roomObject = new RoomObject(roomCopy);
        roomObject.insertRoom();
        roomObject.insertFurniture();


        int id = roomObject.getNewRoomId();
        Room room = Emulator.getGameEnvironment().getRoomManager().loadRoom(id,true);
        room.loadData();
        room.setState(RoomState.INVISIBLE);
        room.setHideWall(true);
        room.setNeedsUpdate(true);

        this.roomGame = room;
    }

    public void destroyRoom() {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.roomGame.getId());
        Emulator.getThreading().run(new ThreadDeleteBattleBuildRoom(room));
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

    public void broadcastQueue() {
        for (GameClient s : this.players) {
            try {
                WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(s.getHabbo().getHabboInfo().getId());
                wsClient.sendMessage(new GamePlayerQueue(this));
            } catch (Exception exception) {
            }
        }
    }

    public void initPlayerTeam(){
        boolean teamHandler = false;

        for (GameClient player : this.players) {
            if(!teamHandler){
                teamA.add(player);
            } else {
                teamB.add(player);
            }

            teamHandler = !teamHandler;
        }

        for(GameClient player : this.teamA){
            game.addHabbo(this.roomGame.getHabbo(player.getHabbo().getRoomUnit()), GameTeamColors.RED);
            RoomTile t = new RoomTile((short)39, (short)22, (short)2, RoomTileState.OPEN, true);
            this.roomGame.sendComposer(new RoomUnitOnRollerComposer(player.getHabbo().getRoomUnit(), t, this.roomGame).compose());
        }

        for(GameClient player : this.teamB){
            game.addHabbo(this.roomGame.getHabbo(player.getHabbo().getRoomUnit()), GameTeamColors.BLUE);
            RoomTile t = new RoomTile((short)7, (short)22, (short)2, RoomTileState.OPEN, true);
            this.roomGame.sendComposer(new RoomUnitOnRollerComposer(player.getHabbo().getRoomUnit(), t, this.roomGame).compose());
        }
    }

    private void run() throws InterruptedException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        while (!this.hasStarted && !this.hasFinished) {

            if (this.players.stream().count() >= this.MIN_PLAYERS && this.timeToStart != 0L && this.roomGame == null &&
                    Emulator.getIntUnixTimestamp() > this.timeToStart + 6L) {

                createRoom();
                Thread.sleep(4000L);

                for (GameClient player : this.players) {
                    player.getHabbo().roomBypass = true;
                    player.sendResponse(new ForwardToRoomComposer(this.roomGame.getId()));

                    WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(player.getHabbo().getHabboInfo().getId());
                    wsClient.sendMessage(new CloseGameQueue());
                    player.getHabbo().getRoomUnit().setCanWalk(false);
                }

                Thread.sleep(2000L);

                this.game = BattleBallGame.class.getDeclaredConstructor(Room.class).newInstance(this.roomGame);
                this.roomGame.addGame(this.game);

                this.initPlayerTeam();

                for(GameClient player : this.players){
                    player.getHabbo().getRoomUnit().setCanWalk(true);
                }

                this.hasStarted = true;
                this.game.initialise();
                this.timeToFinish = Emulator.getIntUnixTimestamp() + (this.MATCH_DURATION * 60);
                broadcastTimeToFinish();

                while (Emulator.getIntUnixTimestamp() < this.timeToFinish) {
                    for (GameClient client : players){
                        BattleBallGamePlayer bPlayer = (BattleBallGamePlayer) client.getHabbo().getHabboInfo().getGamePlayer();

                        if (bPlayer != null && bPlayer.skillsType != null){
                            bPlayer.updateSkillTime();

                            WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(client.getHabbo().getHabboInfo().getId());
                            if(wsClient != null){
                                wsClient.sendMessage(new PowerInfoComposer(bPlayer));
                            }
                        }
                    }
                    Thread.sleep(2000L);
                }

                this.game.onEnd();
                this.game.stop();

                Thread.sleep(4000L);

                for (Iterator<GameClient> iterator = this.players.iterator(); iterator.hasNext();) {
                    GameClient value = iterator.next();
                    iterator.remove();
                    leftPlayer(value);
                }

                destroyRoom();
            }

            Thread.sleep(3000L);

            if (this.players.size() == 0) {
                Globals.gameBattleBall.remove(this);
                this.hasFinished = true;

                for (Iterator<GameClient> iterator = this.players.iterator(); iterator.hasNext();) {
                    GameClient value = iterator.next();
                    iterator.remove();
                    leftPlayer(value);
                }
            }

        }

        Globals.gameBattleBall.remove(this);
        this.hasFinished = true;
    }
}
