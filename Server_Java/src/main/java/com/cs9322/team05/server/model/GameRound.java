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
    private final SessionManager sessionManager;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public GameRound(List<GamePlayer> playersList, String wordToGuess, int roundNumber) {
        this.players = new HashMap<>();
        this.countdownTasks = new HashMap<>();
        this.playerGuessStates = new HashMap<>();
        this.sessionManager = SessionManager.getInstance();

        this.roundNumber = roundNumber;
        this.wordToGuess = wordToGuess;
        int wordToGuessLength = (wordToGuess != null) ? wordToGuess.length() : 0;

        if (playersList == null || playersList.isEmpty()) {
            System.out.println("GameRound Constructor: WARNING - playersList is null or empty for round " + this.roundNumber);
            return;
        }

        for (GamePlayer player : playersList)
            if (player != null && player.username != null) {
                System.out.println("  - " + player.username);
                playerGuessStates.put(player.username, new PlayerGuessWordState(wordToGuessLength));
                this.players.put(player.username, player);
            } else
                System.out.println("  - Warning: Null player or player with null username in list for round " + this.roundNumber);
    }



    public void startRound(int roundDuration, String gameId, Runnable onRoundComplete) {
        if (playerGuessStates.isEmpty()) {
            System.out.printf("GameRound.startRound: No players for round %d, gameId: %s. Cannot start round.%n", roundNumber, gameId);
            return;
        }

        for (String username : playerGuessStates.keySet()) {
            ClientCallback callback = getClientCallback(username, gameId);
            if (callback == null) continue;

            notifyClientStartRound(callback, username, gameId);
            ScheduledFuture<?> task = scheduleEndRoundTask(roundDuration, username, gameId);
            countdownTasks.put(username, task);
        }

        scheduler.schedule(onRoundComplete, roundDuration, TimeUnit.SECONDS);
    }



    public synchronized void cancelCountdownForPlayer(String username) {
        ScheduledFuture<?> task = countdownTasks.get(username);
        if (task == null)
            System.out.println("GameRound.cancelCountdownForPlayer: No task found for " + username);
    }



    public synchronized void stopAllCountdowns() {
        for (Map.Entry<String, ScheduledFuture<?>> entry : countdownTasks.entrySet())
            if (entry.getValue() != null)
                entry.getValue().cancel(false);

        countdownTasks.clear();
    }



    public synchronized GuessResponse guessLetter(String username, char letter) {
        PlayerGuessWordState state = playerGuessStates.get(username);
        if (state == null)
            throw new IllegalArgumentException("Player " + username + " is not in this game round or has no state.");

        char lowerLetter = Character.toLowerCase(letter);
        boolean isLetterInWord = wordToGuess != null && wordToGuess.indexOf(lowerLetter) >= 0;
        updatePlayerGuessState(state, lowerLetter, isLetterInWord);
        String maskedWord = updateMaskedWord(state, lowerLetter);
        boolean isWordGuessed = !maskedWord.contains("_");

        if (isWordGuessed)
            checkAndSetWinner(username);

        AttemptedLetter[] attemptedLetters = state.getAttemptedLetters().entrySet().stream()
                .map(entry -> new AttemptedLetter(entry.getKey(), entry.getValue()))
                .toArray(AttemptedLetter[]::new);

        return new GuessResponse(isWordGuessed, maskedWord, attemptedLetters, state.getRemainingGuess());
    }



    private void updatePlayerGuessState(PlayerGuessWordState state, char letter, boolean isCorrect) {
        state.addAttemptedLetter(letter, isCorrect);
        if (!isCorrect && !state.getAttemptedLetters().containsKey(letter))
            state.setRemainingGuess(state.getRemainingGuess() - 1);
    }



    private String updateMaskedWord(PlayerGuessWordState state, char letter) {
        StringBuilder updatedMasked = getUpdatedMaskedWord(state, letter);
        state.setCurrentMaskedWord(updatedMasked.toString());
        return updatedMasked.toString();
    }



    private void checkAndSetWinner(String username) {
        cancelCountdownForPlayer(username);
        if (this.winner == null) {
            this.winner = players.get(username);
            if (this.winner != null)
                this.winner.wins++;
        } else
            System.out.printf("GameRound.guessLetter: Word guessed by %s but winner already %s%n", username, this.winner.username);
    }



    private StringBuilder getUpdatedMaskedWord(PlayerGuessWordState playerGuessWordState, char letter) {
        if (wordToGuess == null || playerGuessWordState == null || playerGuessWordState.getCurrentMaskedWord() == null) {
            System.out.println("GameRound.getUpdatedMaskedWord: Error - wordToGuess or player state is null. Returning empty/current masked word.");
            return new StringBuilder(playerGuessWordState != null && playerGuessWordState.getCurrentMaskedWord() != null ? playerGuessWordState.getCurrentMaskedWord() : "");
        }


        StringBuilder updatedMasked = new StringBuilder(playerGuessWordState.getCurrentMaskedWord());

        for (int i = 0; i < wordToGuess.length(); i++)
            if (Character.toLowerCase(wordToGuess.charAt(i)) == letter)
                updatedMasked.setCharAt(i, wordToGuess.charAt(i));

        return updatedMasked;
    }



    private ClientCallback getClientCallback(String username, String gameId) {
        try {
            ClientCallback callback = sessionManager.getCallback(username);
            if (callback == null)
                System.out.printf("GameRound.startRound: NULL callback for player %s (gameId: %s, round %d)%n", username, gameId, roundNumber);

            return callback;
        } catch (Exception e) {
            System.out.printf("GameRound.startRound: EXCEPTION getting callback for %s - %s%n", username, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }



    private void notifyClientStartRound(ClientCallback callback, String username, String gameId) {
        try {
            int wordLength = (wordToGuess != null) ? wordToGuess.length() : 0;
            callback.startRound(wordLength, roundNumber);
        } catch (org.omg.CORBA.SystemException se) {
            System.out.printf("GameRound.startRound: CORBA SystemException for %s (gameId: %s, round %d): %s%n", username, gameId, roundNumber, se.getMessage());
            se.printStackTrace();
        } catch (Exception e) {
            System.out.printf("GameRound.startRound: Exception for %s (gameId: %s, round %d): %s%n", username, gameId, roundNumber, e.getMessage());
            e.printStackTrace();
        }
    }



    private ScheduledFuture<?> scheduleEndRoundTask(int roundDuration, String username, String gameId) {
        return scheduler.schedule(() -> {
            GamePlayer roundWinner = (this.winner != null) ? this.winner : new GamePlayer("no_winner", 0);
            String message = determineStatusMessage(username, roundWinner);

            GamePlayer[] leaderboard = players.values().stream()
                    .sorted(Comparator.comparingInt((GamePlayer p) -> p.wins).reversed())
                    .toArray(GamePlayer[]::new);

            RoundResult result = new RoundResult(gameId, roundNumber, roundWinner, wordToGuess, message, leaderboard);

            try {
                ClientCallback callback = sessionManager.getCallback(username);
                if (callback != null)
                    callback.endRound(result);
                else
                    System.out.printf("GameRound (EndRoundTask): NULL callback for %s. Cannot send endRound.%n", username);
            } catch (Exception e) {
                System.out.printf("GameRound (EndRoundTask): Exception for %s - %s%n", username, e.getMessage());
                e.printStackTrace();
            }
        }, roundDuration, TimeUnit.SECONDS);
    }



    private String determineStatusMessage(String username, GamePlayer winner) {
        if (winner.username.equals("no_winner"))
            return "No one guessed the word this round.";

        return winner.username.equals(username) ? "You won this Round!" : "You lost this round. Winner was " + winner.username;
    }

}