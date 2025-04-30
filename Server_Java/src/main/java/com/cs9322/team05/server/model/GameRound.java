package com.cs9322.team05.server.model;

import ModifiedHangman.GamePlayer;

import java.util.Map;

public class GameRound {
    private int roundNumber;
    private String wordToGuess;
    private GamePlayer winner;
    private Map<String, CountdownTimer> playerCountdowns; // username to their countdown timers, change the Countdown to the actual object to be used
    private Map<String, PlayerGuessWordState> playerGuessStates;

}
