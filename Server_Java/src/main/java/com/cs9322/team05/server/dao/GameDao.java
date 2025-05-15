package com.cs9322.team05.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GameDao {
    private final DatabaseConnection databaseConnection;

    public GameDao(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public int getCurrentWaitingTimeLength() {
        String query = "SELECT waiting_time_length FROM game_settings WHERE id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("waiting_time_length");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // fallback value
    }

    public int getCurrentRoundLength() {
        String query = "SELECT round_length FROM game_settings WHERE id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("round_length");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // fallback value
    }


    public void setWaitingTimeLength(int length) {
        String query = "UPDATE game_settings SET waiting_time_length = ? WHERE id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, length);
            stmt.setInt(2, 1);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRoundLength(int length) {
        String query = "UPDATE game_settings SET round_length = ? WHERE id = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, length);
            stmt.setInt(2, 1);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




}
