package com.cs9322.team05.server.impl;

import ModifiedHangman.AdminNotLoggedInException;
import ModifiedHangman.AdminServicePOA;
import ModifiedHangman.Player;
import com.cs9322.team05.server.dao.GameDao;
import com.cs9322.team05.server.dao.PlayerDao;
import com.cs9322.team05.server.manager.SessionManager;

import java.util.List;

public class AdminServiceImpl extends AdminServicePOA {
    private final PlayerDao playerDao;
    private final SessionManager sessionManager;
    private final GameDao gameDao;

    public AdminServiceImpl(SessionManager sessionManager, PlayerDao playerDao, GameDao gameDao) {
        this.playerDao = playerDao;
        this.sessionManager = sessionManager;
        this.gameDao = gameDao;
    }

    @Override
    public void create_player(String username, String password, String token) throws AdminNotLoggedInException {
        if (isTokenNotValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to create a player.");

        playerDao.addPlayer(new Player(username, password, 0)); // TODO : hash the password
    }

    @Override
    public void update_player(String username, String new_password, String token) throws AdminNotLoggedInException {
        if (isTokenNotValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to update a player.");


        playerDao.updatePlayer(new Player(username, new_password, 0)); // TODO : hash the password
    }

    @Override
    public void delete_player(String username, String token) throws AdminNotLoggedInException {
        if (isTokenNotValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to delete a player.");

        playerDao.removePlayer(username);
    }

    @Override
    public Player search_player(String keyword, String token) throws AdminNotLoggedInException {
        if (isTokenNotValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to search a player.");

        Player[] players = get_all_player(token);
        for (Player player : players)
            if (player.username.toLowerCase().contains(keyword.toLowerCase()))
                return player;

        return null;
    }

    @Override
    public Player[] get_all_player(String token) throws AdminNotLoggedInException {
        if (isTokenNotValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to retrieve all players.");

        List<Player> playerList = playerDao.getAllPlayers();
        return playerList.toArray(new Player[0]);
    }



    @Override
    public void set_waiting_time(int seconds, String token) throws AdminNotLoggedInException {
        if (isTokenNotValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to modify the waiting time");

        gameDao.setWaitingTimeLength(seconds);
    }

    @Override
    public void set_round_duration(int seconds, String token) throws AdminNotLoggedInException {
        if (isTokenNotValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to modify the round duration.");

        gameDao.setRoundLength(seconds);
    }

    @Override
    public int get_waiting_time(String token) throws AdminNotLoggedInException {
        if (isTokenNotValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to retrieve the waiting time.");

        return gameDao.getCurrentWaitingTimeLength();
    }

    @Override
    public int get_round_duration(String token) throws AdminNotLoggedInException {
        if (isTokenNotValid(token))
            throw new AdminNotLoggedInException("Access denied: Admin login is required to retrieve the round duration.");
        return gameDao.getCurrentRoundLength();
    }

    private boolean isTokenNotValid(String token) {
        return !sessionManager.isSessionValid(token);
    }
}
