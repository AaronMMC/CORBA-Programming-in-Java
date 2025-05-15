package com.cs9322.team05.client.main;

import ModifiedHangman.*;
import com.cs9322.team05.client.player.callback.ClientCallbackImpl;
import com.cs9322.team05.client.player.controller.GameController;
import com.cs9322.team05.client.player.controller.AuthenticationController;
import com.cs9322.team05.client.player.controller.MatchmakingController;
import com.cs9322.team05.client.player.model.AuthenticationModel;
import com.cs9322.team05.client.player.model.GameModel;
import com.cs9322.team05.client.player.model.LeaderboardModel;
import com.cs9322.team05.client.player.services.HomeController;
import com.cs9322.team05.client.player.services.HomeView;
import com.cs9322.team05.client.player.view.GameView;
import com.cs9322.team05.client.player.view.AuthenticationView;
import com.cs9322.team05.client.admin.controller.AdminController;
import com.cs9322.team05.client.admin.model.AdminModel;
import com.cs9322.team05.client.admin.view.AdminView;
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
        logger.info("Initializing ORB");
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBInitialHost", "localhost");
        props.put("org.omg.CORBA.ORBInitialPort", "2634"); // changed from 1050 to 2634
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

        loginCtrl.setOnLoginSuccess((user, tok) -> {
            this.username = user;
            this.token = tok;
            if ("admin".equalsIgnoreCase(this.username)) {
                showAdminView();
            } else {
                showHome();
            }
        });

        AuthenticationView authenticationView = new AuthenticationView(loginCtrl);
        setScene(authenticationView.createLoginPane());
    }

    private void showAdminView() {
        logger.info("Transitioning to Admin view for user=" + this.username);
        try {
            AdminService adminSvc = AdminServiceHelper.narrow(getNamingRef("AdminService"));
            AdminModel adminModel = new AdminModel(adminSvc);
            AdminController adminCtrl = new AdminController(adminModel);
            AdminView adminView = new AdminView(this.token, adminCtrl);

            if (adminView.getScene() != null && adminView.getScene().getRoot() != null) {
                setScene(adminView.getScene().getRoot());
                primaryStage.setTitle("Admin Panel - What's The Word");
                logger.info("Admin view shown");
            } else {
                logger.log(Level.SEVERE, "AdminView scene or root is null. Cannot display Admin view.");
                showLogin();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to transition to Admin view", e);
            showLogin();
        }
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
        logger.info("Transitioning to Matchmaking view for user=" + username);
        GameService gameSvc = GameServiceHelper.narrow(getNamingRef("GameService"));
        GameModel gameModel = new GameModel(gameSvc, username, token);
        GameController gc = new GameController(gameModel, null);
        ClientCallbackImpl callback = new ClientCallbackImpl(orb, gameSvc, token, gc);

        MatchmakingController mmCtrl = new MatchmakingController(gameModel, callback);

        mmCtrl.setOnMatchReadyToProceed(() -> {
            logger.info("Matchmaking successful and initial wait period over. Proceeding to game view setup.");
            showGame(gameModel, gc, callback);
        });
        mmCtrl.setOnMatchmakingCancelledOrFailed(() -> {
            logger.info("Matchmaking cancelled or failed. Returning to home view.");
            showHome();
        });

        setScene(mmCtrl.getView().getRootPane());
        logger.info("Displaying matchmaking view and starting matchmaking process...");
        mmCtrl.startMatchmaking();
    }

    private void showGame(GameModel gameModel, GameController gc, ClientCallbackImpl callback) {
        logger.info("Transitioning to Game view for user=" + username);
        GameView view = new GameView();
        gc.setGameView(view);

        view.setOnStart(() -> {
            logger.info("Game start requested by user via UI (should ideally be server-driven after matchmaking).");
            gc.startGame();
        });
        view.setOnGuess(guess -> {
            logger.info("Submitting guess: " + guess);
            gc.submitGuess(guess);
        });
        view.setOnLeaderboard(() -> {
            logger.info("Fetching in-game leaderboard");
            gc.fetchLeaderboard();
        });
        view.setOnPlayAgain(() -> {
            logger.info("User requested Play Again");
            showMatchmaking();
        });
        view.setOnBackToMenu(() -> {
            logger.info("User returning to Home menu from game");
            showHome();
        });

        view.clearAll();
        setScene(view.getRoot());
        logger.info("Game view shown. Waiting for server to initiate round via ClientCallback.startGame(wordLength).");
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