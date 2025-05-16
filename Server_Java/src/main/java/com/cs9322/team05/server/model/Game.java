package com.cs9322.team05.server.model;

import ModifiedHangman.*;
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
    private List<GamePlayer> players;
    private List<GameRound> rounds;
    private final SessionManager sessionManager;

    public Game(String gameId, int roundDuration, String initialPlayerUsername) {
        System.out.println("Game Constructor: Creating gameId: " + gameId + " with roundDuration: " + roundDuration + " for player: " + initialPlayerUsername);
        this.gameId = gameId;
        this.winner = null;
        this.roundDuration = roundDuration;
        this.players = new ArrayList<>();
        this.rounds = new ArrayList<>();

        if (initialPlayerUsername != null && !initialPlayerUsername.isEmpty()) {
            this.players.add(new GamePlayer(initialPlayerUsername, 0));
        } else {
            System.out.println("Game Constructor: Warning - initialPlayerUsername is null or empty for gameId: " + gameId);
        }
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
        if (player != null) {
            System.out.println("Game.addPlayer: Adding player " + player.username + " to gameId: " + this.gameId);
            this.players.add(player);
        } else {
            System.out.println("Game.addPlayer: Attempted to add null player to gameId: " + this.gameId);
        }
    }

    public List<GameRound> getRounds() {
        return rounds;
    }

    public void addRounds(GameRound round) {
        this.rounds.add(round);
    }

    public GuessResponse guessLetter(String username, char letter) {
        System.out.println("Game.guessLetter: User " + username + " guessed '" + letter + "' in gameId: " + this.gameId);
        boolean playerExists = players.stream().anyMatch(p -> p.username.equals(username));
        if (!playerExists) {
            System.out.println("Game.guessLetter: ERROR - Player " + username + " not found in gameId: " + this.gameId);
            throw new RuntimeException("Player " + username + " is not part of the game.");
        }

        if (rounds.isEmpty()) {
            System.out.println("Game.guessLetter: ERROR - No rounds started yet for gameId: " + this.gameId);
            throw new RuntimeException("No rounds available to guess in.");
        }
        GameRound currentRound = rounds.get(rounds.size() - 1);
        return currentRound.guessLetter(username, letter);
    }

    public void startGame() {

        if (players.isEmpty()) {
            System.out.println("Game.startGame: No players in game " + this.gameId + ". Cannot start round.");
            return;
        }

        String wordForRound = WordDao.getInstance().getAWord();
        if (wordForRound == null || wordForRound.isEmpty()) {
            System.out.println("Game.startGame: ERROR - Failed to get a word for gameId: " + this.gameId + ". Cannot start round.");
            return;
        }

        System.out.println("Game.startGame: Word selected for gameId " + wordForRound);
        GameRound gameRound = new GameRound(new ArrayList<>(players), wordForRound, ++roundCount);
        rounds.add(gameRound);
        System.out.println("Game.startGame: New GameRound created for gameId: " + this.gameId + ", roundNumber: " + roundCount);

        try {
            System.out.println("Game.startGame: Attempting to call gameRound.startRound for gameId: " + this.gameId + " with duration: " + this.roundDuration);
            gameRound.startRound(this.roundDuration, this.gameId, () -> {
                boolean isGameOver = false;
                GamePlayer potentialGameWinner = null;

                System.out.println("Game.startGame: Checking for game over condition in gameId: " + this.gameId);
                for (GamePlayer player : players) {
                    if (player.wins >= 3) {
                        potentialGameWinner = player;
                        isGameOver = true;
                        System.out.println("Game.startGame: Game over condition met for gameId: " + this.gameId + ". Player " + player.username + " has " + player.wins + " wins.");
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
                            if (callback != null) {
                                System.out.println("Game.startGame: Sending endGame callback to " + player.username + " for gameId: " + this.gameId);
                                callback.endGame(gameResult);
                            } else {
                                System.out.println("Game.startGame: No callback found for player " + player.username + " in gameId: " + this.gameId + " during endGame.");
                            }
                        } catch (Exception e) {
                            System.out.println("Game.startGame: EXCEPTION sending endGame callback to " + player.username + " - " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("Game.startGame: Game " + this.gameId + " is NOT over. Scheduling next round.");
                    ScheduledExecutorService delayScheduler = Executors.newSingleThreadScheduledExecutor();
                    delayScheduler.schedule(() -> {
                        System.out.println("Game.startGame (Delayed Task): Triggering next round for gameId: " + this.gameId);
                        startGame();
                        delayScheduler.shutdown();
                    }, 7, TimeUnit.SECONDS);
                }
            });
            System.out.println("Game.startGame: gameRound.startRound called for gameId: " + this.gameId);
        } catch (Exception e) {
            System.out.println("Game.startGame: EXCEPTION during gameRound.startRound for gameId: " + this.gameId + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

}