package com.client.player.model;

import ModifiedHangman.*;

public class GameModel {
    private final GameService gameService;
    private final String username; 
    private final String token;

    public GameModel(GameService gameService, String username, String token) {
        this.gameService = gameService;
        this.username    = username;
        this.token       = token;
    }

    public String getUsername() { 
        return this.username;
    }

    public String getToken() { 
        return this.token;
    }

    public GameInfo startGame() throws PlayerNotLoggedInException {
        return gameService.start_game(username, token);
    }

    public GuessResponse guessLetter(String gameId, char letter)
            throws GameNotFoundException, PlayerNotLoggedInException {
        return gameService.guessLetter(username, gameId, letter, token);
    }

    public Leaderboard getLeaderboard() throws PlayerNotLoggedInException {
        return gameService.get_leaderboard(token);
    }
}