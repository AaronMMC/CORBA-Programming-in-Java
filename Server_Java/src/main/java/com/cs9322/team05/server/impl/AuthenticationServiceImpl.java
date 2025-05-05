package com.cs9322.team05.server.impl;

import ModifiedHangman.AuthenticationServicePOA;
import ModifiedHangman.ClientCallback;
import ModifiedHangman.LogInException;
import ModifiedHangman.PlayerNotLoggedInException;
import com.cs9322.team05.server.session.SessionManager;

import java.util.Random;

public class AuthenticationServiceImpl extends AuthenticationServicePOA {
    private SessionManager sessionManager;

    public AuthenticationServiceImpl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }


    @Override
    public String login(String username, String password) throws LogInException {
        return System.currentTimeMillis() + "-" + new Random().nextInt(10000); // to be changed since this is the same as the generation of the id of the game in the GameService
    }


    public void registerCallback(ClientCallback callback) {
        sessionManager.addCallback(callback);
    }

    @Override
    public void logout(String token) throws PlayerNotLoggedInException {

    }
}
