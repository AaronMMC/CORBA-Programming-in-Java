package com.cs9322.team05.client.admin.controller;

import ModifiedHangman.Player;
import com.cs9322.team05.client.admin.model.AdminModel;

public class AdminController {

    private final AdminModel adminModel;

    public AdminController(AdminModel adminModel) {
        this.adminModel = adminModel;
    }

    public boolean create_player(String username, String password, String token) {
        return adminModel.create_player(username,password,token);
    }

    public boolean update_player(String username, String password, String token) {
        return adminModel.update_player(username,password,token);
    }

    public boolean delete_player(String username, String token) {
        return adminModel.delete_player(username,token);
    }

    //TODO: Search Player method that has questionable return type. in the AdminServiceImpl.
    public Player search_player (String keyword, String token) {
        return adminModel.search_player(keyword,token);
    }

    public void set_waiting_time(int seconds, String token) {
        adminModel.set_waiting_time(seconds,token);
    }

    public int get_waiting_time(String token) {
        return adminModel.get_waiting_time(token);
    }

    public int get_round_duration(String token) {
        return adminModel.get_round_duration(token);
    }
}