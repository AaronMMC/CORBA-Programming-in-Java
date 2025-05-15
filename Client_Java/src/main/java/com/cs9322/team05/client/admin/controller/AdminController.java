package com.cs9322.team05.client.admin.controller;

import ModifiedHangman.Player;
import com.cs9322.team05.client.admin.model.AdminModel;

import java.util.List;

public class AdminController {

    private final AdminModel adminModel;

    public AdminController(AdminModel adminModel) {
        this.adminModel = adminModel;
    }

    public void create_player(String username, String password, String token) {
        adminModel.create_player(username,password,token);
    }

    public void update_player(String username, String password, String token) {
        adminModel.update_player(username,password,token);
    }

    public void delete_player(String username, String token) {
        adminModel.delete_player(username,token);
    }

    public Player search_player (String keyword, String token) {
        return adminModel.search_player(keyword,token);
    }

    public List<Player> getAllPlayers(String token) {
        return adminModel.getAllPlayers(token);
    }

    public void set_waiting_time(int seconds, String token) {
        adminModel.set_waiting_time(seconds,token);
    }

    public void set_round_duration(int seconds, String token) {
        adminModel.set_round_duration(seconds,token);
    }

    public int get_waiting_time(String token) {
        return adminModel.get_waiting_time(token);
    }

    public int get_round_duration(String token) {
        return adminModel.get_round_duration(token);
    }
}