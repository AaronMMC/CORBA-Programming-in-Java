
package com.cs9322.team05.client.admin.view;

import ModifiedHangman.Player;
import com.cs9322.team05.client.admin.controller.AdminController;
import com.cs9322.team05.client.player.controller.AuthenticationController; 
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;

import javafx.scene.control.*;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminView {
    private static final Logger logger = Logger.getLogger(AdminView.class.getName());

    private final BorderPane rootLayout;
    private final AdminController adminController;
    private final AuthenticationController authController; 
    private final String token;

    private Runnable onLogout;


    private TableView<Player> playerTable;
    private TextField usernameFieldCR;
    private PasswordField passwordFieldCR;
    private TextField updatePasswordFieldUP;
    private TextField searchPlayerFieldSE;

    private Label currentWaitingTimeLabelGS;
    private TextField waitingTimeInputGS;
    private Label currentRoundDurationLabelGS;
    private TextField roundDurationInputGS;

    
    public AdminView(String token, AdminController adminController, AuthenticationController authController) {
        this.token = token;
        this.adminController = adminController;
        this.authController = authController; 

        if (this.adminController == null) {
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Initialization Critical Error", "Admin Controller is missing. Admin panel cannot function."));
        }
        if (this.authController == null) { 
            Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "Initialization Warning", "Authentication Controller is missing. Logout may not fully function."));
        }
        this.rootLayout = createCompleteUI();

        Platform.runLater(() -> {
            if (this.adminController != null) {
                refreshPlayerTable();
                fetchAndDisplayGameSettings();
            }
        });
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    public Parent getRootPane() {
        return this.rootLayout;
    }

    private BorderPane createCompleteUI() {
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefSize(1000, 700);
        borderPane.setStyle("-fx-background-color: #f0f2f5;");

        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #3f51b5;");

        Label titleLabel = new Label("Administrator Dashboard");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: white;");

        HBox logoutButtonContainer = new HBox();
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-weight: bold;");
        logoutButton.setOnAction(e -> handleLogout()); 
        logoutButtonContainer.getChildren().add(logoutButton);
        HBox.setHgrow(logoutButtonContainer, Priority.ALWAYS);
        logoutButtonContainer.setAlignment(Pos.CENTER_RIGHT);

        topBar.getChildren().addAll(titleLabel, logoutButtonContainer);
        borderPane.setTop(topBar);

        GridPane mainContentGrid = new GridPane();
        mainContentGrid.setAlignment(Pos.TOP_CENTER);
        mainContentGrid.setPadding(new Insets(20));
        mainContentGrid.setVgap(20);
        mainContentGrid.setHgap(20);

        VBox playerManagementSection = createPlayerManagementSection();
        GridPane.setConstraints(playerManagementSection, 0, 0);

        VBox gameSettingsSection = createGameSettingsSection();
        GridPane.setConstraints(gameSettingsSection, 1, 0);

        mainContentGrid.getChildren().addAll(playerManagementSection, gameSettingsSection);
        borderPane.setCenter(mainContentGrid);

        return borderPane;
    }

    private VBox createPlayerManagementSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-border-color: #d1d9e6; -fx-border-width: 1; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 0);");

        Label sectionTitle = new Label("Player Management");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        sectionTitle.setStyle("-fx-text-fill: #3f51b5;");
        sectionTitle.setPadding(new Insets(0, 0, 10, 0));

        GridPane addPlayerPane = new GridPane();
        addPlayerPane.setHgap(10);
        addPlayerPane.setVgap(10);
        usernameFieldCR = new TextField();
        usernameFieldCR.setPromptText("Enter username");
        passwordFieldCR = new PasswordField();
        passwordFieldCR.setPromptText("Enter password");
        Button addPlayerBtn = new Button("Add New Player");
        addPlayerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        addPlayerBtn.setOnAction(e -> handleAddPlayer());
        addPlayerPane.add(new Label("Username:"), 0, 0);
        addPlayerPane.add(usernameFieldCR, 1, 0);
        addPlayerPane.add(new Label("Password:"), 0, 1);
        addPlayerPane.add(passwordFieldCR, 1, 1);
        HBox addBtnBox = new HBox(addPlayerBtn);
        addBtnBox.setAlignment(Pos.CENTER_RIGHT);
        addPlayerPane.add(addBtnBox, 1, 2);

        playerTable = new TableView<>();

        TableColumn<Player, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(cellData -> { Player p = cellData.getValue(); return new SimpleStringProperty(p != null && p.username != null ? p.username : ""); });
        usernameCol.setCellFactory(column -> new TableCell<Player, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setText(null); setStyle(""); }
                else { setText(item); setTextFill(Color.BLACK); setStyle("-fx-background-color: lightyellow; -fx-padding: 3px;"); }
            }
        });
        usernameCol.setPrefWidth(150);

        TableColumn<Player, String> passwordDisplayCol = new TableColumn<>("Password (Hash)");
        passwordDisplayCol.setCellValueFactory(cellData ->{ Player p = cellData.getValue(); return new SimpleStringProperty(p != null && p.password != null ? p.password : ""); });
        passwordDisplayCol.setCellFactory(column -> new TableCell<Player, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setText(null); setStyle(""); }
                else { setText(item); setTextFill(Color.DARKSLATEBLUE); setStyle("-fx-background-color: lightcyan; -fx-padding: 3px;"); }
            }
        });
        passwordDisplayCol.setPrefWidth(250);

        TableColumn<Player, String> winsCol = new TableColumn<>("Wins");
        winsCol.setCellValueFactory(cellData -> { Player p = cellData.getValue(); return new SimpleStringProperty(p != null ? String.valueOf(p.wins) : "0"); });
        winsCol.setCellFactory(column -> new TableCell<Player, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setText(null); setStyle(""); }
                else { setText(item); setTextFill(Color.FORESTGREEN); setStyle("-fx-background-color: lightgreen; -fx-padding: 3px;"); }
            }
        });
        winsCol.setPrefWidth(70);

        playerTable.getColumns().setAll(usernameCol, passwordDisplayCol, winsCol);
        playerTable.setPlaceholder(new Label("No players to display. Check logs."));
        playerTable.setMinHeight(250);
        playerTable.setMaxHeight(300);
        playerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchPlayerFieldSE = new TextField();
        searchPlayerFieldSE.setPromptText("Search by Username");
        searchPlayerFieldSE.setPrefWidth(180);
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> handleSearchPlayer());
        Button clearSearchBtn = new Button("Show All");
        clearSearchBtn.setOnAction(e -> { searchPlayerFieldSE.clear(); refreshPlayerTable(); });
        searchBox.getChildren().addAll(searchPlayerFieldSE, searchBtn, clearSearchBtn);

        HBox tableActionsBox = new HBox(10);
        tableActionsBox.setAlignment(Pos.CENTER_LEFT);
        updatePasswordFieldUP = new TextField();
        updatePasswordFieldUP.setPromptText("New Password");
        updatePasswordFieldUP.setPrefWidth(150);
        Button updatePlayerBtn = new Button("Update Pwd");
        updatePlayerBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
        updatePlayerBtn.setOnAction(e -> handleUpdatePlayerPassword());
        Button deletePlayerBtn = new Button("Delete Player");
        deletePlayerBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        deletePlayerBtn.setOnAction(e -> handleDeletePlayer());
        tableActionsBox.getChildren().addAll(new Label("For Selected:"), updatePasswordFieldUP, updatePlayerBtn, deletePlayerBtn);

        section.getChildren().addAll(sectionTitle,
                new Label("Create Player:"), addPlayerPane,
                new Separator(javafx.geometry.Orientation.HORIZONTAL),
                new Label("Registered Players:"), searchBox, playerTable, tableActionsBox);
        return section;
    }

    private VBox createGameSettingsSection() {
        
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-border-color: #d1d9e6; -fx-border-width: 1; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.5, 0, 0);");

        Label sectionTitle = new Label("Game Configuration");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        sectionTitle.setStyle("-fx-text-fill: #3f51b5;");
        sectionTitle.setPadding(new Insets(0, 0, 10, 0));

        GridPane waitingTimePane = new GridPane();
        waitingTimePane.setHgap(10);
        waitingTimePane.setVgap(10);
        currentWaitingTimeLabelGS = new Label("Current Matchmaking Waiting Time: N/A");
        waitingTimeInputGS = new TextField();
        waitingTimeInputGS.setPromptText("Seconds (e.g., 10)");
        waitingTimeInputGS.setPrefWidth(120);
        Button setWaitingTimeBtn = new Button("Set Wait Time");
        setWaitingTimeBtn.setOnAction(e -> handleSetWaitingTime());
        waitingTimePane.add(currentWaitingTimeLabelGS, 0, 0, 2, 1);
        waitingTimePane.add(new Label("New Value:"), 0, 1);
        waitingTimePane.add(waitingTimeInputGS, 1, 1);
        HBox waitBtnBox = new HBox(setWaitingTimeBtn);
        waitBtnBox.setAlignment(Pos.CENTER_LEFT);
        waitingTimePane.add(waitBtnBox, 1, 2);

        GridPane roundDurationPane = new GridPane();
        roundDurationPane.setHgap(10);
        roundDurationPane.setVgap(10);
        currentRoundDurationLabelGS = new Label("Current Round Duration (Guessing): N/A");
        roundDurationInputGS = new TextField();
        roundDurationInputGS.setPromptText("Seconds (e.g., 30)");
        roundDurationInputGS.setPrefWidth(120);
        Button setRoundDurationBtn = new Button("Set Round Duration");
        setRoundDurationBtn.setOnAction(e -> handleSetRoundDuration());
        roundDurationPane.add(currentRoundDurationLabelGS, 0, 0, 2, 1);
        roundDurationPane.add(new Label("New Value:"), 0, 1);
        roundDurationPane.add(roundDurationInputGS, 1, 1);
        HBox roundBtnBox = new HBox(setRoundDurationBtn);
        roundBtnBox.setAlignment(Pos.CENTER_LEFT);
        roundDurationPane.add(roundBtnBox, 1, 2);

        section.getChildren().addAll(sectionTitle, waitingTimePane, new Separator(javafx.geometry.Orientation.HORIZONTAL), roundDurationPane);
        return section;
    }


    
    private void handleLogout() {
        logger.info("Logout button clicked. Token for logout: " + this.token); 
        if (authController == null) {
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Authentication Controller not available for logout.");
            logger.warning("handleLogout: authController is null.");
            return;
        }

        try {
            
            String logoutMessage = authController.handleLogout(this.token);
            showAlert(Alert.AlertType.INFORMATION, "Logout Status", logoutMessage);

            if (logoutMessage != null && logoutMessage.toLowerCase().contains("successful")) {
                logger.info("Logout successful via AuthenticationController. Triggering onLogout callback.");
                if (onLogout != null) {
                    onLogout.run(); 
                } else {
                    logger.warning("onLogout callback is null. No UI transition will occur from AdminView.");
                }
            } else {
                logger.warning("Logout attempt reported as not successful by AuthenticationController: " + logoutMessage);
            }
        } catch (Exception e) {
            
            
            logger.log(Level.SEVERE, "Exception during logout process via AuthenticationController.", e);
            showAlert(Alert.AlertType.ERROR, "Logout Error", "An unexpected error occurred: " + e.getMessage());
        }
    }


    private void handleAddPlayer() {
        if (adminController == null) { showAlert(Alert.AlertType.ERROR, "Error", "Admin Controller not available."); return; }
        String username = usernameFieldCR.getText().trim();
        String password = passwordFieldCR.getText();
        if (username.isEmpty() || password.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Validation", "Username and Password required."); return; }
        try {
            adminController.create_player(username, password, token);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Player '" + username + "' creation request sent.");
            usernameFieldCR.clear();
            passwordFieldCR.clear();
            refreshPlayerTable();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error add player", ex);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create player: " + ex.getMessage());
        }
    }

    private void handleUpdatePlayerPassword() {
        if (adminController == null) { showAlert(Alert.AlertType.ERROR, "Error", "Admin Controller not available."); return; }
        Player selectedPlayer = playerTable.getSelectionModel().getSelectedItem();
        if (selectedPlayer == null) { showAlert(Alert.AlertType.WARNING, "Selection", "Please select a player."); return; }
        String newPassword = updatePasswordFieldUP.getText();
        if (newPassword.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Validation", "New password cannot be empty."); return; }
        try {
            adminController.update_player(selectedPlayer.username, newPassword, token);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password update for '" + selectedPlayer.username + "' sent.");
            updatePasswordFieldUP.clear();
            
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error update player", ex);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password: " + ex.getMessage());
        }
    }

    private void handleDeletePlayer() {
        if (adminController == null) { showAlert(Alert.AlertType.ERROR, "Error", "Admin Controller not available."); return; }
        Player selectedPlayer = playerTable.getSelectionModel().getSelectedItem();
        if (selectedPlayer == null) { showAlert(Alert.AlertType.WARNING, "Selection", "Please select a player to delete."); return; }
        Optional<ButtonType> result = showConfirmation("Confirm Deletion", "Are you sure you want to delete player: " + selectedPlayer.username + "?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                adminController.delete_player(selectedPlayer.username, token);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Player '" + selectedPlayer.username + "' deletion request sent.");
                refreshPlayerTable();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error delete player", ex);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete player: " + ex.getMessage());
            }
        }
    }

    private void handleSearchPlayer() {
        if (adminController == null) { showAlert(Alert.AlertType.ERROR, "Error", "Admin Controller not available."); return; }
        String keyword = searchPlayerFieldSE.getText().trim();
        if (keyword.isEmpty()) { refreshPlayerTable(); return; }
        try {
            Player foundPlayer = adminController.search_player(keyword, token);
            Platform.runLater(() -> {
                if (this.playerTable == null) { logger.warning("handleSearchPlayer (UI): playerTable is null."); return; }
                if (foundPlayer != null) {
                    this.playerTable.setItems(FXCollections.observableArrayList(foundPlayer));
                    logger.info("handleSearchPlayer (UI): Displaying found player: " + foundPlayer.username);
                } else {
                    this.playerTable.setItems(FXCollections.emptyObservableList());
                    showAlert(Alert.AlertType.INFORMATION, "Not Found", "No player found matching username '" + keyword + "'.");
                }
            });
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error search player", ex);
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Search Error", "Failed to search player: " + ex.getMessage()));
        }
    }

    private void refreshPlayerTable() {
        if (adminController == null) {
            logger.warning("AdminView: refreshPlayerTable - adminController is null.");
            return;
        }
        logger.info("AdminView: refreshPlayerTable - Initiating with token: " + token);
        try {
            List<Player> playerList = adminController.getAllPlayers(token);

            
            if (playerList == null) { System.out.println("PLAYER_LIST_OUTPUT: NULL"); }
            else { System.out.println("PLAYER_LIST_OUTPUT: START - Size: " + playerList.size());
                for (int i = 0; i < playerList.size(); i++) { Player p = playerList.get(i);
                    if (p == null) { System.out.println("PLAYER_LIST_OUTPUT: Player at index " + i + " is NULL"); }
                    else { String u = (p.username==null)?"NULL_U":p.username; String pw = (p.password==null)?"NULL_PW":p.password;
                        System.out.println("PLAYER_LIST_OUTPUT: Player " + i + " | U: [" + u + "] | P: [" + pw + "] | W: " + p.wins); } }
                System.out.println("PLAYER_LIST_OUTPUT: END"); }

            if (playerList == null) { logger.warning("AdminView: refreshPlayerTable - Received NULL player list from controller."); }
            else { logger.info("AdminView: refreshPlayerTable - Received player list from controller. Size: " + playerList.size()); }

            Platform.runLater(() -> {
                if (this.playerTable == null) { logger.severe("AdminView: refreshPlayerTable (UI) - this.playerTable is NULL."); return; }
                if (playerList != null && !playerList.isEmpty()) {
                    ObservableList<Player> itemsToSet = FXCollections.observableArrayList(playerList);
                    this.playerTable.setItems(itemsToSet);
                    this.playerTable.refresh();
                    logger.info("AdminView: refreshPlayerTable (UI) - Table items set (" + itemsToSet.size() + "). First user (if any): " + (itemsToSet.isEmpty() ? "N/A" : itemsToSet.get(0).username));
                } else {
                    this.playerTable.setItems(FXCollections.emptyObservableList());
                    logger.info("AdminView: refreshPlayerTable (UI) - Table set to empty (list was " + (playerList == null ? "null" : "empty") + ").");
                    if(playerList == null) showAlert(Alert.AlertType.WARNING, "Data Issue", "Could not retrieve player list (was null).");
                }
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, "AdminView: refreshPlayerTable - Exception during data fetch or UI update.", e);
            Platform.runLater(() -> { if (this.playerTable != null) this.playerTable.setItems(FXCollections.emptyObservableList());
                showAlert(Alert.AlertType.ERROR, "Data Error", "Failed to refresh player list: " + e.getMessage()); });
        }
    }

    private void handleSetWaitingTime() {
        if (adminController == null) { showAlert(Alert.AlertType.ERROR, "Error", "Admin Controller not available."); return; }
        try {
            int newTime = Integer.parseInt(waitingTimeInputGS.getText().trim());
            if (newTime < 1 || newTime > 1200) { showAlert(Alert.AlertType.WARNING, "Validation", "Waiting time invalid (1-1200s)."); return; }
            adminController.set_waiting_time(newTime, token);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Waiting time update sent.");
            fetchAndDisplayGameSettings();
            waitingTimeInputGS.clear();
        } catch (NumberFormatException e) { showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid number for waiting time.");
        } catch (Exception ex) { logger.log(Level.SEVERE, "Error set waiting time", ex); showAlert(Alert.AlertType.ERROR, "Operation Error", "Failed: " + ex.getMessage()); }
    }

    private void handleSetRoundDuration() {
        if (adminController == null) { showAlert(Alert.AlertType.ERROR, "Error", "Admin Controller not available."); return; }
        try {
            int newTime = Integer.parseInt(roundDurationInputGS.getText().trim());
            if (newTime < 10 || newTime > 600) { showAlert(Alert.AlertType.WARNING, "Validation", "Round duration invalid (10-600s)."); return; }
            adminController.set_round_duration(newTime, token);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Round duration update sent.");
            fetchAndDisplayGameSettings();
            roundDurationInputGS.clear();
        } catch (NumberFormatException e) { showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid number for round duration.");
        } catch (Exception ex) { logger.log(Level.SEVERE, "Error set round duration", ex); showAlert(Alert.AlertType.ERROR, "Operation Error", "Failed: " + ex.getMessage()); }
    }

    private void fetchAndDisplayGameSettings() {
        if (adminController == null) { logger.warning("AdminView: fetchGameSettings - adminController is null."); return; }
        logger.info("AdminView: fetchGameSettings - Fetching.");
        try {
            int waitingTime = adminController.get_waiting_time(token);
            int roundDuration = adminController.get_round_duration(token);
            Platform.runLater(() -> {
                currentWaitingTimeLabelGS.setText("Current Matchmaking Waiting Time: " + waitingTime + "s");
                waitingTimeInputGS.setText(String.valueOf(waitingTime));
                currentRoundDurationLabelGS.setText("Current Round Duration (Guessing): " + roundDuration + "s");
                roundDurationInputGS.setText(String.valueOf(roundDuration));
                logger.info("AdminView: fetchGameSettings (UI) - Settings updated: WT=" + waitingTime + ", RD=" + roundDuration);
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, "AdminView: fetchGameSettings - Error.", e);
            Platform.runLater(() -> {
                currentWaitingTimeLabelGS.setText("Current Time: Error"); waitingTimeInputGS.clear();
                currentRoundDurationLabelGS.setText("Current Duration: Error"); roundDurationInputGS.clear();
                showAlert(Alert.AlertType.ERROR, "Settings Error", "Failed to fetch game settings: " + e.getMessage());
            });
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(alertType); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
        } else {
            Platform.runLater(() -> { Alert alert = new Alert(alertType); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait(); });
        }
    }

    private Optional<ButtonType> showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content);
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL); return alert.showAndWait();
    }
}