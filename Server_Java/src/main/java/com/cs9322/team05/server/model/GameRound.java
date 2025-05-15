package com.cs9322.team05.server.model;

import ModifiedHangman.*;
import com.cs9322.team05.server.manager.SessionManager;

import java.util.*;
import java.util.concurrent.*;

public class GameRound {
    private final int roundNumber;
    private final String wordToGuess;
    private GamePlayer winner;
    private final Map<String, GamePlayer> players;
    private final Map<String, ScheduledFuture<?>> countdownTasks;
    private final Map<String, PlayerGuessWordState> playerGuessStates;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public GameRound(List<GamePlayer> playersList, String wordToGuess, int roundNumber) {
        players = new HashMap<>();
        countdownTasks = new HashMap<>();
        playerGuessStates = new HashMap<>();

        this.roundNumber = roundNumber;
        this.wordToGuess = wordToGuess;
        int wordToGuessLength = wordToGuess.length();
        this.sessionManager = SessionManager.getInstance();

        for (GamePlayer player : playersList) {
            playerGuessStates.put(player.username, new PlayerGuessWordState(wordToGuessLength));
            this.players.put(player.username, player);
        }
    }

    public void startRound(int seconds, String gameId) {
        for (String username : playerGuessStates.keySet()) {
            ClientCallback clientCallback = sessionManager.getCallback(username);
            try {
                if (roundNumber == 1)
                    clientCallback.startRound(wordToGuess.length(), roundNumber); // send start signal to clientt
                else clientCallback.proceedToNextRound(wordToGuess.length(), roundNumber);
            } catch (RuntimeException e) {
                e.printStackTrace();  }

            ScheduledFuture<?> task = scheduler.schedule(() -> {
                System.out.println("Time's up for " + username);
                String statusMessage;

                if (winner == null)
                    statusMessage = "No one guessed the word";
                else if (this.winner.username.equals(username)) {
                    winner.wins++;
                    statusMessage = "You won this Round!"; }
                else
                    statusMessage = "You lost this round!";

                GamePlayer[] leaderboard = players.values()
                        .stream()
                        .sorted((p1, p2) -> Integer.compare(p2.wins, p1.wins))
                        .toArray(GamePlayer[]::new);

                RoundResult roundResult = new RoundResult(gameId, roundNumber, winner, wordToGuess, statusMessage, leaderboard);
                try {
                    clientCallback.endRound(roundResult);}
                catch (RuntimeException e) {
                    e.printStackTrace(); }
            }, seconds, TimeUnit.SECONDS);

            countdownTasks.put(username, task);
        }
    }


    public void cancelCountdownForPlayer(String username) {
        ScheduledFuture<?> task = countdownTasks.get(username);
        if (task != null)
            task.cancel(false);

    }

    public void stopAllCountdowns() {
        for (ScheduledFuture<?> task : countdownTasks.values())
            task.cancel(false);

        countdownTasks.clear();
    }


    public GuessResponse guessLetter(String username, char letter) {
        PlayerGuessWordState playerGuessWordState = playerGuessStates.get(username);
        if (playerGuessWordState == null)
            throw new IllegalArgumentException("This player is not in the game.");

        boolean isTheLetterInTheWord = wordToGuess.indexOf(letter) >= 0;

        playerGuessWordState.addAttemptedLetter(letter, isTheLetterInTheWord);
        if (!isTheLetterInTheWord)
            playerGuessWordState.setRemainingGuess(playerGuessWordState.getRemainingGuess() - 1);

        StringBuilder updatedMasked = getUpdatedMaskedWord(playerGuessWordState, letter);
        playerGuessWordState.setCurrentMaskedWord(updatedMasked.toString());
        boolean isWordGuessed = !updatedMasked.toString().contains("_");

        if (isWordGuessed) {
            cancelCountdownForPlayer(username);
            if (winner == null)
                winner = players.get(username);
        }

        // convert the Set<Charactr> to Array
        AttemptedLetter[] attemptedLetters = playerGuessWordState.getAttemptedLetters()
                .keySet()
                .stream()
                .map(ch -> new AttemptedLetter(ch, playerGuessWordState.getAttemptedLetters().get(ch)))
                .toArray(AttemptedLetter[]::new);

        return new GuessResponse(
                isWordGuessed,
                playerGuessWordState.getCurrentMaskedWord(),
                attemptedLetters,
                playerGuessWordState.getRemainingGuess()
        );
    }

    private StringBuilder getUpdatedMaskedWord(PlayerGuessWordState playerGuessWordState, char letter) {
        StringBuilder updatedMasked = new StringBuilder(playerGuessWordState.getCurrentMaskedWord());
        for (int i = 0; i < wordToGuess.length(); i++)
            if (wordToGuess.charAt(i) == letter)
                updatedMasked.setCharAt(i, letter);
        return updatedMasked;
    }



    private final SessionManager sessionManager;

}
