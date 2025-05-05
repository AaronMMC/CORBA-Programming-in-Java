package com.cs9322.team05.server.impl;

import ModifiedHangman.*;
import com.cs9322.team05.server.dao.GameDao;
import com.cs9322.team05.server.model.Game;
import com.cs9322.team05.server.session.PendingGameManager;
import com.cs9322.team05.server.session.SessionManager;

import java.util.*;

public class GameServiceImpl extends GameServicePOA {
    private GameDao gameDao;
    private SessionManager sessionManager;
    private PendingGameManager pendingGameManager;
    private Map<String, Game> activeGames;


    public GameServiceImpl(SessionManager sessionManager, GameDao gameDao, PendingGameManager pendingGameManager) {
        this.sessionManager = sessionManager;
        this.gameDao = gameDao;
        this.activeGames = new HashMap<>();
        this.pendingGameManager = pendingGameManager;
    }



    @Override
    public GameInfo start_game(String username, String token) throws PlayerNotLoggedInException {
        if (!pendingGameManager.isPendingGameExists()) {
            String gameId = System.currentTimeMillis() + "-" + new Random().nextInt(10000);
            int roundDuration = gameDao.getCurrentRoundLength();
            pendingGameManager.setPendingGame(new Game(gameId, roundDuration, username));
            pendingGameManager.startCountdownToStartGame(gameDao.getCurrentRoundLength());
        }
        else
            pendingGameManager.addPlayer(new GamePlayer(username, 0));

        String gameId = pendingGameManager.getPendingGameId();
        int roundLength = pendingGameManager.getPendingGameRoundDuration();
        int remainingWaitingTime = 0;
        return new GameInfo(gameId, roundLength, remainingWaitingTime);
    }


    @Override
    public void registerCallback(ClientCallback callback, String token) throws PlayerNotLoggedInException {
        sessionManager.addCallback(callback);
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
