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
import com.cs9322.team05.client.player.services.HomeView; // Your styled HomeView
import com.cs9322.team05.client.player.view.GameView;     // Your GameView
import com.cs9322.team05.client.player.view.AuthenticationView; // Your NEW AuthenticationView
import com.cs9322.team05.client.admin.controller.AdminController;
import com.cs9322.team05.client.admin.model.AdminModel;
import com.cs9322.team05.client.admin.view.AdminView; // Your NEW AdminView
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.omg.CORBA.ORB;
// Explicitly use org.omg.CORBA.Object to avoid ambiguity if java.lang.Object is also meant
// import org.omg.CORBA.Object;
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
                "[%1$tF %1$tT] [%4$-7s] %5$s %n"); // Optional: More detailed log format
        logger.info("Launching Hangman Client Application");
        launch(args);
    }

    @Override
    public void init() throws Exception {
        logger.info("Initializing ORB connection properties.");
        Properties props = new Properties();
        // Ensure these match your ORB server configuration
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
            performUserLogout(true); // Attempt server logout if user was logged in
            if (orb != null) {
                logger.info("Shutting down ORB connection.");
                orb.shutdown(true); // true indicates wait for shutdown completion
                orb.destroy();
                logger.info("ORB shutdown complete.");
            }
            Platform.exit(); // Gracefully exits JavaFX application thread
            System.exit(0);  // Ensures JVM terminates if other non-daemon threads are running
        });
        primaryStage.show();
        logger.info("Primary stage is now visible.");
    }

    private void cleanupGameResources() {
        if (this.gameControllerInstance != null) {
            logger.info("Cleaning up active game controller state.");
            this.gameControllerInstance.cleanupControllerState();
        }
        this.gameControllerInstance = null;
        this.clientCallbackInstance = null; // Callback instance is tied to a game session
        logger.fine("Game resources cleaned up.");
    }

    private void processLogoutNavigation() {
        logger.info("Processing logout: Clearing local session and navigating to login screen.");
        this.username = null;
        this.token = null;
        cleanupGameResources(); // Clear any active game state
        this.homeControllerInstance = null; // Clear reference to home controller
        // authControllerInstance is handled by showLogin (re-initialized)
        showLogin();
    }

    private void performUserLogout(boolean appExiting) {
        if (this.token != null && this.authControllerInstance != null) {
            logger.info("Attempting server-side logout. App exiting: " + appExiting);
            try {
                // authController.handleLogout will trigger its onLogoutSuccess callback
                // which in turn calls processLogoutNavigation().
                this.authControllerInstance.handleLogout(this.token);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception during server-side logout attempt.", e);
            }
        }
        // If application is exiting, ensure local state is cleared regardless of server call success.
        // For user-initiated logout, processLogoutNavigation handles clearing state.
        if (appExiting) {
            this.username = null;
            this.token = null;
            cleanupGameResources();
            this.homeControllerInstance = null;
        }
    }

    private void showLogin() {
        logger.info("Navigating to Login View.");
        this.username = null;
        this.token = null;
        cleanupGameResources();
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
            // Critical: Set the onLogoutSuccess for the now authenticated session's controller
            this.authControllerInstance.setOnLogoutSuccess(this::processLogoutNavigation);
            if ("admin".equalsIgnoreCase(this.username)) {
                showAdminView();
            } else {
                showHome(null, null); // No special message on fresh login to home
            }
        });
        // Login failure messages are handled by AuthenticationView calling controller.handleLogin
        // and displaying the returned string, or via onLoginFailure if implemented.

        setScene(authenticationView.createLoginPane());
    }

    private void showAdminView() {
        logger.info("Navigating to Admin Dashboard for user: " + username);
        cleanupGameResources();
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
        // AdminView's internal logout button calls authControllerInstance.handleLogout.
        // The onLogoutSuccess callback on authControllerInstance (set during login)
        // will then trigger processLogoutNavigation -> showLogin.
        // We can also set AdminView's specific onLogout if it has one for other cleanup.
        adminView.setOnLogout(this::processLogoutNavigation);


        setScene(adminView.getRootPane());
    }

    private void showHome(String initialMessage, AlertType alertType) {
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
                this.authControllerInstance.handleLogout(this.token); // Triggers onLogoutSuccess
            } else {
                logger.warning("HomeView: authControllerInstance is null during logout. Forcing navigation to login.");
                processLogoutNavigation(); // Fallback
            }
        });
        // Ensure onLogoutSuccess is set for the current authControllerInstance
        this.authControllerInstance.setOnLogoutSuccess(this::processLogoutNavigation);

        setScene(homeView.getRootPane());

        // Display initial message/alert if provided (e.g., from matchmaking failure)
        if (initialMessage != null && this.homeControllerInstance != null) {
            final String finalMessage = initialMessage;
            Platform.runLater(() -> { // Ensure runs after scene is set
                if (this.homeControllerInstance != null) { // Check again as it's in a lambda
                    if (alertType == AlertType.ERROR || alertType == AlertType.WARNING) {
                        this.homeControllerInstance.displayMatchmakingFailureMessage(finalMessage);
                    } else { // For INFORMATION or other types
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
    // Overload for convenience
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

        MatchmakingController mmCtrl = new MatchmakingController(gameModel, this.clientCallbackInstance, this.gameControllerInstance);
        mmCtrl.setOnMatchReadyToProceed(this::showGame);
        mmCtrl.setOnMatchmakingCancelledOrFailed(reason -> {
            logger.info("Matchmaking concluded. Reason: " + reason + ". Navigating from matchmaking.");
            cleanupGameResources();
            String userMessage;
            AlertType alertType = AlertType.INFORMATION;

            switch (reason) {
                case MatchmakingController.REASON_TIMEOUT:
                    userMessage = "No players found to start a game. Please try again later.";
                    alertType = AlertType.WARNING;
                    break;
                case MatchmakingController.REASON_SERVER_ERROR:
                    userMessage = "The server couldn't create or find a game for you at this moment.";
                    alertType = AlertType.ERROR;
                    break;
                case MatchmakingController.REASON_LOGIN_EXPIRED:
                    userMessage = "Your session has expired. Please log in again to continue.";
                    alertType = AlertType.ERROR;
                    showLogin(); // Critical: Go to login for expired session
                    return;
                case MatchmakingController.REASON_UNEXPECTED_ERROR:
                default:
                    userMessage = "An unexpected error occurred during matchmaking. Please try again.";
                    alertType = AlertType.ERROR;
                    break;
            }
            showHome(userMessage, alertType); // Show HomeView with the determined message and alert type
        });

        setScene(mmCtrl.getView().getRootPane());
        mmCtrl.startMatchmaking();
    }

    private void showGame() {
        if (this.gameControllerInstance == null) {
            logger.severe("GameController instance is null when trying to show Game View. Critical error. Returning to Home.");
            cleanupGameResources();
            showHome();
            return;
        }

        GameView gameView = new GameView(); // Your styled GameView from previous steps
        this.gameControllerInstance.setGameView(gameView);

        if (this.gameControllerInstance.getGameId() == null || this.gameControllerInstance.getGameId().isEmpty()) {
            logger.warning("GameID is not set in GameController when showing GameView. " +
                    "This usually means startGameFailed was called (e.g. not enough players). " +
                    "GameController should update GameView to an error state.");
            // GameController's handleGameStartFailed (triggered by ClientCallbackImpl.startGameFailed)
            // should have already updated the gameView instance (e.g., view.showError(...)).
            // So, MainApp just needs to set the scene.
        } else {
            logger.info("Displaying Game View for user=" + username + ", GameID: " + this.gameControllerInstance.getGameId());
        }

        gameView.setOnBackToMenu(() -> {
            logger.info("GameView: 'Back to Menu' requested.");
            cleanupGameResources();
            showHome();
        });
        gameView.setOnPlayAgain(() -> {
            logger.info("GameView: 'Play Again' requested.");
            cleanupGameResources();
            showMatchmaking(); // Go back to matchmaking for a new game
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
        } catch (Exception e) { // org.omg.CosNaming.NamingContextPackage.NotFound, CannotProceed, InvalidName
            String errorMsg = "Failed to resolve '" + name + "' from Naming Service. Ensure service is running and correctly named.";
            logger.log(Level.SEVERE, errorMsg, e);
            Platform.runLater(() -> showErrorAlertAndExit("Service Resolution Error", errorMsg));
            throw new RuntimeException(errorMsg, e);
        }
    }



    private void showErrorAlert(String title, String header, Runnable onOkAction) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText("Please try again. If the problem persists, please check the server status or contact support.");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); // Ensure content fits
            if (onOkAction != null) {
                alert.setOnHidden(evt -> onOkAction.run());
            }
            alert.showAndWait();
        });
    }

    private void showErrorAlertAndExit(String title, String header) {
        Runnable task = () -> {
            Alert alert = new Alert(AlertType.ERROR);
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
                System.exit(1); // Indicate error exit
            });
            alert.show(); // Use show() instead of showAndWait() if Platform.exit() is called inside onHidden
        };
        if (Platform.isFxApplicationThread()) task.run(); else Platform.runLater(task);
    }

    private void setScene(Parent rootPane) {
        if (rootPane == null) {
            logger.severe("CRITICAL: Attempted to set scene with a null root pane. Application cannot proceed.");
            showErrorAlertAndExit("Fatal UI Error", "A critical error occurred while preparing the display (null content).");
            return;
        }
        Runnable task = () -> {
            if (primaryStage == null) {
                logger.severe("CRITICAL: PrimaryStage is null in setScene. UI cannot be updated.");
                return;
            }
            Scene scene = primaryStage.getScene();
            double currentWidth = (scene != null && scene.getWidth() > 0) ? scene.getWidth() : 1024;
            double currentHeight = (scene != null && scene.getHeight() > 0) ? scene.getHeight() : 768;

            if (scene == null) {
                primaryStage.setScene(new Scene(rootPane, currentWidth, currentHeight));
            } else {
                scene.setRoot(rootPane);
                // If you want to maintain the current window size after changing root:
                primaryStage.setWidth(currentWidth);
                primaryStage.setHeight(currentHeight);
            }
            // primaryStage.sizeToScene(); // This can shrink the window if the new root is smaller.
            // Remove if you prefer to maintain size or set explicit sizes.
            primaryStage.centerOnScreen(); // Center the window
        };
        if (Platform.isFxApplicationThread()) task.run(); else Platform.runLater(task);
    }
}