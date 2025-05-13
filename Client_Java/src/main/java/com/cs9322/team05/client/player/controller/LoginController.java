// src/main/java/com/cs9322/team05/client/player/controller/LoginController.java
package com.cs9322.team05.client.player.controller;

import com.cs9322.team05.client.player.model.LoginModel;
import ModifiedHangman.LogInException;
import ModifiedHangman.PlayerNotLoggedInException;

import java.util.function.BiConsumer;

public class LoginController {
    private final LoginModel authModel;
    private BiConsumer<String,String> onLoginSuccess;

    public LoginController(LoginModel authModel) {
        this.authModel = authModel;
    }

    /**
     * Attempts login and fires the onLoginSuccess callback if successful.
     * @return a status message for display.
     */
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

    public void setOnLoginSuccess(BiConsumer<String,String> callback) {
        this.onLoginSuccess = callback;
    }
}
