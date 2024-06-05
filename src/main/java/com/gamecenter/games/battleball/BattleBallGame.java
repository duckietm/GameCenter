package com.gamecenter.games.battleball;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.games.*;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.games.battlebanzai.InteractionBattleBanzaiSphere;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUserAction;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.items.AddFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserActionComposer;
import com.eu.habbo.threading.runnables.QueryDeleteHabboItem;
import com.gamecenter.games.battleball.skils.*;
import com.gamecenter.interactions.battleball.InteractionBattleBallTile;
import com.gamecenter.websocket.WebSocketManager;
import com.gamecenter.websocket.client.WebSocketClient;
import com.gamecenter.websocket.outgoing.common.games.gamecenter.WinnerInfo;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class BattleBallGame extends Game {
    private static final Logger LOGGER = LoggerFactory.getLogger(BattleBallGame.class);

    public static final int POINTS_HIJACK_TILE = Emulator.getConfig().getInt("hotel.banzai.points.tile.steal", 0);

    public static final int POINTS_FILL_TILE = Emulator.getConfig().getInt("hotel.banzai.points.tile.fill", 0);

    public static final int POINTS_LOCK_TILE = Emulator.getConfig().getInt("hotel.banzai.points.tile.lock", 1);
    public static final int ITEMID_STUN = 103999;

    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Emulator.getConfig().getInt("hotel.banzai.fill.threads", 2));
    private final THashMap<GameTeamColors, THashSet<HabboItem>> lockedTiles;
    private final THashMap<Integer, HabboItem> gameTiles;
    private int tileCount;
    private int countDown;
    private int countDown2;

    public final static THashMap<SkillsType, BattleBallSkills> skills = new THashMap<>();

    public BattleBallGame(Room room) {
        super(BattleBallGameTeam.class, BattleBallGamePlayer.class, room, true);

        this.lockedTiles = new THashMap<>();
        this.gameTiles = new THashMap<>();

        room.setAllowEffects(true);

        addSkill(new BoomSkill());
        addSkill(new BulbSkills());
        addSkill(new CannonSkill());
        addSkill(new DrillSkill());
        addSkill(new FlashSkill());
        addSkill(new HarleSkill());
        addSkill(new PinsSkills());
        addSkill(new RandomSkill());
        addSkill(new SprinSkill());
    }

    public void addSkill(BattleBallSkills skill){
        skills.put(skill.type, skill);
    }

    @Override
    public void initialise() {
        if (!this.state.equals(GameState.IDLE))
            return;
        
        /* The first countdown is activated for the first two seconds emitting only the blue light (second interaction),
            the second, after another two seconds, completely activates the sphere (third interaction).
         */
        this.countDown = 3;
        this.countDown2 = 2;

        this.resetMap();

        synchronized (this.teams) {
            for (GameTeam t : this.teams.values()) {
                t.initialise();
            }
        }


        this.start();
    }

    @Override
    public boolean addHabbo(Habbo habbo, GameTeamColors teamColor) {
        return super.addHabbo(habbo, teamColor);
    }

    @Override
    public void start() {
        if (!this.state.equals(GameState.IDLE))
            return;

        super.start();

        Emulator.getThreading().run(this, 0);
    }

    @Override
    public void run() {
        try {
            if (this.state.equals(GameState.IDLE) || this.room == null)
                return;

            if (this.countDown > 0) {
                this.countDown--;

                if (this.countDown == 0) {
                    for (HabboItem item : this.room.getRoomSpecialTypes().getItemsOfType(InteractionBattleBanzaiSphere.class)) {
                        item.setExtradata("1");
                        this.room.updateItemState(item);
                        if(this.countDown2 > 0) {
                            this.countDown2--;
                            if(this.countDown2 == 0) {
                                item.setExtradata("2");
                                this.room.updateItemState(item);
                            }
                        }
                    }
                }

                if (this.countDown > 1) {
                    Emulator.getThreading().run(this, 500);
                    return;
                }
            }

            Emulator.getThreading().run(this, 1000);

            if (this.state.equals(GameState.PAUSED)) return;

            int total = 0;
            synchronized (this.lockedTiles) {
                for (Map.Entry<GameTeamColors, THashSet<HabboItem>> set : this.lockedTiles.entrySet()) {
                    total += set.getValue().size();
                }
            }

            GameTeam highestScore = null;
            synchronized (this.teams) {
                for (Map.Entry<GameTeamColors, GameTeam> set : this.teams.entrySet()) {
                    if (highestScore == null || highestScore.getTotalScore() < set.getValue().getTotalScore()) {
                        highestScore = set.getValue();
                    }
                }
            }

            if (highestScore != null) {
                for (HabboItem item : this.room.getRoomSpecialTypes().getItemsOfType(InteractionBattleBanzaiSphere.class)) {
                    item.setExtradata((highestScore.teamColor.type + 2) + "");
                    this.room.updateItemState(item);
                }
            }

            if (Math.random() < 0.07 && this.room != null) {
                SkillsType type = getRandomSkill();

                Item item = Emulator.getGameEnvironment().getItemManager().getItem(skills.get(type).itemId);
                if (item != null) {
                    HabboItem newItem = Emulator.getGameEnvironment().getItemManager().createItem(12, item, 0, 0, "");

                    if (newItem != null) {
                        RoomTile tile = this.getRandomTile();

                        if (tile != null) {
                            HabboItem itemTile = this.room.getTopItemAt(tile.x, tile.y);

                            newItem.setX(tile.x);
                            newItem.setY(tile.y);
                            newItem.setZ(itemTile != null ? itemTile.getZ() + 0.1 : 0);
                            newItem.setRoomId(this.room.getId());
                            newItem.needsUpdate(true);

                            this.room.addHabboItem(newItem);
                            this.room.updateItem(newItem);
                            this.room.updateTile(this.room.getLayout().getTile(tile.x, tile.y));
                            this.room.sendComposer(new AddFloorItemComposer(newItem, "BattleBall").compose());
                        }
                    } else {
                        LOGGER.error("Failed to create new item for skill type: " + type);
                    }
                } else {
                    LOGGER.error("Item not found for skill type: " + type);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }

    @Override
    public void onEnd() {
        GameTeam winningTeam = null;

        boolean singleTeamGame = this.teams.values().stream().filter(t -> t.getMembers().size() > 0).count() == 1;

        List<GamePlayer> players = new ArrayList<>();

        for (GameTeam team : this.teams.values()) {
            if (!singleTeamGame) {
                for (GamePlayer player : team.getMembers()) {
                    if (player.getScoreAchievementValue() > 0) {
                        AchievementManager.progressAchievement(player.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("BattleBallPlayer"));
                    }

                    players.add(player);
                }
            }

            if (winningTeam == null || team.getTotalScore() > winningTeam.getTotalScore()) {
                winningTeam = team;
            }
        }

        if (winningTeam != null) {
            if (!singleTeamGame) {
                for (GamePlayer player : winningTeam.getMembers()) {
                    if (player.getScoreAchievementValue() > 0) {
                        this.room.sendComposer(new RoomUserActionComposer(player.getHabbo().getRoomUnit(), RoomUserAction.WAVE).compose());
                        AchievementManager.progressAchievement(player.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("BattleBallWinner"));

                    }
                }
            }

            for (HabboItem item : this.room.getRoomSpecialTypes().getItemsOfType(InteractionBattleBanzaiSphere.class)) {
                item.setExtradata((6 + winningTeam.teamColor.type) + "");
                this.room.updateItemState(item);
            }

            synchronized (this.lockedTiles) {
                //Emulator.getThreading().run(new BattleBanzaiTilesFlicker(this.lockedTiles.get(winningTeam.teamColor), winningTeam.teamColor, this.room));
            }


            //Win Websocket
            for (GamePlayer player : players) {
                BattleBallGamePlayer bPlayer = (BattleBallGamePlayer)player;

                WebSocketClient wsClient = WebSocketManager.getInstance().getClientManager().getWebSocketClientForHabbo(bPlayer.getHabbo().getHabboInfo().getId());

                if(wsClient != null ){
                    wsClient.sendMessage(new WinnerInfo(this, winningTeam));
                }
            }

        }



        super.onEnd();
    }

    @Override
    public void stop() {
        super.stop();

        for (HabboItem tile : this.gameTiles.values()) {
            if (tile.getExtradata().equals("1")) {
                tile.setExtradata("0");
                this.room.updateItem(tile);
            }
        }
        synchronized (this.lockedTiles) {
            this.lockedTiles.clear();
        }
    }

    private synchronized void resetMap() {
        this.tileCount = 0;
        for (HabboItem item : this.room.getFloorItems()) {
            if (item instanceof InteractionBattleBallTile) {
                item.setExtradata("1");
                this.room.updateItemState(item);
                this.tileCount++;
                this.gameTiles.put(item.getId(), item);
            }
        }
    }


    public void tileLocked(GameTeamColors teamColor, HabboItem item, Habbo habbo) {
        this.tileLocked(teamColor, item, habbo, false);
    }

    public void tileLocked(GameTeamColors teamColor, HabboItem item, Habbo habbo, boolean doNotCheckFill) {
        synchronized (this.lockedTiles) {
            if (item instanceof InteractionBattleBallTile) {
                if (!this.lockedTiles.containsKey(teamColor)) {
                    this.lockedTiles.put(teamColor, new THashSet<>());
                }

                this.lockedTiles.get(teamColor).add(item);
            }

            if (doNotCheckFill) return;

            final int x = item.getX();
            final int y = item.getY();

            final List<List<RoomTile>> filledAreas = new ArrayList<>();
            final THashSet<HabboItem> lockedTiles = new THashSet<>(this.lockedTiles.get(teamColor));

            executor.execute(() -> {
                filledAreas.add(this.floodFill(x, y - 1, lockedTiles, new ArrayList<>(), teamColor));
                filledAreas.add(this.floodFill(x, y + 1, lockedTiles, new ArrayList<>(), teamColor));
                filledAreas.add(this.floodFill(x - 1, y, lockedTiles, new ArrayList<>(), teamColor));
                filledAreas.add(this.floodFill(x + 1, y, lockedTiles, new ArrayList<>(), teamColor));

                Optional<List<RoomTile>> largestAreaOfAll = filledAreas.stream().filter(Objects::nonNull).max(Comparator.comparing(List::size));

                if (largestAreaOfAll.isPresent()) {
                    for (RoomTile tile : largestAreaOfAll.get()) {
                        Optional<HabboItem> tileItem = this.gameTiles.values().stream().filter(i -> i.getX() == tile.x && i.getY() == tile.y && i instanceof InteractionBattleBallTile).findAny();

                        tileItem.ifPresent(habboItem -> {
                            this.tileLocked(teamColor, habboItem, habbo, true);

                            habboItem.setExtradata((2 + (teamColor.type * 3)) + "");
                            this.room.updateItem(habboItem);
                        });
                    }

                    if (habbo != null) {
                        habbo.getHabboInfo().getGamePlayer().addScore(BattleBallGame.POINTS_LOCK_TILE * largestAreaOfAll.get().size());
                    }
                }
            });
        }
    }

    private List<RoomTile> floodFill(int x, int y, THashSet<HabboItem> lockedTiles, List<RoomTile> stack, GameTeamColors color) {
        if (this.isOutOfBounds(x, y) || this.isForeignLockedTile(x, y, color)) return null;

        RoomTile tile = this.room.getLayout().getTile((short) x, (short) y);

        if (this.hasLockedTileAtCoordinates(x, y, lockedTiles) || stack.contains(tile)) return stack;

        stack.add(tile);

        List<List<RoomTile>> result = new ArrayList<>();
        result.add(this.floodFill(x, y - 1, lockedTiles, stack, color));
        result.add(this.floodFill(x, y + 1, lockedTiles, stack, color));
        result.add(this.floodFill(x - 1, y, lockedTiles, stack, color));
        result.add(this.floodFill(x + 1, y, lockedTiles, stack, color));

        if (result.contains(null)) return null;

        Optional<List<RoomTile>> biggestArea = result.stream().max(Comparator.comparing(List::size));

        return biggestArea.orElse(null);

    }

    private boolean hasLockedTileAtCoordinates(int x, int y, THashSet<HabboItem> lockedTiles) {
        for (HabboItem item : lockedTiles) {
            if (item.getX() == x && item.getY() == y) return true;
        }

        return false;
    }

    private boolean isOutOfBounds(int x, int y) {
        for (HabboItem item : this.gameTiles.values()) {
            if (item.getX() == x && item.getY() == y) return false;
        }

        return true;
    }

    private boolean isForeignLockedTile(int x, int y, GameTeamColors color) {
        for (HashMap.Entry<GameTeamColors, THashSet<HabboItem>> lockedTilesForColor : this.lockedTiles.entrySet()) {
            if (lockedTilesForColor.getKey() == color) continue;

            for (HabboItem item : lockedTilesForColor.getValue()) {
                if (item.getX() == x && item.getY() == y) return true;
            }
        }

        return false;
    }

    public void markTile(Habbo habbo, InteractionBattleBallTile tile, int state) {
        if (!this.gameTiles.contains(tile.getId())) return;

        int typeColor = habbo.getHabboInfo().getGamePlayer().getTeamColor().type;
        boolean checkSpring = habbo.getHabboStats().cache.get(SprinSkill.SKILL_SPRIN_ATTRIBUTE) != null;

        if(habbo.getHabboStats().cache.get(HarleSkill.SKILL_HARLE_ATTRIBUTE) != null){
            BattleBallGamePlayer p = (BattleBallGamePlayer) habbo.getHabboStats().cache.get(HarleSkill.SKILL_HARLE_ATTRIBUTE);

            if (p != null){
                habbo = p.getHabbo();
                typeColor = p.getTeamColor().type;
            }
        }

        int check = state - (typeColor * 3);

        if (check == 0 || check == 1) {
            state++;

            if (state % 3 == 2 || checkSpring) {
                habbo.getHabboInfo().getGamePlayer().addScore(BattleBallGame.POINTS_LOCK_TILE);
                this.tileLocked(habbo.getHabboInfo().getGamePlayer().getTeamColor(), tile, habbo);
            } else {
                habbo.getHabboInfo().getGamePlayer().addScore(BattleBallGame.POINTS_FILL_TILE);
            }
        } else {
            state = typeColor * 3;
            habbo.getHabboInfo().getGamePlayer().addScore(BattleBallGame.POINTS_HIJACK_TILE);
        }

        if(checkSpring){
            state = 2 + (typeColor * 3);
        }

        tile.setExtradata(state + "");
        this.room.updateItem(tile);
    }

    public SkillsType getRandomSkill() {
        List<SkillsType> keys = new ArrayList<>(skills.keySet());
        Random random = new Random();
        int randomIndex = random.nextInt(keys.size());
        return keys.get(randomIndex);
    }

    public RoomTile getRandomTile() {
        RoomTile tile = null;

        if(this.getRoom() == null || this.getRoom().getLayout() == null){
            return tile;
        }

        while (tile == null){

            if(this.getRoom() == null || this.getRoom().getLayout() == null){
                return tile;
            }

            RoomTile t = this.room.getRandomWalkableTile();
            if(t != null){
                HabboItem item = this.room.getTopItemAt(t.x, t.y);

                if(item != null){
                    if(item instanceof InteractionBattleBallTile){
                        tile = t;
                    }
                }
            }
        }

       return tile;
    }

    public void stunPlayer(Habbo habbo){
        habbo.getRoomUnit().setCanWalk(false);
        RoomTile position = habbo.getRoomUnit().getCurrentLocation();

        Item item = Emulator.getGameEnvironment().getItemManager().getItem(ITEMID_STUN);
        HabboItem newItem = Emulator.getGameEnvironment().getItemManager().createItem(12, item, 0, 0, "");

        HabboItem itemTile = this.room.getTopItemAt(position.x, position.y);

        newItem.setX(position.x);
        newItem.setY(position.y);
        newItem.setZ(itemTile.getZ() + 0.1);
        newItem.setRoomId(this.room.getId());
        newItem.needsUpdate(true);

        this.room.addHabboItem(newItem);

        this.room.updateItem(newItem);
        this.room.updateTile(this.room.getLayout().getTile(position.x, position.y));
        this.room.sendComposer(new AddFloorItemComposer(newItem, "BattleBall").compose());

        Emulator.getThreading().run(() -> {
            new QueryDeleteHabboItem(newItem.getId()).run();
            room.sendComposer(new RemoveFloorItemComposer(newItem).compose());
            room.updateTile(room.getLayout().getTile(position.x, position.y));
            habbo.getRoomUnit().setCanWalk(true);
        }, 4000);

    }

    public THashMap<GameTeamColors, THashSet<HabboItem>> getLockedTiles(){
        return this.lockedTiles;
    }
}
