package com.cs9322.team05.server.impl;

import ModifiedHangman.*;
import com.cs9322.team05.server.dao.PlayerDao;
import com.cs9322.team05.server.manager.SessionManager;


public class AuthenticationServiceImpl extends AuthenticationServicePOA {
    private final SessionManager sessionManager;
    private final PlayerDao playerDao;

    public AuthenticationServiceImpl(SessionManager sessionManager, PlayerDao playerDao) {
        this.sessionManager = sessionManager;
        this.playerDao = playerDao;
    }


    @Override
    public String login(String username, String password) throws LogInException { // it should throw PlayerNotFoundException
        Player player = playerDao.getPlayerByUsername(username);

        if (player == null)
            throw new LogInException("Player " + username +  " not found. ");
        if (!player.password.equals(password))
            throw new LogInException("Incorrect password. ");

        return sessionManager.createSession(username);
    }

    @Override
    public void registerCallback(ClientCallback callback, String token) throws PlayerNotLoggedInException {
        if (isTokenValid(token))
            throw new PlayerNotLoggedInException("Access denied: Player login is required to register a Callback.");

        sessionManager.addCallback(callback, token);
    }


    @Override
    public void logout(String token) throws PlayerNotLoggedInException {
        if (isTokenValid(token))
            throw new PlayerNotLoggedInException("Access denied: User login is required to log out.");

        sessionManager.invalidateSession(token);
    }

    private boolean isTokenValid(String token) {
        return sessionManager.isSessionValid(token);
    }
}
