package com.cs9322.team05.server.impl;

import ModifiedHangman.AdminServicePOA;
import ModifiedHangman.Player;
import com.cs9322.team05.server.dao.PlayerDao;

import java.util.List;

public class AdminServiceImpl extends AdminServicePOA {
    @Override
    public boolean create_player(String username, String password, String token) {
        return false;
    }

    @Override
    public boolean update_player(String username, String new_password, String token) {
        return false;
    }

    @Override
    public boolean delete_player(String username, String token) {
        return false;
    }

    @Override
    public Player search_player(String keyword, String token) {
        return null;
    }//TODO: Lowkey kinda useless pede naman sa jvafx lang logic nung pagsearch need lang ng list nung mga players.

    @Override
    public List<Player> get_all_players() {
        PlayerDao playerDao = new PlayerDao();
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
