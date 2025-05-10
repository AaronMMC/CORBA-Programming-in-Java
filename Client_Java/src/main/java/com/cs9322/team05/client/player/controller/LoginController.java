package com.cs9322.team05.client.player.controller;

import ModifiedHangman.AuthenticationService;
import ModifiedHangman.LogInException;
import ModifiedHangman.PlayerNotLoggedInException;
import com.cs9322.team05.client.player.interfaces.ILoginView;
import com.cs9322.team05.client.player.model.PlayerSession;

public class LoginController {

    private final ILoginView view;
    private final AuthenticationService authService;
    private final PlayerSession playerSession;


    public LoginController(ILoginView view, AuthenticationService authService) {
        this.view = view;
        this.authService = authService;
        this.playerSession = PlayerSession.getInstance();
    }


    public void handleLoginAttempt() {
        if (view == null) {
            System.err.println("LoginView is not set in LoginController.");
            return;
        }

        String username = view.getUsername();
        String password = view.getPassword();

        if (username.isEmpty() || password.isEmpty()) {
            view.showMessage("Username and Password cannot be empty.", true);
            return;
        }


        if (authService == null) {
            System.out.println("Controller: authService is null. Simulating login for GUI testing.");

            if (username.equals("player") && password.equals("password")) {
                playerSession.setUsername(username);
                playerSession.setSessionToken("fake-test-token");
                view.showMessage("Login successful! (Simulated - No Server)", false);
                System.out.println("Simulated login successful. User: " + username);
            } else {
                view.showLoginError("Invalid credentials. (Simulated - No Server)");
                playerSession.clearSession();
                view.clearFields();
            }
            return;
        }


        try {
            System.out.println("Controller: Attempting login for " + username + " via CORBA.");
            String sessionToken = authService.login(username, password);

            if (sessionToken != null && !sessionToken.isEmpty()) {
                playerSession.setUsername(username);
                playerSession.setSessionToken(sessionToken);
                view.showMessage("Login successful! Welcome " + username + ".", false);
                System.out.println("Login successful. Token: " + sessionToken);

            } else {

                view.showLoginError("Login failed: Received an invalid response from server.");
                playerSession.clearSession();
                view.clearFields();
            }

        } catch (LogInException e) {
            System.err.println("Login Exception from server: " + e);

            view.showLoginError("Login failed: Invalid username/password or account issue.");
            playerSession.clearSession();
            view.clearFields();
        } catch (org.omg.CORBA.COMM_FAILURE e) {
            System.err.println("CORBA Communication Failure during login: " + e);
            e.printStackTrace();
            view.showLoginError("Cannot connect to server. Please try again later.");
            playerSession.clearSession();
        } catch (org.omg.CORBA.SystemException e) {
            System.err.println("CORBA System Exception during login: " + e);
            e.printStackTrace();
            view.showLoginError("A system error occurred. Please try again later.");
            playerSession.clearSession();
        } catch (Exception e) {
            System.err.println("Unexpected error during login: " + e);
            e.printStackTrace();
            view.showLoginError("An unexpected error occurred. Please try again.");
            playerSession.clearSession();
        }
    }


    public void handleLogout() {
        if (playerSession.isLoggedIn()) {
            String tokenToLogout = playerSession.getSessionToken();
            System.out.println("Controller: Attempting logout for token: " + tokenToLogout);

            if (authService != null && !"fake-test-token".equals(tokenToLogout)) {
                try {
                    authService.logout(tokenToLogout);
                    System.out.println("Logout call to server successful for token: " + tokenToLogout);
                } catch (PlayerNotLoggedInException e) {
                    System.err.println("Logout failed on server: Player was not considered logged in. " + e);
                } catch (org.omg.CORBA.COMM_FAILURE e) {
                    System.err.println("CORBA Communication Failure during logout: " + e);
                    e.printStackTrace();
                } catch (org.omg.CORBA.SystemException e) {
                    System.err.println("CORBA System Exception during logout: " + e);
                    e.printStackTrace();
                } catch (Exception e) {
                    System.err.println("Unexpected error during server logout: " + e);
                    e.printStackTrace();
                }
            } else if (authService == null) {
                System.out.println("Controller: authService is null. Skipping server logout call.");
            } else if ("fake-test-token".equals(tokenToLogout)) {
                System.out.println("Controller: Logged in with fake token. Skipping server logout call.");
            }


            playerSession.clearSession();
            if (view != null) {
                view.showMessage("You have been logged out.", false);
                view.clearFields();

            }
            System.out.println("Client session cleared.");

        } else {
            if (view != null) {
                view.showMessage("You are not currently logged in.", true);
            }
            System.out.println("Controller: No active session to logout.");
        }
    }
}
