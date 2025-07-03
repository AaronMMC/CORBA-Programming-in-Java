package com.client.admin.model;

import ModifiedHangman.AdminNotLoggedInException;
import ModifiedHangman.AdminService;
import ModifiedHangman.Player;
import ModifiedHangman.PlayerAlreadyExistException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AdminModel {

    private static AdminService adminServiceImpl;

    public AdminModel(AdminService adminService) {
        AdminModel.adminServiceImpl = adminService;
    }

    public void create_player(String username, String password, String token)
            throws AdminNotLoggedInException, PlayerAlreadyExistException {

        List<Player> existingPlayers = getAllPlayers(token); // assumes this is already implemented

        boolean usernameExists = existingPlayers.stream()
                .anyMatch(p -> p.username != null && p.username.equalsIgnoreCase(username));

        if (usernameExists) {
            throw new PlayerAlreadyExistException("Username '" + username + "' already exists.");
        }

        adminServiceImpl.create_player(username, password, token);
    }


    public void update_player(String username, String password, String token) throws AdminNotLoggedInException {
        adminServiceImpl.update_player(username, password, token);
    }

    public void delete_player(String username, String token) throws AdminNotLoggedInException {
        adminServiceImpl.delete_player(username, token);
    }

    public Player search_player(String keyword, String token) throws AdminNotLoggedInException {
        return adminServiceImpl.search_player(keyword, token);
    }

    public List<Player> getAllPlayers(String token) throws AdminNotLoggedInException {
        System.out.println("AdminModel.getAllPlayers: Called with token: " + token);
        if (adminServiceImpl == null) {
            System.err.println("AdminModel.getAllPlayers: CRITICAL ERROR - adminServiceImpl is NULL!");
            throw new IllegalStateException("AdminService (adminServiceImpl) has not been initialized in AdminModel.");
        }
        try {
            Player[] playersArray = adminServiceImpl.get_all_player(token);
            if (playersArray == null) {

                System.err.println("AdminModel.getAllPlayers: Received NULL Player array from adminServiceImpl. Returning empty list.");
                return Collections.emptyList();
            }
            System.out.println("AdminModel.getAllPlayers: Received Player array with " + playersArray.length + " elements from service.");


            List<Player> playerList = Arrays.asList(playersArray);
            System.out.println("AdminModel.getAllPlayers: Converted to List with " + playerList.size() + " players. Returning list.");
            return playerList;
        } catch (AdminNotLoggedInException e) {
            System.err.println("AdminModel.getAllPlayers: Rethrowing AdminNotLoggedInException from service: " + e.getMessage());
            throw e;
        } catch (org.omg.CORBA.SystemException e) {
            System.err.println("AdminModel.getAllPlayers: CORBA SystemException from adminServiceImpl: " + e);
            e.printStackTrace();

            throw new RuntimeException("CORBA communication error while getting all players.", e);
        } catch (Exception e) {
            System.err.println("AdminModel.getAllPlayers: Unexpected Exception during adminServiceImpl.get_all_player(): " + e);
            e.printStackTrace();
            throw new RuntimeException("Unexpected error while getting all players from service.", e);
        }
    }

    public void set_waiting_time(int seconds, String token) throws AdminNotLoggedInException {
        adminServiceImpl.set_waiting_time(seconds, token);
    }

    public void set_round_duration(int seconds, String token) throws AdminNotLoggedInException {
        adminServiceImpl.set_round_duration(seconds, token);
    }

    public int get_waiting_time(String token) throws AdminNotLoggedInException {

        System.out.println("AdminModel.get_waiting_time: Called with token: " + token);
        if (adminServiceImpl == null) {

            System.err.println("AdminModel.get_waiting_time: CRITICAL ERROR - adminServiceImpl is NULL!");

            throw new IllegalStateException("AdminService (adminServiceImpl) has not been initialized in AdminModel.");
        }
        try {
            int value = adminServiceImpl.get_waiting_time(token);

            System.out.println("AdminModel.get_waiting_time: Value from adminServiceImpl: " + value);
            return value;
        } catch (AdminNotLoggedInException e) {

            System.err.println("AdminModel.get_waiting_time: Rethrowing AdminNotLoggedInException from service: " + e.getMessage());
            throw e;
        } catch (org.omg.CORBA.SystemException e) {
            System.err.println("AdminModel.get_waiting_time: CORBA SystemException from adminServiceImpl: " + e);
            e.printStackTrace();
            throw new RuntimeException("CORBA communication error getting waiting time", e);
        }

    }


    public int get_round_duration(String token) throws AdminNotLoggedInException {
        System.out.println("AdminModel.get_round_duration: Called with token: " + token);
        if (adminServiceImpl == null) {
            System.err.println("AdminModel.get_round_duration: CRITICAL ERROR - adminServiceImpl is NULL!");
            throw new IllegalStateException("AdminService (adminServiceImpl) has not been initialized in AdminModel.");
        }
        try {
            int value = adminServiceImpl.get_round_duration(token);
            System.out.println("AdminModel.get_round_duration: Value from adminServiceImpl: " + value);
            return value;
        } catch (AdminNotLoggedInException e) {
            System.err.println("AdminModel.get_round_duration: Rethrowing AdminNotLoggedInException from service: " + e.getMessage());
            throw e;
        } catch (org.omg.CORBA.SystemException e) {
            System.err.println("AdminModel.get_round_duration: CORBA SystemException from adminServiceImpl: " + e);
            e.printStackTrace();
            throw new RuntimeException("CORBA communication error getting round duration", e);
        }
    }
}