
package com.cs9322.team05.client.player.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.InputMethodTextRun;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.util.logging.Logger; 


public class MatchmakingView {
    private static final Logger logger = Logger.getLogger(MatchmakingView.class.getName()); 

    private final VBox root = new VBox(20);
    private final Label titleLabel = new Label("Matchmaking");
    private final Label statusLabel = new Label("Looking for an opponent...");
    private final Label timerLabel = new Label("");
    private final ProgressIndicator progressIndicator = new ProgressIndicator(-1.0);

    
    private Timeline countdownTimeline;

    public MatchmakingView() {
        setupUI();
        
    }

    private void setupUI() {
        titleLabel.setFont(new Font("Arial", 28));
        statusLabel.setFont(new Font("Arial", 18));
        timerLabel.setFont(new Font("Arial", 16));

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        
        root.getChildren().addAll(titleLabel, progressIndicator, statusLabel, timerLabel);
        timerLabel.setVisible(false);
        timerLabel.setManaged(false);
    }

    public Parent getRootPane() {
        return root;
    }

    public void showSearching() {
        Platform.runLater(() -> {
            statusLabel.setText("Searching for a game...");
            progressIndicator.setVisible(true);
            timerLabel.setVisible(false);
            timerLabel.setManaged(false);
            
            
        });
    }

    public void startCountdown(int seconds, String message) {
        Platform.runLater(() -> {
            progressIndicator.setVisible(false);
            statusLabel.setText(message);
            timerLabel.setText(String.valueOf(seconds) + "s");
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
                    timerLabel.setText(String.valueOf(remainingSeconds[0]) + "s");
                }
                if (remainingSeconds[0] <= 0) {
                    timerLabel.setText("Starting...");
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
            statusLabel.setText("Matchmaking Failed: " + reason);
            progressIndicator.setVisible(false);
            stopCountdown();
            logger.info("MatchmakingView displaying failure: " + reason + ". Awaiting navigation by controller.");
        });
    }
}