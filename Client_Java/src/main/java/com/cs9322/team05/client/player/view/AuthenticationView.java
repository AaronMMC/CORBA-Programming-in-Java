package com.cs9322.team05.client.player.view;
//
import com.cs9322.team05.client.player.controller.AuthenticationController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
//
//public class AuthenticationView {
//    private final AuthenticationController controller;
//    private String currentToken = "";
//
//    public AuthenticationView(AuthenticationController controller) {
//        this.controller = controller;
//    }
//
//    /**
//     * Creates and returns the login pane.
//     */
//    public Parent createLoginPane() {
//
//        VBox root = new VBox(20);
//        root.setPadding(new Insets(50));
//
//
//        Text title = new Text("Player Login");
//        title.setFont(Font.font(24));
//
//
//        GridPane grid = new GridPane();
//        grid.setHgap(10);
//        grid.setVgap(15);
//
//        Label userLabel = new Label("Username:");
//        TextField userField = new TextField();
//        userField.setPromptText("Enter your username");
//
//        Label passLabel = new Label("Password:");
//        PasswordField passField = new PasswordField();
//        passField.setPromptText("Enter your password");
//
//        grid.add(userLabel, 0, 0);
//        grid.add(userField, 1, 0);
//        grid.add(passLabel, 0, 1);
//        grid.add(passField, 1, 1);
//
//
//        Button loginBtn = new Button("Login");
//        Button logoutBtn = new Button("Logout");
//        logoutBtn.setDisable(true);
//
//        HBox buttons = new HBox(15, loginBtn, logoutBtn);
//
//
//        loginBtn.setOnAction(evt -> {
//            String username = userField.getText().trim();
//            String password = passField.getText();
//            String result = controller.handleLogin(username, password);
//            showAlert(result);
//
//            if (result.startsWith("Login successful")) {
//
//                currentToken = result.substring(result.indexOf("Token:") + 6).trim();
//                loginBtn.setDisable(true);
//                logoutBtn.setDisable(false);
//            }
//        });
//
//        logoutBtn.setOnAction(evt -> {
//            String result = controller.handleLogout(currentToken);
//            showAlert(result);
//            if (result.startsWith("Logout successful")) {
//
//                userField.clear();
//                passField.clear();
//                currentToken = "";
//                loginBtn.setDisable(false);
//                logoutBtn.setDisable(true);
//            }
//        });
//
//
//        root.getChildren().addAll(title, grid, buttons);
//        VBox.setVgrow(grid, Priority.ALWAYS);
//        return root;
//    }
//
//    /**
//     * Utility to pop up a simple alert dialog.
//     */
//    private void showAlert(String message) {
//        Alert alert = new Alert(AlertType.INFORMATION);
//        alert.setTitle("Authentication");
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//}

//import adminmockapp.controller.LoginController;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;


public class AuthenticationView {
    private final AuthenticationController controller;
    private final Scene scene;
    private String currentToken = "";

    public AuthenticationView(AuthenticationController controller) {
        this.controller = controller;

        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setLeft(createSidebar());
        root.setCenter(createLoginContent());

        this.scene = new Scene(root, 1000, 600);
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #2f3e47;");
        Label title = new Label("Player Login");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        header.getChildren().add(title);
        return header;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPadding(new Insets(20));
        sidebar.setSpacing(10);
        sidebar.setStyle("-fx-background-color: #f5f7fa;");

        Label loginLabel = new Label("Login");
        loginLabel.setStyle("-fx-text-fill: #4a6ee0; -fx-font-weight: bold;");

        sidebar.getChildren().add(loginLabel);
        return sidebar;
    }

    private VBox createLoginContent() {
        VBox content = new VBox(25);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color: #ffffff;");

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(20);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: lightgray; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-font-weight: bold;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefWidth(300);

        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-weight: bold;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefWidth(300);

        form.add(usernameLabel, 0, 0);
        form.add(usernameField, 1, 0);
        form.add(passwordLabel, 0, 1);
        form.add(passwordField, 1, 1);

        // Buttons
        Button loginBtn = new Button("🔓 Login");
        loginBtn.setStyle("-fx-background-color: #4a6ee0; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        Button logoutBtn = new Button("🔒 Logout");
        logoutBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        logoutBtn.setDisable(true);

        HBox buttons = new HBox(20, loginBtn, logoutBtn);
        buttons.setAlignment(Pos.CENTER);

        // Hangman button below login/logout buttons with matching color style
        Button hangmanBtn = new Button("Hangman");
        hangmanBtn.setDisable(true); // looks like a banner/label
        hangmanBtn.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-color: linear-gradient(to right, #2f3e47, #4a6ee0);" + // blending header and sidebar blue
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 15 60 15 60;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 6, 0, 0, 3);"
        );

        // Actions
        loginBtn.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String result = controller.handleLogin(username, password);
            showAlert(result);
            if (result.startsWith("Login successful")) {
                currentToken = result.substring(result.indexOf("Token:") + 6).trim();
                loginBtn.setDisable(true);
                logoutBtn.setDisable(false);
            }
        });

        logoutBtn.setOnAction(event -> {
            String result = controller.handleLogout(currentToken);
            showAlert(result);
            if (result.startsWith("Logout successful")) {
                usernameField.clear();
                passwordField.clear();
                currentToken = "";
                loginBtn.setDisable(false);
                logoutBtn.setDisable(true);
            }
        });

        content.getChildren().addAll(form, buttons, hangmanBtn);
        return content;
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Authentication");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }
}

