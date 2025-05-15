package com.cs9322.team05.client.admin.controller;

import ModifiedHangman.AdminNotLoggedInException;
import ModifiedHangman.Player;
import com.cs9322.team05.client.admin.model.AdminModel;

import java.util.List;

public class AdminController {

    private final AdminModel adminModel;

    public AdminController(AdminModel adminModel) {
        this.adminModel = adminModel;
    }

    public void create_player(String username, String password, String token) {
        try {
            adminModel.create_player(username,password,token);
        } catch (AdminNotLoggedInException e) {
            e.printStackTrace();
        }
    }

    public void update_player(String username, String password, String token) {
        try {
            adminModel.update_player(username,password,token);
        } catch (AdminNotLoggedInException e) {
            e.printStackTrace();
        }
    }

    public void delete_player(String username, String token) {
        try {
            adminModel.delete_player(username,token);
        } catch (AdminNotLoggedInException e) {
            e.printStackTrace();
        }
    }

    public Player search_player (String keyword, String token) {
        try {
           return adminModel.search_player(keyword,token);
        } catch (AdminNotLoggedInException e) {
           e.printStackTrace();
        }
        return null;
    }

    public List<Player> getAllPlayers(String token) {
        try {
            return adminModel.getAllPlayers(token);
        } catch (AdminNotLoggedInException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void set_waiting_time(int seconds, String token) {
        try {
            adminModel.set_waiting_time(seconds,token);
        } catch (AdminNotLoggedInException e) {
            e.printStackTrace();
        }
    }

    public void set_round_duration(int seconds, String token) {
        try {
            adminModel.set_round_duration(seconds,token);
        } catch (AdminNotLoggedInException e) {
            e.printStackTrace();
        }
    }

    public int get_waiting_time(String token) {
        try {
            return adminModel.get_waiting_time(token);
        } catch (AdminNotLoggedInException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int get_round_duration(String token) {
        try {
            return adminModel.get_round_duration(token);
        } catch (AdminNotLoggedInException e) {
            e.printStackTrace();
        }
        return 0;
    }
}