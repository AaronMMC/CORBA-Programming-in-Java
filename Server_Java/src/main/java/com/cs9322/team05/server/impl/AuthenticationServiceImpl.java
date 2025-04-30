package com.cs9322.team05.server.impl;

import ModifiedHangman.AuthenticationServicePOA;
import ModifiedHangman.LogInException;
import ModifiedHangman.PlayerNotLoggedInException;

public class AuthenticationServiceImpl extends AuthenticationServicePOA {
    @Override
    public String login(String username, String password) throws LogInException {
        return "";
    }

    @Override
    public void logout(String token) throws PlayerNotLoggedInException {

    }
}
