package com.cs9322.team05.server.session;

import ModifiedHangman.GamePlayer;
import com.cs9322.team05.server.model.Game;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PendingGameManager {
    private Game pendingGame;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pendingGameTask;
    private int remainingSeconds;

    public PendingGameManager() {
        this.scheduler = Executors.newScheduledThreadPool(1);
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

    public void startCountdownToStartGame(int currentRoundLength) {
        remainingSeconds = currentRoundLength;

        if (pendingGameTask != null && !pendingGameTask.isDone())
            pendingGameTask.cancel(true);


        pendingGameTask = scheduler.scheduleAtFixedRate(() -> {
            synchronized (this) {
                if (remainingSeconds > 0)
                    remainingSeconds -= 1;
                else {
                    pendingGame = null;
                    remainingSeconds = 0;
                    pendingGameTask.cancel(false);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }


}
