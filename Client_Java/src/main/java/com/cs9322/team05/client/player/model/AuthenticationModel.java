package com.cs9322.team05.client.player.model;

import ModifiedHangman.AuthenticationService;
import ModifiedHangman.LogInException;
import ModifiedHangman.PlayerNotLoggedInException;

public class AuthenticationModel {
    private static AuthenticationService authService;
    public AuthenticationModel(AuthenticationService authService){
        AuthenticationModel.authService = authService;
    }
    public String login (String username, String password) throws LogInException {
        return authService.login(username,password);
    }
    public void logout (String token) throws PlayerNotLoggedInException {
        authService.logout(token);
    }
}