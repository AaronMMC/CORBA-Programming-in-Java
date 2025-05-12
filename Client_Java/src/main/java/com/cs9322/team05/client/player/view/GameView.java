package com.cs9322.team05.client.player.view;

import ModifiedHangman.*;
import com.cs9322.team05.client.player.interfaces.GameViewInterface;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
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
    private final TextField guessInputField = new TextField();
    private final Button startGameButton = new Button("Start Game");
    private final Button leaderboardButton = new Button("Show Leaderboard");
    private final Button backToMenuButton = new Button("Back to Menu");
    private final Button playAgainButton = new Button("Play Again");
    private final VBox root;
    private Consumer<Character> onGuessSubmitted;
    private Runnable onStartGame;
    private Runnable onShowLeaderboard;
    private Runnable onBackToMenu;
    private Runnable onPlayAgain;

    public GameView() {
        guessInputField.setPromptText("Enter a letter");
        guessInputField.setMaxWidth(100);
        guessInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && onGuessSubmitted != null) {
                String text = guessInputField.getText();
                if (!text.isEmpty()) {
                    onGuessSubmitted.accept(text.charAt(0));
                    guessInputField.clear();
                }
            }
        });

        startGameButton.setOnAction(e -> {
            if (onStartGame != null) onStartGame.run();
        });

        leaderboardButton.setOnAction(e -> {
            if (onShowLeaderboard != null) onShowLeaderboard.run();
        });

        backToMenuButton.setOnAction(e -> {
            if (onBackToMenu != null) onBackToMenu.run();
        });

        playAgainButton.setOnAction(e -> {
            if (onPlayAgain != null) onPlayAgain.run();
        });

        HBox timers = new HBox(20, countdownLabel, roundDurationLabel);
        HBox controls = new HBox(10, startGameButton, leaderboardButton, playAgainButton, backToMenuButton);
        VBox layout = new VBox(10, timers, maskedWordLabel, guessInputField, attemptsLeftLabel, guessedLettersList, controls);
        layout.setAlignment(Pos.CENTER);
        root = layout;
    }

    public Parent getRoot() {
        return root;
    }

    public void setOnGuessSubmitted(Consumer<Character> onGuessSubmitted) {
        this.onGuessSubmitted = onGuessSubmitted;
    }

    public void setOnStartGame(Runnable onStartGame) {
        this.onStartGame = onStartGame;
    }

    public void setOnShowLeaderboard(Runnable onShowLeaderboard) {
        this.onShowLeaderboard = onShowLeaderboard;
    }

    public void setOnBackToMenu(Runnable onBackToMenu) {
        this.onBackToMenu = onBackToMenu;
    }

    public void setOnPlayAgain(Runnable onPlayAgain) {
        this.onPlayAgain = onPlayAgain;
    }

    @Override
    public void showWaitingTimer(int seconds) {
        countdownLabel.setText("Starting in " + seconds + "s");
        Timeline t = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int s = Integer.parseInt(countdownLabel.getText().replaceAll("\\D", ""));
            if (s > 1) countdownLabel.setText("Starting in " + (s - 1) + "s");
        }));
        t.setCycleCount(seconds);
        t.play();
    }

    @Override
    public void showRoundDuration(int seconds) {
        roundDurationLabel.setText("Round time: " + seconds + "s");
    }

    @Override
    public void prepareNewRound(int wordLength) {
        maskedWordLabel.setText("_ ".repeat(wordLength).trim());
        attemptsLeftLabel.setText("Attempts: …");
        guessedLettersList.getItems().clear();
        guessInputField.setDisable(false);
    }

    @Override
    public void updateMaskedWord(String masked) {
        maskedWordLabel.setText(masked);
    }

    @Override
    public void updateAttemptsLeft(long n) {
        attemptsLeftLabel.setText("Attempts left: " + n);
    }

    @Override
    public void showAttemptedLetters(List<AttemptedLetter> letters) {
        List<String> items = letters.stream().map(a -> a.letter + (a.isLetterCorrect ? "✓" : "✗")).collect(Collectors.toList());
        guessedLettersList.getItems().setAll(items);
    }

    @Override
    public void showRoundResult(RoundResult r) {
        guessInputField.setDisable(true);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Round Result");
        alert.setContentText(r.statusMessage);
        alert.showAndWait();
    }

    @Override
    public void showFinalResult(GameResult g) {
        guessInputField.setDisable(true);
        StringBuilder sb = new StringBuilder("Winner: " + g.gameWinner + "\\n\\nLeaderboard:\\n");
        for (GamePlayer p : g.leaderboard) {
            sb.append(p.username).append(" – wins: ").append(p.wins).append("\\n");
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Game Over");
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    @Override
    public void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    @Override
    public void showLeaderboard(List<GamePlayer> leaderboard) {
        StringBuilder sb = new StringBuilder("Current Leaderboard:\\n");
        for (GamePlayer p : leaderboard) {
            sb.append(p.username).append(" – wins: ").append(p.wins).append("\\n");
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Leaderboard");
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }
}