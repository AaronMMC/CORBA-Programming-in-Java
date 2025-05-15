package com.cs9322.team05.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GameDao {
    private final Connection connection;

    public GameDao(Connection connection) {
        this.connection = connection;
    }

    public int getCurrentWaitingTimeLength() {
        String query = "SELECT waiting_time_length FROM game_settings WHERE id = ?";
        // ADD Log: Indicate which ID is being queried if it's not always 1 in the future.
        System.out.println("GameDao: Querying game_settings for waiting_time_length with id = 1");
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, 1); // Hardcoded to 1
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int value = rs.getInt("waiting_time_length");
                    // ADD Log: Value found
                    System.out.println("GameDao: Found waiting_time_length: " + value + " for id = 1");
                    return value;
                } else {
                    // ADD Log: ID not found
                    System.err.println("GameDao: No record found in game_settings for id = 1. Returning fallback 0.");
                }
            }
        } catch (SQLException e) {
            // MODIFY Log: More specific error
            System.err.println("GameDao: SQLException fetching waiting_time_length for id = 1: " + e.getMessage());
            e.printStackTrace();
        }
        return 0; // fallback value
    }

    public int getCurrentRoundLength() {
        String query = "SELECT round_length FROM game_settings WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {

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
        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, length);
            stmt.setInt(2, 1);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRoundLength(int length) {
        String query = "UPDATE game_settings SET round_length = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, length);
            stmt.setInt(2, 1);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




}
