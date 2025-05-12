// LeaderboardModel.java
package com.cs9322.team05.client.player.model;
import ModifiedHangman.GamePlayer;
import ModifiedHangman.Leaderboard;
import ModifiedHangman.PlayerNotLoggedInException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardModel {
  private final GameModel gameModel;
  public LeaderboardModel(GameModel gameModel) { this.gameModel = gameModel; }

  public List<GamePlayer> fetchTop5() throws PlayerNotLoggedInException {
    Leaderboard lb = gameModel.getLeaderboard();
    return Arrays.stream(lb.players)
                 .sorted(Comparator.comparingInt((GamePlayer p)->p.wins).reversed())
                 .limit(5)
                 .collect(Collectors.toList());
  }
}
