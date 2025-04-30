package com.cs9322.team05.server.model;

import java.util.Map;

public class PlayerGuessWordState {
    private String currentMaskedWord;
    private int remainingGuess;
    private Map<Character, Boolean> attemptedLetters;

}
