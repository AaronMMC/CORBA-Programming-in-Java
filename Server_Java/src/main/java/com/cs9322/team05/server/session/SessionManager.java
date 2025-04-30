package com.cs9322.team05.server.session;

import javax.security.auth.callback.Callback;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private Map<String, String> userSessions;
    private Map<String, Callback> clientCallbacks;


    public SessionManager() {
        this.userSessions = new HashMap<>();
        this.clientCallbacks = new HashMap<>();
    }


    public String createSession(String username) {
        return null;
    }


    public void invalidateSession(String sessionId) {

    }


    public boolean isSessionValid(String sessionId) {
        return false;
    }




}
