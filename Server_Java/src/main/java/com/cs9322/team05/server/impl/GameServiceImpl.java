package com.cs9322.team05.server.impl;

import ModifiedHangman.*;
import com.cs9322.team05.server.dao.GameDao;
import com.cs9322.team05.server.dao.UserDao;
import com.cs9322.team05.server.model.Game;
import com.cs9322.team05.server.manager.PendingGameManager;
import com.cs9322.team05.server.manager.SessionManager;

import java.util.*;
import java.util.stream.Collectors;

public class GameServiceImpl extends GameServicePOA {
    private static GameServiceImpl instance;

    private final GameDao gameDao;
    private final UserDao userDao;
    private final SessionManager sessionManager;
    private final PendingGameManager pendingGameManager;
    private final Map<String, Game> activeGames;

    private GameServiceImpl(SessionManager sessionManager, GameDao gameDao, UserDao userDao, PendingGameManager pendingGameManager) {
        this.sessionManager = sessionManager;
        this.gameDao = gameDao;
        this.userDao = userDao;
        this.activeGames = new HashMap<>();
        this.pendingGameManager = pendingGameManager;
    }

    public static synchronized GameServiceImpl getInstance(SessionManager sessionManager, GameDao gameDao, UserDao userDao, PendingGameManager pendingGameManager) {
        if (instance == null)
            instance = new GameServiceImpl(sessionManager, gameDao, userDao, pendingGameManager);

        return instance;
    }

    public static GameServiceImpl getInstance() {
        if (instance == null)
            throw new IllegalStateException("GameServiceImpl is not yet initialized.");
        return instance;
    }



    @Override
    public GameInfo start_game(String username, String token) throws PlayerNotLoggedInException {
        if (!isTokenValid(token))
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
        if (!isTokenValid(token))
            throw new PlayerNotLoggedInException("Player is not Logged in.");

        sessionManager.addCallback(callback, token);
    }



    @Override
    public GuessResponse guessLetter(String username, String gameId, char letter, String token) throws GameNotFoundException, PlayerNotLoggedInException {
        if (!isTokenValid(token))
            throw new PlayerNotLoggedInException("Player is not Logged in.");

        Game game = activeGames.get(gameId);
        if (game == null)
            throw new GameNotFoundException("Game with ID " + gameId + " not found.");

        return game.guessLetter(username, letter);
    }

    @Override
    public Leaderboard get_leaderboard(String token) throws PlayerNotLoggedInException {
        if (!isTokenValid(token))
            throw new PlayerNotLoggedInException("Player is not Logged in.");

        List<Player> players = userDao.getAllPlayers();

        List<Player> sortedPlayers = players.stream()
                .filter(player -> player.wins > 0)
                .sorted((player1, player2) -> Integer.compare(player2.wins, player1.wins))
                .collect(Collectors.toList());

        GamePlayer[] gamePlayers = new GamePlayer[sortedPlayers.size()];
        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player player = sortedPlayers.get(i);
            gamePlayers[i] = new GamePlayer(player.username, player.wins);
        }

        return new Leaderboard(gamePlayers);
    }
    
    
    // TODO : add this to the IDL file
    public Player getPlayerByUsername(String username, String token) throws PlayerNotLoggedInException {
        if (!isTokenValid(token))
            throw new PlayerNotLoggedInException("Player is not Logged in.");
        
        return userDao.getPlayerByUsername(username);
    }




    // this ain't declared in the .idl file, so clients can't call it and it should be okay //
    public void addActiveGame(Game pendingGame) {
        if (pendingGame.getPlayers().size() == 1)
            sessionManager.getCallback(pendingGame.getPlayers().get(0).username).startGameFailed();
        else {
            activeGames.put(pendingGame.getGameId(), pendingGame);
            pendingGame.startGame();
        }
    }


    private boolean pendingGameDoesNotExist() {
        return !pendingGameManager.isPendingGameExists();
    }

    private boolean isTokenValid(String token) {
        return sessionManager.isSessionValid(token);
    }
}
