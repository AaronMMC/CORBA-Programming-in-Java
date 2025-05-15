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
    public String login(String username, String password) throws LogInException { // it should throw PlayerNotFoundException
        Player player = userDao.getPlayerByUsername(username);
        if (player != null) {
            if (BCrypt.checkpw(password, player.password))
                return sessionManager.createSession(username, "player");
            else
                throw new LogInException("Incorrect password. Please try again.");
        }

        Admin admin = userDao.getAdminByUsername(username);
        if (admin != null)
            if (admin.getPassword().equals(password))
                return sessionManager.createSession(username, "admin");
            else
                throw new LogInException("Incorrect password. Please try again.");

        throw new LogInException("User not found. Please try again.");
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
