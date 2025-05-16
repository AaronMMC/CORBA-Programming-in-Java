package com.cs9322.team05.client.player.services;

import ModifiedHangman.GamePlayer;
import com.cs9322.team05.client.player.interfaces.HomeViewInterface;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;

import java.util.List;

public class HomeView implements HomeViewInterface {
    private final VBox root = new VBox(20); 
    private final Label welcomeLabel = new Label(); 
    private final Button startBtn = new Button("Start New Game");
    private final Button leaderboardBtn = new Button("View Leaderboard");
    private final Button logoutBtn = new Button("Logout");

    
    

    private Runnable onStartGame;
    private Runnable onViewLeaderboard;
    private Runnable onLogout;


    public HomeView(String userToken, HomeController controller) { 
        
        welcomeLabel.setFont(new Font("Arial", 24));
        welcomeLabel.setText("Welcome!"); 

        
        double buttonWidth = 200;
        Font buttonFont = new Font("Arial", 16);

        startBtn.setPrefWidth(buttonWidth);
        startBtn.setFont(buttonFont);
        startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");


        leaderboardBtn.setPrefWidth(buttonWidth);
        leaderboardBtn.setFont(buttonFont);
        leaderboardBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");


        logoutBtn.setPrefWidth(buttonWidth);
        logoutBtn.setFont(buttonFont);
        logoutBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");


        root.setAlignment(Pos.CENTER); 
        root.setPadding(new Insets(40)); 
        root.getChildren().addAll(welcomeLabel, startBtn, leaderboardBtn, logoutBtn);

        
        startBtn.setOnAction(e -> {
            if (onStartGame != null) onStartGame.run();
        });
        leaderboardBtn.setOnAction(e -> {
            if (onViewLeaderboard != null) onViewLeaderboard.run();
        });
        logoutBtn.setOnAction(e -> {
            if (onLogout != null) onLogout.run();
        });
    }

    public void setWelcomeMessage(String username) {
        if (username != null && !username.isEmpty()) {
            welcomeLabel.setText("Welcome, " + username + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
    }

    @Override
    public Parent getRootPane() { 
        return root;
    }

    @Override
    public void openGameScreen() {
        
        
    }

    @Override
    public void showLeaderboard(List<GamePlayer> top5) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Overall Leaderboard");
        alert.setHeaderText("Top 5 Players (by Wins)");
        alert.initModality(Modality.APPLICATION_MODAL); 

        if (top5 == null || top5.isEmpty()) {
            alert.setContentText("The leaderboard is currently empty or could not be retrieved.");
        } else {
            StringBuilder sb = new StringBuilder();
            int rank = 1;
            for (GamePlayer p : top5) {
                sb.append(rank++).append(". ").append(p.username).append(" – Wins: ").append(p.wins).append("\n");
            }
            alert.setContentText(sb.toString());
        }
        alert.showAndWait();
    }

    @Override
    public void returnToLogin() {
        
        
    }

    @Override
    public void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }


    public void setOnStartGame(Runnable cb) {
        this.onStartGame = cb;
    }

    public void setOnViewLeaderboard(Runnable cb) {
        this.onViewLeaderboard = cb;
    }

    public void setOnLogout(Runnable cb) {
        this.onLogout = cb;
    }

}