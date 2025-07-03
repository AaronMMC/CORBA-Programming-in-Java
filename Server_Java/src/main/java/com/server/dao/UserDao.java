package com.server.dao;

import ModifiedHangman.Player;
import com.server.model.Admin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private static UserDao instance;
    private final Connection connection;

    private UserDao(Connection connection) {
        this.connection = connection;
    }

    public static synchronized UserDao getInstance(Connection connection) {
        if (instance == null) {
            instance = new UserDao(connection);
        }
        return instance;
    }

    public static UserDao getInstance() {
        if (instance == null)
            throw new IllegalStateException("UserDao has not been initialized. Call getInstance(Connection) first.");

        return instance;
    }
    public void addPlayer(Player player) {
        String query = " INSERT INTO player (username, hashed_password, totalWins) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE hashed_password = ?, totalWins = ? ";
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
        String query = "DELETE FROM player WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        String query = "SELECT username, hashed_password, totalWins FROM player";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                Player player = new Player();
                player.username = rs.getString("username");
                player.password = rs.getString("hashed_password");
                player.wins = rs.getInt("totalWins");
                players.add(player);
                count++;
            }
        } catch (SQLException e) {
            System.err.println("UserDao.getAllPlayers: SQLException occurred: " + e.getMessage());
            e.printStackTrace(); // Crucial for seeing DB errors
        }
        return players;
    }

    public Player getPlayerByUsername(String username) {
        String query = "SELECT username, hashed_password, totalWins FROM player WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Player player = new Player();
                    player.username = rs.getString("username");
                    player.password = rs.getString("hashed_password");
                    player.wins = rs.getInt("totalWins");
                    return player;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updatePlayer(Player player) {
        String query = "UPDATE player SET hashed_password = ?, totalWins = ? WHERE username = ?";
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
        String query = "SELECT * FROM admin WHERE username = ?";
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


    public void addGameWinsOfPlayer(String username) {
        String sql = "UPDATE Player SET totalWins = totalWins + 1 WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
