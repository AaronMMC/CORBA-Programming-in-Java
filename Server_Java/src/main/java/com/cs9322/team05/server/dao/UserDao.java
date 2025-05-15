package com.cs9322.team05.server.dao;

import ModifiedHangman.Player;
import com.cs9322.team05.server.model.Admin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final Connection connection;

    public UserDao(Connection connection) {
        this.connection = connection;
    }

    public void addPlayer(Player player) {
        String query = " INSERT INTO player (username, password, total_wins) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE password = ?, total_wins = ? ";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {

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
        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        String query = "SELECT username, password, total_wins FROM players";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Player player = new Player();
                player.username = rs.getString("username");
                player.password = rs.getString("password");
                player.wins = rs.getInt("total_wins");
                players.add(player);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }

    public Player getPlayerByUsername(String username) {
        String query = "SELECT username, password, total_wins FROM player WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Player player = new Player();
                    player.username = rs.getString("username");
                    player.password = rs.getString("password");
                    player.wins = rs.getInt("total_wins");
                    return player;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updatePlayer(Player player) {
        String query = "UPDATE player SET password = ?, total_wins = ? WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, player.password);
            stmt.setInt(2, player.wins);
            stmt.setString(3, player.username);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0)
                System.out.println("Player updated successfully.");
            else
                System.out.println("No player found with username: " + player.username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public Admin getAdminByUsername(String username) {
        String query = "SELECT username, password FROM admin WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String uname = rs.getString("username");
                String password = rs.getString("password");
                return new Admin(uname, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
