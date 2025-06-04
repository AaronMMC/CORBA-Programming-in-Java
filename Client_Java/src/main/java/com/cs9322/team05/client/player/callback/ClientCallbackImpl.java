package com.cs9322.team05.client.player.callback;

import ModifiedHangman.*;
import com.cs9322.team05.client.player.controller.GameController;
import javafx.application.Platform;
import javafx.scene.control.Alert; // Import Alert
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientCallbackImpl extends ClientCallbackPOA {
    private static final Logger logger = Logger.getLogger(ClientCallbackImpl.class.getName());
    private final ORB orb;
    private final GameService gameService;
    private final String token;
    private GameController controller;
    private final POA rootPoaInstance;
    private Runnable onSessionInvalidatedCallback; // ADDED: Field to hold the callback

    public ClientCallbackImpl(ORB orb, GameService gameService, String token, GameController controller) {
        this.orb = orb;
        this.gameService = gameService;
        this.token = token;
        this.controller = controller;
        try {
            this.rootPoaInstance = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            this.rootPoaInstance.the_POAManager().activate();
        } catch (InvalidName | AdapterInactive e) {
            logger.log(Level.SEVERE, "Failed to initialize or activate RootPOA in ClientCallbackImpl constructor", e);
            throw new RuntimeException("POA initialization/activation failed", e);
        }
    }

    // ADDED: Setter for the session invalidation callback
    public void setOnSessionInvalidatedCallback(Runnable onSessionInvalidatedCallback) {
        this.onSessionInvalidatedCallback = onSessionInvalidatedCallback;
    }

    public void register() throws PlayerNotLoggedInException, ServantNotActive, WrongPolicy, InvalidName, AdapterInactive {
        if (rootPoaInstance == null) {
            logger.severe("RootPOA is null in register method. Cannot proceed.");
            throw new RuntimeException("RootPOA not initialized.");
        }
        org.omg.CORBA.Object ref = rootPoaInstance.servant_to_reference(this);
        ClientCallback stub = ClientCallbackHelper.narrow(ref);
        gameService.registerCallback(stub, token);
        logger.info("Client callback registered with the server. Token: " + token);
    }

    public GameController getController() {
        return this.controller;
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void startRound(int wordLength, int roundNumber) {
        logger.info("SERVER CALLBACK: startRound RECEIVED - wordLength: " + wordLength + ", roundNumber: " + roundNumber);
        Platform.runLater(() -> {
            logger.fine("Platform.runLater: Executing controller.onStartRound for round " + roundNumber);
            if (controller != null) {
                controller.onStartRound(wordLength, roundNumber);
            } else {
                logger.severe("CRITICAL: GameController is NULL in startRound callback!");
            }
        });
    }

    @Override
    public void proceedToNextRound(int wordLength) {
        logger.info("SERVER CALLBACK: proceedToNextRound RECEIVED - nextWordLength: " + wordLength);
        Platform.runLater(() -> {
            logger.fine("Platform.runLater: Executing controller.onProceedToNextRound");
            if (controller != null) {
                controller.onProceedToNextRound(wordLength);
            } else {
                logger.severe("CRITICAL: GameController is NULL in proceedToNextRound callback!");
            }
        });
    }

    @Override
    public void endRound(RoundResult result) {
        String winnerUsername = "None";
        if (result != null && result.roundWinner != null && result.roundWinner.username != null) {
            winnerUsername = result.roundWinner.username;
        }
        logger.info("SERVER CALLBACK: endRound RECEIVED - Round: " + (result != null ? result.roundNumber : "N/A") + ", Winner: " + winnerUsername + ", Correct Word: " + (result != null ? result.wordToGuess : "N/A"));
        Platform.runLater(() -> {
            logger.fine("Platform.runLater: Executing controller.onEndRound");
            if (controller != null) {
                controller.onEndRound(result);
            } else {
                logger.severe("CRITICAL: GameController is NULL in endRound callback!");
            }
        });
    }

    @Override
    public void endGame(GameResult result) {
        String winner = "N/A";
        if (result != null && result.gameWinner != null) {
            winner = result.gameWinner;
        }
        logger.info("SERVER CALLBACK: endGame RECEIVED - Game Winner: " + winner);
        Platform.runLater(() -> {
            logger.fine("Platform.runLater: Executing controller.onEndGame");
            if (controller != null) {
                controller.onEndGame(result);
            } else {
                logger.severe("CRITICAL: GameController is NULL in endGame callback!");
            }
        });
    }

    @Override
    public void startGameFailed() {
        logger.info("SERVER CALLBACK: startGameFailed RECEIVED");
        Platform.runLater(() -> {
            logger.fine("Platform.runLater: Executing controller action for startGameFailed");
            if (controller != null) {
                String failureMessage = "GAME_START_FAILED: Not enough players.";
                logger.info("Processed startGameFailed: Relaying to GameController. Message: " + failureMessage);
                controller.handleGameStartFailed(failureMessage);
            } else {
                logger.severe("CRITICAL: GameController is NULL in startGameFailed callback!");
            }
        });
    }

    @Override
    public void notifySessionInvalidated(String reason) {
        logger.info("SERVER CALLBACK: notifySessionInvalidated RECEIVED - Reason: " + reason);
        Platform.runLater(() -> {
            logger.fine("Platform.runLater: Displaying session invalidated pop-up.");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Session Expired");
            alert.setHeaderText("Your session has been invalidated!");
            alert.setContentText("Another device has logged in with your account, or your session has otherwise expired. You will be logged out.");
            alert.showAndWait();

            if (onSessionInvalidatedCallback != null) {
                logger.info("Triggering onSessionInvalidatedCallback.");
                onSessionInvalidatedCallback.run();
            } else {
                logger.warning("onSessionInvalidatedCallback is null in ClientCallbackImpl.notifySessionInvalidated. Cannot navigate logout.");
            }
        });
    }
}