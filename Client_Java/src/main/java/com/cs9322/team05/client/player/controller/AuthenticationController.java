package com.cs9322.team05.client.player.controller;

import ModifiedHangman.LogInException;
import ModifiedHangman.PlayerNotLoggedInException;
import com.cs9322.team05.client.player.model.AuthenticationModel;
import javafx.application.Platform;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthenticationController {
    private static final Logger logger = Logger.getLogger(AuthenticationController.class.getName());
    private final AuthenticationModel authModel;
    private BiConsumer<String, String> onLoginSuccess;
    private Consumer<String> onLoginFailure;
    private Runnable onLogoutSuccess;

    public AuthenticationController(AuthenticationModel authModel) {
        this.authModel = authModel;
    }

    public String handleLogin(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            String msg = "Username and password cannot be empty.";
            logger.warning("Login attempt: " + msg);
            if (onLoginFailure != null) Platform.runLater(() -> onLoginFailure.accept(msg));
            return msg;
        }
        try {
            logger.info("Attempting login for user: " + username);
            String token = authModel.login(username, password);
            if (token != null && !token.isEmpty()) {
                logger.info("Login successful for user: " + username);
                if (onLoginSuccess != null) {
                    Platform.runLater(() -> onLoginSuccess.accept(username, token));
                }
                return "Login successful.";
            } else {
                String msg = "Login failed: Invalid token received (null or empty).";
                logger.warning(msg + " User: " + username);
                if (onLoginFailure != null) Platform.runLater(() -> onLoginFailure.accept(msg));
                return msg;
            }
        } catch (LogInException e) {
            String msg = "Login failed: " + (e.message != null && !e.message.isEmpty() ? e.message : "Invalid credentials or server error.");
            logger.log(Level.WARNING, msg + " User: " + username, e);
            if (onLoginFailure != null) Platform.runLater(() -> onLoginFailure.accept(e.message != null ? e.message : msg));
            return msg;
        } catch (Exception e) {
            String msg = "Login error: An unexpected issue occurred. Please check server connection.";
            logger.log(Level.SEVERE, "Unexpected error during login for " + username, e);
            if (onLoginFailure != null) Platform.runLater(() -> onLoginFailure.accept(msg));
            return msg;
        }
    }

    public String handleLogout(String token) {
        if (token == null || token.isEmpty()){
            String msg = "Logout failed: No token provided.";
            logger.warning(msg);

            if (onLogoutSuccess != null) {
                Platform.runLater(onLogoutSuccess);
            }
            return msg;
        }
        try {
            logger.info("Attempting logout for token: " + token);
            authModel.logout(token);
            String msg = "Logout successful.";
            logger.info(msg + " Triggering onLogoutSuccess callback for token: " + token);
            if (onLogoutSuccess != null) {
                Platform.runLater(onLogoutSuccess);
            }
            return msg;
        } catch (PlayerNotLoggedInException e) {
            String msg = "Logout failed: " + (e.message != null ? e.message : "Session might have already expired or was invalid.");
            logger.log(Level.WARNING, msg + " Token: " + token, e);

            if (onLogoutSuccess != null) {
                Platform.runLater(onLogoutSuccess);
            }
            return msg;
        } catch (Exception e) {
            String msg = "Logout error: An unexpected issue occurred.";
            logger.log(Level.SEVERE, "Unexpected error during logout for token " + token, e);

            if (onLogoutSuccess != null) {
                Platform.runLater(onLogoutSuccess);
            }
            return msg;
        }
    }

    public void setOnLoginSuccess(BiConsumer<String, String> callback) {
        this.onLoginSuccess = callback;
    }

    public void setOnLoginFailure(Consumer<String> callback) {
        this.onLoginFailure = callback;
    }

    public void setOnLogoutSuccess(Runnable callback) {
        this.onLogoutSuccess = callback;
    }

    public AuthenticationModel getModel() {
        return authModel;
    }
}