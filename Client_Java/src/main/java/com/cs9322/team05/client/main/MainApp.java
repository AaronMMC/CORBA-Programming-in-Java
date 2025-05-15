package com.cs9322.team05.client.main;

import ModifiedHangman.*;
import com.cs9322.team05.client.player.callback.ClientCallbackImpl;
import com.cs9322.team05.client.player.controller.GameController;
import com.cs9322.team05.client.player.controller.LoginController;
import com.cs9322.team05.client.player.controller.MatchmakingController;
import com.cs9322.team05.client.player.model.GameModel;
import com.cs9322.team05.client.player.model.LeaderboardModel;
import com.cs9322.team05.client.player.model.LoginModel;
import com.cs9322.team05.client.player.services.HomeController;
import com.cs9322.team05.client.player.services.HomeView;
import com.cs9322.team05.client.player.view.GameView;
import com.cs9322.team05.client.player.view.LoginView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExtHelper;

public class MainApp extends Application {
    private Stage primaryStage;
    private ORB orb;
    private String username, token;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        orb = ORB.init(new String[0], null);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showLogin();
        primaryStage.setTitle("What's The Word");
        primaryStage.show();
    }

    private void showLogin() {
        AuthenticationService authSvc = AuthenticationServiceHelper.narrow(getNamingRef("AuthenticationService"));
        LoginModel authModel = new LoginModel(authSvc);
        LoginController loginCtrl = new LoginController(authModel);

        LoginView loginView = new LoginView(loginCtrl);
        loginCtrl.setOnLoginSuccess((user, tok) -> {
            this.username = user;
            this.token = tok;
            showHome();
        });

        setScene(loginView.createLoginPane());
    }

    private void showHome() {

        GameService gameSvc = GameServiceHelper.narrow(getNamingRef("GameService"));
        GameModel gameModel = new GameModel(gameSvc, username, token);
        LeaderboardModel lbModel = new LeaderboardModel(gameModel);


        HomeController homeCtrl = new HomeController(new LoginModel(AuthenticationServiceHelper.narrow(getNamingRef("AuthenticationService"))), gameModel, lbModel, null);
        HomeView homeView = new HomeView(token, homeCtrl);
        homeCtrl.setHomeView(homeView);


        homeView.setOnStartGame(this::showMatchmaking);
        homeView.setOnViewLeaderboard(() -> {
            try {
                homeView.showLeaderboard(lbModel.fetchTop5());
            } catch (PlayerNotLoggedInException e) {
                homeView.showError("Failed to fetch leaderboard");
            }
        });
        homeView.setOnLogout(() -> {
            homeCtrl.onLogout(token);
            showLogin();
        });

        setScene(homeView.getRoot());
    }

    private void showMatchmaking() {
        GameService gameSvc = GameServiceHelper.narrow(getNamingRef("GameService"));
        GameModel gameModel = new GameModel(gameSvc, username, token);


        GameController gc = new GameController(gameModel, null);
        ClientCallbackImpl callback = new ClientCallbackImpl(orb, gameSvc, token, gc);
        try {
            callback.register();
        } catch (Exception e) {

            e.printStackTrace();
        }
        gc.setGameView(null);

        MatchmakingController mmCtrl = new MatchmakingController(gameModel, callback);
        mmCtrl.setOnMatchFound(() -> showGame(gameModel, gc, callback));
        mmCtrl.setOnCancel(this::showHome);

        setScene(mmCtrl.getView().getRootPane());
        mmCtrl.startMatchmaking();
    }

    private void showGame(GameModel gameModel, GameController gc, ClientCallbackImpl callback) {
        GameView view = new GameView();
        gc.setGameView(view);


        view.setOnStart(gc::startGame);
        view.setOnGuess(gc::submitGuess);
        view.setOnLeaderboard(gc::fetchLeaderboard);
        view.setOnPlayAgain(gc::resetAndStart);
        view.setOnBackToMenu(this::showHome);


        view.clearAll();

        setScene(view.getRoot());
    }

    private org.omg.CORBA.Object getNamingRef(String name) {
        try {
            return NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService")).resolve_str(name);
        } catch (Exception e) {
            throw new RuntimeException("Lookup failed: " + name, e);
        }
    }

    private void setScene(Parent root) {
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            primaryStage.setScene(new Scene(root, 1000, 700));
        } else {
            scene.setRoot(root);
        }
    }
}
