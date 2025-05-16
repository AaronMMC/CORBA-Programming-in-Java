package com.cs9322.team05.client.player.controller;

import ModifiedHangman.*;
import com.cs9322.team05.client.player.interfaces.GameViewInterface;
import com.cs9322.team05.client.player.model.GameModel;
import javafx.application.Platform;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameController {
    private static final Logger logger = Logger.getLogger(GameController.class.getName());
    private final GameModel gameModel;
    private GameViewInterface view;
    private String gameId;
    private int expectedRoundDurationSeconds;
    private int currentRoundNumber = 0;
    private Consumer<String> onGameStartFailedForNavigation;

    public GameController(GameModel gameModel, GameViewInterface view) {
        this.gameModel = gameModel;
        if (view != null) {
            setGameView(view);
        } else {
            logger.info("GameController initialized with a null view. View must be set later.");
        }
    }

    private void bindViewActions() {
        if (view == null) {
            logger.warning("GameController.bindViewActions() called with a null view. Actions will not be bound.");
            return;
        }
        logger.fine("Binding view actions for GameController.");
        view.setOnGuess(this::submitGuess);
        view.setOnLeaderboard(this::fetchLeaderboard);
        view.setOnPlayAgain(this::requestPlayAgain);
        view.setOnBackToMenu(this::requestBackToMenu);
    }

    public void setGameContext(String gameId, int roundDurationSeconds) {
        this.gameId = gameId;
        this.expectedRoundDurationSeconds = roundDurationSeconds;
        this.currentRoundNumber = 0;
        logger.info("GameController context set - GameID: " + this.gameId + ", ExpectedRoundDuration: " + this.expectedRoundDurationSeconds + "s");
        if (view != null) {
            Platform.runLater(() -> {
                if (this.gameId != null && !this.gameId.isEmpty()){
                    view.clearAll();
                    view.showStatusMessage("Game context set. Waiting for server...");
                }
            });
        }
    }
    public void setOnGameStartFailedForNavigation(Consumer<String> callback) {
        this.onGameStartFailedForNavigation = callback;
    }
    public void handleGameStartFailed(String message) {
        logger.info("Controller: handleGameStartFailed called with server message: " + message);
        if (onGameStartFailedForNavigation != null) {
            
            String userFriendlyMessage;
            if (message != null && message.toLowerCase().contains("not enough players")) {
                userFriendlyMessage = "Not enough players available to start the game. Please try again later.";
            } else if (message != null && !message.isEmpty()){
                userFriendlyMessage = "Failed to start game: " + message;
            } else {
                userFriendlyMessage = "Could not start game: Not enough players found. Returning to home.";
            }
            logger.info("Invoking onGameStartFailedForNavigation callback to MainApp with message: " + userFriendlyMessage);
            Platform.runLater(() -> onGameStartFailedForNavigation.accept(userFriendlyMessage));
        } else {
            logger.warning("GameController: onGameStartFailedForNavigation callback is not set. Cannot navigate back to Home for startGameFailed event.");
            
            if (view != null) {
                Platform.runLater(() -> view.showError("Failed to start game: " + message + " (Navigation to home failed)"));
            }
        }
    }

    public void onStartRound(int wordLength, int roundNumber) {
        this.currentRoundNumber = roundNumber;
        logger.info("Controller: Server initiated onStartRound for round " + this.currentRoundNumber + " with wordLength: " + wordLength);
        if (view == null) {
            logger.severe("View is null in onStartRound! Cannot prepare round.");
            return;
        }
        if (this.gameId == null || this.gameId.isEmpty()) {
            logger.severe("GameId is null or empty in onStartRound! This is critical.");
            Platform.runLater(() -> view.showError("Critical Error: Game ID not set for round start."));
            return;
        }

        Platform.runLater(() -> {
            view.prepareNewRound(wordLength, this.currentRoundNumber);
            view.showRoundDuration(this.expectedRoundDurationSeconds);
        });
    }

    public void submitGuess(char letter) {
        if (view == null) {
            logger.warning("SubmitGuess called but view is null.");
            return;
        }
        if (gameId == null || gameId.isEmpty()) {
            logger.severe("Cannot submit guess: gameId is not set!");
            Platform.runLater(() -> view.showError("Error: Game session not properly initialized. Game ID missing."));
            return;
        }
        logger.info("Submitting guess '" + letter + "' for gameId: " + gameId + ", round: " + currentRoundNumber);
        try {
            GuessResponse resp = gameModel.guessLetter(this.gameId, letter);
            logger.fine("GuessResponse received: wordGuessed=" + resp.isWordGuessed + ", attemptsLeft=" + resp.remainingAttemptsLeft + ", maskedWord=" + resp.maskedWord);
            Platform.runLater(() -> {
                view.updateMaskedWord(resp.maskedWord);
                view.updateAttemptsLeft(resp.remainingAttemptsLeft);
                view.showAttemptedLetters(Arrays.asList(resp.attemptedLetters));
                if (resp.isWordGuessed) {
                    logger.info("Word guessed correctly by player " + gameModel.getUsername());
                    view.disableGuessing();
                    view.showStatusMessage("Correct! Waiting for round to end...");
                } else if (resp.remainingAttemptsLeft <= 0) {
                    logger.info("No attempts left for player " + gameModel.getUsername());
                    view.disableGuessing();
                    view.showStatusMessage("No attempts left. Waiting for round to end...");
                }
            });
        } catch (GameNotFoundException e) {
            logger.log(Level.SEVERE, "GameNotFoundException while guessing: " + gameId, e);
            Platform.runLater(() -> view.showError("Error: Game not found. (" + gameId + ")"));
        } catch (PlayerNotLoggedInException e) {
            logger.log(Level.SEVERE, "PlayerNotLoggedInException while guessing.", e);
            Platform.runLater(() -> view.showError("Error: You are not logged in."));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected exception while submitting guess: " + e.getMessage(), e);
            Platform.runLater(() -> view.showError("Error during guess: " + e.getMessage()));
        }
    }

    public void fetchLeaderboard() {
        if (view == null) {
            logger.warning("FetchLeaderboard called but view is null.");
            return;
        }
        logger.info("Fetching overall leaderboard.");
        try {
            Leaderboard lb = gameModel.getLeaderboard();
            if (lb != null && lb.players != null) {
                Platform.runLater(() -> view.showLeaderboard(Arrays.asList(lb.players)));
            } else {
                Platform.runLater(() -> view.showLeaderboard(Arrays.asList())); 
                logger.warning("Fetched leaderboard or its players array is null.");
            }
        } catch (PlayerNotLoggedInException e) {
            logger.log(Level.WARNING, "PlayerNotLoggedInException for leaderboard.", e);
            Platform.runLater(() -> view.showError("Cannot fetch leaderboard: Not logged in."));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception fetching leaderboard.", e);
            Platform.runLater(() -> view.showError("Error fetching leaderboard: " + e.getMessage()));
        }
    }

    public void onProceedToNextRound(int nextWordLength) {
        logger.info("Controller: Server signaled onProceedToNextRound. Next wordLength hint: " + nextWordLength);
        if (view == null) {
            logger.warning("onProceedToNextRound called but view is null.");
            return;
        }
        Platform.runLater(() -> {
            view.showStatusMessage("Round " + currentRoundNumber + " ended. Preparing for the next round...");
        });
    }

    public void onEndRound(RoundResult result) {
        logger.info("Controller: Server signaled onEndRound for round " + result.roundNumber + ". Correct word: " + result.wordToGuess);
        if (view == null) {
            logger.warning("onEndRound called but view is null.");
            return;
        }
        Platform.runLater(() -> view.showRoundResult(result));
    }

    public void onEndGame(GameResult result) {
        logger.info("Controller: Server signaled onEndGame. Winner: " + (result != null ? result.gameWinner : "N/A"));
        if (view == null) {
            logger.warning("onEndGame called but view is null.");
            return;
        }
        this.currentRoundNumber = 0;
        Platform.runLater(() -> view.showFinalResult(result));
    }

    private void requestPlayAgain() {
        logger.info("Controller: Play Again requested by user. Triggering view's onReturnToMenu.");
        if (view == null) return;
        view.onReturnToMenu();
    }

    private void requestBackToMenu() {
        logger.info("Controller: Back to Menu requested by user. Triggering view's onReturnToMenu.");
        if (view == null) return;
        view.onReturnToMenu();
    }

    public void setGameView(GameViewInterface view) {
        logger.fine("Setting GameView for GameController.");
        this.view = view;
        if (this.view != null) {
            bindViewActions();
        } else {
            logger.warning("setGameView called with a null view instance.");
        }
    }

    public String getGameId() {
        return gameId;
    }

    public GameModel getGameModel() {
        return gameModel;
    }

    public void cleanupControllerState() {
        logger.info("GameController: Cleaning up controller state for gameId: " + this.gameId);
        this.gameId = null;
        this.currentRoundNumber = 0;
        this.expectedRoundDurationSeconds = 0;
        if (view != null) {
            view.clearAll();
            logger.fine("GameView instructed to clearAll.");
        }
        logger.info("GameController: Cleanup complete.");
    }
}