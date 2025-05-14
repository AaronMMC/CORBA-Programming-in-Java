package com.cs9322.team05.client.admin.view;

import ModifiedHangman.Player;
import com.cs9322.team05.client.admin.controller.AdminController;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

import java.awt.*;

public class AdminView {

    private final Scene scene;
    private final AdminController adminController;
    public AdminView(String token, AdminController adminController) {
        this.adminController = adminController;

        //Add a panel to add a player via
        TextField usernameField = new TextField("Username");
        TextField passwordField = new TextField("Password");
        String username = usernameField.getText();
        String password = passwordField.getText();
        adminController.create_player(username, password, token);

        //Add a panel to update player


        //Make the entire GUI be dynamic depending on like the window size dictates how big the tables and labels are inside.
        VBox root = new VBox();
        this.scene = new Scene(root);
    }

    public Scene getScene() {
        return scene;
    }
}