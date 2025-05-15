package com.cs9322.team05.server.impl;

import ModifiedHangman.*;
import com.cs9322.team05.server.dao.UserDao;
import com.cs9322.team05.server.manager.SessionManager;
import com.cs9322.team05.server.model.Admin;
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
        if (player != null) {
            String storedHash = player.password;
            if (BCrypt.checkpw(password, storedHash)) {
                return sessionManager.createSession(username, "player");
            } else {
                throw new LogInException("Incorrect password.");
            }
        }

        Admin admin = userDao.getAdminByUsername(username);
        if (admin != null) {
            String storedHash = admin.getPassword();
            if (BCrypt.checkpw(password, storedHash)) {
                return sessionManager.createSession(username, "admin");
            } else {
                throw new LogInException("Incorrect password.");
            }
        }

        throw new LogInException("User not found.");
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
