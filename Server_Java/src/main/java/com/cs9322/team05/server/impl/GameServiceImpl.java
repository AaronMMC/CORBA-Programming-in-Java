package com.cs9322.team05.server.impl;

import ModifiedHangman.*;
import com.cs9322.team05.server.dao.GameDao;
import com.cs9322.team05.server.dao.PlayerDao;
import com.cs9322.team05.server.model.Game;
import com.cs9322.team05.server.manager.PendingGameManager;
import com.cs9322.team05.server.manager.SessionManager;
import com.cs9322.team05.server.model.GameRound;

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
        if (isTokenNotValid(token))
            throw new PlayerNotLoggedInException("Player is not Logged in.");

        if (pendingGameDoesNotExist()) {
            String gameId = UUID.randomUUID().toString();
            int roundDuration = gameDao.getCurrentRoundLength();
            pendingGameManager.setPendingGame(new Game(gameId, roundDuration, username));
            pendingGameManager.startCountdownToStartGame(roundDuration); }
        else
            pendingGameManager.addPlayer(new GamePlayer(username, 0));

        String gameId = pendingGameManager.getPendingGameId();
        int roundLength = pendingGameManager.getPendingGameRoundDuration();
        int remainingWaitingTime = pendingGameManager.getRemainingWaitingTimeInSeconds();
        return new GameInfo(gameId, roundLength, remainingWaitingTime);
    }


    @Override
    public void registerCallback(ClientCallback callback, String token) throws PlayerNotLoggedInException {
        if (isTokenNotValid(token))
            throw new PlayerNotLoggedInException("Player is not Logged in.");

        sessionManager.addCallback(callback, token);
    }



    @Override
    public GuessResponse guessLetter(String username, String gameId, char letter, String token) throws GameNotFoundException, PlayerNotLoggedInException {
        if (isTokenNotValid(token))
            throw new PlayerNotLoggedInException("Player is not Logged in.");

        Game game = activeGames.get(gameId);
        if (game == null)
            throw new GameNotFoundException("Game with ID " + gameId + " not found.");

        return game.guessLetter(username, letter);
    }

    @Override
    public Leaderboard get_leaderboard(String token) throws PlayerNotLoggedInException {
        if (isTokenNotValid(token))
            throw new PlayerNotLoggedInException("Player is not Logged in.");

        List<Player> players = playerDao.getAllPlayers();

        List<Player> sortedPlayers = players.stream()
                .filter(player -> player.getWins() > 0)
                .sorted(Comparator.comparingInt(Player::getWins).reversed())
                .collect(Collectors.toList());

        return new Leaderboard(sortedPlayers);
    }


    // this ain't declared in the .idl file, so clients can't call it? should be //
    // TODO :  make this not exposed to the client side (this method is used by another server class which is not in the same package so I can't use protected or the package protection)
    public void addActiveGame(Game pendingGame) {
        activeGames.put(pendingGame.getGameId(), pendingGame);
    }


    private boolean pendingGameDoesNotExist() {
        return !pendingGameManager.isPendingGameExists();
    }

    private boolean isTokenNotValid(String token) {
        return !sessionManager.isSessionValid(token);
    }
}
