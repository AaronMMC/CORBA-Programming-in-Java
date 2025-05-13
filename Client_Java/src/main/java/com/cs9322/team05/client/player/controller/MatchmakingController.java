package com.cs9322.team05.client.player.controller;

import ModifiedHangman.GameInfo;
import ModifiedHangman.PlayerNotLoggedInException;
import com.cs9322.team05.client.player.callback.ClientCallbackImpl;
import com.cs9322.team05.client.player.model.GameModel;
import com.cs9322.team05.client.player.view.MatchmakingView;

public class MatchmakingController {
    private final GameModel gameModel;
    private final ClientCallbackImpl callback;
    private final MatchmakingView view;

    private Runnable onMatchFound;
    private Runnable onCancel;

    public MatchmakingController(GameModel gameModel,
                                 ClientCallbackImpl callback) {
        this.gameModel = gameModel;
        this.callback  = callback;
        this.view      = new MatchmakingView();

        view.setOnCancel(() -> {
            if (onCancel != null) onCancel.run();
        });
    }

    public void startMatchmaking() {
        new Thread(() -> {
            try {
                // 1) Register our CORBA callback with the server
                gameModel.registerCallback(callback, gameModel.getPoa());

                // 2) Call start_game() to host or join
                GameInfo info = gameModel.startGame();

                // 3) Update UI with remaining wait time
                view.enqueueStatusUpdate(
                        "Match found — starting in "
                                + info.remainingWaitingTime + "s"
                );

                // 4) Fire match-found callback
                if (onMatchFound != null) onMatchFound.run();

            } catch (PlayerNotLoggedInException e) {
                view.enqueueStatusUpdate("Not logged in.");
            } catch (Exception e) {
                view.enqueueStatusUpdate("Failed to start/join game.");
                e.printStackTrace();
            }
        }).start();
    }

    public MatchmakingView getView() {
        return view;
    }

    public void setOnMatchFound(Runnable onMatchFound) {
        this.onMatchFound = onMatchFound;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }
}
