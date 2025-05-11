package com.cs9322.team05.client.admin.model;

import ModifiedHangman.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;

public class AdminModel {

    private static AdminService adminServiceImpl;

    public AdminModel(AdminService adminService) {
        this.adminService = adminService;
    }

    public boolean create_player(String username, String password, String token) {
        return adminServiceImpl.create_player(String username, String password, String token);
    }

    public boolean update_player(String username, String password, String token) {
        return adminServiceImpl.update_player(String username, String new_password, String token);
    }

    public boolean delete_player(String username, String token) {
        return adminServiceImpl.delete_player(String username, String token);
    }

    //TODO: Search Player method that has questionable return type. in the AdminServiceImpl.

    public void set_waiting_time(int seconds, String token) {
        adminServiceImpl.set_waiting_time(int seconds, String token);
    }

    public int get_waiting_time(String token) {
        adminServiceImpl.get_waiting_time(String token);
    }

    public int get_round_duration(String token) {
        adminServiceImpl.get_round_duration(String token);
    }
}