package com.cs9322.team05.server.model;

import ModifiedHangman.*;
import com.cs9322.team05.server.dao.UserDao;
import com.cs9322.team05.server.dao.WordDao;
import com.cs9322.team05.server.manager.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Game {
    private String gameId;
    private GamePlayer winner;
    private int roundDuration;
    private int roundCount;
    private final List<GamePlayer> players;
    private final List<GameRound> rounds;
    private final SessionManager sessionManager;

    public Game(String gameId, int roundDuration, String initialPlayerUsername) {
        this.gameId = gameId;
        this.winner = null;
        this.roundDuration = roundDuration;
        this.players = new ArrayList<>();
        this.rounds = new ArrayList<>();

        if (initialPlayerUsername != null && !initialPlayerUsername.isEmpty())
            this.players.add(new GamePlayer(initialPlayerUsername, 0));
        this.roundCount = 0;
        this.sessionManager = SessionManager.getInstance();
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

    public synchronized void addPlayer(GamePlayer player) {
        if (player != null)
            this.players.add(player);
        else
            System.out.println("Game.addPlayer: Attempted to add null player to gameId: " + this.gameId);
    }

    public List<GameRound> getRounds() {
        return rounds;
    }

    public void addRounds(GameRound round) {
        this.rounds.add(round);
    }

    public GuessResponse guessLetter(String username, char letter) {
        boolean playerExists = players.stream().anyMatch(p -> p.username.equals(username));
        if (!playerExists)
            throw new RuntimeException("Player " + username + " is not part of the game.");

        if (rounds.isEmpty())
            throw new RuntimeException("No rounds available to guess in.");

        GameRound currentRound = rounds.get(rounds.size() - 1);
        return currentRound.guessLetter(username, letter);
    }

    public void startGame() {
        if (players.isEmpty())
            return;


        String wordForRound = WordDao.getInstance().getAWord();
        if (wordForRound == null || wordForRound.isEmpty())
            return;


        System.out.println("Game.startGame: Word selected for gameId " + wordForRound);
        GameRound gameRound = new GameRound(new ArrayList<>(players), wordForRound, ++roundCount);
        rounds.add(gameRound);

        try {
            gameRound.startRound(this.roundDuration, this.gameId, () -> {
                boolean isGameOver = false;
                GamePlayer potentialGameWinner = null;

                for (GamePlayer player : players) {
                    if (player.wins >= 3) {
                        potentialGameWinner = player;
                        UserDao.getInstance().addGameWinsOfPlayer(player.username);
                        isGameOver = true;
                        break;
                    }
                }

                GamePlayer[] finalLeaderboardArray = players.stream()
                        .sorted((p1, p2) -> Integer.compare(p2.wins, p1.wins))
                        .toArray(GamePlayer[]::new);

                if (isGameOver) {
                    System.out.println("Game.startGame: Game " + this.gameId + " IS OVER. Winner: " + (potentialGameWinner != null ? potentialGameWinner.username : "N/A") + ". Notifying players.");
                    this.winner = potentialGameWinner;

                    for (GamePlayer player : players) {
                        String gameWinnerUsername = (this.winner != null) ? this.winner.username : "";
                        GameResult gameResult = new GameResult(this.gameId, gameWinnerUsername, finalLeaderboardArray);

                        try {
                            ClientCallback callback = sessionManager.getCallback(player.username);
                            if (callback != null)
                                callback.endGame(gameResult);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    ScheduledExecutorService delayScheduler = Executors.newSingleThreadScheduledExecutor();
                    delayScheduler.schedule(() -> {
                        startGame();
                        delayScheduler.shutdown();
                    }, 7, TimeUnit.SECONDS);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}