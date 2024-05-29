package com.gamecenter.games.snowstorm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.*;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.guides.GuideSessionPartnerIsPlayingComposer;
import com.eu.habbo.messages.outgoing.hotelview.HotelViewComposer;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;

import com.eu.habbo.messages.outgoing.rooms.items.AddFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUnitOnRollerComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;
import com.eu.habbo.messages.outgoing.users.UpdateUserLookComposer;
import com.eu.habbo.util.pathfinding.Rotation;
import com.gamecenter.games.Globals;
import com.gamecenter.thread.ThreadDeleteBattleBuildRoom;
import com.gamecenter.utils.models.RoomObject;
import com.gamecenter.websocket.WebSocketManager;
import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.*;

public class SnowStormGame {
    public int roomId = 0;

    public ArrayList<GameClient> players = new ArrayList<>();
    public ArrayList<SnowPlayer> snowPlayers = new ArrayList<>();
    public ArrayList<SnowTeam> teams = new ArrayList<>();
    public long timeToStart = 0L;

    public boolean hasStarted = false;

    public boolean hasFinished = false;

    public long timeToFinish = 0;

    public int MAX_PLAYERS = 6;

    public int MIN_PLAYERS = 2;
    public int TEMPLATE_ROOM_ID = 9795;

    public SnowStormGame() {
        gameChecker();
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

    public void broadcastWinner(SnowTeam team) {
        for (GameClient s : this.players) {
            try {
                WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(s.getHabbo().getHabboInfo().getId());
                wsClient.sendMessage(new WinnerInfo(team));
            } catch (Exception exception) {
            }
        }
    }

    public void broadcastSound(String link) {
        for (GameClient s : this.players) {
            try {
                WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(s.getHabbo().getHabboInfo().getId());
                wsClient.sendMessage(new SoundGameInfo(link));
            } catch (Exception exception) {
            }
        }
    }

    public void broadcastEntityInfo(SnowPlayer player) {
        WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(player.habbo.getHabboInfo().getId());
        wsClient.sendMessage(new EntityInfo(player));
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

    public SnowPlayer getSnowPlayerByHabbo(Habbo habbo){
        return this.snowPlayers.stream().filter(x -> x.habbo.getHabboInfo().getId() == habbo.getHabboInfo().getId()).findAny().orElse(null);
    }

    public void leftPlayer(GameClient player) {
        player.getHabbo().roomBypass = false;
        this.players.remove(player);

        SnowPlayer snowPlayer = this.getSnowPlayerByHabbo(player.getHabbo());
        if(snowPlayer != null){
            snowPlayer.habbo.getRoomUnit().setGameSnow(false);
            this.changeLook(snowPlayer.figure, snowPlayer.habbo);
            this.snowPlayers.remove(snowPlayer);
        }

        player.sendResponse(new HotelViewComposer());
        broadcastQueue();

        player.sendResponse(new GuideSessionPartnerIsPlayingComposer(false));
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

    public void changeLook(String look, Habbo habbo){
        habbo.getHabboInfo().setLook(look);

        habbo.getClient().sendResponse(new UpdateUserLookComposer(habbo));

        if (habbo.getHabboInfo().getCurrentRoom() != null) {
            habbo.getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(habbo).compose());
        }
    }

    public void processSnowBall(Habbo client, Habbo target){
        if(client.getHabboInfo().getId() == target.getHabboInfo().getId()) return;
        Room room = client.getHabboInfo().getCurrentRoom();

        if(client.getRoomUnit().isWalking()){
            client.getRoomUnit().stopWalking();
            client.getRoomUnit().removeStatus(RoomUnitStatus.SNOWWAR_RUN);
            room.sendComposer(new RoomUserStatusComposer(client.getRoomUnit()).compose());
        }

        SnowPlayer snowClient = this.getSnowPlayerByHabbo(client);
        SnowPlayer snowTarget = this.getSnowPlayerByHabbo(target);

        if(snowClient == null || snowTarget == null) return;
        if(snowClient.isDeath || snowTarget.isDeath) return;
        if(snowClient.team.reference == snowTarget.team.reference) return;

        client.getRoomUnit().setRotation(RoomUserRotation.values()[Rotation.Calculate(client.getRoomUnit().getX(), client.getRoomUnit().getY(), target.getRoomUnit().getX(), target.getRoomUnit().getY())]);

        if(snowClient.actualBalls == 0) return;

        client.getRoomUnit().setCanWalk(false);
        client.getRoomUnit().setStatus(RoomUnitStatus.SNOWWAR_THROW, "");
        room.sendComposer(new RoomUserStatusComposer(client.getRoomUnit()).compose());

        snowClient.actualBalls = snowClient.actualBalls - 1;
        this.broadcastEntityInfo(snowClient);

        Item rewardItem = Emulator.getGameEnvironment().getItemManager().getItem(10178899);
        HabboItem newItem = Emulator.getGameEnvironment().getItemManager().createItemNoSql(90, rewardItem, 0, 0, "");

        this.broadcastSound("https://images.bobba.chat/sounds/snowstorm/shoot.mp3");

        Emulator.getThreading().run(() -> {
            client.getRoomUnit().removeStatus(RoomUnitStatus.SNOWWAR_THROW);
            room.sendComposer(new RoomUserStatusComposer(client.getRoomUnit()).compose());
            client.getRoomUnit().setCanWalk(true);

            RoomTile tFront = client.getHabboInfo().getCurrentRoom().getLayout().getTileInFront(client.getRoomUnit().getCurrentLocation(), client.getRoomUnit().getBodyRotation().getValue());

            if (rewardItem != null) {

                if (newItem != null) {

                    newItem.setX(tFront.x);
                    newItem.setY(tFront.y);
                    newItem.setZ(0.6);
                    newItem.setRoomId(room.getId());
                    newItem.needsUpdate(true);

                    room.addHabboItem(newItem);
                    room.sendComposer(new AddFloorItemComposer(newItem, room.getFurniOwnerNames().get(newItem.getUserId())).compose());
                }
            }

            room.updateTile(room.getLayout().getTile(tFront.x, tFront.y));

            Emulator.getThreading().run(() -> {
                int[] finalLocation = getFinalTile(room, tFront.x, tFront.y, target.getRoomUnit().getX(), target.getRoomUnit().getY());
                RoomTile oldLocationItemOne = room.getLayout().getTile(tFront.x, tFront.y);
                RoomTile newLocationItemOne = room.getLayout().getTile((short)finalLocation[0], (short)finalLocation[1]);

                newItem.setX(newLocationItemOne.x);
                newItem.setY(newLocationItemOne.y);
                newItem.setZ(0.6);

                newItem.onMove(room, oldLocationItemOne, newLocationItemOne);
                newItem.needsUpdate(true);
                Emulator.getThreading().run(newItem);

                room.sendComposer(new FloorItemOnRollerComposer(newItem, null, oldLocationItemOne, 1.2, newLocationItemOne, 1.2, 0, room).compose());
            }, 200);

            Emulator.getThreading().run(() -> {

                this.processHit(client, target, newItem);

                room.removeHabboItem(newItem);
                room.sendComposer(new RemoveFloorItemComposer(newItem, false).compose());
            }, 800);

        }, 250L);
    }

    public boolean processHitImpact(Habbo target, HabboItem item){
        SnowPlayer snowTarget = this.getSnowPlayerByHabbo(target);
        if(snowTarget == null) return false;

        if(!snowTarget.isDeath && item.getX() == target.getRoomUnit().getX() && item.getY() == target.getRoomUnit().getY())
            return true;

        if(!snowTarget.isDeath && item.getX() == target.getRoomUnit().getX() + 1 && item.getY() == target.getRoomUnit().getY())
            return true;

        if(!snowTarget.isDeath && item.getX() == target.getRoomUnit().getX() - 1 && item.getY() == target.getRoomUnit().getY())
            return true;

        if(!snowTarget.isDeath && item.getX() == target.getRoomUnit().getX() && item.getY() == target.getRoomUnit().getY() + 1)
            return true;

        if(!snowTarget.isDeath && item.getX() == target.getRoomUnit().getX() && item.getY() == target.getRoomUnit().getY() - 1)
            return true;

        return false;
    }

    public HabboItem checkIfItemIsNear(Habbo habbo, HabboItem item){
        HabboItem itemTriggered = habbo.getRoomUnit().getRoom().getItemsAt(item.getX(), item.getY()).stream().findFirst().orElse(null);
        if(itemTriggered != null) return itemTriggered;

        itemTriggered = habbo.getRoomUnit().getRoom().getItemsAt(item.getX() + 1, item.getY()).stream().findFirst().orElse(null);
        if(itemTriggered != null) return itemTriggered;

        itemTriggered = habbo.getRoomUnit().getRoom().getItemsAt(item.getX() - 1, item.getY()).stream().findFirst().orElse(null);
        if(itemTriggered != null) return itemTriggered;

        itemTriggered = habbo.getRoomUnit().getRoom().getItemsAt(item.getX(), item.getY() + 1).stream().findFirst().orElse(null);
        if(itemTriggered != null) return itemTriggered;

        itemTriggered = habbo.getRoomUnit().getRoom().getItemsAt(item.getX(), item.getY() - 1).stream().findFirst().orElse(null);
        if(itemTriggered != null) return itemTriggered;

        return null;
    }

    public void processHit(Habbo habbo, Habbo target, HabboItem item){
        SnowPlayer snowTarget = this.getSnowPlayerByHabbo(target);
        if(snowTarget == null) return;
        HabboItem itemOnPos = this.checkIfItemIsNear(habbo, item);
        if(itemOnPos != null && itemOnPos.getBaseItem().getId() == 4357){
            String extraData = itemOnPos.getExtradata();
            if(itemOnPos.getExtradata().equals("3")) return;

            String finalExtraData = "1";

            if(extraData.equals("0")) finalExtraData = "1";
            if(extraData.equals("1")) finalExtraData = "2";
            if(extraData.equals("2")) finalExtraData = "3";

            itemOnPos.setExtradata(finalExtraData);
            itemOnPos.needsUpdate(true);
            habbo.getRoomUnit().getRoom().updateItemState(itemOnPos);
        }
        else{
            if(this.processHitImpact(target, item)){
                snowTarget.actualHp = snowTarget.actualHp - 1;
                this.broadcastEntityInfo(snowTarget);

                if(snowTarget.actualHp == 0) this.processDie(target);
                else this.broadcastSound("https://images.bobba.chat/sounds/snowstorm/hit.mp3");

                SnowPlayer snowClient = this.getSnowPlayerByHabbo(habbo);
                if(snowClient != null) snowClient.team.points = snowClient.team.points + 1;
                this.broadcastScores();
            }
        }
    }

    public void processNewBall(Habbo habbo, HabboItem item){
        SnowPlayer player = this.getSnowPlayerByHabbo(habbo);

        if(player == null) return;
        if(player.isDeath) return;
        if(player.actualBalls >= 6) return;
        if(!habbo.getRoomUnit().canWalk()) return;

        if(item.getBaseItem().getId() == 4353){
            if(!this.restFloorBalls(item, habbo.getRoomUnit().getRoom())) return;
        }

        if(item.getBaseItem().getId() == 564845){
            if(!this.restSnowMachine(item, habbo.getRoomUnit().getRoom())) return;
        }

        player.habbo.getRoomUnit().setCanWalk(false);
        this.broadcastSound("https://images.bobba.chat/sounds/snowstorm/pick.mp3");

        habbo.getRoomUnit().setStatus(RoomUnitStatus.SNOWWAR_PICK, "");
        habbo.getRoomUnit().getRoom().sendComposer(new RoomUserStatusComposer(habbo.getRoomUnit()).compose());

        Emulator.getThreading().run(() -> {
            player.actualBalls = player.actualBalls + 1;
            if(!player.isDeath) player.habbo.getRoomUnit().setCanWalk(true);

            habbo.getRoomUnit().removeStatus(RoomUnitStatus.SNOWWAR_PICK);
            habbo.getRoomUnit().getRoom().sendComposer(new RoomUserStatusComposer(habbo.getRoomUnit()).compose());

            this.broadcastEntityInfo(player);
        }, 1000);
    }

    public void processDie(Habbo habbo){
        SnowPlayer player = this.getSnowPlayerByHabbo(habbo);
        if(player == null) return;

        habbo.getRoomUnit().setEffectId(0, 0);
        player.isDeath = true;
        habbo.getRoomUnit().setStatus(RoomUnitStatus.SNOWWAR_DIE_BACK, 0.5 + "");
        habbo.getRoomUnit().getRoom().sendComposer(new RoomUserStatusComposer(habbo.getRoomUnit()).compose());
        player.habbo.getRoomUnit().setCanWalk(false);

        this.broadcastSound("https://images.bobba.chat/sounds/snowstorm/die.mp3");

        Emulator.getThreading().run(() -> {
            player.isDeath = false;
            player.actualHp = 5;
            player.habbo.getRoomUnit().setCanWalk(true);

            habbo.getRoomUnit().removeStatus(RoomUnitStatus.SNOWWAR_DIE_BACK);
            habbo.getRoomUnit().getRoom().sendComposer(new RoomUserStatusComposer(habbo.getRoomUnit()).compose());

            this.broadcastEntityInfo(player);
        }, 5000);
    }

    public static int[] getFinalTile(Room room, int xA, int yA, int xB, int yB){
        ArrayList<int[]> tilesInMid = calculateTilesFromCoords(xA, yA, xB, yB);

        for (int[] tile : tilesInMid) {
            RoomTile tileEntity = room.getLayout().getTile((short)tile[0], (short)tile[1]);

            if(tileEntity.getState() == RoomTileState.INVALID)
                continue;

            if(tileEntity.getState() == RoomTileState.BLOCKED){
                HabboItem item = room.getItemsAt(tileEntity).stream().findFirst().orElse(null);
                if(item != null){
                    if(item.getBaseItem().getId() == 4353 || item.getBaseItem().getId() == 564845) continue;
                }
            }

            if(!tileEntity.isWalkable())
                return new int[]{tile[0], tile[1]};
        }

        return new int[]{xB, yB};
    }

    public static ArrayList<int[]> calculateTilesFromCoords(int xA, int yA, int xB, int yB) {
        ArrayList<int[]> tilesInMid = new ArrayList<>();

        int dx = Math.abs(xB - xA);
        int dy = Math.abs(yB - yA);
        int sx = (xA < xB) ? 1 : -1;
        int sy = (yA < yB) ? 1 : -1;
        int error = dx - dy;

        int x = xA;
        int y = yA;

        while (true) {
            tilesInMid.add(new int[]{x, y});

            if (x == xB && y == yB) {
                break;
            }

            int error2 = 2 * error;

            if (error2 > -dy) {
                error -= dy;
                x += sx;
            }

            if (error2 < dx) {
                error += dx;
                y += sy;
            }
        }

        return tilesInMid;
    }

    private void addFloorBalls(HabboItem item, Room room){
        switch(item.getExtradata()){
            case "1":
                return;

            case "2":
                item.setExtradata("1");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "3":
                item.setExtradata("2");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "4":
                item.setExtradata("3");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "5":
                item.setExtradata("4");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "6":
                item.setExtradata("5");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "7":
                item.setExtradata("6");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "8":
                item.setExtradata("7");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "9":
                item.setExtradata("8");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "10":
                item.setExtradata("9");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "11":
                item.setExtradata("10");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "12":
                item.setExtradata("11");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "13":
                item.setExtradata("12");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;
        }
    }

    private boolean restFloorBalls(HabboItem item, Room room){
        switch(item.getExtradata()){
            case "0":
                item.setExtradata("1");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "1":
                item.setExtradata("2");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "2":
                item.setExtradata("3");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "3":
                item.setExtradata("4");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "4":
                item.setExtradata("5");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "5":
                item.setExtradata("6");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "6":
                item.setExtradata("7");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "7":
                item.setExtradata("8");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "8":
                item.setExtradata("9");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "9":
                item.setExtradata("10");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "10":
                item.setExtradata("11");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "11":
                item.setExtradata("12");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "12":
                item.setExtradata("13");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "13":
                return false;
        }

        return false;
    }

    private void addSnowMachineBall(HabboItem item, Room room){
        switch(item.getExtradata()){
            case "5":
                return;

            case "4":
                item.setExtradata("5");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "3":
                item.setExtradata("4");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "2":
                item.setExtradata("3");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "1":
                item.setExtradata("2");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;

            case "0":
                item.setExtradata("1");
                item.needsUpdate(true);
                room.updateItemState(item);
                return;
        }
    }

    private boolean restSnowMachine(HabboItem item, Room room){
        switch(item.getExtradata()){
            case "5":
                item.setExtradata("4");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "4":
                item.setExtradata("3");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "3":
                item.setExtradata("2");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "2":
                item.setExtradata("1");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "1":
                item.setExtradata("0");
                item.needsUpdate(true);
                room.updateItemState(item);
                return true;

            case "0":
                return false;
        }

        return false;
    }

    private void run() throws InterruptedException {
        while (!this.hasStarted && !this.hasFinished) {

            if (this.players.stream().count() >= this.MIN_PLAYERS && this.timeToStart != 0L && this.roomId == 0 &&
                    Emulator.getIntUnixTimestamp() > this.timeToStart + 6L) {

                createRoom();
                Thread.sleep(4000L);

                Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.roomId);
                boolean checkTeam = false;
                SnowTeam teamA = new SnowTeam("a");
                SnowTeam teamB = new SnowTeam("b");

                for (GameClient player : this.players) {
                    if(!checkTeam){
                        SnowPlayer sp = new SnowPlayer(player.getHabbo(), teamA);
                        teamA.players.add(sp);
                        this.snowPlayers.add(sp);
                        checkTeam = true;
                    }
                    else{
                        SnowPlayer sp = new SnowPlayer(player.getHabbo(), teamB);
                        teamB.players.add(sp);
                        this.snowPlayers.add(sp);
                        checkTeam = false;
                    }

                    player.getHabbo().roomBypass = true;
                    player.sendResponse(new ForwardToRoomComposer(this.roomId));
                    WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(player.getHabbo().getHabboInfo().getId());
                    wsClient.sendMessage(new CloseGameQueue());

                    player.getHabbo().getRoomUnit().setCanWalk(false);
                }

                this.teams.add(teamA);
                this.teams.add(teamB);

                Thread.sleep(2000L);


                for(SnowTeam team : this.teams){
                    for(SnowPlayer player : team.players){
                        if(team.reference.equals("a")){
                            if(player.habbo.getHabboInfo().getLook().contains("ch")){
                                this.changeLook(player.habbo.getHabboInfo().getLook().replaceAll("ch-\\d+.*?\\.", "ch-20000-0"), player.habbo);
                            }else {
                                this.changeLook(player.habbo.getHabboInfo().getLook() + ".ch-20000-0", player.habbo);
                            }

                            RoomTile t = new RoomTile((short)12, (short)39, (short)0, RoomTileState.OPEN, true);
                            room.sendComposer(new RoomUnitOnRollerComposer(player.habbo.getRoomUnit(), t, room).compose());
                        }
                        else{
                            if(player.habbo.getHabboInfo().getLook().contains("ch")){
                                this.changeLook(player.habbo.getHabboInfo().getLook().replaceAll("ch-\\d+.*?\\.", "ch-20001-0"), player.habbo);
                            }else {
                                this.changeLook(player.habbo.getHabboInfo().getLook() + ".ch-20001-0", player.habbo);
                            }

                            RoomTile t = new RoomTile((short)37, (short)16, (short)0, RoomTileState.OPEN, true);
                            room.sendComposer(new RoomUnitOnRollerComposer(player.habbo.getRoomUnit(), t, room).compose());
                        }

                        player.habbo.getRoomUnit().setGameSnow(true);
                        this.broadcastEntityInfo(player);
                    }
                }

                for(GameClient c : this.players){
                    c.getHabbo().getClient().sendResponse(new GuideSessionPartnerIsPlayingComposer(true));
                    c.getHabbo().getRoomUnit().setCanWalk(true);
                }

                this.hasStarted = true;
                this.timeToFinish = Emulator.getIntUnixTimestamp() + 120L;
                broadcastTimeToFinish();
                broadcastScores();

                Thread.sleep(1000L);


                while (Emulator.getIntUnixTimestamp() < this.timeToFinish) {
                    for(HabboItem item : room.getFloorItems()){
                        if(item.getBaseItem().getId() == 4353)
                            this.addFloorBalls(item, room);

                        if(item.getBaseItem().getId() == 564845)
                            this.addSnowMachineBall(item, room);
                    }
                    Thread.sleep(10000L);
                }

                if(teamA.points >= teamB.points) this.broadcastWinner(teamA);
                else this.broadcastWinner(teamB);

                for (Iterator<GameClient> iterator = this.players.iterator(); iterator.hasNext();) {
                    GameClient value = iterator.next();
                    iterator.remove();
                    this.leftPlayer(value);
                }

                destroyRoom();
            }

            Thread.sleep(3000L);

            if (this.players.stream().count() == 0L) {
                Globals.gameSnowStorm.remove(this);
                this.hasFinished = true;

                for (Iterator<GameClient> iterator = this.players.iterator(); iterator.hasNext();) {
                    GameClient value = iterator.next();
                    iterator.remove();
                    leftPlayer(value);
                }
            }

        }

        Globals.gameSnowStorm.remove(this);
        this.hasFinished = true;
    }
}
