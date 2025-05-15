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
    private final ClientCallbackImpl callback;
    private final MatchmakingView view;

    private Runnable onMatchReadyToProceed;
    private Runnable onMatchmakingCancelledOrFailed;

    private Thread matchmakingThread;
    private final AtomicBoolean cancellationRequested = new AtomicBoolean(false);
    private static final long CLIENT_SIDE_MATCHMAKING_TIMEOUT_MS = 30000;
    private Timer timeoutTimer = null;

    public MatchmakingController(GameModel gameModel, ClientCallbackImpl callback) {
        this.gameModel = gameModel;
        this.callback = callback;
        this.view = new MatchmakingView();

        view.setOnCancel(() -> {
            logger.info("Cancellation requested by user.");
            cancellationRequested.set(true);
            cleanupTimeoutTimer();
            if (matchmakingThread != null && matchmakingThread.isAlive()) {
                matchmakingThread.interrupt();
            }
            view.showMatchmakingCancelled();
            // Consider unregistering callback here if appropriate
            // callback.unregister();
            if (onMatchmakingCancelledOrFailed != null) {
                onMatchmakingCancelledOrFailed.run();
            }
        });
    }

    private void cleanupTimeoutTimer() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer.purge();
            timeoutTimer = null;
        }
    }

    public void startMatchmaking() {
        cancellationRequested.set(false);
        view.showSearching();
        logger.info("Starting matchmaking process...");

        matchmakingThread = new Thread(() -> {
            try {
                cleanupTimeoutTimer();
                timeoutTimer = new Timer(true);
                timeoutTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (matchmakingThread != null && matchmakingThread.isAlive() && !cancellationRequested.get()) {
                            logger.warning("Client-side matchmaking timeout reached.");
                            Platform.runLater(() -> {
                                if (!cancellationRequested.get()) { // Double check
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

                logger.fine("Registering client callback...");
                callback.register();
                logger.info("Client callback registered.");

                if (cancellationRequested.get()) {
                    logger.info("Matchmaking cancelled before calling startGame.");
                    Platform.runLater(view::showMatchmakingCancelled);
                    cleanupTimeoutTimer();
                    return;
                }

                logger.fine("Calling gameModel.startGame()...");
                GameInfo info = gameModel.startGame();
                logger.info("Received GameInfo: gameId=" + info.gameId + ", waitingTime=" + info.remainingWaitingTime + ", roundLength=" + info.roundLength);

                cleanupTimeoutTimer();

                if (cancellationRequested.get()) {
                    logger.info("Matchmaking cancelled after startGame returned, before processing GameInfo.");
                    Platform.runLater(view::showMatchmakingCancelled);
                    // Potentially inform server to leave if gameId was assigned
                    return;
                }

                if (info.gameId != null && !info.gameId.isEmpty()) {
                    String message = "Game Found! Preparing to start...";
                    Platform.runLater(() -> view.startCountdown(info.remainingWaitingTime, message));

                    long delayToProceed = info.remainingWaitingTime * 1000L;
                    if (delayToProceed < 0) delayToProceed = 0; // Ensure non-negative delay

                    new Timer(true).schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                if (!cancellationRequested.get() && onMatchReadyToProceed != null) {
                                    logger.info("Countdown finished or initial wait period over. Proceeding to game screen setup.");
                                    onMatchReadyToProceed.run();
                                } else if (cancellationRequested.get()){
                                    logger.info("Countdown finished but cancellation was requested during countdown.");
                                    // View should already be in cancelled state by setOnCancel handler
                                }
                            });
                        }
                    }, delayToProceed);

                } else {
                    logger.warning("startGame() returned null or empty gameId.");
                    Platform.runLater(() -> {
                        view.showMatchmakingFailed("Could not join or create a game server-side.");
                        if (onMatchmakingCancelledOrFailed != null) {
                            onMatchmakingCancelledOrFailed.run();
                        }
                    });
                }

            } catch (PlayerNotLoggedInException e) {
                logger.log(Level.SEVERE, "PlayerNotLoggedInException during matchmaking.", e);
                cleanupTimeoutTimer();
                Platform.runLater(() -> {
                    view.showMatchmakingFailed("You are not logged in.");
                    if (onMatchmakingCancelledOrFailed != null) {
                        onMatchmakingCancelledOrFailed.run();
                    }
                });
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception during matchmaking.", e);
                cleanupTimeoutTimer();
                Platform.runLater(() -> {
                    view.showMatchmakingFailed("Error: " + e.getMessage());
                    if (onMatchmakingCancelledOrFailed != null) {
                        onMatchmakingCancelledOrFailed.run();
                    }
                });
            } finally {
                logger.fine("Matchmaking thread finishing.");
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