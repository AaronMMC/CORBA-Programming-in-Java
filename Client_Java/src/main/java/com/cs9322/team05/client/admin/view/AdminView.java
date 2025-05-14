package com.cs9322.team05.client.admin.view;

import com.cs9322.team05.client.admin.controller.AdminController;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

public class AdminView {

    private final Scene scene;
    private final AdminController adminController;
    public AdminView(String token, AdminController adminController) {
        this.adminController = adminController;

        VBox root = new VBox();
        this.scene = new Scene(root);
    }

    public Scene getScene() {
        return scene;
    }
}