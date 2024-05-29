package com.gamecenter.utils.models;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboItem;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomObject {
    public final String name;

    public final String description;

    public final String model;

    public final String password;

    public final int usersMax;

    public final String paperFloor;

    public final String paperWall;

    public final String paperLandscape;

    public final int thicknessWall;

    public final int wallHeight;

    public final int thicknessFloor;

    public final String moodlightData;

    public final int rollerSpeed;

    public final RoomModelObject modelObject;

    public final FurniObject[] items;

    private transient int newRoomId;

    public transient THashMap<String, Integer> missingFurnitureMap = new THashMap<>();

    private transient int ownerId;

    private transient String ownerName;

    private final Logger LOGGER = LoggerFactory.getLogger("CreateRoomGame");

    public RoomObject(Room room) {
        this.name = room.getName();
        this.description = room.getDescription();
        this.model = room.getLayout().getName();
        this.password = room.getPassword();
        this.usersMax = 12;
        this.paperFloor = room.getFloorPaint();
        this.paperWall = room.getWallPaint();
        this.paperLandscape = room.getBackgroundPaint();
        this.thicknessWall = room.getWallSize();
        this.wallHeight = room.getWallHeight();
        this.thicknessFloor = room.getFloorSize();
        this.moodlightData = "";
        this.rollerSpeed = room.getRollerSpeed();
        if (room.hasCustomLayout()) {
            this.modelObject = new RoomModelObject(room.getLayout().getDoorX(), room.getLayout().getDoorY(), room.getLayout().getDoorDirection(), room.getLayout().getHeightmap());
        } else {
            this.modelObject = null;
        }
        THashSet<HabboItem> roomItems = room.getFloorItems();
        roomItems.addAll(room.getWallItems());
        this.items = new FurniObject[roomItems.size()];
        int index = 0;
        for (HabboItem item : roomItems) {
            this.items[index] = new FurniObject(item);
            index++;
        }
    }

    public int insertBattleRoom(HabboInfo info) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement insertRoomStatement = connection.prepareStatement("INSERT INTO rooms (name, description, model, password, users_max, paper_floor, paper_wall, paper_landscape, thickness_wall, wall_height, thickness_floor, moodlight_data, roller_speed, owner_id, owner_name, override_model) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1)) {
                insertRoomStatement.setString(1, "battlebuild-" + info.getUsername());
                insertRoomStatement.setString(2, this.description);
                insertRoomStatement.setString(3, this.model);
                insertRoomStatement.setString(4, this.password);
                insertRoomStatement.setInt(5, this.usersMax);
                insertRoomStatement.setString(6, this.paperFloor);
                insertRoomStatement.setString(7, this.paperWall);
                insertRoomStatement.setString(8, this.paperLandscape);
                insertRoomStatement.setInt(9, this.thicknessWall);
                insertRoomStatement.setInt(10, this.wallHeight);
                insertRoomStatement.setInt(11, this.thicknessFloor);
                insertRoomStatement.setString(12, this.moodlightData);
                insertRoomStatement.setInt(13, this.rollerSpeed);
                insertRoomStatement.setInt(14, info.getId());
                insertRoomStatement.setString(15, info.getUsername());
                insertRoomStatement.setString(16, overrideModel() ? "1" : "0");
                insertRoomStatement.execute();
                try (ResultSet set = insertRoomStatement.getGeneratedKeys()) {
                    if (set.next())
                        this.newRoomId = set.getInt(1);
                }
            }
            if (overrideModel())
                try (PreparedStatement insertCustomModelStatement = connection.prepareStatement("INSERT INTO room_models_custom (id, name, door_x, door_y, door_dir, heightmap) VALUES (?, ?, ?, ?, ?, ?)")) {
                    insertCustomModelStatement.setInt(1, this.newRoomId);
                    insertCustomModelStatement.setString(2, "custom_" + this.newRoomId);
                    insertCustomModelStatement.setInt(3, this.modelObject.doorX);
                    insertCustomModelStatement.setInt(4, this.modelObject.doorY);
                    insertCustomModelStatement.setInt(5, this.modelObject.doorDir);
                    insertCustomModelStatement.setString(6, this.modelObject.heightMap);
                    insertCustomModelStatement.execute();
                }
        } catch (Exception e) {
            LOGGER.error("RoomObjectCatch", e);
        }

        return this.newRoomId;
    }

    public int insertRoom() {
        this.ownerId = 1;
        this.ownerName = "Bottttttttttt";
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement insertRoomStatement = connection.prepareStatement("INSERT INTO rooms (name, description, model, password, users_max, paper_floor, paper_wall, paper_landscape, thickness_wall, wall_height, thickness_floor, moodlight_data, roller_speed, owner_id, owner_name, override_model) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1)) {
                insertRoomStatement.setString(1, this.name);
                insertRoomStatement.setString(2, this.description);
                insertRoomStatement.setString(3, this.model);
                insertRoomStatement.setString(4, this.password);
                insertRoomStatement.setInt(5, this.usersMax);
                insertRoomStatement.setString(6, this.paperFloor);
                insertRoomStatement.setString(7, this.paperWall);
                insertRoomStatement.setString(8, this.paperLandscape);
                insertRoomStatement.setInt(9, this.thicknessWall);
                insertRoomStatement.setInt(10, this.wallHeight);
                insertRoomStatement.setInt(11, this.thicknessFloor);
                insertRoomStatement.setString(12, this.moodlightData);
                insertRoomStatement.setInt(13, this.rollerSpeed);
                insertRoomStatement.setInt(14, this.ownerId);
                insertRoomStatement.setString(15, this.ownerName);
                insertRoomStatement.setString(16, overrideModel() ? "1" : "0");
                insertRoomStatement.execute();
                try (ResultSet set = insertRoomStatement.getGeneratedKeys()) {
                    if (set.next())
                        this.newRoomId = set.getInt(1);
                }
            }
            if (overrideModel())
                try (PreparedStatement insertCustomModelStatement = connection.prepareStatement("INSERT INTO room_models_custom (id, name, door_x, door_y, door_dir, heightmap) VALUES (?, ?, ?, ?, ?, ?)")) {
                    insertCustomModelStatement.setInt(1, this.newRoomId);
                    insertCustomModelStatement.setString(2, "custom_" + this.newRoomId);
                    insertCustomModelStatement.setInt(3, this.modelObject.doorX);
                    insertCustomModelStatement.setInt(4, this.modelObject.doorY);
                    insertCustomModelStatement.setInt(5, this.modelObject.doorDir);
                    insertCustomModelStatement.setString(6, this.modelObject.heightMap);
                    insertCustomModelStatement.execute();
                }
        } catch (Exception e) {
            LOGGER.error("RoomObjectCatch", e);
        }

        return this.newRoomId;
    }

    public void insertFurniture() {
        THashMap<Integer, Integer> originalToNewFurniIdMap = new THashMap<>();
        THashMap<Integer, Integer> newToOriginalFurniIdMap = new THashMap<>();
        try(Connection connection = Emulator.getDatabase().getDataSource().getConnection();

            PreparedStatement statement = connection.prepareStatement("INSERT INTO items (user_id, room_id, item_id, wall_pos, x, y, z, rot, extra_data, wired_data, limited_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1)) {
            List<Integer> itemIds = new ArrayList<>();
            for (FurniObject object : this.items) {
                Item baseItem = Emulator.getGameEnvironment().getItemManager().getItem(object.name);
                if (baseItem == null) {
                    if (!this.missingFurnitureMap.containsKey(object.name))
                        this.missingFurnitureMap.put(object.name, 0);
                    this.missingFurnitureMap.put(object.name, ((Integer) this.missingFurnitureMap.get(object.name)).intValue() + 1);
                } else {
                    statement.setInt(1, this.ownerId);
                    statement.setInt(2, this.newRoomId);
                    statement.setInt(3, baseItem.getId());
                    statement.setString(4, object.wallPosition);
                    statement.setInt(5, object.x);
                    statement.setInt(6, object.y);
                    statement.setDouble(7, object.z);
                    statement.setInt(8, object.rotation);
                    statement.setString(9, object.extradata);
                    statement.setString(10, "");
                    statement.setString(11, object.limitedData);
                    statement.addBatch();
                    itemIds.add(object.id);
                }
            }
            statement.executeLargeBatch();
            try (ResultSet set = statement.getGeneratedKeys()) {
                int count = 0;
                while (set.next()) {
                    originalToNewFurniIdMap.put(itemIds.get(count), set.getInt(1));
                    newToOriginalFurniIdMap.put(set.getInt(1), itemIds.get(count));
                    count++;
                }
            }
        } catch (Exception e) {
            LOGGER.error("RoomObjectCatch", e);
        }
    }
    public int getNewRoomId() {
        return this.newRoomId;
    }

    private boolean overrideModel() {
        return (this.modelObject != null && !this.modelObject.heightMap.isEmpty());
    }
}
