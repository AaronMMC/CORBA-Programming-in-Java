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
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
    private GameController gameControllerInstance;
    private ClientCallbackImpl clientCallbackInstance;


    public static void main(String[] args) {
        logger.info("Launching JavaFX application");
        launch(args);
    }

    @Override
    public void init() throws Exception {
        logger.info("Initializing ORB");
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBInitialHost", "localhost");
        props.put("org.omg.CORBA.ORBInitialPort", "2634");
        orb = ORB.init(new String[0], props);
        logger.info("ORB initialized successfully: host=" +
                props.getProperty("org.omg.CORBA.ORBInitialHost") + ", port=" +
                props.getProperty("org.omg.CORBA.ORBInitialPort"));
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        logger.info("Starting primary stage");
        showLogin();
        primaryStage.setTitle("What's The Word - Hangman");
        primaryStage.setOnCloseRequest(event -> {
            logger.info("Application closing, performing cleanup...");


            if (orb != null) {
                logger.info("Shutting down ORB.");
                orb.shutdown(true);
                orb.destroy();
            }
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
        logger.info("Primary stage shown");
    }

    private void showLogin() {
        logger.info("Displaying Login View.");
        this.gameControllerInstance = null;
        this.clientCallbackInstance = null;

        AuthenticationService authSvc =
                AuthenticationServiceHelper.narrow(getNamingRef("AuthenticationService"));
        if (authSvc == null) return;

        AuthenticationModel authModel = new AuthenticationModel(authSvc);
        AuthenticationController loginCtrl = new AuthenticationController(authModel);
        AuthenticationView authenticationView = new AuthenticationView(loginCtrl);


        loginCtrl.setOnLoginSuccess((user, tok) -> {
            this.username = user;
            this.token = tok;
            logger.info("Login successful for user: " + this.username);
            if ("admin".equalsIgnoreCase(this.username)) {
                showAdminView();
            } else {
                showHome();
            }
        });

        setScene(authenticationView.createLoginPane());
    }


    private void showAdminView() {
        logger.info("Displaying Admin View.");

        
        AdminService adminSvc = AdminServiceHelper.narrow(getNamingRef("AdminService"));
        if (adminSvc == null) return;

        
        AuthenticationService authSvc = AuthenticationServiceHelper.narrow(getNamingRef("AuthenticationService"));
        if (authSvc == null) return;

        
        AdminModel adminModel = new AdminModel(adminSvc);
        AuthenticationModel authModel = new AuthenticationModel(authSvc);

        
        AdminController adminController = new AdminController(adminModel);
        AuthenticationController authController = new AuthenticationController(authModel);

        
        AdminView adminView = new AdminView(token, adminController, authController);

        
        setScene(adminView.getRootPane());
    }

    private void showHome() {
        logger.info("Displaying Home View for user=" + username);
        this.gameControllerInstance = null;
        this.clientCallbackInstance = null;

        GameService gameSvc = GameServiceHelper.narrow(getNamingRef("GameService"));
        if (gameSvc == null) return;

        AuthenticationService authSvc = AuthenticationServiceHelper.narrow(getNamingRef("AuthenticationService"));
        if (authSvc == null) return;


        GameModel tempGameModelForLeaderboard = new GameModel(gameSvc, username, token);
        LeaderboardModel lbModel = new LeaderboardModel(tempGameModelForLeaderboard);
        AuthenticationModel authModelForHome = new AuthenticationModel(authSvc);


        HomeController homeCtrl = new HomeController(authModelForHome, tempGameModelForLeaderboard, lbModel, null);
        HomeView homeView = new HomeView(token, homeCtrl);
        homeCtrl.setHomeView(homeView);

        homeView.setOnStartGame(this::showMatchmaking);
        homeView.setOnViewLeaderboard(() -> {
            try {
                logger.info("Fetching overall leaderboard for Home View.");
                homeView.showLeaderboard(lbModel.fetchTop5());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to fetch leaderboard for Home View.", e);
                Platform.runLater(() -> homeView.showError("Failed to fetch leaderboard: " + e.getMessage()));
            }
        });
        homeView.setOnLogout(() -> {
            logger.info("Logout requested by user: " + username);
            try {
                if (this.token != null) {
                    authModelForHome.logout(this.token);
                    logger.info("Server logout successful for token: " + this.token);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error during server logout attempt for user " + username, e);
            }
            this.username = null;
            this.token = null;
            this.gameControllerInstance = null;
            this.clientCallbackInstance = null;
            showLogin();
        });

        setScene(homeView.getRoot());
    }

    private void showMatchmaking() {
        logger.info("Displaying Matchmaking View for user=" + username);
        GameService gameSvc = GameServiceHelper.narrow(getNamingRef("GameService"));
        if (gameSvc == null) {
            logger.severe("GameService is null, cannot proceed to matchmaking.");
            showHome();
            return;
        }

        GameModel gameModel = new GameModel(gameSvc, username, token);
        this.gameControllerInstance = new GameController(gameModel, null);
        this.clientCallbackInstance = new ClientCallbackImpl(orb, gameSvc, token, this.gameControllerInstance);

        MatchmakingController mmCtrl = new MatchmakingController(gameModel, this.clientCallbackInstance, this.gameControllerInstance);

        mmCtrl.setOnMatchReadyToProceed(() -> {
            logger.info("Matchmaking successful, initial wait period over. Proceeding to Game View setup.");
            showGame();
        });
        mmCtrl.setOnMatchmakingCancelledOrFailed(() -> {
            logger.info("Matchmaking cancelled or failed by user/timeout/error. Returning to Home View.");
            showHome();
        });

        setScene(mmCtrl.getView().getRootPane());
        logger.info("Starting matchmaking process via MatchmakingController...");
        mmCtrl.startMatchmaking();
    }

    private void showGame() {
        if (this.gameControllerInstance == null) {
            logger.severe("Attempted to show Game View, but GameController instance is null! Returning to Home.");
            showHome();
            return;
        }
        if (this.gameControllerInstance.getGameId() == null || this.gameControllerInstance.getGameId().isEmpty()){
            logger.severe("Attempted to show Game View, but GameId is not set in GameController! Returning to Home.");
            Platform.runLater(()-> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Game Error");
                alert.setHeaderText("Failed to initialize game session.");
                alert.setContentText("Game ID was not properly set after matchmaking.");
                alert.showAndWait();
            });
            showHome();
            return;
        }

        logger.info("Displaying Game View for user=" + username + ", GameID: " + this.gameControllerInstance.getGameId());
        GameView view = new GameView();
        this.gameControllerInstance.setGameView(view);

        view.setOnGuess(guess -> {
            logger.fine("UI Event: Guess submitted: " + guess);
            this.gameControllerInstance.submitGuess(guess);
        });
        view.setOnLeaderboard(() -> {
            logger.info("UI Event: Requesting overall leaderboard from GameView.");
            this.gameControllerInstance.fetchLeaderboard();
        });
        view.setOnPlayAgain(() -> {
            logger.info("UI Event: Play Again requested from GameView. Transitioning to matchmaking.");
            this.gameControllerInstance = null;
            this.clientCallbackInstance = null;
            showMatchmaking();
        });
        view.setOnBackToMenu(() -> {
            logger.info("UI Event: Back to Menu requested from GameView.");

            this.gameControllerInstance = null;
            this.clientCallbackInstance = null;
            showHome();
        });

        Platform.runLater(() -> {
            view.clearAll();


            view.showStatusMessage("Waiting for server to start the round...");
            setScene(view.getRoot());
            logger.info("Game View shown. Client is ready and waiting for server's ClientCallback.startRound().");
        });
    }

    private Object getNamingRef(String name) {
        logger.info("Looking up service in NameService: " + name);
        try {
            Object obj = NamingContextExtHelper.narrow(
                    orb.resolve_initial_references("NameService")
            ).resolve_str(name);
            if (obj == null) {
                logger.severe("Naming Service resolved a null object for: " + name);
                Platform.runLater(() -> showErrorAlertAndExit("Critical Service Error", "Failed to resolve critical service: " + name + ". The service might be down or misconfigured."));
                throw new RuntimeException("Critical service lookup failed (null object): " + name);
            }
            logger.info("Service '" + name + "' resolved successfully.");
            return obj;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lookup failed for Naming Service or service: " + name, e);
            Platform.runLater(() -> showErrorAlertAndExit("Connection Error", "Failed to connect to server services. Could not resolve '" + name + "'. Ensure the server and Naming Service are running."));
            throw new RuntimeException("Critical service lookup failed: " + name, e);
        }
    }

    private void showErrorAlertAndExit(String title, String header) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText("The application will now exit.");
        alert.setOnHidden(evt -> {
            if (orb != null) {
                orb.shutdown(true);
                orb.destroy();
            }
            Platform.exit();
            System.exit(1);
        });
        alert.show();
    }


    private void setScene(Parent root) {
        Platform.runLater(() -> {
            if (primaryStage == null) {
                logger.severe("PrimaryStage is null in setScene. Cannot update UI.");
                return;
            }
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                primaryStage.setScene(new Scene(root, 1000, 900));
            } else {
                scene.setRoot(root);
            }
        });
    }
}