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

        for (GamePlayer player : playersList) {
            if (player != null && player.username != null) {
                System.out.println("  - " + player.username);
                playerGuessStates.put(player.username, new PlayerGuessWordState(wordToGuessLength));
                this.players.put(player.username, player);
            } else {
                System.out.println("  - Warning: Null player or player with null username in list for round " + this.roundNumber);
            }
        }
    }

    public void startRound(int seconds, String gameId, Runnable onRoundComplete) {
        if (playerGuessStates.isEmpty()) {
            System.out.println("GameRound.startRound: No players in playerGuessStates for round " + this.roundNumber + ", gameId: " + gameId + ". Cannot start round callbacks.");
            return;
        }

        for (String username : playerGuessStates.keySet()) {
            ClientCallback clientCallback;
            try {
                clientCallback = sessionManager.getCallback(username);
            } catch (Exception e) {
                System.out.println("GameRound.startRound: EXCEPTION while getting callback for " + username + " - " + e.getMessage());
                e.printStackTrace();
                continue;
            }

            if (clientCallback == null) {
                System.out.println("GameRound.startRound: ERROR - ClientCallback is NULL for player " + username + " in gameId: " + gameId + ", round " + this.roundNumber);
                continue;
            }

            try {
                int wordLength = (wordToGuess != null) ? wordToGuess.length() : 0;
                clientCallback.startRound(wordLength, this.roundNumber);
            } catch (org.omg.CORBA.SystemException se) {
                System.out.println("GameRound.startRound: CORBA SystemException while calling startRound for " + username + " (gameId: " + gameId + ", round " + this.roundNumber + "): " + se.getMessage());
                se.printStackTrace();
            } catch (Exception e) {
                System.out.println("GameRound.startRound: GENERAL Exception while calling startRound for " + username + " (gameId: " + gameId + ", round " + this.roundNumber + "): " + e.getMessage());
                e.printStackTrace();
            }

            final String playerUsernameForTask = username;
            ScheduledFuture<?> task = scheduler.schedule(() -> {
                String statusMessage;
                GamePlayer roundWinnerForCallback = this.winner;

                if (roundWinnerForCallback == null) {
                    statusMessage = "No one guessed the word this round.";
                    roundWinnerForCallback = new GamePlayer("no_winner", 0);
                }
                else if (roundWinnerForCallback.username.equals(playerUsernameForTask))
                    statusMessage = "You won this Round!";
                else
                    statusMessage = "You lost this round. Winner was " + roundWinnerForCallback.username;

                GamePlayer[] leaderboardArray = players.values()
                        .stream()
                        .sorted((p1, p2) -> Integer.compare(p2.wins, p1.wins))
                        .toArray(GamePlayer[]::new);

                RoundResult roundResult = new RoundResult(gameId, this.roundNumber, roundWinnerForCallback, this.wordToGuess, statusMessage, leaderboardArray);

                ClientCallback cbToEnd = sessionManager.getCallback(playerUsernameForTask);
                if (cbToEnd != null)
                    try {
                        cbToEnd.endRound(roundResult);
                    } catch (org.omg.CORBA.SystemException se) {
                        se.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                else
                    System.out.println("GameRound (EndRoundTask): Callback was null for player " + playerUsernameForTask + ". Cannot send endRound.");

            }, seconds, TimeUnit.SECONDS);
            countdownTasks.put(username, task);

        }

        scheduler.schedule(() -> onRoundComplete.run(), seconds, TimeUnit.SECONDS);
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
        PlayerGuessWordState playerGuessWordState = playerGuessStates.get(username);
        if (playerGuessWordState == null)
            throw new IllegalArgumentException("Player " + username + " is not in this game round or has no state.");

        boolean isTheLetterInTheWord = (wordToGuess != null) && wordToGuess.indexOf(Character.toLowerCase(letter)) >= 0;
        playerGuessWordState.addAttemptedLetter(Character.toLowerCase(letter), isTheLetterInTheWord);

        if (!isTheLetterInTheWord)
            playerGuessWordState.setRemainingGuess(playerGuessWordState.getRemainingGuess() - 1);

        StringBuilder updatedMasked = getUpdatedMaskedWord(playerGuessWordState, Character.toLowerCase(letter));
        playerGuessWordState.setCurrentMaskedWord(updatedMasked.toString());
        boolean isWordGuessed = !updatedMasked.toString().contains("_");

        if (isWordGuessed) {
            cancelCountdownForPlayer(username);
            if (this.winner == null) {
                this.winner = players.get(username);
                if (this.winner != null)
                    this.winner.wins++; }
            else
                System.out.println("GameRound.guessLetter: Word was guessed by " + username + " but round winner was already " + this.winner.username);
        }

        AttemptedLetter[] attemptedLetters = playerGuessWordState.getAttemptedLetters()
                .entrySet()
                .stream()
                .map(chEntry -> new AttemptedLetter(chEntry.getKey(), chEntry.getValue()))
                .toArray(AttemptedLetter[]::new);

        return new GuessResponse(
                isWordGuessed,
                playerGuessWordState.getCurrentMaskedWord(),
                attemptedLetters,
                playerGuessWordState.getRemainingGuess()
        );
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

}