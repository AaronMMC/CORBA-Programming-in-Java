package com.server.impl;

import ModifiedHangman.AdminNotLoggedInException;
import ModifiedHangman.AdminServicePOA;
import ModifiedHangman.Player;
import com.server.dao.GameDao;
import com.server.dao.UserDao;
import com.server.manager.SessionManager;
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
        System.out.println("AdminServiceImpl.get_all_player: Called with token: " + token);
        if (!isTokenValid(token)) {
            System.err.println("AdminServiceImpl.get_all_player: TOKEN INVALID: " + token + ". Throwing AdminNotLoggedInException.");
            throw new AdminNotLoggedInException("Access denied: Admin login is required to retrieve all players.");
        }

        List<Player> playerList = userDao.getAllPlayers(); // Calls the DAO method above
        if (playerList == null) { // Should ideally not be null from your DAO
            System.err.println("AdminServiceImpl.get_all_player: UserDao returned NULL list! This is unexpected. Returning empty array.");
            return new Player[0];
        }
        System.out.println("AdminServiceImpl.get_all_player: Received " + playerList.size() + " players from UserDao.");
        // Optional: Log details of players received if suspecting data corruption
        // for(Player p : playerList) { System.out.println("AdminServiceImpl: Player from DAO - " + p.username); }


        Player[] playersArray = playerList.toArray(new Player[0]);
        System.out.println("AdminServiceImpl.get_all_player: Returning Player array with " + playersArray.length + " players.");
        return playersArray;
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
        // ADD Log: Entry point
        System.out.println("AdminServiceImpl.get_waiting_time: Called with token: " + token);
        if (!isTokenValid(token)) {
            System.err.println("AdminServiceImpl.get_waiting_time: TOKEN INVALID: " + token);
            throw new AdminNotLoggedInException("Access denied: Admin login is required.");
        }
        int value = gameDao.getCurrentWaitingTimeLength();
        // This log is already good:
        System.out.println("AdminServiceImpl.get_waiting_time: Value from DAO: " + value);
        return value;
    }

    @Override
    public int get_round_duration(String token) throws AdminNotLoggedInException {
        // ADD Log: Entry point
        System.out.println("AdminServiceImpl.get_round_duration: Called with token: " + token);
        if (!isTokenValid(token)) {
            // ADD Log: Token invalid
            System.err.println("AdminServiceImpl.get_round_duration: TOKEN INVALID: " + token);
            throw new AdminNotLoggedInException("Access denied: Admin login is required to retrieve the round duration.");
        }
        int value = gameDao.getCurrentRoundLength();
        // ADD Log: Value from DAO
        System.out.println("AdminServiceImpl.get_round_duration: Value from DAO: " + value);
        return value;
    }

    private boolean isTokenValid(String token) {
        return sessionManager.isSessionValid(token);
    }
}
