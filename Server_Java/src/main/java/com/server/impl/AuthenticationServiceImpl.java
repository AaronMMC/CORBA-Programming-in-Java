package com.server.impl;

import ModifiedHangman.*;
import com.server.dao.UserDao;
import com.server.manager.SessionManager;
import com.server.model.Admin;
import org.mindrot.jbcrypt.BCrypt;

public class AuthenticationServiceImpl extends AuthenticationServicePOA {
    private final SessionManager sessionManager;
    private final UserDao userDao;

    public AuthenticationServiceImpl(SessionManager sessionManager, UserDao userDao) {
        this.sessionManager = sessionManager;
        this.userDao = userDao;
    }

    @Override
    public String login(String username, String password) throws LogInException {
        Player player = userDao.getPlayerByUsername(username);
        if (player != null)
            return attemptLogin(username, password, player.password, "player");

        Admin admin = userDao.getAdminByUsername(username);
        if (admin != null)
            return attemptLogin(username, password, admin.getPassword(), "admin");


        throw new LogInException("User not found.");
    }

    private String attemptLogin(String username, String rawPassword, String hashedPassword, String userType) throws LogInException {
        if (!BCrypt.checkpw(rawPassword, hashedPassword))
            throw new LogInException("Incorrect password.");

        if (sessionManager.isUserLoggedIn(username))
            sessionManager.invalidateSessionByUsername(username);

        return sessionManager.createSession(username, userType);
    }

    @Override
    public void registerCallback(ClientCallback callback, String token) throws PlayerNotLoggedInException {
        if (!isTokenValid(token))
            throw new PlayerNotLoggedInException("Access denied: Player login is required to register a Callback.");

        sessionManager.addCallback(callback, token);
    }

    @Override
    public void logout(String token) throws PlayerNotLoggedInException {
        if (!isTokenValid(token))
            throw new PlayerNotLoggedInException("Access denied: User login is required to log out.");

        sessionManager.invalidateSession(token);
    }

    private boolean isTokenValid(String token) {
        return sessionManager.isSessionValid(token);
    }
}