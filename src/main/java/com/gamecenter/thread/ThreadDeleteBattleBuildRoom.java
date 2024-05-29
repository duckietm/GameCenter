package com.gamecenter.thread;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ThreadDeleteBattleBuildRoom implements Runnable{

    public Room game;

    public ThreadDeleteBattleBuildRoom(Room game){
        this.game = game;
    }
    @Override
    public void run() {

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement rFurnis = connection.prepareStatement("DELETE FROM items WHERE room_id = ?")) {
                rFurnis.setInt(1, game.getId());
                rFurnis.addBatch();
            }

            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM rooms WHERE id = ? LIMIT 1")) {
                statement.setInt(1, game.getId());
                statement.addBatch();

            }

            game.preventUnloading = false;
            game.dispose();
            Emulator.getGameEnvironment().getRoomManager().uncacheRoom(game);


        } catch (SQLException e) {
            System.out.println("Thread DeleteRoom \n " + e);
        }
    }
}
