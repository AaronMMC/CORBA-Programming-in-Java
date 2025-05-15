package com.cs9322.team05.client.player.view;

import ModifiedHangman.AttemptedLetter;
import ModifiedHangman.GamePlayer;
import ModifiedHangman.GameResult;
import ModifiedHangman.RoundResult;
import com.cs9322.team05.client.player.interfaces.GameViewInterface;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GameView implements GameViewInterface {
    private final Label countdownLabel = new Label();
    private final Label roundDurationLabel = new Label();
    private final Label maskedWordLabel = new Label();
    private final Label attemptsLeftLabel = new Label();
    private final ListView<String> guessedLettersList = new ListView<>();
    private final TextField guessInput = new TextField();
    private final Button startBtn = new Button("Start Game");
    private final Button leaderboardBtn = new Button("Show Leaderboard");
    private final Button playAgainBtn = new Button("Play Again");
    private final Button backToMenuBtn = new Button("Back to Menu");
    private final VBox root = new VBox(15);

    private Consumer<Character> onGuess;
    private Runnable onStart;
    private Runnable onLeaderboard;
    private Runnable onPlayAgain;
    private Runnable onBackToMenu;

    public GameView() {
        countdownLabel.setStyle("-fx-font-size: 18px;");
        roundDurationLabel.setStyle("-fx-font-size: 18px;");
        maskedWordLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        attemptsLeftLabel.setStyle("-fx-font-size: 16px;");

        guessInput.setPromptText("Enter letter and press ENTER");
        guessInput.setMaxWidth(120);
        guessInput.setOnKeyPressed(evt -> {
            if (evt.getCode() == KeyCode.ENTER && onGuess != null) {
                String text = guessInput.getText().trim();
                if (!text.isEmpty() && Character.isLetter(text.charAt(0))) {
                    onGuess.accept(Character.toLowerCase(text.charAt(0)));
                    guessInput.clear();
                } else {
                    guessInput.clear();
                }
            }
        });

        HBox timerBox = new HBox(20, countdownLabel, roundDurationLabel);
        timerBox.setAlignment(Pos.CENTER);

        HBox controls = new HBox(10, startBtn, leaderboardBtn, playAgainBtn, backToMenuBtn);
        controls.setAlignment(Pos.CENTER);

        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().addAll(timerBox, maskedWordLabel, guessInput, attemptsLeftLabel, guessedLettersList, controls);

        try {
            String css = getClass().getResource("/css/styles.css").toExternalForm();
            if (css != null) {
                root.getStylesheets().add(css);
            } else {
                System.err.println("Warning: styles.css not found.");
            }
        } catch (NullPointerException e) {
            System.err.println("Warning: styles.css not found or error loading it. " + e.getMessage());
        }


        startBtn.setOnAction(e -> {
            if (onStart != null) onStart.run();
        });
        leaderboardBtn.setOnAction(e -> {
            if (onLeaderboard != null) onLeaderboard.run();
        });
        playAgainBtn.setOnAction(e -> {
            if (onPlayAgain != null) onPlayAgain.run();
        });
        backToMenuBtn.setOnAction(e -> {
            if (onBackToMenu != null) onBackToMenu.run();
        });

        playAgainBtn.setVisible(false);
        backToMenuBtn.setVisible(false);
    }

    private void animateLabelUpdate(Label label, String newText) {
        if (label.getText() == null || label.getText().isEmpty() || !label.getText().equals(newText)) {
            FadeTransition ftOut = new FadeTransition(Duration.millis(150), label);
            ftOut.setFromValue(1.0);
            ftOut.setToValue(0.0);
            ftOut.setOnFinished(event -> {
                label.setText(newText);
                FadeTransition ftIn = new FadeTransition(Duration.millis(150), label);
                ftIn.setFromValue(0.0);
                ftIn.setToValue(1.0);
                ftIn.play();
            });
            ftOut.play();
        } else {
            label.setText(newText);
        }
    }

    @Override
    public void showWaitingTimer(int seconds) {
        countdownLabel.setText("Match starts in " + seconds + "s");
        Timeline t = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            String currentText = countdownLabel.getText();
            if (currentText == null || currentText.isEmpty()) return;
            int s = 0;
            try {
                s = Integer.parseInt(currentText.replaceAll("\\D", ""));
            } catch (NumberFormatException ex) {
                return;
            }
            if (s > 1) {
                countdownLabel.setText("Match starts in " + (s - 1) + "s");
            } else if (s == 1) {
                countdownLabel.setText("Match starts in 0s");
            }
        }));
        t.setCycleCount(seconds);
        t.play();
    }

    @Override
    public void showRoundDuration(int seconds) {
        animateLabelUpdate(roundDurationLabel, "Round time: " + seconds + "s");
    }

    public void prepareNewRound(int wordLength) {
        String repeatedMask = "";
        for (int i = 0; i < wordLength; i++) {
            repeatedMask += "_ ";
        }
        animateLabelUpdate(maskedWordLabel, repeatedMask.trim());
        animateLabelUpdate(attemptsLeftLabel, "Attempts: 5");
        guessedLettersList.getItems().clear();
        guessInput.setDisable(false);
        guessInput.requestFocus();
        startBtn.setDisable(true);
        playAgainBtn.setVisible(false);
        backToMenuBtn.setVisible(false);
        countdownLabel.setText("");
    }

    @Override
    public void updateMaskedWord(String masked) {
        animateLabelUpdate(maskedWordLabel, masked);
    }

    @Override
    public void updateAttemptsLeft(long n) {
        animateLabelUpdate(attemptsLeftLabel, "Attempts left: " + n);
    }

    @Override
    public void showAttemptedLetters(List<AttemptedLetter> letters) {
        List<String> items = letters.stream().map(a -> a.letter + (a.isLetterCorrect ? " ✓" : " ✗")).collect(Collectors.toList());
        guessedLettersList.getItems().setAll(items);
    }

    @Override
    public void showRoundResult(RoundResult r) {
        guessInput.setDisable(true);
        Alert a = new Alert(Alert.AlertType.INFORMATION, r.statusMessage, ButtonType.OK);
        a.setHeaderText("Round Result");
        a.showAndWait();
    }

    @Override
    public void showFinalResult(GameResult g) {
        guessInput.setDisable(true);
        animateLabelUpdate(maskedWordLabel, "");
        animateLabelUpdate(attemptsLeftLabel, "");
        animateLabelUpdate(countdownLabel, "");
        animateLabelUpdate(roundDurationLabel, "");

        StringBuilder sb = new StringBuilder("Winner: ").append(g.gameWinner).append("\n\nLeaderboard:\n");
        for (GamePlayer p : g.leaderboard) {
            sb.append(p.username).append(" – ").append(p.wins).append("\n");
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION, sb.toString(), ButtonType.OK);
        a.setHeaderText("Game Over");
        a.showAndWait();

        playAgainBtn.setVisible(true);
        backToMenuBtn.setVisible(true);
        startBtn.setDisable(true);
        leaderboardBtn.setDisable(false);
    }

    @Override
    public void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    @Override
    public void showLeaderboard(List<GamePlayer> leaderboard) {
        StringBuilder sb = new StringBuilder("Top Players:\n");
        for (GamePlayer p : leaderboard) {
            sb.append(p.username).append(" – ").append(p.wins).append("\n");
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION, sb.toString(), ButtonType.OK);
        a.setHeaderText("Leaderboard");
        a.showAndWait();
    }

    @Override
    public void clearAll() {
        animateLabelUpdate(countdownLabel, "");
        animateLabelUpdate(roundDurationLabel, "");
        animateLabelUpdate(maskedWordLabel, "");
        animateLabelUpdate(attemptsLeftLabel, "");

        guessedLettersList.getItems().clear();
        guessInput.clear();
        guessInput.setDisable(true);

        startBtn.setDisable(false);
        startBtn.setVisible(true);
        leaderboardBtn.setDisable(false);
        leaderboardBtn.setVisible(true);

        playAgainBtn.setVisible(false);
        backToMenuBtn.setVisible(false);
    }

    @Override
    public void onReturnToMenu() {
        if (onBackToMenu != null) {
            onBackToMenu.run();
        }
    }

    public void setOnGuess(Consumer<Character> onGuess) {
        this.onGuess = onGuess;
    }

    public void setOnStart(Runnable onStart) {
        this.onStart = onStart;
    }

    public void setOnLeaderboard(Runnable onLeaderboard) {
        this.onLeaderboard = onLeaderboard;
    }

    public void setOnPlayAgain(Runnable onPlayAgain) {
        this.onPlayAgain = onPlayAgain;
    }

    public void setOnBackToMenu(Runnable onBackToMenu) {
        this.onBackToMenu = onBackToMenu;
    }

    public Parent getRoot() {
        return root;
    }
}