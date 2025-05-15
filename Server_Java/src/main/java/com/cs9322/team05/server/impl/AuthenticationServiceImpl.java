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

    // TODO : add this method in the .idl file
    public void registerCallback(ClientCallback callback, String token) {
        if (isTokenNotValid(token))
            throw new RuntimeException("You are not yet logged in sir. "); // TODO : add in the .idl file that this method throws a PlayerNotLoggedInException

        sessionManager.addCallback(callback, token);
    }


    @Override
    public void logout(String token) throws PlayerNotLoggedInException {
        if (isTokenNotValid(token))
            throw new RuntimeException("You are not yet logged in sir. "); // TODO : add in the .idl file that this method throws a PlayerNotLoggedInException

        sessionManager.invalidateSession(token);
    }

    private boolean isTokenNotValid(String token) {
        return !sessionManager.isSessionValid(token);
    }
}
