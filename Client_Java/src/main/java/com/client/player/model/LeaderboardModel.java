
package com.client.player.model;
import ModifiedHangman.GamePlayer;
import ModifiedHangman.Leaderboard;
import ModifiedHangman.PlayerNotLoggedInException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardModel {
  private final GameModel gameModelImpl;
  public LeaderboardModel(GameModel gameModel) { this.gameModelImpl = gameModel; }

  public List<GamePlayer> fetchTop5() throws PlayerNotLoggedInException {
    Leaderboard lb = gameModelImpl.getLeaderboard();
    if (lb == null || lb.players == null) {
      return new ArrayList<>(); 
    }
    return Arrays.stream(lb.players)
            .sorted(Comparator.comparingInt((GamePlayer p)->p.wins).reversed())
            .limit(5)
            .collect(Collectors.toList());
  }
}