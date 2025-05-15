package com.cs9322.team05.server.model;

import ModifiedHangman.*;
import com.cs9322.team05.server.dao.UserDao;
import com.cs9322.team05.server.dao.WordDao;
import com.cs9322.team05.server.impl.GameServiceImpl;
import com.cs9322.team05.server.manager.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Game {
    private String gameId;
    private GamePlayer winner;
    private int roundDuration;
    private int roundCount;
    private List<GamePlayer> players;
    private List<GameRound> rounds;

    public Game(String gameId, int roundDuration, String username) {
        this.gameId = gameId;
        this.winner = null;
        this.roundDuration = roundDuration;
        this.players = new ArrayList<>();
        this.rounds = new ArrayList<>();

        this.players.add(new GamePlayer(username, 0)); // 0 is round wins
        roundCount = 0;
        sessionManager = SessionManager.getInstance();
    }


    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public GamePlayer getWinner() {
        return winner;
    }

    public void setWinner(GamePlayer winner) {
        this.winner = winner;
    }

    public int getRoundDuration() {
        return roundDuration;
    }

    public void setRoundDuration(int roundDuration) {
        this.roundDuration = roundDuration;
    }

    public int getRoundCount() {
        return roundCount;
    }

    public void setRoundCount(int roundCount) {
        this.roundCount = roundCount;
    }

    public List<GamePlayer> getPlayers() {
        return players;
    }

    public void addPlayer(GamePlayer player) {
        this.players.add(player);
    }

    public List<GameRound> getRounds() {
        return rounds;
    }

    public void addRounds(GameRound round) {
        this.rounds.add(round);
    }

    public GuessResponse guessLetter(String username, char letter) {
        boolean playerExists = players.stream().anyMatch(player -> player.username.equals(username));
        if (!playerExists) // TODO : change the exception type to a more appropriate one
            throw new RuntimeException("Player " + username + " is not part of the game.");

        GameRound currentRound = rounds.get(rounds.size() - 1);
        return currentRound.guessLetter(username, letter);
    }

    public void startGame() {
        GameRound gameRound = new GameRound(players, WordDao.getInstance().getAWord(), ++roundCount);
        gameRound.startRound(roundDuration, gameId);
        rounds.add(gameRound);

        boolean isGameOver = false;

        GamePlayer[] leaderboards = new GamePlayer[0];

        for (GamePlayer player : players) {
            if (player.wins == 3) {
                leaderboards = players.stream()
                        .sorted((p1, p2) -> Integer.compare(p2.wins, p1.wins))
                        .toArray(GamePlayer[]::new);
                isGameOver = true;
                break;
            }
        }

        if (isGameOver)
            for (GamePlayer player : players) {
                GameResult gameResult = new GameResult(gameId, player.username, leaderboards);
                sessionManager.getCallback(player.username).endGame(gameResult);
            }
        else {
            // Delay the next round by 7 seconds
            ScheduledExecutorService delayScheduler = Executors.newSingleThreadScheduledExecutor();
            delayScheduler.schedule(() -> {
                startGame();
                delayScheduler.shutdown();
            }, 7, TimeUnit.SECONDS);
        }
    }



    private SessionManager sessionManager;
}
