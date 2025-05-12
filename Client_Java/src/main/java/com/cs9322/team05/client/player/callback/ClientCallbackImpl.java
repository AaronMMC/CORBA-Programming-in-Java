package com.cs9322.team05.client.player.callback;

import ModifiedHangman.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import javafx.application.Platform;
import com.cs9322.team05.client.player.controller.GameController;

public class ClientCallbackImpl extends ClientCallbackPOA {
    private final GameController    controller;

    public ClientCallbackImpl(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void startRound(int wordLength) {
        Platform.runLater(() -> controller.onStartRound(wordLength));
    }

    @Override
    public void proceedToNextRound(int wordLength) {
        Platform.runLater(() -> controller.onProceedToNextRound(wordLength));
    }

    @Override
    public void endRound(RoundResult result) {
        Platform.runLater(() -> controller.onEndRound(result));
    }

    @Override
    public void endGame(GameResult result) {
        Platform.runLater(() -> controller.onEndGame(result));
    }

    @Override
    public void startGameFailed() {
        Platform.runLater(() -> controller.onEndGame(
            new GameResult(controller.getGameId(), "Error", /* empty leaderboard */ null)
        ));
    }
}
