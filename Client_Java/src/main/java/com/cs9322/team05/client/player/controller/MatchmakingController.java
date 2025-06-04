package com.cs9322.team05.client.player.controller;

import ModifiedHangman.GameInfo;
import ModifiedHangman.PlayerNotLoggedInException;
import com.cs9322.team05.client.player.callback.ClientCallbackImpl;
import com.cs9322.team05.client.player.model.GameModel;
import com.cs9322.team05.client.player.view.MatchmakingView;
import javafx.application.Platform;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer; 
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatchmakingController {
    private static final Logger logger = Logger.getLogger(MatchmakingController.class.getName());
    private final GameModel gameModel;
    private final ClientCallbackImpl clientCallback;
    private final GameController gameController; 
    private final MatchmakingView view;

    private Runnable onMatchReadyToProceed;
    
    private Consumer<String> onMatchmakingCancelledOrFailed;

    private Thread matchmakingThread;
    private final AtomicBoolean cancellationRequested = new AtomicBoolean(false);
    private static final long CLIENT_SIDE_MATCHMAKING_TIMEOUT_MS = 30000; 
    private Timer timeoutTimer = null;

    public static final String REASON_TIMEOUT = "NO_GAME_FOUND_TIMEOUT";
    public static final String REASON_SERVER_ERROR = "SERVER_ASSIGN_ERROR";
    public static final String REASON_LOGIN_EXPIRED = "LOGIN_EXPIRED";
    public static final String REASON_UNEXPECTED_ERROR = "UNEXPECTED_ERROR";


    public MatchmakingController(GameModel gameModel, ClientCallbackImpl clientCallback, GameController gameController) {
        this.gameModel = gameModel;
        this.clientCallback = clientCallback;
        this.gameController = gameController;
        this.view = new MatchmakingView();


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
            String failureReason = null;
            
            
            
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
                                        onMatchmakingCancelledOrFailed.accept(REASON_TIMEOUT);
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



                logger.fine("Calling gameModel.startGame() to find or create a game session...");
                GameInfo info = gameModel.startGame(); 
                logger.info("Received GameInfo from server: gameId=" + (info != null ? info.gameId : "null") +
                        ", remainingWaitingTime=" + (info != null ? info.remainingWaitingTime : "N/A") +
                        ", roundLength (for game rounds)=" + (info != null ? info.roundLength : "N/A"));

                cleanupTimeoutTimer(); 



                if (info != null && info.gameId != null && !info.gameId.isEmpty()) {
                    logger.info("Game session found/created: " + info.gameId + ". Setting game context in GameController.");

                    if (this.gameController != null) {
                        this.gameController.setGameContext(info.gameId, info.roundLength);
                    } else {
                        logger.severe("CRITICAL: GameController instance is null. Cannot set game context!");
                        throw new IllegalStateException("GameController not available.");
                    }

                    String message = "Game Found!. Starting in...";
                    Platform.runLater(() -> view.startCountdown(info.remainingWaitingTime, message));

                    long delayToProceedMs = info.remainingWaitingTime * 1000L;
                    if (delayToProceedMs < 0) delayToProceedMs = 0;

                    logger.fine("Scheduled UI transition to game view in " + delayToProceedMs + "ms.");
                    
                    Timer proceedTimer = new Timer("ProceedToGameViewTimer", true);
                    proceedTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                if (!cancellationRequested.get() && onMatchReadyToProceed != null) {
                                    logger.info("Matchmaking countdown/wait finished. Invoking onMatchReadyToProceed.");
                                    onMatchReadyToProceed.run();
                                } else if (cancellationRequested.get()) {
                                    logger.info("Matchmaking countdown finished, but cancellation was requested.");
                                    
                                }
                            });
                        }
                    }, delayToProceedMs);

                } else { 
                    logger.warning("gameModel.startGame() indicated no game assigned. Server failed to assign game.");
                    failureReason = REASON_SERVER_ERROR;
                    Platform.runLater(() -> view.showMatchmakingFailed("Server could not assign to a game."));
                }

            } catch (PlayerNotLoggedInException e) {
                logger.log(Level.WARNING, "PlayerNotLoggedInException during matchmaking.", e);
                failureReason = REASON_LOGIN_EXPIRED;
                Platform.runLater(() -> view.showMatchmakingFailed("Login session expired or invalid. Please log in again."));
            } catch (IllegalStateException e) { 
                logger.log(Level.SEVERE, "Unexpected exception during matchmaking process.", e);
                failureReason = REASON_UNEXPECTED_ERROR;
                Platform.runLater(() -> view.showMatchmakingFailed("An error occurred: " + e.getMessage()));
            } catch (ServantNotActive e) { 
                logger.log(Level.SEVERE, "Unexpected exception during matchmaking process.", e);
                failureReason = REASON_UNEXPECTED_ERROR;
                Platform.runLater(() -> view.showMatchmakingFailed("An error occurred: " + e.getMessage()));
            } catch (WrongPolicy e) { 
                logger.log(Level.SEVERE, "Unexpected exception during matchmaking process.", e);
                failureReason = REASON_UNEXPECTED_ERROR;
                Platform.runLater(() -> view.showMatchmakingFailed("An error occurred: " + e.getMessage()));
            } catch (InvalidName e) {
                throw new RuntimeException(e);
            } catch (AdapterInactive e) {
                throw new RuntimeException(e);
            } finally {
                logger.fine("Matchmaking thread finished execution. Cancellation: " + cancellationRequested.get() + ", FailureReason: " + failureReason);
                cleanupTimeoutTimer();
                logger.info("Matchmaking thread ending.");
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

    public void setOnMatchmakingCancelledOrFailed(Consumer<String> onMatchmakingCancelledOrFailed) {
        this.onMatchmakingCancelledOrFailed = onMatchmakingCancelledOrFailed;
    }
}