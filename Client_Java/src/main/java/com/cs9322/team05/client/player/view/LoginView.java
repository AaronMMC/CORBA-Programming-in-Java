package com.cs9322.team05.client.player.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


public class LoginView {


    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Text messageText;


    public LoginView() {


        initComponents();
    }

    private void initComponents() {


        usernameField = new TextField();
        passwordField = new PasswordField();
        loginButton = new Button("Login");
        messageText = new Text();
    }


    public Parent createLoginPane() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Player Login");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label usernameLabel = new Label("Username:");
        grid.add(usernameLabel, 0, 1);

        grid.add(usernameField, 1, 1);

        Label passwordLabel = new Label("Password:");
        grid.add(passwordLabel, 0, 2);

        grid.add(passwordField, 1, 2);


        HBox hbLoginButton = new HBox(10);
        hbLoginButton.setAlignment(Pos.BOTTOM_RIGHT);
        hbLoginButton.getChildren().add(loginButton);
        grid.add(hbLoginButton, 1, 3);


        messageText.setFill(Color.FIREBRICK);
        grid.add(messageText, 0, 4, 2, 1);


        loginButton.setOnAction(event -> {


            if (getUsername().isEmpty() || getPassword().isEmpty()) {
                setMessage("Username and Password cannot be empty.", true);
            } else {
                setMessage("Login button clicked. User: " + getUsername(), false);


            }
        });

        return grid;
    }


    public void setMessage(String message, boolean isError) {
        if (messageText != null) {
            messageText.setText(message);
            messageText.setFill(isError ? Color.FIREBRICK : Color.GREEN);
        }
    }


    public void clearFields() {
        if (usernameField != null) usernameField.clear();
        if (passwordField != null) passwordField.clear();
    }


    public String getUsername() {
        return usernameField != null ? usernameField.getText() : "";
    }


    public String getPassword() {
        return passwordField != null ? passwordField.getText() : "";
    }


    public Button getLoginButton() {
        return loginButton;
    }
}