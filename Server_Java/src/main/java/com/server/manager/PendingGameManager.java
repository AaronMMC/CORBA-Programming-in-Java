package com.server.manager;

import ModifiedHangman.GamePlayer;
import com.server.impl.GameServiceImpl;
import com.server.model.Game;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * Manages the pending game state and matchmaking countdown.
 */
public class PendingGameManager {
    private Game pendingGame;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pendingGameTask;
    private int remainingWaitingTimeInSeconds;
    private GameServiceImpl gameService;

    public PendingGameManager() {
        this.scheduler = Executors.newScheduledThreadPool(1);
    }


    /**
     * Sets the game service instance used to manage active games.
     */
    public void setGameService(GameServiceImpl gameService) {
        this.gameService = gameService;
    }


    /**
     * Checks if a pending game currently exists.
     */
    public synchronized boolean isPendingGameExists() {
        return pendingGame != null;
    }


    /**
     * Sets the current pending game.
     */
    public synchronized void setPendingGame(Game game) {
        this.pendingGame = game;
    }


    /**
     * Adds a player to the pending game.
     */
    public synchronized void addPlayer(GamePlayer player) {
        if (player == null) {
            System.out.println("PendingGameManager.addPlayer: Attempted to add a null player. Ignoring.");
            return;
        }
        if (pendingGame != null)
            pendingGame.addPlayer(player);
        else
            System.out.println("PendingGameManager.addPlayer: ERROR - Attempted to add player " + player.username + " but pendingGame is null.");
    }


    /**
     * Returns the ID of the pending game.
     */
    public synchronized String getPendingGameId() {
        if (pendingGame != null)
            return pendingGame.getGameId();

        return null;
    }


    /**
     * Returns the round duration of the pending game.
     */
    public synchronized int getPendingGameRoundDuration() {
        if (pendingGame != null)
            return pendingGame.getRoundDuration();

        return 0;
    }


    /**
     * Returns the remaining waiting time for matchmaking.
     */
    public synchronized int getRemainingWaitingTimeInSeconds() {
        return remainingWaitingTimeInSeconds;
    }


    /**
     * Starts the countdown timer to transition the pending game to active state.
     */
    public void startCountdownToStartGame(int matchmakingWaitDurationInSeconds) {
        System.out.println("PendingGameManager.startCountdownToStartGame: Initializing matchmaking countdown with " + matchmakingWaitDurationInSeconds + " seconds for gameId: " + (pendingGame != null ? pendingGame.getGameId() : "N/A"));

        synchronized (this) {
            // Set the countdown timer for how long to wait before starting the game
            this.remainingWaitingTimeInSeconds = matchmakingWaitDurationInSeconds;

            // Just a simple check if ScheduledFuture countdown is active, if so, cancel it
            if (pendingGameTask != null && !pendingGameTask.isDone()) {
                System.out.println("PendingGameManager.startCountdownToStartGame: Cancelling existing pendingGameTask.");
                pendingGameTask.cancel(true);
            }

            // Schedule a task that runs every second to decrement the countdown timer
            pendingGameTask = scheduler.scheduleAtFixedRate(() -> {
                synchronized (this) {
                    if (pendingGame == null && remainingWaitingTimeInSeconds > 0) {
                        System.out.println("PendingGameManager CountdownTask: pendingGame is null but remainingWaitingTimeInSeconds ("+remainingWaitingTimeInSeconds+") > 0. Cancelling task.");
                        if(pendingGameTask != null) pendingGameTask.cancel(false);
                        return;
                    }

                    // Decrement the countdown timer each second
                    if (remainingWaitingTimeInSeconds > 0)
                        remainingWaitingTimeInSeconds -= 1;
                    else {
                        // Countdown has reached zero, time to transition the game from pending to active
                        if (pendingGameTask != null)
                            pendingGameTask.cancel(false);

                        Game gameToStart = pendingGame;
                        pendingGame = null;
                        remainingWaitingTimeInSeconds = 0;

                        // If gameService is set, move the game into the active game pool
                        if (gameToStart != null)
                            if (gameService != null)
                                gameService.addActiveGame(gameToStart);
                            else
                                System.out.println("PendingGameManager CountdownTask: FATAL - gameService is null. Cannot start game: " + gameToStart.getGameId());
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);
        }
    }
}