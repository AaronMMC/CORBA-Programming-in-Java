package com.cs9322.team05.client.player.view;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class MatchmakingView {

    private final StackPane rootPane = new StackPane();
    private final Label statusLabel = new Label("Looking for an opponent...");

    public MatchmakingView() {
        setupUI();
    }

    private void setupUI() {
        statusLabel.setFont(new Font("Arial", 24));
        rootPane.setAlignment(Pos.CENTER);
        rootPane.getChildren().add(statusLabel);
    }

    public Parent getRootPane() {
        return rootPane;
    }

    public void enqueueStatusUpdate(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }
}
