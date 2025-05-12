// src/main/java/com/cs9322/team05/client/player/view/GameViewInterface.java
package com.cs9322.team05.client.player.interfaces;

import ModifiedHangman.AttemptedLetter;
import ModifiedHangman.GamePlayer;
import ModifiedHangman.GameResult;
import ModifiedHangman.RoundResult;

import java.util.List;

public interface GameViewInterface {
    void showWaitingTimer(int seconds);
    void showRoundDuration(int seconds);
    void prepareNewRound(int wordLength);
    void updateMaskedWord(String maskedWord);
    void updateAttemptsLeft(long remainingAttempts);
    void showAttemptedLetters(List<AttemptedLetter> attempted);
    void showRoundResult(RoundResult result);
    void showFinalResult(GameResult result);
    void showError(String message);
    void showLeaderboard(List<GamePlayer> leaderboard);
}
