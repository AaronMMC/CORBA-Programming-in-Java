package com.cs9322.team05.client.player.interfaces;

import ModifiedHangman.AttemptedLetter;
import ModifiedHangman.GamePlayer;
import ModifiedHangman.GameResult;
import ModifiedHangman.RoundResult;

import java.util.List;
import java.util.function.Consumer;

public interface GameViewInterface {

    void showWaitingTimer(int seconds); 

    void showRoundDuration(int seconds); 

    void prepareNewRound(int wordLength, int roundNumber); 

    void updateMaskedWord(String maskedWord);

    void updateAttemptsLeft(long remainingAttempts);

    void showAttemptedLetters(List<AttemptedLetter> attempted);

    void disableGuessing(); 

    void showStatusMessage(String message); 

    void showRoundResult(RoundResult result);

    void showFinalResult(GameResult result);

    void showError(String message);

    void showLeaderboard(List<GamePlayer> leaderboard); 

    void clearAll();

    void onReturnToMenu(); 

    
    
    

    void setOnGuess(Consumer<Character> onGuess);

    void setOnLeaderboard(Runnable onShowLeaderboard);

    void setOnPlayAgain(Runnable onPlayAgain);

    void setOnBackToMenu(Runnable onBackToMenu);
}