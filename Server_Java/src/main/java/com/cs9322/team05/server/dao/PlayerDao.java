package com.cs9322.team05.server.dao;

import ModifiedHangman.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDao {
    private final DatabaseConnection databaseConnection;

    public PlayerDao(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public void addPlayer(Player player) {
        String query = " INSERT INTO players (username, password, wins) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE password = ?, wins = ? ";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, player.username);
            stmt.setString(2, player.password);
            stmt.setInt(3, player.wins);
            stmt.setString(4, player.password);
            stmt.setInt(5, player.wins);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removePlayer(String username) {
        String query = "DELETE FROM players WHERE username = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        String query = "SELECT username, password, wins FROM players";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Player player = new Player();
                player.username = rs.getString("username");
                player.password = rs.getString("password");
                player.wins = rs.getInt("wins");
                players.add(player);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }

    public Player getPlayerByUsername(String username) {
        String query = "SELECT username, password, wins FROM players WHERE username = ?";
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Player player = new Player();
                    player.username = rs.getString("username");
                    player.password = rs.getString("password");
                    player.wins = rs.getInt("wins");
                    return player;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
