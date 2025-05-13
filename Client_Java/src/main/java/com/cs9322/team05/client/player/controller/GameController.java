package com.cs9322.team05.client.player.controller;

import ModifiedHangman.*;
import com.cs9322.team05.client.player.interfaces.HomeViewInterface;
import com.cs9322.team05.client.player.model.GameModel;
import com.cs9322.team05.client.player.interfaces.GameViewInterface;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.util.Arrays;

public class GameController {
    private final GameModel gameModel;
    private GameViewInterface view;
    private String gameId;

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

    public void registerCallback(ClientCallbackPOA callbackServant, POA poa) {
        try {
            gameModel.registerCallback(callbackServant, poa);
        } catch (PlayerNotLoggedInException | WrongPolicy | ServantNotActive e) {
            view.showError("Callback registration failed: " + e.getMessage());
        }
    }

    public void startGame() {
        view.clearAll(); // you might add this to reset UI before start
        try {
            GameInfo info = gameModel.startGame();
            gameId = info.gameId;
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
            if (resp.isWordGuessed) {
                view.showRoundResult(new RoundResult(
                        gameId,
                        "",
                        0,
                        "You’ve guessed the word!",
                        new GamePlayer[0]
                ));
            }
        } catch (GameNotFoundException e) {
            view.showError("Game not found: " + e.getMessage());
        } catch (PlayerNotLoggedInException e) {
            view.showError("Not logged in: " + e.getMessage());
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

    public String getGameId() {
        return gameId;
    }

    public void resetAndStart() {
        // Clear view and start a fresh game
        view.clearAll();
        startGame();
    }

    public void backToMenu() {
        // signal to MainApp or HomeController to swap scenes
        view.onReturnToMenu();
    }
    public void setGameView(GameViewInterface view) {
        this.view = view;
    }
}
