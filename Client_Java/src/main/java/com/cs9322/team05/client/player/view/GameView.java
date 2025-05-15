package com.cs9322.team05.client.player.view;

import ModifiedHangman.AttemptedLetter;
import ModifiedHangman.GamePlayer;
import ModifiedHangman.GameResult;
import ModifiedHangman.RoundResult;
import com.cs9322.team05.client.player.interfaces.GameViewInterface;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameView implements GameViewInterface {
    private static final Logger logger = Logger.getLogger(GameView.class.getName());

    private final Label systemMessageLabel = new Label("Waiting for game to initialize...");
    private final Label matchmakingCountdownLabel = new Label(); 
    private final Label roundInfoLabel = new Label(); 
    private final Label maskedWordLabel = new Label("---");
    private final Label attemptsLeftLabel = new Label();
    private final ListView<String> guessedLettersList = new ListView<>();
    private final TextField guessInput = new TextField();
    private final Button leaderboardBtn = new Button("Overall Leaderboard");
    private final Button playAgainBtn = new Button("Play Again");
    private final Button backToMenuBtn = new Button("Back to Menu");
    private final VBox root = new VBox(10); 

    private Consumer<Character> onGuess;
    private Runnable onLeaderboard;
    private Runnable onPlayAgain;
    private Runnable onBackToMenu;

    private Timeline roundTimerTimeline;
    private Timeline matchmakingTimerTimeline;


    public GameView() {
        systemMessageLabel.setFont(new Font("Arial", 16));
        matchmakingCountdownLabel.setFont(new Font("Arial", 18));
        matchmakingCountdownLabel.setStyle("-fx-font-weight: bold;");
        roundInfoLabel.setFont(new Font("Arial", 16));
        maskedWordLabel.setFont(new Font("Arial", 32)); 
        maskedWordLabel.setStyle("-fx-font-weight: bold; -fx-letter-spacing: 5px;");
        attemptsLeftLabel.setFont(new Font("Arial", 14));
        guessedLettersList.setMaxHeight(100);
        guessedLettersList.setMinWidth(200);
        guessedLettersList.setMaxWidth(250);


        guessInput.setPromptText("Enter letter & press ENTER");
        guessInput.setMaxWidth(200);
        guessInput.setOnKeyPressed(evt -> {
            if (evt.getCode() == KeyCode.ENTER && !guessInput.isDisabled() && onGuess != null) {
                String text = guessInput.getText().trim();
                if (!text.isEmpty() && Character.isLetter(text.charAt(0))) {
                    onGuess.accept(Character.toLowerCase(text.charAt(0)));
                    guessInput.clear();
                } else {
                    guessInput.clear();
                }
            }
        });

        HBox topInfoBox = new HBox(20, matchmakingCountdownLabel, roundInfoLabel);
        topInfoBox.setAlignment(Pos.CENTER);

        VBox mainGameArea = new VBox(10, maskedWordLabel, guessInput, attemptsLeftLabel, guessedLettersList);
        mainGameArea.setAlignment(Pos.CENTER);

        HBox controls = new HBox(10, leaderboardBtn, playAgainBtn, backToMenuBtn);
        controls.setAlignment(Pos.CENTER);

        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().addAll(systemMessageLabel, topInfoBox, mainGameArea, controls);

        
        leaderboardBtn.setOnAction(e -> {
            if (onLeaderboard != null) onLeaderboard.run();
        });
        playAgainBtn.setOnAction(e -> {
            if (onPlayAgain != null) onPlayAgain.run();
        });
        backToMenuBtn.setOnAction(e -> {
            if (onBackToMenu != null) onBackToMenu.run();
        });

        clearAll();
        logger.fine("GameView initialized.");
    }

    private void animateLabelUpdate(Label label, String newText) {
        Platform.runLater(() -> {
            if (label.getText() == null || !label.getText().equals(newText)) {
                
                label.setText(newText);
            }
        });
    }

    @Override
    public void showWaitingTimer(int seconds) {
        logger.info("GameView: Displaying initial matchmaking waiting timer: " + seconds + "s");
        Platform.runLater(() -> {
            animateLabelUpdate(systemMessageLabel, "Game will start soon...");
            animateLabelUpdate(matchmakingCountdownLabel, "Starting in: " + seconds + "s");
            animateLabelUpdate(roundInfoLabel, ""); 

            if (matchmakingTimerTimeline != null) {
                matchmakingTimerTimeline.stop();
            }
            matchmakingTimerTimeline = new Timeline();
            matchmakingTimerTimeline.setCycleCount(seconds +1);
            final int[] remainingSeconds = {seconds};
            matchmakingTimerTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
                remainingSeconds[0]--;
                if (remainingSeconds[0] > 0) {
                    animateLabelUpdate(matchmakingCountdownLabel, "Starting in: " + remainingSeconds[0] + "s");
                } else if (remainingSeconds[0] == 0) {
                    animateLabelUpdate(matchmakingCountdownLabel, "Starting now!");
                } else { 
                    matchmakingTimerTimeline.stop();
                    animateLabelUpdate(matchmakingCountdownLabel, ""); 
                    animateLabelUpdate(systemMessageLabel, "Waiting for server to start the first round...");
                }
            }));
            matchmakingTimerTimeline.playFromStart();
        });
    }

    @Override
    public void showRoundDuration(int totalSeconds) {
        logger.info("GameView: Displaying round duration: " + totalSeconds + "s");
        Platform.runLater(() -> {
            animateLabelUpdate(matchmakingCountdownLabel, ""); 
            if (roundTimerTimeline != null) {
                roundTimerTimeline.stop();
            }
            roundTimerTimeline = new Timeline();
            roundTimerTimeline.setCycleCount(totalSeconds + 1); 
            final int[] remainingSeconds = {totalSeconds};

            animateLabelUpdate(roundInfoLabel, "Time Left: " + remainingSeconds[0] + "s"); 

            roundTimerTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), event -> {
                remainingSeconds[0]--;
                if (remainingSeconds[0] >= 0) {
                    animateLabelUpdate(roundInfoLabel, "Time Left: " + remainingSeconds[0] + "s");
                }
                if (remainingSeconds[0] < 0) { 
                    roundTimerTimeline.stop();
                    animateLabelUpdate(roundInfoLabel, "Time's Up!");
                    disableGuessing(); 
                }
            }));
            roundTimerTimeline.playFromStart();
        });
    }

    @Override
    public void prepareNewRound(int wordLength, int roundNumber) {
        logger.info("GameView: Preparing new round " + roundNumber + " with word length " + wordLength);
        Platform.runLater(() -> {
            animateLabelUpdate(systemMessageLabel, "Round " + roundNumber + " - Guess the word!");
            animateLabelUpdate(matchmakingCountdownLabel, ""); 

            StringBuilder masked = new StringBuilder();
            for (int i = 0; i < wordLength; i++) {
                masked.append("_");
            }
            animateLabelUpdate(maskedWordLabel, masked.toString().replaceAll(".(?!$)", "$0 "));
            animateLabelUpdate(attemptsLeftLabel, ""); 
            
            
            
            
            animateLabelUpdate(attemptsLeftLabel, "Attempts left: 6"); 
            guessedLettersList.getItems().clear();
            guessInput.clear();
            guessInput.setDisable(false);
            guessInput.requestFocus();

            playAgainBtn.setVisible(false);
            playAgainBtn.setManaged(false);
            backToMenuBtn.setVisible(true);
            backToMenuBtn.setManaged(true);
            leaderboardBtn.setDisable(false);
            leaderboardBtn.setVisible(true);
            leaderboardBtn.setManaged(true);

            if (roundTimerTimeline != null) { 
                roundTimerTimeline.stop();
            }
            
        });
    }

    @Override
    public void updateMaskedWord(String masked) {
        animateLabelUpdate(maskedWordLabel, masked.replaceAll(".(?!$)", "$0 "));
    }

    @Override
    public void updateAttemptsLeft(long n) {
        animateLabelUpdate(attemptsLeftLabel, "Attempts left: " + n);
    }

    @Override
    public void showAttemptedLetters(List<AttemptedLetter> letters) {
        Platform.runLater(() -> {
            List<String> items = letters.stream()
                    .map(a -> a.letter + (a.isLetterCorrect ? " [✓]" : " [✗]"))
                    .collect(Collectors.toList());
            guessedLettersList.getItems().setAll(items);
        });
    }

    @Override
    public void disableGuessing() {
        Platform.runLater(() -> {
            logger.fine("GameView: Disabling guess input.");
            guessInput.setDisable(true);
        });
    }

    @Override
    public void showStatusMessage(String message) {
        Platform.runLater(() -> {
            logger.fine("GameView: Displaying status message - " + message);
            animateLabelUpdate(systemMessageLabel, message);
        });
    }

    @Override
    public void showRoundResult(RoundResult r) {
        logger.info("GameView: Showing round result. Status: " + r.statusMessage);
        Platform.runLater(() -> {
            disableGuessing();
            if (roundTimerTimeline != null) {
                roundTimerTimeline.stop();
            }
            animateLabelUpdate(roundInfoLabel, "Round " + r.roundNumber + " Over");

            StringBuilder sb = new StringBuilder("Round " + r.roundNumber + " Result:\n");
            sb.append(r.statusMessage).append("\n");
            if (r.roundWinner != null && r.roundWinner.username != null && !r.roundWinner.username.isEmpty()) {
                sb.append("Round Winner: ").append(r.roundWinner.username).append("\n");
            }
            sb.append("Correct Word: ").append(r.statusMessage).append("\n\n");
            sb.append("Current Game Standings (Rounds Won):\n");
            if (r.currentGameLeaderboard != null) {
                for(GamePlayer p : r.currentGameLeaderboard) {
                    sb.append(p.username).append(": ").append(p.wins).append("\n");
                }
            } else {
                sb.append("No standings available.\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Round " + r.roundNumber + " Over");
            alert.setHeaderText(r.statusMessage != null && !r.statusMessage.isEmpty() ? r.statusMessage : "Round Concluded");
            alert.setContentText(sb.toString());
            alert.getButtonTypes().setAll(ButtonType.OK);
            alert.showAndWait();
            animateLabelUpdate(systemMessageLabel, "Waiting for next round or game end...");
        });
    }

    @Override
    public void showFinalResult(GameResult g) {
        logger.info("GameView: Showing final game result. Winner: " + g.gameWinner);
        Platform.runLater(() -> {
            disableGuessing();
            if (roundTimerTimeline != null) roundTimerTimeline.stop();
            if (matchmakingTimerTimeline != null) matchmakingTimerTimeline.stop();

            animateLabelUpdate(systemMessageLabel, "Game Over!");
            animateLabelUpdate(matchmakingCountdownLabel, "");
            animateLabelUpdate(roundInfoLabel, "");
            animateLabelUpdate(maskedWordLabel, "");
            animateLabelUpdate(attemptsLeftLabel, "");
            guessedLettersList.getItems().clear();

            StringBuilder sb = new StringBuilder();
            String headerText;

            if (g.gameWinner != null && !g.gameWinner.isEmpty() && !g.gameWinner.equalsIgnoreCase("N/A")) {
                headerText = g.gameWinner + " wins the game!";
                sb.append("Congratulations to ").append(g.gameWinner).append("!\n");
            } else {
                headerText = g.gameWinner != null && !g.gameWinner.isEmpty() ? g.gameWinner : "The game has concluded.";
                sb.append(headerText).append("\n");
            }

            sb.append("\nFinal Standings (Rounds Won):\n");
            if (g.leaderboard != null && g.leaderboard.length > 0) {
                for (GamePlayer p : g.leaderboard) {
                    sb.append(p.username).append(" – ").append(p.wins).append("\n");
                }
            } else {
                sb.append("No final standings available.\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(headerText);
            alert.setContentText(sb.toString());
            alert.getButtonTypes().setAll(ButtonType.OK);
            alert.showAndWait();

            playAgainBtn.setVisible(true);
            playAgainBtn.setManaged(true);
            backToMenuBtn.setVisible(true);
            backToMenuBtn.setManaged(true);
            leaderboardBtn.setDisable(false); 
            leaderboardBtn.setVisible(true);
            leaderboardBtn.setManaged(true);
        });
    }

    @Override
    public void showError(String msg) {
        Platform.runLater(() -> {
            logger.log(Level.SEVERE, "GameView showError: " + msg);
            Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred");
            alert.showAndWait();
        });
    }

    @Override
    public void showLeaderboard(List<GamePlayer> leaderboard) {
        Platform.runLater(() -> {
            StringBuilder sb = new StringBuilder("Overall Top Players (Total Wins):\n");
            if (leaderboard != null && !leaderboard.isEmpty()) {
                for (GamePlayer p : leaderboard) {
                    sb.append(p.username).append(" – ").append(p.wins).append("\n");
                }
            } else {
                sb.append("No overall leaderboard data available yet.");
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION, sb.toString(), ButtonType.OK);
            alert.setHeaderText("Overall Leaderboard");
            alert.showAndWait();
        });
    }

    @Override
    public void clearAll() {
        Platform.runLater(() -> {
            logger.fine("GameView: clearAll UI state.");
            animateLabelUpdate(systemMessageLabel, "Waiting for game to initialize...");
            animateLabelUpdate(matchmakingCountdownLabel, "");
            animateLabelUpdate(roundInfoLabel, "");
            animateLabelUpdate(maskedWordLabel, "---");
            animateLabelUpdate(attemptsLeftLabel, "");
            guessedLettersList.getItems().clear();
            guessInput.clear();
            guessInput.setDisable(true);

            leaderboardBtn.setDisable(true); 
            leaderboardBtn.setVisible(true);
            leaderboardBtn.setManaged(true);

            playAgainBtn.setVisible(false);
            playAgainBtn.setManaged(false);
            backToMenuBtn.setVisible(true);
            backToMenuBtn.setManaged(true);

            if (roundTimerTimeline != null) {
                roundTimerTimeline.stop();
            }
            if (matchmakingTimerTimeline != null) {
                matchmakingTimerTimeline.stop();
            }
        });
    }

    @Override
    public void onReturnToMenu() {
        logger.fine("GameView: onReturnToMenu called, invoking onBackToMenu callback.");
        if (onBackToMenu != null) {
            onBackToMenu.run();
        }
    }

    @Override
    public void setOnGuess(Consumer<Character> onGuess) {
        this.onGuess = onGuess;
    }

    
    
    
    

    @Override
    public void setOnLeaderboard(Runnable onLeaderboard) {
        this.onLeaderboard = onLeaderboard;
    }

    @Override
    public void setOnPlayAgain(Runnable onPlayAgain) {
        this.onPlayAgain = onPlayAgain;
    }

    @Override
    public void setOnBackToMenu(Runnable onBackToMenu) {
        this.onBackToMenu = onBackToMenu;
    }

    public Parent getRoot() {
        return root;
    }
}