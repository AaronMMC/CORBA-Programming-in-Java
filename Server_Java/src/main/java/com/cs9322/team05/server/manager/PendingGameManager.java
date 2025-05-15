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
        this.pendingGame = game;
    }


    public synchronized void addPlayer(GamePlayer player) {
        pendingGame.addPlayer(player);
    }


    public String getPendingGameId() {
        return pendingGame.getGameId();
    }


    public int getPendingGameRoundDuration() {
        return pendingGame.getRoundDuration();
    }

    public int getRemainingWaitingTimeInSeconds() {
        return remainingWaitingTimeInSeconds;
    }

    public void startCountdownToStartGame(int currentRoundLength) {
        remainingWaitingTimeInSeconds = currentRoundLength;

        if (pendingGameTask != null && !pendingGameTask.isDone())
            pendingGameTask.cancel(true);


        pendingGameTask = scheduler.scheduleAtFixedRate(() -> {
            synchronized (this) {
                if (remainingWaitingTimeInSeconds > 0)
                    remainingWaitingTimeInSeconds -= 1;
                else {
                    remainingWaitingTimeInSeconds = 0;
                    pendingGameTask.cancel(false);

                    gameService.addActiveGame(pendingGame);
                    pendingGame = null;
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }


}
