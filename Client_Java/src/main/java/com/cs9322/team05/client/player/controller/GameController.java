package com.cs9322.team05.client.player.controller;

import ModifiedHangman.*;
import com.cs9322.team05.client.player.interfaces.GameViewInterface;
import com.cs9322.team05.client.player.model.GameModel;

import java.util.Arrays;

public class GameController {
    private final GameModel gameModel;
    private       GameViewInterface view;
    private       String gameId;

    public GameController(GameModel gameModel, GameViewInterface view) {
        this.gameModel = gameModel;
        this.view      = view;
        bindViewActions();
    }

    private void bindViewActions() {
        view.setOnStart(this::startGame);
        view.setOnGuess(this::submitGuess);
        view.setOnLeaderboard(this::fetchLeaderboard);
        view.setOnPlayAgain(this::resetAndStart);
        view.setOnBackToMenu(this::backToMenu);
    }




    public void startGame() {
        view.clearAll();
        try {
            GameInfo info = gameModel.startGame();
            this.gameId = info.gameId;
            view.showWaitingTimer(info.remainingWaitingTime);
            view.showRoundDuration(info.roundLength);
        } catch (PlayerNotLoggedInException e) {
            view.showError("Cannot start game: " + e.getMessage());
        }
    }

    public void submitGuess(char letter) {
        try {
            GuessResponse resp = gameModel.guessLetter(gameId, letter);
            view.updateMaskedWord(resp.maskedWord);
            view.updateAttemptsLeft(resp.remainingAttemptsLeft);
            view.showAttemptedLetters(Arrays.asList(resp.attemptedLetters));
        } catch (Exception e) {
            view.showError("Guess failed: " + e.getMessage());
        }
    }

    public void fetchLeaderboard() {
        try {
            Leaderboard lb = gameModel.getLeaderboard();
            view.showLeaderboard(Arrays.asList(lb.players));
        } catch (PlayerNotLoggedInException e) {
            view.showError("Cannot fetch leaderboard: " + e.getMessage());
        }
    }

    public void onStartRound(int wordLength) {
        view.prepareNewRound(wordLength);
    }

    public void onProceedToNextRound(int nextWordLength) {
        view.showWaitingTimer(nextWordLength);
    }

    public void onEndRound(RoundResult result) {
        view.showRoundResult(result);
    }

    public void onEndGame(GameResult result) {
        view.showFinalResult(result);
    }

    public void resetAndStart() {
        view.clearAll();
        startGame();
    }

    public void backToMenu() {
        view.onReturnToMenu();
    }

    public void setGameView(GameViewInterface view) {
        this.view = view;
        bindViewActions();
    }

    public String getGameId() {
        return gameId;
    }
}
