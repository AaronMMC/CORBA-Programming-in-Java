package com.cs9322.team05.client.admin.view;

import ModifiedHangman.Player;
import com.cs9322.team05.client.admin.controller.AdminController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.List;

public class AdminView {

    private final Scene scene;
    private final AdminController adminController;
    private final String token;
    private TableView<Player> playerTable;
    private Label waitingTimeLabel;
    private TextField waitingTimeField;
    private Label roundDurationLabel;
    private TextField roundDurationField;

    public AdminView(String token, AdminController adminController) {
        this.token = token;
        this.adminController = adminController;
        if (this.adminController == null) {
            showAlert("Initialization Error", "Admin Controller is null. The application might not function correctly.");
            // Consider disabling UI elements or taking other appropriate actions
        }
        GridPane root = initializeUI(); // Initialize UI and get the root pane
        this.scene = new Scene(root); // Initialize the scene with the correct root
        // Initially load the player data
        refreshPlayerTable();
        fetchAndDisplayRules();
    }

    private GridPane initializeUI() {
        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setVgap(10);
        root.setHgap(10);

        // Add Player Section
        Label addPlayerLabel = new Label("Add New Player");
        GridPane.setConstraints(addPlayerLabel, 0, 0, 3, 1);
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        GridPane.setConstraints(usernameField, 0, 1);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        GridPane.setConstraints(passwordField, 1, 1);
        Button addPlayerBtn = new Button("Add Player");
        GridPane.setConstraints(addPlayerBtn, 2, 1);
        addPlayerBtn.setOnAction(event -> {
            if (adminController == null) {
                showAlert("Error", "Admin Controller is not available.");
                return;
            }
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Empty Fields", "Please enter both username and password.");
                return;
            }
            adminController.create_player(username, password, token);
            usernameField.clear();
            passwordField.clear();
            refreshPlayerTable();
        });

        // Display Players Table
        Label playerListLabel = new Label("Player List");
        GridPane.setConstraints(playerListLabel, 0, 2, 3, 1);
        playerTable = new TableView<>();
        TableColumn<Player, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        TableColumn<Player, String> playerIdCol = new TableColumn<>("Player ID");
        playerIdCol.setCellValueFactory(new PropertyValueFactory<>("playerId"));
        playerTable.getColumns().addAll(playerIdCol, usernameCol);
        GridPane.setConstraints(playerTable, 0, 3, 3, 1);

        // Update Player Section
        Label updatePlayerLabel = new Label("Update Selected Player");
        GridPane.setConstraints(updatePlayerLabel, 0, 4, 3, 1);
        TextField updatePasswordField = new TextField();
        updatePasswordField.setPromptText("New Username");
        GridPane.setConstraints(updatePasswordField, 0, 5, 2, 1);
        Button updatePlayerBtn = new Button("Update Player");
        GridPane.setConstraints(updatePlayerBtn, 2, 5);
        updatePlayerBtn.setOnAction(event -> {
            if (adminController == null) {
                showAlert("Error", "Admin Controller is not available.");
                return;
            }
            Player selectedPlayer = playerTable.getSelectionModel().getSelectedItem();
            if (selectedPlayer == null) {
                showAlert("No Player Selected", "Please select a player to update.");
                return;
            }
            String newPassword = updatePasswordField.getText();
            if (newPassword.isEmpty()) {
                showAlert("Empty Field", "Please enter a new username.");
                return;
            }
            adminController.update_player(selectedPlayer.getUsername(), newPassword, token);
            updatePasswordField.clear();
            refreshPlayerTable();
        });

        // Delete Player Section
        Label deletePlayerLabel = new Label("Delete Selected Player");
        GridPane.setConstraints(deletePlayerLabel, 0, 6, 3, 1);
        Button deletePlayerBtn = new Button("Delete Player");
        GridPane.setConstraints(deletePlayerBtn, 0, 7, 3, 1);
        deletePlayerBtn.setOnAction(event -> {
            if (adminController == null) {
                showAlert("Error", "Admin Controller is not available.");
                return;
            }
            Player selectedPlayer = playerTable.getSelectionModel().getSelectedItem();
            if (selectedPlayer == null) {
                showAlert("No Player Selected", "Please select a player to delete.");
                return;
            }
            boolean confirmed = showConfirmation("Confirm Delete", "Are you sure you want to delete player: " + selectedPlayer.getUsername() + "?");
            if (confirmed) {
                adminController.delete_player(selectedPlayer.getUsername(), token);
                refreshPlayerTable();
            }
        });

        // Game Rules Section
        Label gameRulesLabel = new Label("Game Rules");
        GridPane.setConstraints(gameRulesLabel, 0, 8, 3, 1);

        waitingTimeLabel = new Label("Waiting Time:"); // Initial value will be set after fetching
        GridPane.setConstraints(waitingTimeLabel, 0, 9);
        waitingTimeField = new TextField();
        GridPane.setConstraints(waitingTimeField, 1, 9);
        Button setWaitingTimeBtn = new Button("Set");
        GridPane.setConstraints(setWaitingTimeBtn, 2, 9);
        setWaitingTimeBtn.setOnAction(event -> {
            if (adminController == null) {
                showAlert("Error", "Admin Controller is not available.");
                return;
            }
            String waitingTimeText = waitingTimeField.getText();
            if (waitingTimeText.isEmpty()) {
                showAlert("Empty Field", "Please enter a value for waiting time.");
                return;
            }
            try {
                int newWaitingTime = Integer.parseInt(waitingTimeText);
                adminController.set_waiting_time(newWaitingTime, token);
                fetchAndDisplayRules();
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number for waiting time.");
            }
        });

        roundDurationLabel = new Label("Round Duration:"); // Initial value will be set after fetching
        GridPane.setConstraints(roundDurationLabel, 0, 10);
        roundDurationField = new TextField();
        GridPane.setConstraints(roundDurationField, 1, 10);
        Button setRoundDurationBtn = new Button("Set");
        GridPane.setConstraints(setRoundDurationBtn, 2, 10);
        setRoundDurationBtn.setOnAction(event -> {
            if (adminController == null) {
                showAlert("Error", "Admin Controller is not available.");
                return;
            }
            String roundDurationText = roundDurationField.getText();
            if (roundDurationText.isEmpty()) {
                showAlert("Empty Field", "Please enter a value for round duration.");
                return;
            }
            try {
                int newRoundDuration = Integer.parseInt(roundDurationText);
                adminController.set_round_duration(newRoundDuration, token);
                fetchAndDisplayRules();
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number for round duration.");
            }
        });

        root.getChildren().addAll(
                addPlayerLabel, usernameField, passwordField, addPlayerBtn,
                playerListLabel, playerTable,
                updatePlayerLabel, updatePasswordField, updatePlayerBtn,
                deletePlayerLabel, deletePlayerBtn,
                gameRulesLabel,
                waitingTimeLabel, waitingTimeField, setWaitingTimeBtn,
                roundDurationLabel, roundDurationField, setRoundDurationBtn
        );

        return root;
    }

    private void refreshPlayerTable() {
        if (adminController == null) {
            showAlert("Error", "Admin Controller is not available.");
            return;
        }
        List<Player> playerList = adminController.getAllPlayers(token);
        if (playerList != null) {
            ObservableList<Player> players = FXCollections.observableArrayList(playerList);
            if (playerTable != null) {
                playerTable.setItems(players);
            } else {
                showAlert("UI Error", "Player table is not initialized.");
                System.err.println("Error: Player table is null during refresh.");
            }
        } else {
            showAlert("Data Error", "Could not retrieve player list.");
            if (playerTable != null) {
                playerTable.getItems().clear();
            }
        }
    }

    private void fetchAndDisplayRules() {
        if (adminController == null) {
            showAlert("Error", "Admin Controller is not available.");
            return;
        }
        // Assuming you have methods in your AdminController to get the current rules
        Integer waitingTime = adminController.get_waiting_time(token); // You'll need to implement this in your controller
        Integer roundDuration = adminController.get_round_duration(token); // You'll need to implement this in your controller

        if (waitingTime != null) {
            waitingTimeLabel.setText("Waiting Time: " + waitingTime);
            waitingTimeField.setText(String.valueOf(waitingTime));
        } else {
            waitingTimeLabel.setText("Waiting Time: N/A");
            waitingTimeField.clear();
        }

        if (roundDuration != null) {
            roundDurationLabel.setText("Round Duration: " + roundDuration);
            roundDurationField.setText(String.valueOf(roundDuration));
        } else {
            roundDurationLabel.setText("Round Duration: N/A");
            roundDurationField.clear();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return alert.showAndWait().filter(response -> response == ButtonType.YES).isPresent();
    }

    public Scene getScene() {
        return scene;
    }
}