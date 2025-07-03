package com.client.player.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.util.logging.Logger;

public class MatchmakingView {
    private static final Logger logger = Logger.getLogger(MatchmakingView.class.getName());

    private final VBox root = new VBox(20);
    private final Label titleLabel = new Label("Finding Your Perfect Match...");
    private final Label statusLabel = new Label("Hang tight! We’re pairing you up...");
    private final Label timerLabel = new Label("");
    private final ProgressIndicator progressIndicator = new ProgressIndicator(-1.0);

    private Timeline countdownTimeline;
    private static final double BASE_WIDTH = 400.0;
    private static final double MIN_SCALE = 0.8;
    private static final double MAX_SCALE = 1.5;

    public MatchmakingView() {
        setupUI();
    }

    private void setupUI() {
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        VBox.setVgrow(root, Priority.ALWAYS);

        root.setStyle("-fx-background-color: #121212;");
        titleLabel.setWrapText(true);
        statusLabel.setWrapText(true);
        timerLabel.setWrapText(true);

        // Set text colors for dark theme
        String textStyle = "-fx-text-fill: #EEEEEE;";
        titleLabel.setStyle(textStyle);
        statusLabel.setStyle(textStyle);
        timerLabel.setStyle(textStyle);

        progressIndicator.setMaxSize(150, 150);
        progressIndicator.setPrefSize(100, 100);
        progressIndicator.styleProperty().bind(
                Bindings.format("-fx-progress-color: #00BCD4; -fx-stroke-width: %f;",
                        root.widthProperty().divide(BASE_WIDTH).multiply(2))
        );

        root.getChildren().addAll(titleLabel, progressIndicator, statusLabel, timerLabel);
        timerLabel.setVisible(false);
        timerLabel.setManaged(false);

        root.widthProperty().addListener((obs, oldVal, newVal) -> {
            applyScaling(calculateScaleFactor(newVal.doubleValue()));
        });

        applyScaling(1.0);
    }

    private double calculateScaleFactor(double currentWidth) {
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, currentWidth / BASE_WIDTH));
    }

    private void applyScaling(double scaleFactor) {
        titleLabel.setFont(new Font("Calibri", 28 * scaleFactor));
        statusLabel.setFont(new Font("Calibri", 18 * scaleFactor));
        timerLabel.setFont(new Font("Calibri", 16 * scaleFactor));

        progressIndicator.setPrefSize(100 * scaleFactor, 100 * scaleFactor);
        root.setPadding(new Insets(20 * scaleFactor));
    }

    public Parent getRootPane() {
        return root;
    }

    public void showSearching() {
        Platform.runLater(() -> {
            statusLabel.setText("Searching for opponents...");
            progressIndicator.setVisible(true);
            timerLabel.setVisible(false);
            timerLabel.setManaged(false);
        });
    }

    public void startCountdown(int seconds, String message) {
        Platform.runLater(() -> {
            progressIndicator.setVisible(false);
            statusLabel.setText(message);
            timerLabel.setText("Game starts in " + seconds + "s");
            timerLabel.setVisible(true);
            timerLabel.setManaged(true);

            if (countdownTimeline != null) {
                countdownTimeline.stop();
            }

            countdownTimeline = new Timeline();
            countdownTimeline.setCycleCount(seconds + 1);
            final int[] remainingSeconds = {seconds};

            countdownTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), event -> {
                remainingSeconds[0]--;
                if (remainingSeconds[0] >= 0) {
                    timerLabel.setText("Game starts in " + remainingSeconds[0] + "s");
                }
                if (remainingSeconds[0] <= 0) {
                    timerLabel.setText("Let’s go!");
                    countdownTimeline.stop();
                }
            }));

            countdownTimeline.playFromStart();
        });
    }

    public void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        Platform.runLater(() -> {
            timerLabel.setVisible(false);
            timerLabel.setManaged(false);
        });
    }

    public void showMatchmakingFailed(String reason) {
        Platform.runLater(() -> {
            statusLabel.setText("Oops! We couldn't find a match: " + reason);
            progressIndicator.setVisible(false);
            stopCountdown();
            logger.info("MatchmakingView displaying failure: " + reason + ". Awaiting navigation by controller.");
        });
    }
}