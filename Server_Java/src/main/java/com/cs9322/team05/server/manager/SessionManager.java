package com.cs9322.team05.server.manager;

import ModifiedHangman.ClientCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {

    private static volatile SessionManager instance;

    private final Map<String, String> userSessions;
    private final Map<String, ClientCallback> clientCallbacks;

    private SessionManager() {
        this.userSessions = new HashMap<>();
        this.clientCallbacks = new HashMap<>();
        System.out.println("SessionManager: Initialized.");
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    public void addCallback(ClientCallback clientCallback, String token) {
        if (token == null || clientCallback == null) {
            System.out.println("SessionManager.addCallback: ERROR - Token or ClientCallback is null. Token: " + token);
            return;
        }
        String username = userSessions.get(token);
        if (username != null) {
            System.out.println("SessionManager.addCallback: Storing callback for username: " + username + " (derived from token: " + token + ")");
            clientCallbacks.put(username, clientCallback);
        } else {
            System.out.println("SessionManager.addCallback: ERROR - No username found for token: " + token + ". Callback NOT stored.");
        }
    }

    public String createSession(String username, String userType) {
        if (username == null || username.isEmpty() || userType == null || userType.isEmpty()) {
            System.out.println("SessionManager.createSession: ERROR - Username or userType is null/empty. Cannot create session.");
            return null;
        }
        String token = userType + ":" + UUID.randomUUID().toString();
        userSessions.put(token, username);
        return token;
    }

    public void invalidateSession(String token) {
        if (token == null) {
            System.out.println("SessionManager.invalidateSession: ERROR - Token is null.");
            return;
        }
        String username = userSessions.remove(token);
        if (username != null) {
            ClientCallback removedCallback = clientCallbacks.remove(username);
            if (removedCallback != null)
                System.out.println("SessionManager.invalidateSession: Removed callback for username: " + username);
            else
                System.out.println("SessionManager.invalidateSession: No callback found to remove for username: " + username);
        } else
            System.out.println("SessionManager.invalidateSession: No session found for token: " + token);
    }

    public boolean isSessionValid(String sessionId) {
        return userSessions.containsKey(sessionId);
    }

    public ClientCallback getCallback(String username) {
        return clientCallbacks.get(username);
    }

    public boolean isUserLoggedIn(String username) {
        boolean isLoggedIn = clientCallbacks.containsKey(username);
        System.out.println("SessionManager.isUserLoggedIn: Checking if user '" + username + "' is logged in (has session and callback): " + isLoggedIn);
        return isLoggedIn;
    }

    public String getUsername(String token) {
        if (token == null) return null;
        return userSessions.get(token);
    }
}