package com.cs9322.team05.server.manager;

import ModifiedHangman.ClientCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private Map<String, String> userSessions; // token -> username
    private Map<String, ClientCallback> clientCallbacks; // token -> callback


    public SessionManager() {
        this.userSessions = new HashMap<>();
        this.clientCallbacks = new HashMap<>();
    }


    public void addCallback(ClientCallback clientCallback, String token) {
        clientCallbacks.put(token, clientCallback);
    }


    public String createSession(String username, String userType) {
        String token = userType + ":" + UUID.randomUUID(); // either "player:hah234ahsdh22sks....." or // "admin:ahh135hdahwq28dhai2...."
        userSessions.put(token, username);
        return token;
    }


    public void invalidateSession(String sessionId) {
        userSessions.remove(sessionId);
    }


    public boolean isSessionValid(String sessionId) {
        return userSessions.containsKey(sessionId);
    }




}
