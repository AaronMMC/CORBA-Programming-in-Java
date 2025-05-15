// HomeViewInterface.java
package com.cs9322.team05.client.player.interfaces;
import ModifiedHangman.GamePlayer;
import java.util.List;

public interface HomeViewInterface {
  void openGameScreen();
  void showLeaderboard(List<GamePlayer> top5);
  void returnToLogin();
  void showError(String msg);
}
