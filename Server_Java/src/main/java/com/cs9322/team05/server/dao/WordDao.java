package com.cs9322.team05.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WordDao {

    private static WordDao instance;
    private static Connection connection;

    private WordDao(Connection connection) {
        if (WordDao.connection == null)
            WordDao.connection = connection;

    }

    public static synchronized WordDao getInstance(Connection connection) {
        if (instance == null)
            instance = new WordDao(connection);

        return instance;
    }


    public static synchronized WordDao getInstance() {
        if (instance == null)
            throw new RuntimeException("No connection established here. ");

        return instance;
    }



    public String getAWord() {
        String query = "SELECT word_text FROM Word ORDER BY RAND() LIMIT 1";
        String word = null;

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                word = rs.getString("word_text");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return word;
    }
}
