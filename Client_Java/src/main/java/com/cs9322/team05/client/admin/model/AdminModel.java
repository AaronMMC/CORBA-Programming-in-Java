    package com.cs9322.team05.client.admin.model;

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

        public boolean create_player(String username, String password, String token) {
            return adminServiceImpl.create_player(username,password,token);
        }

        public boolean update_player(String username, String password, String token) {
            return adminServiceImpl.update_player(username,password,token);
        }

        public boolean delete_player(String username, String token) {
            return adminServiceImpl.delete_player(username,token);
        }

        public Player search_player (String keyword, String token) {
            return adminServiceImpl.search_player(keyword,token);
        }

        public List<Player> getAllPlayers() {
            return adminServiceImpl.get_all_players();
        }

        public void set_waiting_time(int seconds, String token) {
            adminServiceImpl.set_waiting_time(seconds,token);
        }

        public int get_waiting_time(String token) {
            return adminServiceImpl.get_waiting_time(token);
        }

        public int get_round_duration(String token) {
            return adminServiceImpl.get_round_duration(token);
        }
    }