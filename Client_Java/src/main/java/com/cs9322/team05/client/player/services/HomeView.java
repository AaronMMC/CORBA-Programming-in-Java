
package com.cs9322.team05.client.player.services;

import ModifiedHangman.GamePlayer;
import com.cs9322.team05.client.player.interfaces.HomeViewInterface;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.List;

public class HomeView implements HomeViewInterface {
    private final VBox root = new VBox(20);
    private final Button startBtn = new Button("Start Game");
    private final Button leaderboardBtn = new Button("View Leaderboard");
    private final Button logoutBtn = new Button("Logout");
    private final HomeController controller;
    private final String token;
    private Runnable onStartGame;
    private Runnable onViewLeaderboard;
    private Runnable onLogout;


    public HomeView(String token, HomeController controller) {
        this.token = token;
        this.controller = controller;
        root.getChildren().addAll(startBtn, leaderboardBtn, logoutBtn);
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

    public Parent getRoot() {
        return root;
    }

    public void openGameScreen() { /* swap to GameView scene */ }

    public void showLeaderboard(List<GamePlayer> top5) {
        StringBuilder sb = new StringBuilder();
        top5.forEach(p -> sb.append(p.username).append(" – ").append(p.wins).append("\n"));
        new Alert(Alert.AlertType.INFORMATION, sb.toString()).showAndWait();
    }

    public void returnToLogin() { /* swap back to AuthenticationView scene */ }

    public void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    @Override
    public Parent getRootPane() {
        return null;
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
