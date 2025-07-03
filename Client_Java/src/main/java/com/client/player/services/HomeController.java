
package com.client.player.services;
import ModifiedHangman.PlayerNotLoggedInException; 
import com.client.player.controller.MatchmakingController;
import com.client.player.model.AuthenticationModel;
import com.client.player.model.LeaderboardModel;
import com.client.player.model.GameModel;
import com.client.player.interfaces.HomeViewInterface;
import javafx.application.Platform; 

import java.util.logging.Logger;


public class HomeController {
  private static final Logger logger = Logger.getLogger(HomeController.class.getName()); 
  private final AuthenticationModel auth;
  private final GameModel gameModel; 
  private final LeaderboardModel lbModel;
  private HomeViewInterface view;

  public HomeController(AuthenticationModel auth,
                        GameModel gameModel, 
                        LeaderboardModel lbModel,
                        HomeViewInterface view) { 
    this.auth = auth;
    this.gameModel = gameModel;
    this.lbModel = lbModel;
    this.view = view; 
  }

  public void onStartGame() {
    if (view != null) {
      logger.info("HomeController: Start Game requested.");
      view.openGameScreen(); 
    } else {
      logger.warning("HomeController: onStartGame called but view is null.");
    }
  }

  public void onViewLeaderboard()  {
    if (view != null) {
      logger.info("HomeController: View Leaderboard requested.");
      try {
        if (lbModel != null) {
          view.showLeaderboard(lbModel.fetchTop5());
        } else {
          logger.warning("HomeController: LeaderboardModel is null.");
          view.showError("Leaderboard data is currently unavailable.");
        }
      } catch (PlayerNotLoggedInException e) {
        logger.warning("HomeController: PlayerNotLoggedInException when fetching leaderboard.");
        view.showError("Cannot fetch leaderboard: Please log in.");
      } catch (Exception e) { 
        logger.log(java.util.logging.Level.SEVERE, "HomeController: Error fetching leaderboard.", e);
        view.showError("Could not retrieve leaderboard: " + e.getMessage());
      }
    } else {
      logger.warning("HomeController: onViewLeaderboard called but view is null.");
    }
  }

  public void onLogout(String token) { 
    if (view != null) {
      logger.info("HomeController: Logout requested.");
      try {
        if (auth != null) {
          auth.logout(token); 
          view.returnToLogin(); 
        } else {
          logger.warning("HomeController: AuthenticationModel is null during logout.");
          view.showError("Logout service not available.");
        }
      } catch (PlayerNotLoggedInException e) {
        logger.warning("HomeController: PlayerNotLoggedInException during logout.");
        view.showError("Logout failed: " + e.getMessage() + " (Possibly already logged out)");
        view.returnToLogin(); 
      } catch (Exception e) {
        logger.log(java.util.logging.Level.SEVERE, "HomeController: Error during logout.", e);
        view.showError("Logout error: " + e.getMessage());
        view.returnToLogin(); 
      }
    } else {
      logger.warning("HomeController: onLogout called but view is null.");
    }
  }

  
  public void displayMatchmakingFailureMessage(String reason) {
    if (view != null) {
      logger.info("HomeController: Displaying matchmaking failure message - " + reason);
      String userMessage = "Matchmaking failed: " + reason + ". Please try again.";
      if (MatchmakingController.REASON_TIMEOUT.equals(reason)) {
        userMessage = "No game could be found in time. Please try again later.";
      } else if (MatchmakingController.REASON_SERVER_ERROR.equals(reason)) {
        userMessage = "The server could not assign you to a game. Please try again.";
      }
      
      final String finalUserMessage = userMessage;
      Platform.runLater(() -> view.showError(finalUserMessage));
    } else {
      logger.warning("HomeController: displayMatchmakingFailureMessage called but view is null. Message: " + reason);
    }
  }

  public void setHomeView(HomeViewInterface view) {
    this.view = view;
  }

  public AuthenticationModel getAuthModel() { 
    return auth;
  }
}