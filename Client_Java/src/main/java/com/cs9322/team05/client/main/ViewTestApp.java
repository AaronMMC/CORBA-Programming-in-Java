package com.cs9322.team05.client.main;

import com.cs9322.team05.client.player.view.GameView;
import com.cs9322.team05.client.player.view.LoginView;
import com.cs9322.team05.client.player.view.MatchmakingView;
import com.cs9322.team05.client.player.services.HomeView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class ViewTestApp extends Application {
    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane();

        // 1) Login screen (buttons won't do anything here)
        LoginView loginView = new LoginView(null);
        tabs.getTabs().add(new Tab("Login", loginView.createLoginPane()));

        // 2) Home screen (buttons won't do anything here)
        HomeView homeView = new HomeView("dummy-token", null);
        tabs.getTabs().add(new Tab("Home", homeView.getRoot()));

        // 3) Matchmaking screen
        MatchmakingView mmView = new MatchmakingView();
        tabs.getTabs().add(new Tab("Matchmaking", mmView.getRootPane()));

        // 4) Game screen
        GameView gameView = new GameView();
        tabs.getTabs().add(new Tab("Game", gameView.getRoot()));

        stage.setScene(new Scene(tabs, 1000, 700));
        stage.setTitle("UI Preview");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
