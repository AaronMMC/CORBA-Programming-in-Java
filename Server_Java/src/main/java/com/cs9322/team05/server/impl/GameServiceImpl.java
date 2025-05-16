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
        if (this.pendingGameManager != null) {
            this.pendingGameManager.setGameService(this);
        }
    }

    public static synchronized GameServiceImpl getInstance(SessionManager sessionManager, GameDao gameDao, UserDao userDao, PendingGameManager pendingGameManager) {
        if (instance == null) {
            instance = new GameServiceImpl(sessionManager, gameDao, userDao, pendingGameManager);
        }
        return instance;
    }

    public static GameServiceImpl getInstance() {
        if (instance == null) {
            throw new IllegalStateException("GameServiceImpl is not yet initialized.");
        }
        return instance;
    }

    @Override
    public GameInfo start_game(String username, String token) throws PlayerNotLoggedInException {
        if (!isTokenValid(token))
            throw new PlayerNotLoggedInException("Player is not Logged in.");

        synchronized (pendingGameManager) {
            if (!pendingGameManager.isPendingGameExists()) {
                String gameId = UUID.randomUUID().toString();
                int actualGameRoundDuration = gameDao.getCurrentRoundLength();
                pendingGameManager.setPendingGame(new Game(gameId, actualGameRoundDuration, username));

                int matchmakingWaitDuration = gameDao.getCurrentWaitingTimeLength();
                pendingGameManager.startCountdownToStartGame(matchmakingWaitDuration);
            } else
                pendingGameManager.addPlayer(new GamePlayer(username, 0));


            String gameId = pendingGameManager.getPendingGameId();
            int roundLength = pendingGameManager.getPendingGameRoundDuration();
            int remainingWaitingTime = pendingGameManager.getRemainingWaitingTimeInSeconds();
            return new GameInfo(gameId, roundLength, remainingWaitingTime);
        }
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

    public Player getPlayerByUsername(String username, String token) throws PlayerNotLoggedInException {
        if (!isTokenValid(token))
            throw new PlayerNotLoggedInException("Player is not Logged in.");

        return userDao.getPlayerByUsername(username);
    }

    public void addActiveGame(Game pendingGame) {
        if (pendingGame == null) {
            System.out.println("GameServiceImpl.addActiveGame: FATAL - pendingGame parameter is null. Cannot proceed.");
            return;
        }
        System.out.println("GameServiceImpl.addActiveGame: Invoked for gameId: " + pendingGame.getGameId() + ". Current player count: " + pendingGame.getPlayers().size());

        if (pendingGame.getPlayers().isEmpty()) {
            System.out.println("GameServiceImpl.addActiveGame: No players in pendingGame (gameId: " + pendingGame.getGameId() + "). Aborting game start.");
            return;
        }

        if (pendingGame.getPlayers().size() == 1) {
            String lonePlayerUsername = pendingGame.getPlayers().get(0).username;
            System.out.println("GameServiceImpl.addActiveGame: Only one player (" + lonePlayerUsername + ") in game " + pendingGame.getGameId() + ". Notifying player via startGameFailed().");
            ClientCallback callback = sessionManager.getCallback(lonePlayerUsername);
            if (callback != null) {
                try {
                    callback.startGameFailed();
                } catch (Exception e) {
                    System.out.println("GameServiceImpl.addActiveGame: Exception while calling startGameFailed for " + lonePlayerUsername + ": " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("GameServiceImpl.addActiveGame: ERROR - Callback not found for lone player " + lonePlayerUsername + ". Cannot notify startGameFailed.");
            }
        } else {
            System.out.println("GameServiceImpl.addActiveGame: Sufficient players (" + pendingGame.getPlayers().size() + ") for game " + pendingGame.getGameId() + ". Adding to activeGames and starting.");
            activeGames.put(pendingGame.getGameId(), pendingGame);
            try {
                pendingGame.startGame();
                System.out.println("GameServiceImpl.addActiveGame: pendingGame.startGame() successfully called for gameId: " + pendingGame.getGameId());
            } catch (Exception e) {
                System.out.println("GameServiceImpl.addActiveGame: Exception during pendingGame.startGame() for gameId: " + pendingGame.getGameId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean isTokenValid(String token) {
        return sessionManager.isSessionValid(token);
    }
}