package com.cs9322.team05.server.manager;

import ModifiedHangman.GamePlayer;
import com.cs9322.team05.server.impl.GameServiceImpl;
import com.cs9322.team05.server.model.Game;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PendingGameManager {
    private Game pendingGame;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pendingGameTask;
    private int remainingWaitingTimeInSeconds;
    private GameServiceImpl gameService;

    public PendingGameManager() {
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void setGameService(GameServiceImpl gameService) {
        this.gameService = gameService;
    }

    public synchronized boolean isPendingGameExists() {
        return pendingGame != null;
    }

    public synchronized void setPendingGame(Game game) {
        System.out.println("PendingGameManager.setPendingGame: Setting pending game to gameId: " + (game != null ? game.getGameId() : "null"));
        this.pendingGame = game;
    }

    public synchronized void addPlayer(GamePlayer player) {
        if (player == null) {
            System.out.println("PendingGameManager.addPlayer: Attempted to add a null player. Ignoring.");
            return;
        }
        if (pendingGame != null) {
            System.out.println("PendingGameManager.addPlayer: Adding player " + player.username + " to game " + pendingGame.getGameId());
            pendingGame.addPlayer(player);
        } else {
            System.out.println("PendingGameManager.addPlayer: ERROR - Attempted to add player " + player.username + " but pendingGame is null.");
        }
    }

    public synchronized String getPendingGameId() {
        if (pendingGame != null) {
            return pendingGame.getGameId();
        }
        System.out.println("PendingGameManager.getPendingGameId: Warning - pendingGame is null. Returning null for gameId.");
        return null;
    }

    public synchronized int getPendingGameRoundDuration() {
        if (pendingGame != null) {
            return pendingGame.getRoundDuration();
        }
        System.out.println("PendingGameManager.getPendingGameRoundDuration: Warning - pendingGame is null. Returning 0 for round duration.");
        return 0;
    }

    public synchronized int getRemainingWaitingTimeInSeconds() {
        return remainingWaitingTimeInSeconds;
    }

    public void startCountdownToStartGame(int matchmakingWaitDurationInSeconds) {
        System.out.println("PendingGameManager.startCountdownToStartGame: Initializing matchmaking countdown with " + matchmakingWaitDurationInSeconds + " seconds for gameId: " + (pendingGame != null ? pendingGame.getGameId() : "N/A"));

        synchronized (this) {
            this.remainingWaitingTimeInSeconds = matchmakingWaitDurationInSeconds;

            if (pendingGameTask != null && !pendingGameTask.isDone()) {
                System.out.println("PendingGameManager.startCountdownToStartGame: Cancelling existing pendingGameTask.");
                pendingGameTask.cancel(true);
            }

            pendingGameTask = scheduler.scheduleAtFixedRate(() -> {
                synchronized (this) {
                    if (pendingGame == null && remainingWaitingTimeInSeconds > 0) {
                        System.out.println("PendingGameManager CountdownTask: pendingGame is null but remainingWaitingTimeInSeconds ("+remainingWaitingTimeInSeconds+") > 0. Cancelling task.");
                        if(pendingGameTask != null) pendingGameTask.cancel(false);
                        return;
                    }

                    if (remainingWaitingTimeInSeconds > 0) {
                        remainingWaitingTimeInSeconds -= 1;
                    } else {
                        if (pendingGameTask != null) {
                            pendingGameTask.cancel(false);
                        }

                        Game gameToStart = pendingGame;
                        pendingGame = null;
                        remainingWaitingTimeInSeconds = 0;

                        if (gameToStart != null) {
                            System.out.println("PendingGameManager CountdownTask: Matchmaking wait time reached zero for gameId: " + gameToStart.getGameId());
                            System.out.println("PendingGameManager CountdownTask: Attempting to call gameService.addActiveGame for gameId: " + gameToStart.getGameId() + " with players: " + gameToStart.getPlayers().size());
                            if (gameService != null) {
                                gameService.addActiveGame(gameToStart);
                                System.out.println("PendingGameManager CountdownTask: gameService.addActiveGame presumably called for gameId: " + gameToStart.getGameId());
                            } else {
                                System.out.println("PendingGameManager CountdownTask: FATAL - gameService is null. Cannot start game: " + gameToStart.getGameId());
                            }
                            System.out.println("PendingGameManager CountdownTask: pendingGame reference has been cleared after processing gameId: " + gameToStart.getGameId());
                        } else {
                            System.out.println("PendingGameManager CountdownTask: Matchmaking wait time reached zero, but pendingGame was already null. No action taken to start game.");
                        }
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);
            System.out.println("PendingGameManager.startCountdownToStartGame: Matchmaking countdown task scheduled for gameId: " + (pendingGame != null ? pendingGame.getGameId() : "N/A"));
        }
    }
}