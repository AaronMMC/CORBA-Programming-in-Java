package com.client.player.interfaces;
import ModifiedHangman.GamePlayer;
import javafx.scene.Parent;

import java.util.List;

public interface HomeViewInterface {
  void openGameScreen();
  void showLeaderboard(List<GamePlayer> top5);
  void returnToLogin();
  void showError(String msg);

  Parent getRootPane();
}
