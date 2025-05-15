package com.cs9322.team05.client.main;

import ModifiedHangman.*;
import com.cs9322.team05.client.player.callback.ClientCallbackImpl;
import com.cs9322.team05.client.player.controller.GameController;
import com.cs9322.team05.client.player.controller.AuthenticationController;
import com.cs9322.team05.client.player.controller.MatchmakingController;
import com.cs9322.team05.client.player.model.AuthenticationModel;
import com.cs9322.team05.client.player.model.GameModel;
import com.cs9322.team05.client.player.model.LeaderboardModel;
import com.cs9322.team05.client.player.model.AuthenticationModel;
import com.cs9322.team05.client.player.services.HomeController;
import com.cs9322.team05.client.player.services.HomeView;
import com.cs9322.team05.client.player.view.GameView;
import com.cs9322.team05.client.player.view.AuthenticationView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp extends Application {
    private static final Logger logger = Logger.getLogger(MainApp.class.getName());

    private Stage primaryStage;
    private ORB orb;
    private String username, token;

    public static void main(String[] args) {
        logger.info("Launching JavaFX application");
        launch(args);
    }

    @Override
    public void init() throws Exception {
        // Initialize ORB with hard‑coded defaults
        logger.info("Initializing ORB");
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBInitialHost", "localhost");
        props.put("org.omg.CORBA.ORBInitialPort", "1050");
        orb = ORB.init(new String[0], props);
        logger.info("ORB initialized successfully: " +
                "host=" + props.getProperty("org.omg.CORBA.ORBInitialHost") +
                " port=" + props.getProperty("org.omg.CORBA.ORBInitialPort"));
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        logger.info("Starting primary stage");
        showLogin();
        primaryStage.setTitle("What's The Word");
        primaryStage.show();
        logger.info("Primary stage shown");
    }

    private void showLogin() {
        AuthenticationService authSvc =
                AuthenticationServiceHelper.narrow(getNamingRef("AuthenticationService"));

        AuthenticationModel authModel = new AuthenticationModel(authSvc);
        AuthenticationController loginCtrl = new AuthenticationController(authModel);

        // (a) Normal user login
        loginCtrl.setOnLoginSuccess((user, tok) -> {
            this.username = user;
            this.token = tok;
            showHome();
        });

        AuthenticationView authenticationView = new AuthenticationView(loginCtrl);
        setScene(authenticationView.createLoginPane());
    }


    private void showHome() {
        logger.info("Transitioning to Home view for user=" + username);
        GameService gameSvc = GameServiceHelper.narrow(getNamingRef("GameService"));
        GameModel gameModel = new GameModel(gameSvc, username, token);
        LeaderboardModel lbModel = new LeaderboardModel(gameModel);

        HomeController homeCtrl = new HomeController(
                new AuthenticationModel(AuthenticationServiceHelper.narrow(getNamingRef("AuthenticationService"))),
                gameModel, lbModel, null
        );
        HomeView homeView = new HomeView(token, homeCtrl);
        homeCtrl.setHomeView(homeView);

        homeView.setOnStartGame(this::showMatchmaking);
        homeView.setOnViewLeaderboard(() -> {
            try {
                logger.info("Fetching top 5 leaderboard");
                homeView.showLeaderboard(lbModel.fetchTop5());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to fetch leaderboard", e);
                homeView.showError("Failed to fetch leaderboard");
            }
        });
        homeView.setOnLogout(() -> {
            logger.info("Logging out user=" + username);
            homeCtrl.onLogout(token);
            showLogin();
        });

        setScene(homeView.getRoot());
    }

    private void showMatchmaking() {
        logger.info("Transitioning to Matchmaking view");
        GameService gameSvc = GameServiceHelper.narrow(getNamingRef("GameService"));
        GameModel gameModel = new GameModel(gameSvc, username, token);

        GameController gc = new GameController(gameModel, null);
        ClientCallbackImpl callback = new ClientCallbackImpl(orb, gameSvc, token, gc);
        try {
            logger.info("Registering callback with server");
            callback.register();
            logger.info("Callback registered successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Callback registration failed", e);
        }
        gc.setGameView(null);

        MatchmakingController mmCtrl = new MatchmakingController(gameModel, callback);
        mmCtrl.setOnMatchFound(() -> {
            logger.info("Match found, launching game view");
            showGame(gameModel, gc, callback);
        });
        mmCtrl.setOnCancel(() -> {
            logger.info("Matchmaking cancelled by user");
            showHome();
        });

        setScene(mmCtrl.getView().getRootPane());
        logger.info("Starting matchmaking process");
        mmCtrl.startMatchmaking();
    }

    private void showGame(GameModel gameModel, GameController gc, ClientCallbackImpl callback) {
        logger.info("Transitioning to Game view");
        GameView view = new GameView();
        gc.setGameView(view);

        view.setOnStart(() -> {
            logger.info("Game start requested");
            gc.startGame();
        });
        view.setOnGuess(guess -> {
            logger.info("Submitting guess: " + guess);
            gc.submitGuess(guess);
        });
        view.setOnLeaderboard(() -> {
            logger.info("Fetching in‑game leaderboard");
            gc.fetchLeaderboard();
        });
        view.setOnPlayAgain(() -> {
            logger.info("User requested Play Again");
            gc.resetAndStart();
        });
        view.setOnBackToMenu(() -> {
            logger.info("User returning to Home menu from game");
            showHome();
        });

        view.clearAll();
        setScene(view.getRoot());
    }

    private Object getNamingRef(String name) {
        logger.info("Looking up service in NameService: " + name);
        try {
            Object obj = NamingContextExtHelper.narrow(
                    orb.resolve_initial_references("NameService")
            ).resolve_str(name);
            logger.info("Service '" + name + "' resolved: " + obj);
            return obj;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lookup failed for: " + name, e);
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
