// HomeView.java
package com.cs9322.team05.client.player.services;
import ModifiedHangman.GamePlayer;
import com.cs9322.team05.client.player.interfaces.HomeViewInterface;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.List;

public class HomeView implements HomeViewInterface {
  private final VBox root = new VBox(20);
  private final Button startBtn       = new Button("Start Game");
  private final Button leaderboardBtn = new Button("View Leaderboard");
  private final Button logoutBtn      = new Button("Logout");
  private HomeController controller;
  private String token;

  public HomeView(String token, HomeController controller) {
    this.token = token;
    this.controller = controller;
    root.getChildren().addAll(startBtn, leaderboardBtn, logoutBtn);
    startBtn.setOnAction(e -> controller.onStartGame());
    leaderboardBtn.setOnAction(e -> controller.onViewLeaderboard());
    logoutBtn.setOnAction(e -> controller.onLogout(token));
  }

  public Parent getRoot() { return root; }
  public void openGameScreen() { /* swap to GameView scene */ }
  public void showLeaderboard(List<GamePlayer> top5) {
    StringBuilder sb = new StringBuilder();
    top5.forEach(p -> sb.append(p.username).append(" – ").append(p.wins).append("\n"));
    new Alert(Alert.AlertType.INFORMATION, sb.toString()).showAndWait();
  }
  public void returnToLogin() { /* swap back to LoginView scene */ }
  public void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg).showAndWait(); }
}
