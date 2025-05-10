package com.cs9322.team05.client.player.model;

public class PlayerSession {
    private static PlayerSession instance;
    private String username;
    private String sessionToken;


    private PlayerSession() {
    }

    public static synchronized PlayerSession getInstance() {
        if (instance == null) {
            instance = new PlayerSession();
        }
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public boolean isLoggedIn() {
        return sessionToken != null && !sessionToken.isEmpty();
    }

    public void clearSession() {
        this.username = null;
        this.sessionToken = null;
    }

    @Override
    public String toString() {
        return "PlayerSession{" +
                "username='" + username + '\'' +
                ", sessionToken='" + (sessionToken != null ? "******" : "null") + '\'' +
                '}';
    }
}
