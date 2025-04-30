package com.cs9322.team05.server.impl;

import ModifiedHangman.*;
import com.cs9322.team05.server.dao.GameDao;
import com.cs9322.team05.server.model.Game;
import com.cs9322.team05.server.session.SessionManager;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameServiceImpl extends GameServicePOA {
    private GameDao gameDao;
    private SessionManager sessionManager;
    private Game pendingGame;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pendingGameTask;
    private int pendingGameCountdown;
    private Map<String, Game> activeGames;


    // move the pending games related fields to a different class called pending game manager

    public GameServiceImpl(SessionManager sessionManager, GameDao gameDao) {
        this.sessionManager = sessionManager;
        this.gameDao = gameDao;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }


    @Override
    public void registerCallback(ClientCallback callback, String token) throws PlayerNotLoggedInException {

    }



    @Override
    public GameInfo start_game(String username, String token) throws PlayerNotLoggedInException {
        if (pendingGame == null) {
            String gameId = System.currentTimeMillis() + "-" + new Random().nextInt(10000);
            int roundDuration = gameDao.getCurrentRoundLength();
            pendingGame = new Game(gameId, roundDuration, username);
            startPendingGameCountdown();
        }
        else
            pendingGame.addPlayer(new GamePlayer(username, 0));

        String gameId = pendingGame.getGameId();
        int roundLength = pendingGame.getRoundDuration();
        int remainingWaitingTime = 0;
        return new GameInfo(gameId, roundLength, remainingWaitingTime);
    }



    private void startPendingGameCountdown() {
        pendingGameCountdown = gameDao.getCurrentWaitingTimeLength();

        if (pendingGameTask != null && !pendingGameTask.isDone())
            pendingGameTask.cancel(true);


        pendingGameTask = scheduler.scheduleAtFixedRate(() -> {
            synchronized (this) {
                if (pendingGameCountdown > 0)
                    pendingGameCountdown -= 1;
                else {
                    pendingGame = null;
                    pendingGameCountdown = 0;
                    pendingGameTask.cancel(false);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }



    @Override
    public GuessResponse guessLetter(String username, String gameId, char letter, String token) throws GameNotFoundException, PlayerNotLoggedInException {
        return null;
    }

    @Override
    public Leaderboard get_leaderboard(String token) throws PlayerNotLoggedInException{
        return null;
    }
}
