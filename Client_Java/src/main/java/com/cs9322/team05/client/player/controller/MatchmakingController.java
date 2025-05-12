package com.cs9322.team05.client.player.controller;

import ModifiedHangman.GameService;
import com.cs9322.team05.client.player.callback.ClientCallbackImpl;
import com.cs9322.team05.client.player.view.MatchmakingView;

public class MatchmakingController {

    private final GameService gameService;
    private final String playerUsername;
    private final ClientCallbackImpl callback;
    private final MatchmakingView view;

    public MatchmakingController(GameService gameService, String playerUsername, ClientCallbackImpl callback) {
        this.gameService = gameService;
        this.playerUsername = playerUsername;
        this.callback = callback;
        this.view = new MatchmakingView();
    }

    public void startMatchmaking() {
        new Thread(() -> {
            try {
                gameService.joinGame(playerUsername, callback);
                view.enqueueStatusUpdate("Match found! Starting game...");
            } catch (Exception e) {
                view.enqueueStatusUpdate("Failed to find a match.");
                e.printStackTrace();
            }
        }).start();
    }

    public MatchmakingView getView() {
        return view;
    }
}
