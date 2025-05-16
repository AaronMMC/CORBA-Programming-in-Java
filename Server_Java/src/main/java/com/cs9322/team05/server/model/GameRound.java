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

        System.out.println("GameRound Constructor: Initializing round " + this.roundNumber + " with word length: " + wordToGuessLength);
        if (playersList == null || playersList.isEmpty()) {
            System.out.println("GameRound Constructor: WARNING - playersList is null or empty for round " + this.roundNumber);
            return;
        }

        System.out.println("GameRound Constructor: Players for round " + this.roundNumber + ":");
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

    public void startRound(int seconds, String gameId) {
        System.out.println("GameRound.startRound: Called for gameId: " + gameId + ", roundNumber: " + this.roundNumber + ", duration: " + seconds + "s, wordLength: " + (wordToGuess != null ? wordToGuess.length() : "N/A"));

        if (playerGuessStates.isEmpty()) {
            System.out.println("GameRound.startRound: No players in playerGuessStates for round " + this.roundNumber + ", gameId: " + gameId + ". Cannot start round callbacks.");
            return;
        }

        for (String username : playerGuessStates.keySet()) {
            System.out.println("GameRound.startRound: Processing player: " + username + " for gameId: " + gameId + ", round " + this.roundNumber);
            ClientCallback clientCallback = null;
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
            System.out.println("GameRound.startRound: ClientCallback obtained for player " + username + ". Attempting to call clientCallback.startRound().");

            try {
                int wordLength = (wordToGuess != null) ? wordToGuess.length() : 0;
                clientCallback.startRound(wordLength, this.roundNumber);
                System.out.println("GameRound.startRound: Successfully called clientCallback.startRound() for player " + username + " (gameId: " + gameId + ", round " + this.roundNumber + ")");
            } catch (org.omg.CORBA.SystemException se) {
                System.out.println("GameRound.startRound: CORBA SystemException while calling startRound for " + username + " (gameId: " + gameId + ", round " + this.roundNumber + "): " + se.getMessage());
                se.printStackTrace();
            } catch (Exception e) {
                System.out.println("GameRound.startRound: GENERAL Exception while calling startRound for " + username + " (gameId: " + gameId + ", round " + this.roundNumber + "): " + e.getMessage());
                e.printStackTrace();
            }

            final String playerUsernameForTask = username;
            ScheduledFuture<?> task = scheduler.schedule(() -> {
                System.out.println("GameRound (EndRoundTask): Time's up for player " + playerUsernameForTask + " in gameId: " + gameId + ", round " + this.roundNumber);
                String statusMessage;
                GamePlayer roundWinnerForCallback = this.winner;

                if (roundWinnerForCallback == null) {
                    statusMessage = "No one guessed the word this round.";
                } else if (roundWinnerForCallback.username.equals(playerUsernameForTask)) {
                    statusMessage = "You won this Round!";
                } else {
                    statusMessage = "You lost this round. Winner was " + roundWinnerForCallback.username;
                }

                GamePlayer[] leaderboardArray = players.values()
                        .stream()
                        .sorted((p1, p2) -> Integer.compare(p2.wins, p1.wins))
                        .toArray(GamePlayer[]::new);

                RoundResult roundResult = new RoundResult(gameId, this.roundNumber, roundWinnerForCallback, this.wordToGuess, statusMessage, leaderboardArray);

                ClientCallback cbToEnd = sessionManager.getCallback(playerUsernameForTask);
                if (cbToEnd != null) {
                    try {
                        System.out.println("GameRound (EndRoundTask): Sending endRound callback to " + playerUsernameForTask + " for gameId: " + gameId + ", round " + this.roundNumber);
                        cbToEnd.endRound(roundResult);
                    } catch (org.omg.CORBA.SystemException se) {
                        System.out.println("GameRound (EndRoundTask): CORBA SystemException sending endRound for " + playerUsernameForTask + ": " + se.getMessage());
                        se.printStackTrace();
                    } catch (Exception e) {
                        System.out.println("GameRound (EndRoundTask): GENERAL Exception sending endRound for " + playerUsernameForTask + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("GameRound (EndRoundTask): Callback was null for player " + playerUsernameForTask + ". Cannot send endRound.");
                }
            }, seconds, TimeUnit.SECONDS);
            countdownTasks.put(username, task);
            System.out.println("GameRound.startRound: Scheduled endRound task for player " + username + " in " + seconds + "s (gameId: " + gameId + ", round " + this.roundNumber + ")");
        }
    }

    public synchronized void cancelCountdownForPlayer(String username) {
        System.out.println("GameRound.cancelCountdownForPlayer: Attempting to cancel for " + username + " in round " + this.roundNumber);
        ScheduledFuture<?> task = countdownTasks.get(username);
        if (task != null) {
            boolean cancelled = task.cancel(false);
            System.out.println("GameRound.cancelCountdownForPlayer: Task for " + username + " cancellation attempt result: " + cancelled);
        } else {
            System.out.println("GameRound.cancelCountdownForPlayer: No task found for " + username);
        }
    }

    public synchronized void stopAllCountdowns() {
        System.out.println("GameRound.stopAllCountdowns: Stopping all countdowns for round " + this.roundNumber);
        for (Map.Entry<String, ScheduledFuture<?>> entry : countdownTasks.entrySet()) {
            System.out.println("GameRound.stopAllCountdowns: Cancelling task for player " + entry.getKey());
            if (entry.getValue() != null) {
                entry.getValue().cancel(false);
            }
        }
        countdownTasks.clear();
    }

    public synchronized GuessResponse guessLetter(String username, char letter) {
        System.out.println("GameRound.guessLetter: Player " + username + " guessed '" + letter + "' in round " + this.roundNumber + " for word: " + this.wordToGuess);
        PlayerGuessWordState playerGuessWordState = playerGuessStates.get(username);
        if (playerGuessWordState == null) {
            System.out.println("GameRound.guessLetter: ERROR - PlayerGuessWordState not found for player " + username + " in round " + this.roundNumber);
            throw new IllegalArgumentException("Player " + username + " is not in this game round or has no state.");
        }

        boolean isTheLetterInTheWord = (wordToGuess != null) && wordToGuess.indexOf(Character.toLowerCase(letter)) >= 0;
        playerGuessWordState.addAttemptedLetter(Character.toLowerCase(letter), isTheLetterInTheWord);

        if (!isTheLetterInTheWord) {
            playerGuessWordState.setRemainingGuess(playerGuessWordState.getRemainingGuess() - 1);
        }

        StringBuilder updatedMasked = getUpdatedMaskedWord(playerGuessWordState, Character.toLowerCase(letter));
        playerGuessWordState.setCurrentMaskedWord(updatedMasked.toString());
        boolean isWordGuessed = !updatedMasked.toString().contains("_");

        System.out.println("GameRound.guessLetter: Player " + username + " state after guess '" + letter + "': maskedWord=" + updatedMasked + ", remainingGuesses=" + playerGuessWordState.getRemainingGuess() + ", isWordGuessed=" + isWordGuessed);

        if (isWordGuessed) {
            System.out.println("GameRound.guessLetter: Player " + username + " GUESSED THE WORD '" + this.wordToGuess + "' in round " + this.roundNumber);
            cancelCountdownForPlayer(username);
            if (this.winner == null) {
                this.winner = players.get(username);
                System.out.println("GameRound.guessLetter: Player " + username + " is set as winner for round " + this.roundNumber);
                if (this.winner != null) {
                    this.winner.wins++;
                    System.out.println("GameRound.guessLetter: Player " + username + " round wins incremented to " + this.winner.wins);
                }
            } else {
                System.out.println("GameRound.guessLetter: Word was guessed by " + username + " but round winner was already " + this.winner.username);
            }
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
        for (int i = 0; i < wordToGuess.length(); i++) {
            if (Character.toLowerCase(wordToGuess.charAt(i)) == letter) {
                updatedMasked.setCharAt(i*2, wordToGuess.charAt(i)); // Assuming masked word has spaces: "w _ r d"
            }
        }
        return updatedMasked;
    }
}