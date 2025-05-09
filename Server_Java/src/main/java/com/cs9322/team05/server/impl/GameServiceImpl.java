package com.cs9322.team05.server.impl;

import ModifiedHangman.*;
import com.cs9322.team05.server.dao.GameDao;
import com.cs9322.team05.server.dao.PlayerDao;
import com.cs9322.team05.server.model.Game;
import com.cs9322.team05.server.manager.PendingGameManager;
import com.cs9322.team05.server.manager.SessionManager;

import java.util.*;
import java.util.stream.Collectors;

public class GameServiceImpl extends GameServicePOA {
    private GameDao gameDao;
    private PlayerDao playerDao;
    private SessionManager sessionManager;
    private PendingGameManager pendingGameManager;
    private Map<String, Game> activeGames;


    public GameServiceImpl(SessionManager sessionManager, GameDao gameDao, PlayerDao playerDao, PendingGameManager pendingGameManager) {
        this.sessionManager = sessionManager;
        this.gameDao = gameDao;
        this.playerDao = playerDao;
        this.activeGames = new HashMap<>();
        this.pendingGameManager = pendingGameManager;
    }



    @Override
    public GameInfo start_game(String username, String token) throws PlayerNotLoggedInException {
        if (!sessionManager.isSessionValid(token))
            throw new PlayerNotLoggedInException("Player is not Logged in.");

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
        if (!sessionManager.isSessionValid(token))
            throw new PlayerNotLoggedInException("Player is not Logged in.");

        sessionManager.addCallback(callback);
    }



    @Override
    public GuessResponse guessLetter(String username, String gameId, char letter, String token) throws GameNotFoundException, PlayerNotLoggedInException {
        if (!sessionManager.isSessionValid(token))
            throw new PlayerNotLoggedInException("Player is not Logged in.");


        return null;
    }

    @Override
    public Leaderboard get_leaderboard(String token) throws PlayerNotLoggedInException {
        List<Player> players = playerDao.getAllPlayers();

        List<Player> sortedPlayers = players.stream()
                .sorted(Comparator.comparingInt(Player::getWins).reversed())
                .collect(Collectors.toList());

        return new Leaderboard(sortedPlayers);
    }


    public void addActiveGame(Game pendingGame) { // TODO :  make this not exposed to the client side
        activeGames.put(pendingGame.getGameId(), pendingGame);
    }
}
