package com.cs9322.team05.client.player.interfaces;

import ModifiedHangman.AttemptedLetter;
import ModifiedHangman.GamePlayer;
import ModifiedHangman.GameResult;
import ModifiedHangman.RoundResult;

import java.util.List;
import java.util.function.Consumer;

public interface GameViewInterface {

    void showWaitingTimer(int seconds); // For matchmaking countdown

    void showRoundDuration(int seconds); // For actual round playing time

    void prepareNewRound(int wordLength, int roundNumber); // Added roundNumber

    void updateMaskedWord(String maskedWord);

    void updateAttemptsLeft(long remainingAttempts);

    void showAttemptedLetters(List<AttemptedLetter> attempted);

    void disableGuessing(); // Added

    void showStatusMessage(String message); // Added

    void showRoundResult(RoundResult result);

    void showFinalResult(GameResult result);

    void showError(String message);

    void showLeaderboard(List<GamePlayer> leaderboard); // For overall leaderboard

    void clearAll();

    void onReturnToMenu(); // Callback for MainApp

    // Event Setters
    // setOnStart is removed as game start is server-driven after matchmaking.
    // void setOnStart(Runnable onStart);

    void setOnGuess(Consumer<Character> onGuess);

    void setOnLeaderboard(Runnable onShowLeaderboard);

    void setOnPlayAgain(Runnable onPlayAgain);

    void setOnBackToMenu(Runnable onBackToMenu);
}