package com.cs9322.team05.client.player.controller;

import ModifiedHangman.LogInException;
import ModifiedHangman.PlayerNotLoggedInException;
import com.cs9322.team05.client.player.model.AuthenticationModel;

import java.util.function.BiConsumer;

public class AuthenticationController {
    private final AuthenticationModel authModel;
    private BiConsumer<String, String> onLoginSuccess;

    public AuthenticationController(AuthenticationModel authModel) {
        this.authModel = authModel;
    }

    public String handleLogin(String username, String password) {
        try {
            String token = authModel.login(username, password);
            if (token != null && !token.isEmpty()) {
                if (onLoginSuccess != null) {
                    onLoginSuccess.accept(username, token);
                }
                return "Login successful.";
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

    public void setOnLoginSuccess(BiConsumer<String, String> callback) {
        this.onLoginSuccess = callback;
    }
}
