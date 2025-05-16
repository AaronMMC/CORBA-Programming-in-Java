package com.cs9322.team05.server.model;

import java.util.HashMap;
import java.util.Map;

public class PlayerGuessWordState {
    private String currentMaskedWord;
    private int remainingGuess;
    private Map<Character, Boolean> attemptedLetters; // Character is the letter and Boolean is whether it is in the word or not

    public PlayerGuessWordState(int wordLength) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < wordLength; i++)
            stringBuilder.append("_");

        currentMaskedWord = stringBuilder.toString();
        attemptedLetters = new HashMap<>();
        remainingGuess = 5;
    }

    public String getCurrentMaskedWord() {
        return currentMaskedWord;
    }

    public void setCurrentMaskedWord(String currentMaskedWord) {
        this.currentMaskedWord = currentMaskedWord;
    }

    public int getRemainingGuess() {
        return remainingGuess;
    }

    public void setRemainingGuess(int remainingGuess) {
        this.remainingGuess = remainingGuess;
    }

    public Map<Character, Boolean> getAttemptedLetters() {
        return attemptedLetters;
    }

    public void setAttemptedLetters(Map<Character, Boolean> attemptedLetters) {
        this.attemptedLetters = attemptedLetters;
    }


    public void addAttemptedLetter(char letter, boolean isItInTheWord) {
        attemptedLetters.put(letter, isItInTheWord);

    }
}
