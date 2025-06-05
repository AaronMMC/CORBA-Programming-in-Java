
package com.cs9322.team05.client.admin.view;

import ModifiedHangman.Player;
import ModifiedHangman.PlayerAlreadyExistException;
import com.cs9322.team05.client.admin.controller.AdminController;
import com.cs9322.team05.client.player.controller.AuthenticationController;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
            logger.severe("AdminController is NULL in AdminView constructor.");
        }
        if (this.authController == null) {
            Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "Initialization Warning", "Authentication Controller is missing. Logout may not fully function."));
            logger.warning("AuthenticationController is NULL in AdminView constructor.");
        }
        this.rootLayout = createCompleteUI();

        Platform.runLater(() -> {
            if (this.adminController != null) {
                refreshPlayerTable();
                fetchAndDisplayGameSettings();
            } else {
                showAlert(Alert.AlertType.ERROR, "Functionality Limited", "Admin functions cannot be loaded because the Admin Controller is not available.");
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
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24)); 
        titleLabel.setStyle("-fx-text-fill: white;");

        Region spacer = new Region(); 
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Logout");
        logoutButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        logoutButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-background-radius: 4px; -fx-padding: 8px 15px;"); 
        logoutButton.setOnAction(e -> handleLogout());

        topBar.getChildren().addAll(titleLabel, spacer, logoutButton);
        borderPane.setTop(topBar);

        GridPane mainContentGrid = new GridPane();
        mainContentGrid.setAlignment(Pos.TOP_CENTER); 
        mainContentGrid.setPadding(new Insets(20));
        mainContentGrid.setVgap(20);
        mainContentGrid.setHgap(20);

        VBox playerManagementSection = createPlayerManagementSection();
        GridPane.setConstraints(playerManagementSection, 0, 0);
        GridPane.setVgrow(playerManagementSection, Priority.ALWAYS);


        VBox gameSettingsSection = createGameSettingsSection();
        GridPane.setConstraints(gameSettingsSection, 1, 0);
        GridPane.setVgrow(gameSettingsSection, Priority.ALWAYS);



        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(60); 
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(40);
        mainContentGrid.getColumnConstraints().addAll(col1, col2);

        mainContentGrid.getChildren().addAll(playerManagementSection, gameSettingsSection);
        borderPane.setCenter(mainContentGrid);

        return borderPane;
    }

    private VBox createPlayerManagementSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-border-color: #dfe4ea; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Label sectionTitle = createSectionTitle("Player Management");
        section.getChildren().add(sectionTitle);

        section.getChildren().add(createTitledSeparator("Create New Player"));
        section.getChildren().add(createAddPlayerPane());

        section.getChildren().add(createTitledSeparator("Registered Players"));
        section.getChildren().add(createSearchPlayerPane());
        section.getChildren().add(createPlayerTablePane());
        section.getChildren().add(createTableActionsPane());

        return section;
    }

    private GridPane createAddPlayerPane() {
        GridPane addPlayerPane = new GridPane();
        addPlayerPane.setHgap(10);
        addPlayerPane.setVgap(10);
        addPlayerPane.setPadding(new Insets(0,0,10,0)); 

        usernameFieldCR = new TextField();
        usernameFieldCR.setPromptText("Enter username");
        usernameFieldCR.setPrefColumnCount(15);
        passwordFieldCR = new PasswordField();
        passwordFieldCR.setPromptText("Enter password");
        passwordFieldCR.setPrefColumnCount(15);

        Button addPlayerBtn = new Button("Add Player");
        addPlayerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 15px;");
        addPlayerBtn.setOnAction(e -> handleAddPlayer());

        addPlayerPane.add(new Label("Username:"), 0, 0); addPlayerPane.add(usernameFieldCR, 1, 0);
        addPlayerPane.add(new Label("Password:"), 0, 1); addPlayerPane.add(passwordFieldCR, 1, 1);
        HBox btnContainer = new HBox(addPlayerBtn);
        btnContainer.setAlignment(Pos.CENTER_RIGHT);
        addPlayerPane.add(btnContainer, 1, 2); 

        return addPlayerPane;
    }

    private HBox createSearchPlayerPane() {
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(5,0,5,0));
        searchPlayerFieldSE = new TextField();
        searchPlayerFieldSE.setPromptText("Search by Username");
        searchPlayerFieldSE.setPrefWidth(180);
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(e -> handleSearchPlayer());
        Button clearSearchBtn = new Button("Show All");
        clearSearchBtn.setOnAction(e -> { searchPlayerFieldSE.clear(); refreshPlayerTable(); });
        searchBox.getChildren().addAll(new Label("Find:"), searchPlayerFieldSE, searchBtn, clearSearchBtn);
        return searchBox;
    }

    private TableView<Player> createPlayerTablePane() {
        playerTable = new TableView<>();
        playerTable.setPlaceholder(new Label("No players data found or error loading."));
        playerTable.setMinHeight(200); 
        playerTable.setPrefHeight(300); 
        VBox.setVgrow(playerTable, Priority.ALWAYS); 

        TableColumn<Player, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().username));
        usernameCol.setPrefWidth(150);

        TableColumn<Player, String> passwordDisplayCol = new TableColumn<>("Password (Stored)"); 
        passwordDisplayCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().password));
        passwordDisplayCol.setPrefWidth(200);

        TableColumn<Player, Integer> winsCol = new TableColumn<>("Wins");
        winsCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().wins));
        winsCol.setPrefWidth(80);
        winsCol.setCellFactory(column -> new TableCell<Player, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });


        playerTable.getColumns().setAll(usernameCol, passwordDisplayCol, winsCol);
        playerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return playerTable;
    }

    private HBox createTableActionsPane() {
        HBox tableActionsBox = new HBox(10);
        tableActionsBox.setAlignment(Pos.CENTER_LEFT);
        tableActionsBox.setPadding(new Insets(10,0,0,0)); 

        updatePasswordFieldUP = new TextField();
        updatePasswordFieldUP.setPromptText("New Password for Selected");
        updatePasswordFieldUP.setPrefWidth(180);

        Button updatePlayerBtn = new Button("Update Password");
        updatePlayerBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px;");
        updatePlayerBtn.setOnAction(e -> handleUpdatePlayerPassword());

        Button updatePlayerUsernameBtn = new Button("Update Username");
        updatePlayerUsernameBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px;");
        updatePlayerUsernameBtn.setOnAction(e -> handleUpdatePlayerUsername());

        Button deletePlayerBtn = new Button("Delete Selected Player");
        deletePlayerBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px;");
        deletePlayerBtn.setOnAction(e -> handleDeletePlayer());

        tableActionsBox.getChildren().addAll(updatePasswordFieldUP, updatePlayerBtn, updatePlayerUsernameBtn, deletePlayerBtn);
        return tableActionsBox;
    }

    private VBox createGameSettingsSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-border-color: #dfe4ea; -fx-border-width: 1; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Label sectionTitle = createSectionTitle("Game Configuration");
        section.getChildren().add(sectionTitle);

        section.getChildren().add(createTitledSeparator("Matchmaking Settings"));
        GridPane waitingTimePane = new GridPane();
        waitingTimePane.setHgap(10); waitingTimePane.setVgap(8);
        currentWaitingTimeLabelGS = new Label("Current Matchmaking Waiting Time: N/A");
        waitingTimeInputGS = new TextField();
        waitingTimeInputGS.setPromptText("Seconds (e.g., 10)");
        waitingTimeInputGS.setPrefWidth(100);
        Button setWaitingTimeBtn = new Button("Set Wait Time");
        setWaitingTimeBtn.setOnAction(e -> handleSetWaitingTime());
        waitingTimePane.add(currentWaitingTimeLabelGS, 0, 0, 2, 1);
        waitingTimePane.add(new Label("New Value (s):"), 0, 1); waitingTimePane.add(waitingTimeInputGS, 1, 1);
        waitingTimePane.add(setWaitingTimeBtn, 2, 1);
        section.getChildren().add(waitingTimePane);

        section.getChildren().add(createTitledSeparator("Round Settings"));
        GridPane roundDurationPane = new GridPane();
        roundDurationPane.setHgap(10); roundDurationPane.setVgap(8);
        currentRoundDurationLabelGS = new Label("Current Round Duration (Guessing): N/A");
        roundDurationInputGS = new TextField();
        roundDurationInputGS.setPromptText("Seconds (e.g., 30)");
        roundDurationInputGS.setPrefWidth(100);
        Button setRoundDurationBtn = new Button("Set Round Duration");
        setRoundDurationBtn.setOnAction(e -> handleSetRoundDuration());
        roundDurationPane.add(currentRoundDurationLabelGS, 0, 0, 2, 1);
        roundDurationPane.add(new Label("New Value (s):"), 0, 1); roundDurationPane.add(roundDurationInputGS, 1, 1);
        roundDurationPane.add(setRoundDurationBtn, 2, 1);
        section.getChildren().add(roundDurationPane);

        return section;
    }

    private Label createSectionTitle(String text) {
        Label title = new Label(text);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18)); 
        title.setStyle("-fx-text-fill: #3f51b5;"); 
        title.setPadding(new Insets(0, 0, 5, 0)); 
        return title;
    }

    private Node createTitledSeparator(String titleText) {
        Separator separator = new Separator(javafx.geometry.Orientation.HORIZONTAL);
        Label label = new Label(titleText);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #555;");
        label.setPadding(new Insets(8,0,2,0)); 

        VBox titledSeparator = new VBox(label, separator);
        titledSeparator.setPadding(new Insets(10,0,5,0)); 
        return titledSeparator;
    }


    private void handleLogout() {
        logger.info("AdminView: Logout button clicked. Token for logout: " + this.token);
        if (authController == null) {
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Authentication Controller not available for logout.");
            logger.warning("AdminView.handleLogout: authController is null.");
            return;
        }
        String logoutMessage = authController.handleLogout(this.token);
        showAlert(Alert.AlertType.INFORMATION, "Logout Status", logoutMessage);
        if (logoutMessage.toLowerCase().contains("successful") && onLogout != null) {
            logger.info("AdminView: Logout successful, invoking AdminView's onLogout callback (if set by MainApp).");
            
            onLogout.run(); 
        }
    }


    private void handleAddPlayer() {
        if (adminController == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Admin Controller not available.");
            return;
        }

        String username = usernameFieldCR.getText().trim();
        String password = passwordFieldCR.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Username and Password cannot be empty.");
            return;
        }

        try {
            adminController.create_player(username, password, token);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Player '" + username + "' was created successfully.");
            usernameFieldCR.clear();
            passwordFieldCR.clear();
            refreshPlayerTable();
        } catch (PlayerAlreadyExistException ex) {
            logger.warning("Player creation failed: Username already exists - " + username);
            showAlert(Alert.AlertType.WARNING, "Player Exists", "A player with the username '" + username + "' already exists.");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Unexpected error adding player: " + username, ex);
            showAlert(Alert.AlertType.ERROR, "Add Player Error", "An unexpected error occurred while creating player: " + ex.getMessage());
        }
    }

    private void handleUpdatePlayerPassword() {
        if (adminController == null) { showAlert(Alert.AlertType.ERROR, "Error", "Admin Controller not available."); return; }
        Player selectedPlayer = playerTable.getSelectionModel().getSelectedItem();
        if (selectedPlayer == null) { showAlert(Alert.AlertType.WARNING, "Selection Needed", "Please select a player from the table to update."); return; }
        String newPassword = updatePasswordFieldUP.getText();
        if (newPassword.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Validation Error", "New password field cannot be empty."); return; }
        try {
            adminController.update_player(selectedPlayer.username, newPassword, token);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password for player '" + selectedPlayer.username + "' update request sent.");
            updatePasswordFieldUP.clear();
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error updating password for player: " + selectedPlayer.username, ex);
            showAlert(Alert.AlertType.ERROR, "Update Player Error", "Failed to update password: " + ex.getMessage());
        }
    }

    private void handleUpdatePlayerUsername() {
        if (adminController == null) { showAlert(Alert.AlertType.ERROR, "Error", "Admin Controller not available."); return;}
        Player selectedPlayer = playerTable.getSelectionModel().getSelectedItem();
        if (selectedPlayer == null) { showAlert(Alert.AlertType.WARNING, "Selection Needed", "Please select a player from the table to update."); return; }
        String newUsername = usernameFieldCR.getText().trim();
        if (newUsername.isEmpty()) {showAlert(Alert.AlertType.WARNING, "Validation Error", "New username field cannot be empty."); return;}
        try {
            adminController.update_player(newUsername, selectedPlayer.password, token);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Username for player '" + selectedPlayer.username + "' update request sent.");
            updatePasswordFieldUP.clear();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating username for player: " + selectedPlayer.username, e);
            showAlert(Alert.AlertType.ERROR, "Update Player Error", "Failed to update username: " + e.getMessage());
        }
    }

    private void handleDeletePlayer() {
        if (adminController == null) { showAlert(Alert.AlertType.ERROR, "Error", "Admin Controller not available."); return; }
        Player selectedPlayer = playerTable.getSelectionModel().getSelectedItem();
        if (selectedPlayer == null) { showAlert(Alert.AlertType.WARNING, "Selection Needed", "Please select a player from the table to delete."); return; }

        Optional<ButtonType> result = showConfirmationDialog("Confirm Deletion",
                "Are you sure you want to permanently delete player: " + selectedPlayer.username + "? This action cannot be undone.");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                adminController.delete_player(selectedPlayer.username, token);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Player '" + selectedPlayer.username + "' deletion request sent.");
                refreshPlayerTable();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error deleting player: " + selectedPlayer.username, ex);
                showAlert(Alert.AlertType.ERROR, "Delete Player Error", "Failed to delete player: " + ex.getMessage());
            }
        }
    }

    private void handleSearchPlayer() {
        if (adminController == null) { showAlert(Alert.AlertType.ERROR, "Error", "Admin Controller not available."); return; }
        String keyword = searchPlayerFieldSE.getText().trim();
        if (keyword.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Search", "Search field is empty. Displaying all players.");
            refreshPlayerTable(); return;
        }
        try {
            List<Player> foundPlayers = adminController.getAllPlayers(token)
                    .stream()
                    .filter(player -> player.username.contains(keyword))
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                if (playerTable == null) {
                    logger.warning("handleSearchPlayer (UI): playerTable is null.");
                    return;
                }
                if (!foundPlayers.isEmpty()) {
                    playerTable.setItems(FXCollections.observableArrayList(foundPlayers));
                    foundPlayers.forEach(player ->
                            logger.info("handleSearchPlayer (UI): Displaying found player: " + player.username));
                } else {
                    playerTable.setItems(FXCollections.emptyObservableList());
                    showAlert(Alert.AlertType.INFORMATION, "Search Result", "No player found matching username '" + keyword + "'.");
                }
            });
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error searching for player: " + keyword, ex);
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Search Error", "Failed to search for player: " + ex.getMessage()));
        }
    }

    private void refreshPlayerTable() {
        if (adminController == null) {
            logger.warning("AdminView: refreshPlayerTable - adminController is null. Cannot refresh.");
            Platform.runLater(() -> { if (this.playerTable != null) this.playerTable.setPlaceholder(new Label("Admin controller not available. Cannot load players."));});
            return;
        }
        logger.info("AdminView: refreshPlayerTable - Attempting to fetch all players with token.");
        try {
            List<Player> playerList = adminController.getAllPlayers(token);
            Platform.runLater(() -> {
                if (playerTable == null) { logger.severe("AdminView: refreshPlayerTable (UI) - playerTable is NULL."); return; }
                if (playerList != null) {
                    ObservableList<Player> itemsToSet = FXCollections.observableArrayList(playerList);
                    playerTable.setItems(itemsToSet);
                    playerTable.refresh(); 
                    logger.info("AdminView: refreshPlayerTable (UI) - Table items set/refreshed (" + itemsToSet.size() + " players).");
                    if (itemsToSet.isEmpty()){
                        playerTable.setPlaceholder(new Label("No players found or registered yet."));
                    }
                } else {
                    playerTable.setItems(FXCollections.emptyObservableList());
                    playerTable.setPlaceholder(new Label("Failed to load player data (list was null). Check server logs."));
                    logger.warning("AdminView: refreshPlayerTable (UI) - Received NULL player list from controller.");
                    showAlert(Alert.AlertType.WARNING, "Data Issue", "Could not retrieve player list (received null).");
                }
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, "AdminView: refreshPlayerTable - Exception during data fetch or UI update.", e);
            Platform.runLater(() -> {
                if (playerTable != null) {
                    playerTable.setItems(FXCollections.emptyObservableList());
                    playerTable.setPlaceholder(new Label("Error loading players: " + e.getMessage()));
                }
                showAlert(Alert.AlertType.ERROR, "Player Data Error", "Failed to refresh player list: " + e.getMessage());
            });
        }
    }

    private void handleSetWaitingTime() {
        if (adminController == null) { showAlert(Alert.AlertType.ERROR, "Error", "Admin Controller not available."); return; }
        try {
            int newTime = Integer.parseInt(waitingTimeInputGS.getText().trim());
            if (newTime < 1 || newTime > 1200) { 
                showAlert(Alert.AlertType.WARNING, "Input Validation", "Waiting time must be between 1 and 1200 seconds."); return;
            }
            adminController.set_waiting_time(newTime, token);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Matchmaking waiting time update request sent.");
            fetchAndDisplayGameSettings(); 
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid number format for waiting time. Please enter digits only.");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error setting waiting time", ex);
            showAlert(Alert.AlertType.ERROR, "Operation Error", "Failed to set waiting time: " + ex.getMessage());
        }
    }

    private void handleSetRoundDuration() {
        if (adminController == null) { showAlert(Alert.AlertType.ERROR, "Error", "Admin Controller not available."); return; }
        try {
            int newTime = Integer.parseInt(roundDurationInputGS.getText().trim());
            if (newTime < 10 || newTime > 600) { 
                showAlert(Alert.AlertType.WARNING, "Input Validation", "Round duration must be between 10 and 600 seconds."); return;
            }
            adminController.set_round_duration(newTime, token);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Round duration update request sent.");
            fetchAndDisplayGameSettings(); 
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid number format for round duration. Please enter digits only.");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error setting round duration", ex);
            showAlert(Alert.AlertType.ERROR, "Operation Error", "Failed to set round duration: " + ex.getMessage());
        }
    }

    private void fetchAndDisplayGameSettings() {
        if (adminController == null) { logger.warning("AdminView: fetchAndDisplayGameSettings - adminController is null."); return; }
        logger.info("AdminView: Fetching current game settings.");
        try {
            
            int waitingTime = adminController.get_waiting_time(token);
            int roundDuration = adminController.get_round_duration(token);
            Platform.runLater(() -> {
                currentWaitingTimeLabelGS.setText("Current Matchmaking Waiting Time: " + waitingTime + "s");
                waitingTimeInputGS.setPromptText(String.valueOf(waitingTime) + "s (current)"); 
                waitingTimeInputGS.clear(); 

                currentRoundDurationLabelGS.setText("Current Round Duration (Guessing): " + roundDuration + "s");
                roundDurationInputGS.setPromptText(String.valueOf(roundDuration) + "s (current)");
                roundDurationInputGS.clear();
                logger.info("AdminView: Game settings displayed: WT=" + waitingTime + ", RD=" + roundDuration);
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, "AdminView: Error fetching game settings.", e);
            Platform.runLater(() -> {
                currentWaitingTimeLabelGS.setText("Current Waiting Time: Error loading");
                currentRoundDurationLabelGS.setText("Current Round Duration: Error loading");
                showAlert(Alert.AlertType.ERROR, "Settings Load Error", "Failed to fetch current game settings: " + e.getMessage());
            });
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null); 
            alert.setContentText(content);
            alert.showAndWait();
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(alertType);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(content);
                alert.showAndWait();
            });
        }
    }

    private Optional<ButtonType> showConfirmationDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        return alert.showAndWait();
    }
}