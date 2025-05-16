
package com.cs9322.team05.client.player.view;

import com.cs9322.team05.client.player.controller.AuthenticationController;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.text.Font;


import java.util.logging.Logger;

public class AuthenticationView {
    private static final Logger logger = Logger.getLogger(AuthenticationView.class.getName());
    private final AuthenticationController controller;
    private String viewInternalToken = "";
    private Button loginBtn;
    private Button logoutBtnInternal;
    private TextField usernameField;
    private PasswordField passwordField;

    public AuthenticationView(AuthenticationController controller) {
        this.controller = controller;


        this.controller.setOnLoginFailure(this::showLoginErrorAlert);
    }

    private void showLoginErrorAlert(String message) {
        showAlert("Login Failed", message, AlertType.ERROR);
    }

    public Parent createLoginPane() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #eef1f5;");

        HBox header = new HBox();
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: #34495e;");
        header.setAlignment(Pos.CENTER);

        Label title = new Label("Player Login");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        header.getChildren().add(title);
        root.setTop(header);

        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setAlignment(Pos.CENTER);

        form.setStyle("-fx-padding: 30px; -fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 2, 2);");


        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefWidth(300);
        usernameField.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");


        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefWidth(300);
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");

        form.add(usernameLabel, 0, 0);
        form.add(usernameField, 1, 0);
        form.add(passwordLabel, 0, 1);
        form.add(passwordField, 1, 1);

        loginBtn = new Button("🔓 Login");
        loginBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10px 20px; -fx-font-size: 14px;");

        HBox buttonBox = new HBox(20, loginBtn);
        buttonBox.setAlignment(Pos.CENTER);
        GridPane.setColumnSpan(buttonBox,2);
        form.add(buttonBox,0,2);

        Label hangmanBannerLabel = new Label("Hangman Challenge");
        hangmanBannerLabel.setStyle(
                "-fx-font-size: 36px;" +
                        "-fx-font-family: 'Arial Black';" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2c3e50;" +
                        "-fx-padding: 20px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);"
        );
        VBox.setMargin(hangmanBannerLabel, new Insets(20,0,0,0));


        content.getChildren().addAll(hangmanBannerLabel, form);

        StackPane centerPane = new StackPane(content);
        centerPane.setAlignment(Pos.CENTER);
        root.setCenter(centerPane);

        loginBtn.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();


            String resultMessage = controller.handleLogin(username, password);



            if (resultMessage.toLowerCase().contains("successful")) {








            } else {
                showAlert("Login Attempt", resultMessage, AlertType.WARNING);
            }
        });
        passwordField.setOnAction(e -> loginBtn.fire());





        return root;
    }


