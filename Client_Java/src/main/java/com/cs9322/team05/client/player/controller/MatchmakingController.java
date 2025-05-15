package com.cs9322.team05.client.player.controller;

import ModifiedHangman.GameInfo;
import ModifiedHangman.PlayerNotLoggedInException;
import com.cs9322.team05.client.player.callback.ClientCallbackImpl;
import com.cs9322.team05.client.player.model.GameModel;
import com.cs9322.team05.client.player.view.MatchmakingView;
import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatchmakingController {
    private static final Logger logger = Logger.getLogger(MatchmakingController.class.getName());
    private final GameModel gameModel;
    private final ClientCallbackImpl clientCallback;
    private final GameController gameController;
    private final MatchmakingView view;

    private Runnable onMatchReadyToProceed;
    private Runnable onMatchmakingCancelledOrFailed;

    private Thread matchmakingThread;
    private final AtomicBoolean cancellationRequested = new AtomicBoolean(false);
    private static final long CLIENT_SIDE_MATCHMAKING_TIMEOUT_MS = 30000;
    private Timer timeoutTimer = null;

    public MatchmakingController(GameModel gameModel, ClientCallbackImpl clientCallback, GameController gameController) {
        this.gameModel = gameModel;
        this.clientCallback = clientCallback;
        this.gameController = gameController;
        this.view = new MatchmakingView();

        this.view.setOnCancel(() -> {
            logger.info("Matchmaking cancellation requested by user via UI.");
            cancellationRequested.set(true);
            cleanupTimeoutTimer();
            if (matchmakingThread != null && matchmakingThread.isAlive()) {
                matchmakingThread.interrupt();
            }
        });
    }

    private void cleanupTimeoutTimer() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer.purge();
            timeoutTimer = null;
            logger.fine("Matchmaking client-side timeout timer cleaned up.");
        }
    }

    public void startMatchmaking() {
        cancellationRequested.set(false);
        Platform.runLater(view::showSearching);
        logger.info("Starting matchmaking process for user: " + gameModel.getUsername());

        matchmakingThread = new Thread(() -> {
            boolean registrationSuccessful = false;
            try {
                logger.fine("Matchmaking thread started.");
                cleanupTimeoutTimer();
                timeoutTimer = new Timer("MatchmakingTimeoutTimer", true);
                timeoutTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (matchmakingThread != null && matchmakingThread.isAlive() && !cancellationRequested.get()) {
                            logger.warning("Client-side matchmaking timeout (" + CLIENT_SIDE_MATCHMAKING_TIMEOUT_MS + "ms) reached.");
                            Platform.runLater(() -> {
                                if (!cancellationRequested.get()) {
                                    view.showMatchmakingFailed("No game found in time.");
                                    if (onMatchmakingCancelledOrFailed != null) {
                                        onMatchmakingCancelledOrFailed.run();
                                    }
                                }
                            });
                            if (matchmakingThread != null) matchmakingThread.interrupt();
                        }
                    }
                }, CLIENT_SIDE_MATCHMAKING_TIMEOUT_MS);

                logger.fine("Attempting to register client callback with server...");
                clientCallback.register();
                registrationSuccessful = true;
                logger.info("Client callback successfully registered with server.");

                if (cancellationRequested.get()) {
                    logger.info("Matchmaking cancelled by user before calling gameModel.startGame().");
                    return;
                }

                logger.fine("Calling gameModel.startGame() to find or create a game session...");
                GameInfo info = gameModel.startGame();
                logger.info("Received GameInfo from server: gameId=" + info.gameId +
                        ", remainingWaitingTime=" + info.remainingWaitingTime +
                        ", roundLength (for game rounds)=" + info.roundLength);

                cleanupTimeoutTimer();

                if (cancellationRequested.get()) {
                    logger.info("Matchmaking cancelled by user after gameModel.startGame() returned.");
                    return;
                }

                if (info.gameId != null && !info.gameId.isEmpty()) {
                    logger.info("Game session found/created: " + info.gameId + ". Setting game context in GameController.");

                    if (this.gameController != null) {
                        this.gameController.setGameContext(info.gameId, info.roundLength);
                    } else {
                        logger.severe("CRITICAL: GameController instance is null in MatchmakingController. Cannot set game context!");
                        throw new IllegalStateException("GameController not available to set context from MatchmakingController.");
                    }

                    String message = "Game Found! Preparing your session...";
                    Platform.runLater(() -> view.startCountdown(info.remainingWaitingTime, message));

                    long delayToProceedMs = info.remainingWaitingTime * 1000L;
                    if (delayToProceedMs < 0) delayToProceedMs = 0;

                    logger.fine("Scheduled UI transition to game view in " + delayToProceedMs + "ms.");
                    new Timer("ProceedToGameViewTimer", true).schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                if (!cancellationRequested.get() && onMatchReadyToProceed != null) {
                                    logger.info("Matchmaking countdown/wait finished. Invoking onMatchReadyToProceed.");
                                    onMatchReadyToProceed.run();
                                } else if (cancellationRequested.get()) {
                                    logger.info("Matchmaking countdown finished, but cancellation was requested during this period.");
                                }
                            });
                        }
                    }, delayToProceedMs);

                } else {
                    logger.warning("gameModel.startGame() returned null or empty gameId. Server failed to assign game.");
                    Platform.runLater(() -> {
                        view.showMatchmakingFailed("Server could not assign to a game.");
                        if (onMatchmakingCancelledOrFailed != null) {
                            onMatchmakingCancelledOrFailed.run();
                        }
                    });
                }

            } catch (PlayerNotLoggedInException e) {
                logger.log(Level.WARNING, "PlayerNotLoggedInException during matchmaking.", e);
                cleanupTimeoutTimer();
                Platform.runLater(() -> {
                    view.showMatchmakingFailed("Login session expired or invalid. Please log in again.");
                    if (onMatchmakingCancelledOrFailed != null) {
                        onMatchmakingCancelledOrFailed.run();
                    }
                });
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected exception during matchmaking process.", e);
                cleanupTimeoutTimer();
                Platform.runLater(() -> {
                    view.showMatchmakingFailed("An error occurred: " + e.getMessage());
                    if (onMatchmakingCancelledOrFailed != null) {
                        onMatchmakingCancelledOrFailed.run();
                    }
                });
            } finally {
                logger.fine("Matchmaking thread finished execution. Cancellation requested: " + cancellationRequested.get());
                if (cancellationRequested.get() && !registrationSuccessful) {
                    // If cancelled before successful registration and thread ends here.
                    // Ensure UI reflects cancellation if not already handled by interrupt.
                    Platform.runLater(() -> {
                        view.showMatchmakingCancelled();
                        if (onMatchmakingCancelledOrFailed != null) {
                            onMatchmakingCancelledOrFailed.run();
                        }
                    });
                }
            }
        });
        matchmakingThread.setDaemon(true);
        matchmakingThread.start();
    }

    public MatchmakingView getView() {
        return view;
    }

    public void setOnMatchReadyToProceed(Runnable onMatchReadyToProceed) {
        this.onMatchReadyToProceed = onMatchReadyToProceed;
    }

    public void setOnMatchmakingCancelledOrFailed(Runnable onMatchmakingCancelledOrFailed) {
        this.onMatchmakingCancelledOrFailed = onMatchmakingCancelledOrFailed;
    }
}