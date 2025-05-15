package com.cs9322.team05.server.impl;

import ModifiedHangman.AdminServicePOA;
import ModifiedHangman.Player;
import com.cs9322.team05.server.dao.PlayerDao;

import java.util.List;

public class AdminServiceImpl extends AdminServicePOA {
    private final PlayerDao playerDao;

    public AdminServiceImpl(PlayerDao playerDao) {
        this.playerDao = playerDao;
    }

    @Override
    public void create_player(String username, String password, String token) {}

    @Override
    public void update_player(String username, String new_password, String token) {}

    @Override
    public void delete_player(String username, String token) {}

    @Override
    public Player search_player(String keyword, String token) {
        return null;
    }

    @Override
    public List<Player> get_all_players(String token) {
        return playerDao.getAllPlayers();
    }

    @Override
    public void set_waiting_time(int seconds, String token) {

    }

    @Override
    public void set_round_duration(int seconds, String token) {

    }

    @Override
    public int get_waiting_time(String token) {
        return 0;
    }

    @Override
    public int get_round_duration(String token) {
        return 0;
    }
}
