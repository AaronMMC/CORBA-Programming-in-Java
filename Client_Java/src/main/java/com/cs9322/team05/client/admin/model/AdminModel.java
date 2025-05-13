    package com.cs9322.team05.client.admin.model;

    import ModifiedHangman.AdminService;
    import org.omg.CORBA.*;
    import org.omg.CosNaming.*;

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

        //TODO: Search Player method that has questionable return type. in the AdminServiceImpl.

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