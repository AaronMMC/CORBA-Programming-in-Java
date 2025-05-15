package com.cs9322.team05.client.main;

import com.cs9322.team05.client.admin.view.AdminView;
import com.cs9322.team05.client.player.services.HomeView;
import com.cs9322.team05.client.player.view.GameView;
import com.cs9322.team05.client.player.view.LoginView;
import com.cs9322.team05.client.player.view.MatchmakingView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class ViewTestApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane();


        LoginView loginView = new LoginView(null);
        tabs.getTabs().add(new Tab("Login", loginView.createLoginPane()));


        HomeView homeView = new HomeView("dummy-token", null);
        tabs.getTabs().add(new Tab("Home", homeView.getRoot()));


        MatchmakingView mmView = new MatchmakingView();
        tabs.getTabs().add(new Tab("Matchmaking", mmView.getRootPane()));


        GameView gameView = new GameView();
        tabs.getTabs().add(new Tab("Game", gameView.getRoot()));

        AdminView adminView = new AdminView("asd",null);
        tabs.getTabs().add(new Tab("Admin", adminView.getScene().getRoot()));

        stage.setScene(new Scene(tabs, 1000, 700));
        stage.setTitle("UI Preview");
        stage.show();
    }
}
