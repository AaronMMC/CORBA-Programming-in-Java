package com.cs9322.team05.server.manager;

import ModifiedHangman.ClientCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {

    private static volatile SessionManager instance;

    private final Map<String, String> userSessions; // token -> username
    private final Map<String, ClientCallback> clientCallbacks; // username -> callback

    private SessionManager() {
        this.userSessions = new HashMap<>();
        this.clientCallbacks = new HashMap<>();
    }

    public static SessionManager getInstance() {
        if (instance == null)
            synchronized (SessionManager.class) {
                if (instance == null)
                    instance = new SessionManager();
            }
        return instance;
    }

    public void addCallback(ClientCallback clientCallback, String token) {
        String username = userSessions.get(token);
        clientCallbacks.put(username, clientCallback);
    }

    public String createSession(String username, String userType) {
        String token = userType + ":" + UUID.randomUUID(); // either "player:hah234ahsdh22sks....." or // "admin:ahh135hdahwq28dhai2...."
        userSessions.put(token, username);
        return token;
    }

    public void invalidateSession(String token) {
        String username = userSessions.get(token);
        userSessions.remove(token);
        clientCallbacks.remove(username);
    }

    public boolean isSessionValid(String sessionId) {
        return userSessions.containsKey(sessionId);
    }

    public ClientCallback getCallback(String username) {
        return clientCallbacks.get(username);
    }
}
