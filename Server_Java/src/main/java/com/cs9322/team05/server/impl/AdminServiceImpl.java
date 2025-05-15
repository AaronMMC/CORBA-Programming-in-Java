package com.cs9322.team05.server.impl;

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
    public void create_player(String username, String password, String token) {
        if (isTokenNotValid(token))
            throw new RuntimeException("You are not yet logged in sir. "); // TODO : add in the .idl file that this method throws a AdminNotLoggedInException

        playerDao.addPlayer(new Player(username, password)); // TODO : hash the password
    }

    @Override
    public void update_player(String username, String new_password, String token) {
        if (isTokenNotValid(token))
            throw new RuntimeException("You are not yet logged in sir. "); // TODO : add in the .idl file that this method throws a AdminNotLoggedInException

        playerDao.updatePlayer(new Player(username, new_password)); // TODO : hash the password
    }

    @Override
    public void delete_player(String username, String token) {
        if (isTokenNotValid(token))
            throw new RuntimeException("You are not yet logged in sir. "); // TODO : add in the .idl file that this method throws a AdminNotLoggedInException

        playerDao.removePlayer(username);
    }

    @Override
    public Player search_player(String keyword, String token) {
        if (isTokenNotValid(token))
            throw new RuntimeException("You are not yet logged in sir. "); // TODO : add in the .idl file that this method throws a AdminNotLoggedInException

        List<Player> players = get_all_players(token);
        for (Player player : players)
            if (player.getUsername().toLowerCase().contains(keyword.toLowerCase()))
                return player;

        return null;
    }

    @Override
    public List<Player> get_all_players(String token) {
        if (isTokenNotValid(token))
            throw new RuntimeException("You are not yet logged in sir. "); // TODO : add in the .idl file that this method throws a AdminNotLoggedInException

        return playerDao.getAllPlayers();
    }

    @Override
    public void set_waiting_time(int seconds, String token) {
        if (isTokenNotValid(token))
            throw new RuntimeException("You are not yet logged in sir. "); // TODO : add in the .idl file that this method throws a AdminNotLoggedInException

        gameDao.setWaitingTimeLength(seconds);
    }

    @Override
    public void set_round_duration(int seconds, String token) {
        if (isTokenNotValid(token))
            throw new RuntimeException("You are not yet logged in sir. "); // TODO : add in the .idl file that this method throws a AdminNotLoggedInException

        gameDao.setRoundLength(seconds);
    }

    @Override
    public int get_waiting_time(String token) {
        if (isTokenNotValid(token))
            throw new RuntimeException("You are not yet logged in sir. "); // TODO : add in the .idl file that this method throws a AdminNotLoggedInException

        return gameDao.getCurrentWaitingTimeLength();
    }

    @Override
    public int get_round_duration(String token) {
        if (isTokenNotValid(token))
            throw new RuntimeException("You are not yet logged in sir. "); // TODO : add in the .idl file that this method throws a AdminNotLoggedInException

        return gameDao.getCurrentRoundLength();
    }

    private boolean isTokenNotValid(String token) {
        return !sessionManager.isSessionValid(token);
    }
}
