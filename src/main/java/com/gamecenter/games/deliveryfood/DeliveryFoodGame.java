package com.gamecenter.games.deliveryfood;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomState;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;
import com.eu.habbo.messages.outgoing.rooms.items.AddFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;

import com.gamecenter.games.Globals;
import com.gamecenter.games.classes.GameDelivery;
import com.gamecenter.games.classes.GameFurni;
import com.gamecenter.games.classes.GamePlayerScore;
import com.gamecenter.games.classes.GameUserDelivery;
import com.gamecenter.games.classes.RoomPosition;
import com.gamecenter.thread.ThreadDeleteBattleBuildRoom;
import com.gamecenter.utils.models.RoomObject;
import com.gamecenter.websocket.WebSocketManager;
import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.CloseGameQueue;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.GameInfo;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.GamePlayerQueue;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.GameScoreInfo;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.WinnerInfo;

public class DeliveryFoodGame {
    public int roomId = 0;

    public ArrayList<GameClient> players = new ArrayList<>();

    public long timeToStart = 0L;

    public boolean hasStarted = false;

    public boolean hasFinished = false;

    public long timeToFinish = 0;

    public int MAX_PLAYERS = 6;

    public int MIN_PLAYERS = 2;
    public int TEMPLATE_ROOM_ID = 9804;

    public ArrayList<GameFurni> items = new ArrayList<>();

    public int[] itemDefinitions = new int[] { 50843 , 51821, 9370 };

    public ArrayList<GamePlayerScore> gamePlayerScores = new ArrayList<>();
    public ArrayList<GameUserDelivery> deliveries = new ArrayList<>();

    public GameDelivery[] gameDeliveries = new GameDelivery[] {
            new GameDelivery(new RoomPosition(13, 14, 0.0D), new RoomPosition(14, 14, 0.0D)),
            new GameDelivery(new RoomPosition(11, 17, 0.0D), new RoomPosition(11, 18, 0.0D)),
            new GameDelivery(new RoomPosition(14, 20, 0.0D), new RoomPosition(15, 20, 0.0D)),
            new GameDelivery(new RoomPosition(19, 18, 0.0D), new RoomPosition(19, 19, 0.0D)),
            new GameDelivery(new RoomPosition(19, 14, 0.0D), new RoomPosition(20, 14, 0.0D))
    };

    public DeliveryFoodGame() {
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

        this.roomId = id;
    }

    public void destroyRoom() {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.roomId);
        Emulator.getThreading().run(new ThreadDeleteBattleBuildRoom(room));
    }

    public void shuffleDeliveryPositions() {
        Random random = new Random();

        for (int i = this.gameDeliveries.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            GameDelivery temp = this.gameDeliveries[index];

            this.gameDeliveries[index] = this.gameDeliveries[i];
            this.gameDeliveries[i] = temp;
        }
    }

    public void processFurniDelivery(HabboItem item, GameClient player) {

        shuffleDeliveryPositions();

        for (Iterator<GameFurni> iterator = this.items.iterator(); iterator.hasNext();) {
            GameFurni value = iterator.next();
            if (value.furni.getId() == item.getId())
                iterator.remove();
        }

        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.roomId);
        room.removeHabboItem(item);
        room.sendComposer(new RemoveFloorItemComposer(item, false).compose());

        for (GameDelivery delivery : this.gameDeliveries) {
            boolean check = false;

            for (GameClient p : this.players) {
                if(this.deliveries.stream().filter(x -> x.delivery == delivery && p.getHabbo().getHabboInfo().getId() == x.player.getHabbo().getHabboInfo().getId()).count() > 0)
                    check = true;
            }

            if (!check) {
                HabboInfo info = player.getHabbo().getHabboInfo();
                Bot bot = new Bot((int) (Math.random() * 9.9E8D), info.getUsername(), info.getMotto(), info.getLook(), info.getGender(), 2, "ElMayor");
                bot.setCanWalk(false);
                bot.setChatAuto(false);
                Emulator.getGameEnvironment().getBotManager().placeBot(bot, player.getHabbo(), player.getHabbo().getHabboInfo().getCurrentRoom(), room.getLayout().getTile((short) delivery.chair.x, (short) delivery.chair.y));
               
                this.deliveries.add(new GameUserDelivery(player, delivery));
                player.getHabbo().getHabboInfo().getCurrentRoom().giveEffect(player.getHabbo(), 554, -1);
                return;
            }
        }
    }

    public void addScore(GameClient player) {
        for (GamePlayerScore playerScore : this.gamePlayerScores) {
            if (player.getHabbo().getHabboInfo().getId() == playerScore.player.getHabbo().getHabboInfo().getId())
                playerScore.score++;
        }

        broadcastScores();
    }

    public void processGame(Room room) {
        try {
            for (int i = 0; i < 4; i++) {
                int inumerate = i;

                if (this.items.stream().filter(x -> (x.id == inumerate)).count() == 0L) {
                    int xPosition = getXPositionByNumber(i); 

                    Item rewardItem = Emulator.getGameEnvironment().getItemManager().getItem(this.itemDefinitions[(new Random()).nextInt(this.itemDefinitions.length)]);
                    HabboItem newItem = Emulator.getGameEnvironment().getItemManager().createItem(1, rewardItem, 0, 0, "");


                    if (xPosition != 0) {
                        short x = 8;
                        short y = (short)xPosition;
                        int rotation = 0;
                        double height = 1.2D;

                        Emulator.getThreading().run(() -> {
                            if (rewardItem != null) {

                                if (newItem != null) {

                                        newItem.setX(x);
                                        newItem.setY(y);
                                        newItem.setZ(height);
                                        newItem.setRoomId(room.getId());
                                        newItem.needsUpdate(true);

                                        room.addHabboItem(newItem);
                                        room.sendComposer(new AddFloorItemComposer(newItem, room.getFurniOwnerNames().get(newItem.getUserId())).compose());
                                }
                            }

                            room.updateTile(room.getLayout().getTile(x, y));
                        }, 800L);
                    }
                    
                    this.items.add(new GameFurni(i, newItem));
                }

            }
        } catch (Exception exception) {
        }
    }

    public int getXPositionByNumber(int number) {
        switch (number) {
            case 1:
                return 18;
            case 2:
                return 17;
            case 3:
                return 16;
            case 4:
                return 15;
        }
        return 15;
    }

    private void run() throws InterruptedException {
        while (!this.hasStarted && !this.hasFinished) {

            if (this.players.stream().count() >= this.MIN_PLAYERS && this.timeToStart != 0L && this.roomId == 0 &&
                Emulator.getIntUnixTimestamp() > this.timeToStart + 6L) {

                createRoom();
                Thread.sleep(4000L);

                Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.roomId);

                for (GameClient player : this.players) {
                    player.getHabbo().roomBypass = true;
                    player.sendResponse(new ForwardToRoomComposer(this.roomId));
                    WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(player.getHabbo().getHabboInfo().getId());
                    wsClient.sendMessage(new CloseGameQueue());
                    this.gamePlayerScores.add(new GamePlayerScore(player));
                }

                this.hasStarted = true;
                this.timeToFinish = Emulator.getIntUnixTimestamp() + 120L;
                broadcastTimeToFinish();
                broadcastScores();
                Thread.sleep(1000L);

                while (Emulator.getIntUnixTimestamp() < this.timeToFinish) {
                    processGame(room);
                    Thread.sleep(2000L);
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
                    this.leftPlayer(value);
                }

                destroyRoom();
            }

            Thread.sleep(3000L);

            if (this.players.stream().count() == 0L) {
                Globals.gameDeliveryFood.remove(this);
                this.hasFinished = true;

                for (Iterator<GameClient> iterator = this.players.iterator(); iterator.hasNext();) {
                    GameClient value = iterator.next();
                    iterator.remove();
                    leftPlayer(value);
                }
            }

        }

        Globals.gameDeliveryFood.remove(this);
        this.hasFinished = true;
    }
}
