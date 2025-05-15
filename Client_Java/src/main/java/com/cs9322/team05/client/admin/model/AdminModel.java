    package com.cs9322.team05.client.admin.model;

    import ModifiedHangman.AdminNotLoggedInException;
    import ModifiedHangman.AdminService;
    import ModifiedHangman.Player;
    import org.omg.CORBA.*;
    import org.omg.CosNaming.*;

    import java.util.List;

    public class AdminModel {

        private static AdminService adminServiceImpl;

        public AdminModel(AdminService adminService) {
            AdminModel.adminServiceImpl = adminService;
        }

        public void create_player(String username, String password, String token) throws AdminNotLoggedInException {
            adminServiceImpl.create_player(username,password,token);
        }

        public void update_player(String username, String password, String token) throws AdminNotLoggedInException {
            adminServiceImpl.update_player(username,password,token);
        }

        public void delete_player(String username, String token) throws AdminNotLoggedInException {
            adminServiceImpl.delete_player(username,token);
        }

        public Player search_player (String keyword, String token) throws AdminNotLoggedInException {
            return adminServiceImpl.search_player(keyword,token);
        }

        public List<Player> getAllPlayers(String token) {
            //return adminServiceImpl.get_all_players(token);
            return null;
        }

        public void set_waiting_time(int seconds, String token) throws AdminNotLoggedInException {
            adminServiceImpl.set_waiting_time(seconds,token);
        }

        public void set_round_duration(int seconds, String token) throws AdminNotLoggedInException {
            adminServiceImpl.set_round_duration(seconds,token);
        }

        public int get_waiting_time(String token) throws AdminNotLoggedInException {
            return adminServiceImpl.get_waiting_time(token);
        }

        public int get_round_duration(String token) throws AdminNotLoggedInException {
            return adminServiceImpl.get_round_duration(token);
        }
    }