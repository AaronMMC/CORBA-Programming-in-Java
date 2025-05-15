package com.cs9322.team05.client.player.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MatchmakingView {

    private final VBox root = new VBox(20);
    private final Label statusLabel = new Label("Looking for an opponent...");
    private final Button cancelBtn = new Button("Cancel");
    private Runnable onCancel;

    public MatchmakingView() {
        setupUI();
        wireActions();
    }

    private void setupUI() {
        statusLabel.setFont(new Font("Arial", 24));
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.getChildren().addAll(statusLabel, cancelBtn);
    }

    private void wireActions() {
        cancelBtn.setOnAction(e -> {
            if (onCancel != null) onCancel.run();
        });
    }

    public Parent getRootPane() {
        return root;
    }

    public void enqueueStatusUpdate(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }
}
