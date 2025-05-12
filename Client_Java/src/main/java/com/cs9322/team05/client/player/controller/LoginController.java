package com.cs9322.team05.client.player.controller;

import com.cs9322.team05.client.player.model.LoginModel;
import ModifiedHangman.LogInException;
import ModifiedHangman.PlayerNotLoggedInException;

public class LoginController {
    private LoginModel authModel;

    public LoginController(LoginModel authModel) {
        this.authModel = authModel;
    }

    public String handleLogin(String username, String password) {
        try {
            String token = authModel.login(username, password);
            if (token != null && !token.isEmpty()) {
                return "Login successful. Token: " + token;
            } else {
                return "Login failed: Invalid token received.";
            }
        } catch (LogInException e) {
            return "Login failed: " + e.getMessage();
        }
    }

    public String handleLogout(String token) {
        try {
            authModel.logout(token);
            return "Logout successful.";
        } catch (PlayerNotLoggedInException e) {
            return "Logout failed: " + e.getMessage();
        }
    }
}
