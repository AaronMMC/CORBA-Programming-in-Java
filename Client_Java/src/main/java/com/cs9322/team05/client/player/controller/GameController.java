package com.cs9322.team05.client.player.controller;

import ModifiedHangman.*;
import com.cs9322.team05.client.player.model.GameModel;
import com.cs9322.team05.client.player.interfaces.GameViewInterface;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.util.List;

public class GameController {
    private final GameModel gameModel;
    private final GameViewInterface view;
    private String gameId;

    public GameController(GameModel gameModel, GameViewInterface view) {
        this.gameModel = gameModel;
        this.view = view;
    }

    /**
     * After login, wire up your CORBA callback servant.
     */
    public void registerCallback(ClientCallbackPOA callbackServant, POA poa) {
        try {
            gameModel.registerCallback(callbackServant, poa);
        } catch (PlayerNotLoggedInException | WrongPolicy | ServantNotActive e) {
            view.showError("Callback registration failed: " + e.getMessage());
        }
    }

    /**
     * Start (or join) a game on the server.
     */
    public void startGame() {
        try {
            GameInfo info = gameModel.startGame();
            this.gameId = info.gameId;


            view.showWaitingTimer(info.remainingWaitingTime);


            view.showRoundDuration(info.roundLength);

        } catch (PlayerNotLoggedInException e) {
            view.showError("Cannot start game: " + e.getMessage());
        }
    }

    /**
     * Called when the user clicks “Guess” with a letter.
     */
    public void submitGuess(char letter) {
        try {
            GuessResponse resp = gameModel.guessLetter(gameId, letter);
            AttemptedLetter[] attempts = resp.attemptedLetters;
            view.updateMaskedWord(resp.maskedWord);
            view.updateAttemptsLeft(resp.remainingAttemptsLeft);
            view.showAttemptedLetters(List.of(attempts));

        } catch (GameNotFoundException e) {
            view.showError("Game not found: " + e.getMessage());
        } catch (PlayerNotLoggedInException e) {
            view.showError("Not logged in: " + e.getMessage());
        }
    }
//    public void fetchLeaderboard() {
//        try {
//            Leaderboard lb = gameModel.getLeaderboard();
//            view.showLeaderboard(lb.players);
//        } catch (PlayerNotLoggedInException e) {
//            view.showError("Cannot fetch leaderboard: " + e.getMessage());
//        }
//    }

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
}
