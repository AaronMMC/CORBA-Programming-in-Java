package com.cs9322.team05.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WordDao {

    private static WordDao instance;
    private static Connection connection;

    private WordDao() {
        if (connection == null) {
            try {
                connection = DatabaseConnection.getConnection();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to initialize WordDao, connection error.", e);
            }
        }
    }

    public static synchronized WordDao getInstance() {
        if (instance == null) {
            instance = new WordDao();
        }
        return instance;
    }

    public String getAWord() {
        String query = "SELECT word FROM Word ORDER BY RAND() LIMIT 1";
        String word = null;

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                word = rs.getString("word");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return word;
    }
}
