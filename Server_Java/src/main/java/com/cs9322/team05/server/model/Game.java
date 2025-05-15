package com.cs9322.team05.server.model;

import ModifiedHangman.GamePlayer;
import ModifiedHangman.GuessResponse;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private String gameId;
    private GamePlayer winner;
    private int roundDuration;
    private int roundCount;
    private List<GamePlayer> players;
    private List<GameRound> rounds;

    public Game(String gameId, int roundDuration, String username) {
        this.gameId = gameId;
        this.winner = null;
        this.roundDuration = roundDuration;
        this.players = new ArrayList<>();
        this.rounds = new ArrayList<>();

        this.players.add(new GamePlayer(username, 0)); // 0 is round wins
    }


    public String getGameId() {
        return gameId;

    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public GamePlayer getWinner() {
        return winner;
    }

    public void setWinner(GamePlayer winner) {
        this.winner = winner;
    }

    public int getRoundDuration() {
        return roundDuration;
    }

    public void setRoundDuration(int roundDuration) {
        this.roundDuration = roundDuration;
    }

    public int getRoundCount() {
        return roundCount;
    }

    public void setRoundCount(int roundCount) {
        this.roundCount = roundCount;
    }

    public List<GamePlayer> getPlayers() {
        return players;
    }

    public void addPlayer(GamePlayer player) {
        this.players.add(player);
    }

    public List<GameRound> getRounds() {
        return rounds;
    }

    public void addRounds(GameRound round) {
        this.rounds.add(round);
    }

    public GuessResponse guessLetter(String username, char letter) {
        boolean playerExists = players.stream().anyMatch(player -> player.username.equals(username));
        if (!playerExists) // TODO : change the exception type to a more appropriate one
            throw new RuntimeException("Player " + username + " is not part of the game.");

        GameRound currentRound = rounds.get(rounds.size() - 1);
        return currentRound.guessLetter(username, letter);
    }
}
