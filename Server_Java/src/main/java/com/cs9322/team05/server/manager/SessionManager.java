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
            clientCallbacks.remove(username);
        } else {
            System.out.println("SessionManager.invalidateSession: No session found for token: " + token);
        }
    }

    public void invalidateSessionByUsername(String username) {
        if (username == null || username.isEmpty()) {
            System.out.println("SessionManager.invalidateSessionByUsername: ERROR - Username is null or empty.");
            return;
        }

        String tokenToInvalidate = null;
        for (Map.Entry<String, String> entry : userSessions.entrySet()) {
            if (username.equals(entry.getValue())) {
                tokenToInvalidate = entry.getKey();
                break;
            }
        }

        if (tokenToInvalidate != null) {
            userSessions.remove(tokenToInvalidate);
            ClientCallback callback = clientCallbacks.remove(username);
            if (callback != null) {
                try {
                    callback.notifySessionInvalidated("Your session has been invalidated by a new login on another device.");
                    System.out.println("SessionManager.invalidateSessionByUsername: Notified client " + username + " of session invalidation.");
                } catch (Exception e) {
                    System.err.println("SessionManager.invalidateSessionByUsername: Error notifying client " + username + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("SessionManager.invalidateSessionByUsername: Session invalidated for user: " + username + " (token: " + tokenToInvalidate + ")");
        }
    }

    public boolean isSessionValid(String sessionId) {
        return userSessions.containsKey(sessionId);
    }

    public ClientCallback getCallback(String username) {
        return clientCallbacks.get(username);
    }

    public boolean isUserLoggedIn(String username) {
        return userSessions.containsValue(username);
    }

    public String getUsername(String token) {
        if (token == null) {
            return null;
        }
        return userSessions.get(token);
    }
}