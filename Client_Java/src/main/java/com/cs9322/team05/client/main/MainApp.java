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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.omg.CORBA.ORB;


import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp extends Application {
    private static final Logger logger = Logger.getLogger(MainApp.class.getName());

    private Stage primaryStage;
    private ORB orb;
    private String username, token;

    private AuthenticationController authControllerInstance;
    private HomeController homeControllerInstance;
    private GameController gameControllerInstance;
    private ClientCallbackImpl clientCallbackInstance;


    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        logger.info("Launching Hangman Client Application");
        launch(args);
    }

    @Override
    public void init() throws Exception {
        logger.info("Initializing ORB connection properties.");
        Properties props = new Properties();

        props.put("org.omg.CORBA.ORBInitialHost", "localhost");
        props.put("org.omg.CORBA.ORBInitialPort", "1050");
        orb = ORB.init(new String[0], props);
        logger.info("ORB initialized successfully. Host: " +
                props.getProperty("org.omg.CORBA.ORBInitialHost") + ", Port: " +
                props.getProperty("org.omg.CORBA.ORBInitialPort"));
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        logger.info("Application start: Setting up primary stage.");
        showLogin();
        primaryStage.setTitle("What's The Word - Hangman Challenge");
        primaryStage.setOnCloseRequest(event -> {
            logger.info("Application window close requested. Performing shutdown sequence.");
            performUserLogout(true);
            if (orb != null) {
                logger.info("Shutting down ORB connection.");
                orb.shutdown(true);
                orb.destroy();
                logger.info("ORB shutdown complete.");
            }
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
        logger.info("Primary stage is now visible.");
    }

    private void cleanupGameResources() {
        if (this.gameControllerInstance != null) {
            logger.info("Cleaning up active game controller state.");
            this.gameControllerInstance.cleanupControllerState();
        }
        
        
        
        
        logger.fine("Game resources cleaned up (controller state reset, not necessarily nulled).");
    }

    private void processLogoutNavigation() {
        logger.info("Processing logout: Clearing local session and navigating to login screen.");
        this.username = null;
        this.token = null;
        cleanupGameResources();
        this.gameControllerInstance = null; 
        this.clientCallbackInstance = null;
        this.homeControllerInstance = null;

        showLogin();
    }

    private void performUserLogout(boolean appExiting) {
        if (this.token != null && this.authControllerInstance != null) {
            logger.info("Attempting server-side logout. App exiting: " + appExiting);
            try {


                this.authControllerInstance.handleLogout(this.token);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception during server-side logout attempt.", e);
            }
        }


        if (appExiting) {
            this.username = null;
            this.token = null;
            cleanupGameResources(); 
            this.gameControllerInstance = null; 
            this.clientCallbackInstance = null;
            this.homeControllerInstance = null;
        }
    }

    private void showLogin() {
        logger.info("Navigating to Login View.");
        this.username = null;
        this.token = null;
        cleanupGameResources(); 
        this.gameControllerInstance = null; 
        this.clientCallbackInstance = null;
        this.homeControllerInstance = null;


        AuthenticationService authSvc = AuthenticationServiceHelper.narrow(getNamingRef("AuthenticationService"));
        if (authSvc == null) {
            showErrorAlertAndExit("Critical Service Error", "Authentication Service is unavailable. Application cannot continue.");
            return;
        }

        AuthenticationModel authModel = new AuthenticationModel(authSvc);
        this.authControllerInstance = new AuthenticationController(authModel);
        AuthenticationView authenticationView = new AuthenticationView(this.authControllerInstance);

        this.authControllerInstance.setOnLoginSuccess((user, receivedToken) -> {
            this.username = user;
            this.token = receivedToken;
            logger.info("Login successful for user: " + this.username + ". Token acquired.");

            this.authControllerInstance.setOnLogoutSuccess(this::processLogoutNavigation);
            if ("admin".equalsIgnoreCase(this.username)) {
                showAdminView();
            } else {
                showHome(null, null);
            }
        });


        setScene(authenticationView.createLoginPane());
    }

    private void showAdminView() {
        logger.info("Navigating to Admin Dashboard for user: " + username);
        cleanupGameResources();
        this.gameControllerInstance = null;
        this.clientCallbackInstance = null;
        this.homeControllerInstance = null;


        if (this.authControllerInstance == null || this.token == null) {
            logger.severe("AdminView: Authentication context is missing. Redirecting to login.");
            showLogin();
            return;
        }

        AdminService adminSvc = AdminServiceHelper.narrow(getNamingRef("AdminService"));
        if (adminSvc == null) {
            showErrorAlert("Service Unavailable", "Admin service is not reachable. Please try again later.", this::showLogin);
            return;
        }
        AdminModel adminModel = new AdminModel(adminSvc);
        AdminController adminCtrl = new AdminController(adminModel);

        AdminView adminView = new AdminView(token, adminCtrl, this.authControllerInstance);




        adminView.setOnLogout(this::processLogoutNavigation);


        setScene(adminView.getRootPane());
    }

    private void showHome(String initialMessage, Alert.AlertType alertType) {
        logger.info("Navigating to Home View for user: " + username +
                (initialMessage != null ? ". Initial message: " + initialMessage : ""));
        cleanupGameResources(); 
        
        
        


        if (this.authControllerInstance == null || this.token == null) {
            logger.warning("HomeView: User is not properly authenticated. Redirecting to login.");
            showLogin();
            return;
        }

        GameService gameSvc = GameServiceHelper.narrow(getNamingRef("GameService"));
        if (gameSvc == null) {
            showErrorAlert("Service Unavailable", "Game service is not reachable. Cannot proceed to Home.", this::showLogin);
            return;
        }
        AuthenticationModel currentAuthModel = this.authControllerInstance.getModel();
        if (currentAuthModel == null) {
            logger.severe("HomeView: AuthenticationModel unavailable. Critical error.");
            showLogin();
            return;
        }

        GameModel tempGameModelForLeaderboard = new GameModel(gameSvc, username, token);
        LeaderboardModel lbModel = new LeaderboardModel(tempGameModelForLeaderboard);

        this.homeControllerInstance = new HomeController(currentAuthModel, tempGameModelForLeaderboard, lbModel, null);
        HomeView homeView = new HomeView(token, this.homeControllerInstance);
        this.homeControllerInstance.setHomeView(homeView);

        if (this.username != null) {
            homeView.setWelcomeMessage(this.username);
        }

        homeView.setOnStartGame(this::showMatchmaking);
        homeView.setOnViewLeaderboard(() -> {
            try {
                logger.info("HomeView: Leaderboard requested by user.");
                if (lbModel != null) {
                    homeView.showLeaderboard(lbModel.fetchTop5());
                } else {
                    homeView.showError("Leaderboard data service is currently unavailable.");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "HomeView: Error fetching leaderboard data.", e);
                Platform.runLater(() -> homeView.showError("Failed to display leaderboard: " + e.getMessage()));
            }
        });
        homeView.setOnLogout(() -> {
            logger.info("HomeView: Logout action initiated by user.");
            if (this.authControllerInstance != null) {
                this.authControllerInstance.handleLogout(this.token);
            } else {
                logger.warning("HomeView: authControllerInstance is null during logout. Forcing navigation to login.");
                processLogoutNavigation();
            }
        });

        this.authControllerInstance.setOnLogoutSuccess(this::processLogoutNavigation);

        setScene(homeView.getRootPane());


        if (initialMessage != null && this.homeControllerInstance != null) {
            final String finalMessage = initialMessage;
            Platform.runLater(() -> {
                if (this.homeControllerInstance != null) {
                    if (alertType == AlertType.ERROR || alertType == AlertType.WARNING) {
                        this.homeControllerInstance.displayMatchmakingFailureMessage(finalMessage);
                    } else {
                        Alert infoAlert = new Alert(alertType != null ? alertType : AlertType.INFORMATION);
                        infoAlert.setTitle("Notification");
                        infoAlert.setHeaderText(null);
                        infoAlert.setContentText(finalMessage);
                        infoAlert.showAndWait();
                    }
                }
            });
        }
    }

    private void showHome() {
        showHome(null, null);
    }

    private void showMatchmaking() {
        logger.info("Navigating to Matchmaking View for user: " + username);
        cleanupGameResources(); 

        if (this.token == null) {
            logger.warning("Matchmaking: No user token. Redirecting to login.");
            showLogin();
            return;
        }
        GameService gameSvc = GameServiceHelper.narrow(getNamingRef("GameService"));
        if (gameSvc == null) {
            showErrorAlert("Service Error", "Game service is unavailable for matchmaking.", this::showHome);
            return;
        }

        GameModel gameModel = new GameModel(gameSvc, username, token);
        
        this.gameControllerInstance = new GameController(gameModel, null); 
        this.clientCallbackInstance = new ClientCallbackImpl(orb, gameSvc, token, this.gameControllerInstance);

        
        this.gameControllerInstance.setOnGameStartFailedForNavigation(
                (String errorMessage) -> {
                    logger.info("MainApp: GameStartFailed Navigation Callback. Message: " + errorMessage);
                    cleanupGameResources(); 
                    this.gameControllerInstance = null; 
                    this.clientCallbackInstance = null;
                    showHome(errorMessage, Alert.AlertType.ERROR); 
                }
        );

        MatchmakingController mmCtrl = new MatchmakingController(gameModel, this.clientCallbackInstance, this.gameControllerInstance);
        mmCtrl.setOnMatchReadyToProceed(this::showGame);
        mmCtrl.setOnMatchmakingCancelledOrFailed(reason -> {
            logger.info("Matchmaking concluded. Reason: " + reason + ". Navigating from matchmaking.");
            cleanupGameResources(); 
            this.gameControllerInstance = null; 
            this.clientCallbackInstance = null;

            String userMessage;
            Alert.AlertType alertType = Alert.AlertType.INFORMATION;

            switch (reason) {
                case MatchmakingController.REASON_TIMEOUT:
                    userMessage = "No players found to start a game. Please try again later.";
                    alertType = Alert.AlertType.WARNING;
                    break;
                case MatchmakingController.REASON_SERVER_ERROR:
                    userMessage = "The server couldn't create or find a game for you at this moment.";
                    alertType = Alert.AlertType.ERROR;
                    break;
                case MatchmakingController.REASON_LOGIN_EXPIRED:
                    userMessage = "Your session has expired. Please log in again to continue.";
                    alertType = Alert.AlertType.ERROR;
                    showLogin();
                    return; 
                case MatchmakingController.REASON_UNEXPECTED_ERROR:
                default:
                    userMessage = "An unexpected error occurred during matchmaking. Please try again.";
                    alertType = Alert.AlertType.ERROR;
                    break;
            }
            showHome(userMessage, alertType);
        });

        setScene(mmCtrl.getView().getRootPane());
        mmCtrl.startMatchmaking();
    }

    private void showGame() {
        
        
        if (this.gameControllerInstance == null) {
            logger.severe("GameController instance is null when trying to show Game View. This might be due to an earlier startGameFailed event. Navigating to Home.");
            
            showHome("Could not start the game due to an earlier error.", Alert.AlertType.ERROR);
            return;
        }

        GameView gameView = new GameView();
        this.gameControllerInstance.setGameView(gameView); 

        
        
        
        
        if (this.gameControllerInstance.getGameId() == null || this.gameControllerInstance.getGameId().isEmpty()) {
            
            
            
            
            logger.warning("GameID is not set in GameController when showing GameView. " +
                    "This implies a potential issue in the matchmaking-to-game transition or startGameFailed handling.");
            
            cleanupGameResources();
            this.gameControllerInstance = null;
            this.clientCallbackInstance = null;
            showHome("There was an issue preparing the game. Please try again.", Alert.AlertType.ERROR);
            return;
        }

        logger.info("Displaying Game View for user=" + username + ", GameID: " + this.gameControllerInstance.getGameId());


        gameView.setOnBackToMenu(() -> {
            logger.info("GameView: 'Back to Menu' requested.");
            cleanupGameResources();
            this.gameControllerInstance = null;
            this.clientCallbackInstance = null;
            showHome();
        });
        gameView.setOnPlayAgain(() -> {
            logger.info("GameView: 'Play Again' requested.");
            cleanupGameResources();
            this.gameControllerInstance = null;
            this.clientCallbackInstance = null;
            showMatchmaking();
        });

        setScene(gameView.getRootPane());
        logger.info("Game View scene is now set. Awaiting server callbacks or user interaction.");
    }

    private org.omg.CORBA.Object getNamingRef(String name) {
        logger.fine("Attempting to resolve Naming Service reference for: " + name);
        try {
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt nceh = NamingContextExtHelper.narrow(objRef);
            if (nceh == null) {
                String errorMsg = "Failed to narrow NamingContextExt from 'NameService' reference.";
                logger.severe(errorMsg);
                Platform.runLater(() -> showErrorAlertAndExit("Critical Naming Error", errorMsg));
                throw new RuntimeException(errorMsg); 
            }
            org.omg.CORBA.Object obj = nceh.resolve_str(name);

            if (obj == null) {
                String errorMsg = "Naming Service resolved a null object for service: '" + name + "'. Service may be down or not registered.";
                logger.severe(errorMsg);
                Platform.runLater(() -> showErrorAlertAndExit("Critical Service Error", errorMsg));
                throw new RuntimeException(errorMsg); 
            }
            logger.info("Successfully resolved service: '" + name + "'.");
            return obj;
        } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
            String errorMsg = "'NameService' not found. ORB may not be configured correctly or Naming Service is down.";
            logger.log(Level.SEVERE, errorMsg, e);
            Platform.runLater(() -> showErrorAlertAndExit("ORB Configuration Error", errorMsg));
            throw new RuntimeException(errorMsg, e); 
        } catch (Exception e) {
            String errorMsg = "Failed to resolve '" + name + "' from Naming Service. Ensure service is running and correctly named.";
            logger.log(Level.SEVERE, errorMsg, e);
            Platform.runLater(() -> showErrorAlertAndExit("Service Resolution Error", errorMsg));
            throw new RuntimeException(errorMsg, e); 
        }
    }



    private void showErrorAlert(String title, String header, Runnable onOkAction) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText("Please try again. If the problem persists, please check the server status or contact support.");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            if (onOkAction != null) {
                alert.setOnHidden(evt -> onOkAction.run());
            }
            alert.showAndWait();
        });
    }

    private void showErrorAlertAndExit(String title, String header) {
        Runnable task = () -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText("The application will now exit due to a critical error.");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.setOnHidden(evt -> {
                if (orb != null) {
                    try {
                        orb.shutdown(true);
                        orb.destroy();
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Exception during ORB shutdown on critical exit.", e);
                    }
                }
                Platform.exit();
                System.exit(1);
            });
            alert.show();
        };
        if (Platform.isFxApplicationThread()) task.run(); else Platform.runLater(task);
    }

    private void setScene(Parent rootPane) {
        if (rootPane == null) {
            logger.severe("CRITICAL: Attempted to set scene with a null root pane. Application cannot proceed.");
            showErrorAlertAndExit("Fatal UI Error", "A critical error occurred while preparing the display (null content).");
            return;
        }

        String newRootType = rootPane.getClass().getSimpleName();
        logger.info("setScene: Called with new root pane type: " + newRootType);

        Runnable task = () -> {
            if (primaryStage == null) {
                logger.severe("CRITICAL: PrimaryStage is null in setScene task. UI cannot be updated.");
                return;
            }
            Scene scene = primaryStage.getScene();
            double stageWidthBefore = primaryStage.getWidth();
            double stageHeightBefore = primaryStage.getHeight();
            double sceneWidthFromGetter = 0;
            double sceneHeightFromGetter = 0;
            String oldRootType = "N/A";

            if (scene != null) {
                sceneWidthFromGetter = scene.getWidth();
                sceneHeightFromGetter = scene.getHeight();
                if (scene.getRoot() != null) {
                    oldRootType = scene.getRoot().getClass().getSimpleName();
                }
            }

            logger.info("setScene TASK [Before Change]: Old Root Type: " + oldRootType);
            logger.info("setScene TASK [Before Change]: Stage      W/H: " + stageWidthBefore + " / " + stageHeightBefore);
            logger.info("setScene TASK [Before Change]: Scene      W/H: " + sceneWidthFromGetter + " / " + sceneHeightFromGetter);

            double currentWidth;
            double currentHeight;

            if (scene != null && sceneWidthFromGetter > 0 && !Double.isNaN(sceneWidthFromGetter)) {
                currentWidth = sceneWidthFromGetter;
            } else if (stageWidthBefore > 0 && !Double.isNaN(stageWidthBefore)) {
                currentWidth = stageWidthBefore;
            } else {
                currentWidth = 1024;
            }

            if (scene != null && sceneHeightFromGetter > 0 && !Double.isNaN(sceneHeightFromGetter)) {
                currentHeight = sceneHeightFromGetter;
            } else if (stageHeightBefore > 0 && !Double.isNaN(stageHeightBefore)) {
                currentHeight = stageHeightBefore;
            } else {
                currentHeight = 768;
            }

            logger.info("setScene TASK [Dimensions Chosen]: Using W/H: " + currentWidth + " / " + currentHeight + " for next view (" + newRootType + ")");

            if (scene == null) {
                logger.info("setScene TASK: Creating NEW Scene for " + newRootType);
                primaryStage.setScene(new Scene(rootPane, currentWidth, currentHeight));
            } else {
                logger.info("setScene TASK: Setting root of EXISTING Scene to " + newRootType);
                scene.setRoot(rootPane);
                logger.info("setScene TASK: Setting Stage W/H to: " + currentWidth + " / " + currentHeight);
                primaryStage.setWidth(currentWidth);
                primaryStage.setHeight(currentHeight);
            }

            Scene finalScene = primaryStage.getScene();
            double finalSceneWidth = (finalScene != null) ? finalScene.getWidth() : -1;
            double finalSceneHeight = (finalScene != null) ? finalScene.getHeight() : -1;
            logger.info("setScene TASK [After Change]: Stage W/H: " + primaryStage.getWidth() + " / " + primaryStage.getHeight());
            logger.info("setScene TASK [After Change]: Scene W/H: " + finalSceneWidth + " / " + finalSceneHeight);

            primaryStage.centerOnScreen();
            logger.info("setScene TASK: Centered " + newRootType + " on screen.");
        };

        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }
}
