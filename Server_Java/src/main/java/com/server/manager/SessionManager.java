package com.server.manager;

import ModifiedHangman.ClientCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Singleton class that manages user sessions and associated client callbacks.
 */
public class SessionManager {

    private static volatile SessionManager instance;

    // Maps session tokens to usernames.
    private final Map<String, String> userSessions;
    // Maps usernames to client callbacks for server push communication.
    private final Map<String, ClientCallback> clientCallbacks;

    private SessionManager() {
        this.userSessions = new HashMap<>();
        this.clientCallbacks = new HashMap<>();
        System.out.println("SessionManager: Initialized.");
    }

    /**
     * Singleton Accessor
     */
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


    /**
     * Adds a client callback for a given session token if the token is valid.
     */
    public void addCallback(ClientCallback clientCallback, String token) {
        if (token == null || clientCallback == null) {
            System.out.println("SessionManager.addCallback: ERROR - Token or ClientCallback is null. Token: " + token);
            return;
        }

        String username = userSessions.get(token);
        if (username != null) {
            System.out.println("SessionManager.addCallback: Storing callback for username: " + username + " (derived from token: " + token + ")");
            clientCallbacks.put(username, clientCallback);
        }
        else
            System.out.println("SessionManager.addCallback: ERROR - No username found for token: " + token + ". Callback NOT stored.");
    }


    /**
     * Creates a new session for the user, generating a token based on userType and UUID.
     */
    public String createSession(String username, String userType) {
        if (username == null || username.isEmpty() || userType == null || userType.isEmpty()) {
            System.out.println("SessionManager.createSession: ERROR - Username or userType is null/empty. Cannot create session.");
            return null;
        }

        // Token format includes userType to distinguish between user roles (e.g., admin, player)
        String token = userType + ":" + UUID.randomUUID().toString();
        userSessions.put(token, username);
        return token;
    }


    /**
     * Invalidates a session given its token. Removes user session and associated callback.
     */
    public void invalidateSession(String token) {
        if (token == null) {
            System.out.println("SessionManager.invalidateSession: ERROR - Token is null.");
            return;
        }

        // Remove session and associated callback if any
        String username = userSessions.remove(token);
        if (username != null)
            clientCallbacks.remove(username);
        else
            System.out.println("SessionManager.invalidateSession: No session found for token: " + token);

    }


    /**
     * Invalidates a session by username, used for forced logout scenarios like new device login.
     */
    public void invalidateSessionByUsername(String username) {
        if (username == null || username.isEmpty()) {
            System.out.println("SessionManager.invalidateSessionByUsername: ERROR - Username is null or empty.");
            return;
        }

        String tokenToInvalidate = null;

        // Find the token linked to this username
        for (Map.Entry<String, String> entry : userSessions.entrySet())
            if (username.equals(entry.getValue())) {
                tokenToInvalidate = entry.getKey();
                break;
            }

        // Remove and notify the client callback if present
        if (tokenToInvalidate != null) {
            userSessions.remove(tokenToInvalidate);
            ClientCallback callback = clientCallbacks.remove(username);
            if (callback != null)
                try {
                    callback.notifySessionInvalidated("Your session has been invalidated by a new login on another device.");
                    System.out.println("SessionManager.invalidateSessionByUsername: Notified client " + username + " of session invalidation.");
                } catch (Exception e) {
                    System.err.println("SessionManager.invalidateSessionByUsername: Error notifying client " + username + ": " + e.getMessage());
                    e.printStackTrace();
                }

            System.out.println("SessionManager.invalidateSessionByUsername: Session invalidated for user: " + username + " (token: " + tokenToInvalidate + ")");
        }
    }

    /**
     * Checks if a session is currently valid (i.e., token exists).
     */
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