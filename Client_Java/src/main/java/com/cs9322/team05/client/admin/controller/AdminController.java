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
        System.out.println("AdminController.getAllPlayers: Called with token: " + token);
        List<Player> players = null;
        try {
            players = adminModel.getAllPlayers(token); 
            if (players == null) {
                System.out.println("AdminController.getAllPlayers: Received NULL list from adminModel.");
            } else {
                System.out.println("AdminController.getAllPlayers: Received list with " + players.size() + " players from adminModel.");
            }
        } catch (AdminNotLoggedInException e) {
            System.err.println("AdminController.getAllPlayers: Caught AdminNotLoggedInException from adminModel: " + e.getMessage() + ". Returning null.");
            
            return null; 
        } catch (Exception e) { 
            System.err.println("AdminController.getAllPlayers: Caught UNEXPECTED Exception from adminModel: " + e.toString() + ". Returning null.");
            e.printStackTrace();
            return null; 
        }
        
        if (players == null) {
            System.out.println("AdminController.getAllPlayers: Returning NULL list to AdminView.");
        } else {
            System.out.println("AdminController.getAllPlayers: Returning list with " + players.size() + " players to AdminView.");
        }
        return players;
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
        
        System.out.println("AdminController.get_waiting_time: Called with token: " + token);
        int result = 0; 
        try {
            result = adminModel.get_waiting_time(token);
            
            System.out.println("AdminController.get_waiting_time: Value from adminModel: " + result);
        } catch (AdminNotLoggedInException e) {
            
            System.err.println("AdminController.get_waiting_time: Caught AdminNotLoggedInException: " + e.getMessage() + ". Returning 0.");
            
        } catch (Exception e) {
            
            System.err.println("AdminController.get_waiting_time: Caught UNEXPECTED Exception from adminModel: " + e.toString() + ". Returning 0.");
            e.printStackTrace();
        }
        
        System.out.println("AdminController.get_waiting_time: Returning to view: " + result);
        return result;
    }

    public int get_round_duration(String token) {
        System.out.println("AdminController.get_round_duration: Called with token: " + token);
        int result = 0;
        try {
            result = adminModel.get_round_duration(token);
            System.out.println("AdminController.get_round_duration: Value from adminModel: " + result);
        } catch (AdminNotLoggedInException e) {
            System.err.println("AdminController.get_round_duration: Caught AdminNotLoggedInException: " + e.getMessage() + ". Returning 0.");
            
        } catch (Exception e) {
            System.err.println("AdminController.get_round_duration: Caught UNEXPECTED Exception from adminModel: " + e.toString() + ". Returning 0.");
            e.printStackTrace();
        }
        System.out.println("AdminController.get_round_duration: Returning to view: " + result);
        return result;
    }
}