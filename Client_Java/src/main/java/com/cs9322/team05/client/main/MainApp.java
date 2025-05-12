package com.cs9322.team05.client.main;  // or your correct package

import com.cs9322.team05.client.player.controller.LoginController;
import com.cs9322.team05.client.player.model.LoginModel;
import com.cs9322.team05.client.player.view.LoginView;
//import com.cs9322.team05.client.admin.model.AdminModel;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import ModifiedHangman.AuthenticationService;
import ModifiedHangman.AuthenticationServiceHelper;
import ModifiedHangman.AdminService;
import ModifiedHangman.AdminServiceHelper;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class MainApp extends Application {

    private LoginView loginView;
    // you may later add an AdminView reference here

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();

        // Initialize CORBA
        String[] orbArgs = {};
        ORB orb = ORB.init(orbArgs, null);
        NamingContextExt ncRef = NamingContextExtHelper.narrow(
                orb.resolve_initial_references("NameService")
        );

        // CLIENT LOOKUP
        // Dapat yata dito lahat ng controllers ilagay nalang sa List or Set tapos iiterate ung set kung ano man yung need na controller sa view
        // Sa mga view parameters sana parang new PlayerView (Stage stage, Set<HangmanControllers> controllers)

        AuthenticationService authService = AuthenticationServiceHelper.narrow(
                ncRef.resolve_str("AuthenticationService")
        );
        LoginModel authModel       = new LoginModel(authService);
        LoginController loginController     = new LoginController(authModel);
        loginView                           = new LoginView(loginController);

        // ADMIN LOOKUP
        AdminService adminService = AdminServiceHelper.narrow(
                ncRef.resolve_str("AdminService")
        );
//        AdminModel adminModel     = new AdminModel(adminService);

        // TODO: AdminController/AdminView
//        AdminController adminCtrl = new AdminController(adminModel);
        // adminView = new AdminView(adminCtrl);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("What's The Word – Player Login");

        Parent loginRoot = loginView.createLoginPane();
        Scene scene = new Scene(loginRoot, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // TODO:  Admin window/stage
    }
}
