package com.server.model;

import ModifiedHangman.*;
import com.server.manager.SessionManager;

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
    private Runnable onRoundComplete;
    private ScheduledFuture<?> roundCompleteTask;
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

        for (GamePlayer player : playersList) {
            if (player != null && player.username != null) {
                System.out.println("  - " + player.username);
                playerGuessStates.put(player.username, new PlayerGuessWordState(wordToGuessLength));
                players.put(player.username, player);
            } else {
                System.out.println("  - Warning: Null player or player with null username in list for round " + this.roundNumber);
            }
        }
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

        this.onRoundComplete = onRoundComplete;
        this.roundCompleteTask = scheduler.schedule(onRoundComplete, roundDuration, TimeUnit.SECONDS);
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

        if (isWordGuessed) checkAndSetWinner(username);

        AttemptedLetter[] attemptedLetters = state.getAttemptedLetters().entrySet().stream()
                .map(entry -> new AttemptedLetter(entry.getKey(), entry.getValue()))
                .toArray(AttemptedLetter[]::new);

        return new GuessResponse(isWordGuessed, maskedWord, attemptedLetters, state.getRemainingGuess());
    }


    public synchronized void stopAllCountdowns() {
        for (ScheduledFuture<?> task : countdownTasks.values())
            if (task != null) task.cancel(false);

        countdownTasks.clear();

        if (roundCompleteTask != null) {
            roundCompleteTask.cancel(false);
            roundCompleteTask = null;
        }
    }

    public void endRoundEarly(String gameId) {
        stopAllCountdowns(); // Ensure no other timers will fire

        GamePlayer roundWinner = (this.winner != null) ? this.winner : new GamePlayer("no_winner", 0);

        for (String username : players.keySet()) {
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
                    System.out.printf("GameRound (EarlyEnd): NULL callback for %s. Cannot send endRound.%n", username);
            } catch (Exception e) {
                System.out.printf("GameRound (EarlyEnd): Exception for %s - %s%n", username, e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private void updatePlayerGuessState(PlayerGuessWordState state, char letter, boolean isCorrect) {
        if (!isCorrect && !state.getAttemptedLetters().containsKey(letter))
            state.setRemainingGuess(state.getRemainingGuess() - 1);

        state.addAttemptedLetter(letter, isCorrect);
    }

    private String updateMaskedWord(PlayerGuessWordState state, char letter) {
        StringBuilder updatedMasked = getUpdatedMaskedWord(state, letter);
        state.setCurrentMaskedWord(updatedMasked.toString());
        return updatedMasked.toString();
    }

    private synchronized void checkAndSetWinner(String username) {
        if (this.winner == null) {
            this.winner = players.get(username);
            if (this.winner != null) {
                this.winner.wins++;
                stopAllCountdowns();

                if (roundCompleteTask != null) {
                    roundCompleteTask.cancel(false);
                    roundCompleteTask = null;
                }

                if (onRoundComplete != null) {
                    onRoundComplete.run();
                    onRoundComplete = null;
                }

                endRoundEarly("random"); // since the gameId is not needed in the showing of results
            }
        }
    }

    private StringBuilder getUpdatedMaskedWord(PlayerGuessWordState state, char letter) {
        if (wordToGuess == null || state == null || state.getCurrentMaskedWord() == null) {
            System.out.println("GameRound.getUpdatedMaskedWord: Error - wordToGuess or player state is null.");
            return new StringBuilder(state != null && state.getCurrentMaskedWord() != null ? state.getCurrentMaskedWord() : "");
        }

        StringBuilder updatedMasked = new StringBuilder(state.getCurrentMaskedWord());

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
            GamePlayer roundWinner = (winner != null) ? winner : new GamePlayer("no_winner", 0);
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
