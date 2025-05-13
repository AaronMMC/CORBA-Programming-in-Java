// HomeController.java
package com.cs9322.team05.client.player.services;
import ModifiedHangman.PlayerNotLoggedInException;
import com.cs9322.team05.client.player.model.LoginModel;
import com.cs9322.team05.client.player.model.LeaderboardModel;
import com.cs9322.team05.client.player.model.GameModel;
import com.cs9322.team05.client.player.interfaces.HomeViewInterface;

public class HomeController {
  private final LoginModel auth;
  private final GameModel gameModel;
  private final LeaderboardModel lbModel;
  private HomeViewInterface view;

  public HomeController(LoginModel auth,
                        GameModel gameModel,
                        LeaderboardModel lbModel,
                        HomeViewInterface view) {
    this.auth = auth;
    this.gameModel = gameModel;
    this.lbModel = lbModel;
    this.view = view;
  }

  public void onStartGame()        { view.openGameScreen(); }
  public void onViewLeaderboard()  {
    try {
      view.showLeaderboard(lbModel.fetchTop5());
    } catch (PlayerNotLoggedInException e) {
      view.showError("Cannot fetch leaderboard");
    }
  }
  public void onLogout(String token) {
    try {
      auth.logout(token);
      view.returnToLogin();
    } catch (PlayerNotLoggedInException e) {
      view.showError("Logout failed");
    }
  }

  public void setHomeView(HomeViewInterface view) {
    this.view = view;
  }
}
