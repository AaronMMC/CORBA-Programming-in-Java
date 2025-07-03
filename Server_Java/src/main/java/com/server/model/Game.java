package com.server.model;

import ModifiedHangman.*;
import com.server.dao.UserDao;
import com.server.dao.WordDao;
import com.server.impl.GameServiceImpl;
import com.server.manager.SessionManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Represents a multiplayer Hangman game instance with players, rounds, and game state.
 */
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


    /** Processes a letter guess from a specific player. */
    public GuessResponse guessLetter(String username, char letter) {
        boolean playerExists = players.stream().anyMatch(p -> p.username.equals(username));
        if (!playerExists)
            throw new RuntimeException("Player " + username + " is not part of the game.");

        // won't happen, just a check to make sure
        if (rounds.isEmpty())
            throw new RuntimeException("No rounds available to guess in.");

        GameRound currentRound = rounds.get(rounds.size() - 1);
        return currentRound.guessLetter(username, letter);
    }


    /** Starts a new round or ends the game if a player wins. */
    public void startGame() {
        if (players.isEmpty()) return;

        String wordForRound = WordDao.getInstance().getAWord();
        if (wordForRound == null || wordForRound.isEmpty()) return;

        System.out.println("Game.startGame: Word selected for gameId " + wordForRound);
        GameRound gameRound = new GameRound(new ArrayList<>(players), wordForRound, ++roundCount);
        rounds.add(gameRound);

        try {
            gameRound.startRound(this.roundDuration, this.gameId, () -> {
                GamePlayer potentialWinner = checkIfGameOver();

                GamePlayer[] leaderboard = players.stream()
                        .sorted(Comparator.comparingInt(p -> -p.wins))
                        .toArray(GamePlayer[]::new);

                if (potentialWinner != null) {
                    this.winner = potentialWinner;
                    System.out.printf("Game.startGame: Game %s IS OVER. Winner: %s. Notifying players in 5 seconds.%n", this.gameId, winner.username);

                    ScheduledExecutorService notifier = Executors.newSingleThreadScheduledExecutor();
                    notifier.schedule(() -> {
                        notifyPlayersGameOver(winner, leaderboard);
                        GameServiceImpl gameService = GameServiceImpl.getInstance();
                        gameService.finishActiveGame(gameId);
                        notifier.shutdown();
                    }, 5, TimeUnit.SECONDS);
                } else {
                    scheduleNextRound();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Checks if any player has met the win condition. */
    private GamePlayer checkIfGameOver() {
        for (GamePlayer player : players) {
            if (player.wins >= 3) {
                UserDao.getInstance().addGameWinsOfPlayer(player.username);
                return player;
            }
        }
        return null;
    }


    /** Notifies all players of game completion and the final result. */
    private void notifyPlayersGameOver(GamePlayer winner, GamePlayer[] leaderboard) {
        for (GamePlayer player : players) {
            GameResult result = new GameResult(this.gameId, winner.username, leaderboard);
            try {
                ClientCallback callback = sessionManager.getCallback(player.username);
                if (callback != null)
                    callback.endGame(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /** Schedules the start of the next round after a short delay. */
    private void scheduleNextRound() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            startGame();
            scheduler.shutdown();
        }, 6, TimeUnit.SECONDS);
    }

    /** Checks if a given player is part of this game. */
    public boolean hasPlayer(String username) {
        for (GamePlayer player : players)
            if (username.equals(player.username))
                return true;
        return false;
    }
}