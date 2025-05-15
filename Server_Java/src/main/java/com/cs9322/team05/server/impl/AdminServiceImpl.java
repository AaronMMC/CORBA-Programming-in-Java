package com.cs9322.team05.server.impl;

import ModifiedHangman.AdminNotLoggedInException;
import ModifiedHangman.AdminServicePOA;
import ModifiedHangman.Player;
import com.cs9322.team05.server.dao.GameDao;
import com.cs9322.team05.server.dao.UserDao;
import com.cs9322.team05.server.manager.SessionManager;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class AdminServiceImpl extends AdminServicePOA {
    private final UserDao userDao;
    private final SessionManager sessionManager;
    private final GameDao gameDao;

    public AdminServiceImpl(SessionManager sessionManager, UserDao userDao, GameDao gameDao) {
        this.userDao = userDao;
        this.sessionManager = sessionManager;
        this.gameDao = gameDao;
    }

    @Override
    public void create_player(String username, String password, String token) throws AdminNotLoggedInException {
        if (!isTokenValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to create a player.");

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        userDao.addPlayer(new Player(username, hashedPassword, 0));
    }

    @Override
    public void update_player(String username, String newPassword, String token) throws AdminNotLoggedInException {
        if (!isTokenValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to update a player.");

        String newHashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        userDao.updatePlayer(new Player(username, newHashedPassword, 0));
    }

    @Override
    public void delete_player(String username, String token) throws AdminNotLoggedInException {
        if (!isTokenValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to delete a player.");

        userDao.removePlayer(username);
    }

    @Override
    public Player search_player(String keyword, String token) throws AdminNotLoggedInException {
        if (!isTokenValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to search a player.");

        Player[] players = get_all_player(token);
        for (Player player : players)
            if (player.username.toLowerCase().contains(keyword.toLowerCase()))
                return player;

        return null;
    }

    @Override
    public Player[] get_all_player(String token) throws AdminNotLoggedInException {
        if (!isTokenValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to retrieve all players.");

        List<Player> playerList = userDao.getAllPlayers();
        return playerList.toArray(new Player[0]);
    }



    @Override
    public void set_waiting_time(int seconds, String token) throws AdminNotLoggedInException {
        if (!isTokenValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to modify the waiting time");

        gameDao.setWaitingTimeLength(seconds);
    }

    @Override
    public void set_round_duration(int seconds, String token) throws AdminNotLoggedInException {
        if (!isTokenValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to modify the round duration.");

        gameDao.setRoundLength(seconds);
    }

    @Override
    public int get_waiting_time(String token) throws AdminNotLoggedInException {
        if (!isTokenValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to retrieve the waiting time.");

        return gameDao.getCurrentWaitingTimeLength();
    }

    @Override
    public int get_round_duration(String token) throws AdminNotLoggedInException {
        if (!isTokenValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to retrieve the round duration.");

        return gameDao.getCurrentRoundLength();
    }

    private boolean isTokenValid(String token) {
        return sessionManager.isSessionValid(token);
    }
}
