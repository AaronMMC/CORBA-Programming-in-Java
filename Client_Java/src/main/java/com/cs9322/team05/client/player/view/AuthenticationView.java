package com.cs9322.team05.client.player.view;

import com.cs9322.team05.client.player.controller.AuthenticationController;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class AuthenticationView {
    private final AuthenticationController controller;
    private String currentToken = "";

    public AuthenticationView(AuthenticationController controller) {
        this.controller = controller;
    }

    /**
     * Creates and returns the login pane.
     */
    public Parent createLoginPane() {

        VBox root = new VBox(20);
        root.setPadding(new Insets(50));


        Text title = new Text("Player Login");
        title.setFont(Font.font(24));


        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        userField.setPromptText("Enter your username");

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");

        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);


        Button loginBtn = new Button("Login");
        Button logoutBtn = new Button("Logout");
        logoutBtn.setDisable(true);

        HBox buttons = new HBox(15, loginBtn, logoutBtn);


        loginBtn.setOnAction(evt -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();
            String result = controller.handleLogin(username, password);
            showAlert(result);

            if (result.startsWith("Login successful")) {

                currentToken = result.substring(result.indexOf("Token:") + 6).trim();
                loginBtn.setDisable(true);
                logoutBtn.setDisable(false);
            }
        });

        logoutBtn.setOnAction(evt -> {
            String result = controller.handleLogout(currentToken);
            showAlert(result);
            if (result.startsWith("Logout successful")) {

                userField.clear();
                passField.clear();
                currentToken = "";
                loginBtn.setDisable(false);
                logoutBtn.setDisable(true);
            }
        });


        root.getChildren().addAll(title, grid, buttons);
        VBox.setVgrow(grid, Priority.ALWAYS);
        return root;
    }

    /**
     * Utility to pop up a simple alert dialog.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Authentication");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
