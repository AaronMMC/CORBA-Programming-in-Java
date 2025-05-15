package com.cs9322.team05.client.player.model;

import ModifiedHangman.AuthenticationService;
import ModifiedHangman.LogInException;
import ModifiedHangman.PlayerNotLoggedInException;

public class AuthenticationModel {
    private static AuthenticationService authServiceImpl;
    public AuthenticationModel(AuthenticationService authService){
        AuthenticationModel.authServiceImpl = authService;
    }
    public String login (String username, String password) throws LogInException {
        return authServiceImpl.login(username,password);
    }
    public void logout (String token) throws PlayerNotLoggedInException {
        authServiceImpl.logout(token);
    }
}