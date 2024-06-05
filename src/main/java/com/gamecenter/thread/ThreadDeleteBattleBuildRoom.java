package com.gamecenter.thread;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ThreadDeleteBattleBuildRoom implements Runnable {

    private final Room game;

    public ThreadDeleteBattleBuildRoom(Room game) {
        this.game = game;
    }

    @Override
    public void run() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement removeFurni = connection.prepareStatement("DELETE FROM items WHERE room_id = ?")) {
                removeFurni.setInt(1, game.getId());
                removeFurni.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM rooms WHERE id = ? LIMIT 1")) {
                statement.setInt(1, game.getId());
                statement.executeUpdate();
            }

            game.preventUnloading = false;
            game.dispose();

            Emulator.getGameEnvironment().getRoomManager().uncacheRoom(game);

        } catch (SQLException e) {
            System.err.println("ThreadDeleteBattleBuildRoom encountered an error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}