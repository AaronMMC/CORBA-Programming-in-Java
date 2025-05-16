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
import com.cs9322.team05.client.player.services.HomeView;     // Your styled HomeView
import com.cs9322.team05.client.player.view.GameView;         // Your GameView
import com.cs9322.team05.client.player.view.AuthenticationView; // Your NEW styled AuthenticationView
import com.cs9322.team05.client.admin.controller.AdminController;
import com.cs9322.team05.client.admin.model.AdminModel;
import com.cs9322.team05.client.admin.view.AdminView;         // Your NEW styled AdminView
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region; // For setting min height on alerts
import javafx.stage.Stage;
import org.omg.CORBA.ORB;
// import org.omg.CORBA.Object; // Can be ambiguous, qualify if needed
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
    private boolean isMatchmakingEffectivelyActive;


    public static void main(String[] args) {
        // Optional: Configure logger for better output if not already done elsewhere
        // System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        logger.info("Launching Hangman Client Application...");
        launch(args);
    }

    @Override
    public void init() throws Exception {
        logger.info("Initializing ORB connection properties.");
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBInitialHost", "localhost"); // Ensure this matches your server
        props.put("org.omg.CORBA.ORBInitialPort", "1050");    // Ensure this matches your server
        orb = ORB.init(new String[0], props);
        logger.info("ORB initialized. Target Host: " + props.getProperty("org.omg.CORBA.ORBInitialHost") +
                ", Port: " + props.getProperty("org.omg.CORBA.ORBInitialPort"));
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        logger.info("Application start: Setting up primary stage.");
        showLogin(); // Initial view
        primaryStage.setTitle("What's The Word - Hangman Challenge");
        primaryStage.setOnCloseRequest(event -> {
            logger.info("Application window close requested. Initiating shutdown sequence...");
            performUserLogout(true); // Attempt server logout for graceful exit
            if (orb != null) {
                logger.info("Shutting down ORB connection.");
                orb.shutdown(true); // true to wait for completion
                orb.destroy();
                logger.info("ORB shutdown finalized.");
            }
            Platform.exit(); // Gracefully exits JavaFX application thread
            System.exit(0);  // Ensures JVM terminates if other non-daemon threads are active
        });
        primaryStage.show();
        logger.info("Primary stage is now visible.");
    }

    private void cleanupGameResources() {
        if (this.gameControllerInstance != null) {
            logger.info("Cleaning up active game controller resources and state.");
            this.gameControllerInstance.cleanupControllerState(); // Ensures view is also reset
        }
        this.gameControllerInstance = null;
        this.clientCallbackInstance = null; // Callback tied to game session
        logger.fine("Game-specific resources have been cleaned up.");
    }

    // Central method to handle navigation to login screen after any logout event
    private void processLogoutNavigation() {
        logger.info("Processing logout navigation: Clearing local user session, game resources, and returning to login screen.");
        this.username = null;
        this.token = null;
        cleanupGameResources();
        this.homeControllerInstance = null; // Clear reference to home controller
        // authControllerInstance will be re-created by showLogin() for a fresh login attempt
        showLogin();
    }

    // Attempts server-side logout; primarily for app exit or explicit user action
    private void performUserLogout(boolean appIsExiting) {
        if (this.token != null && this.authControllerInstance != null) {
            logger.info("Attempting server-side logout for token: " + this.token + ". Application exiting: " + appIsExiting);
            try {
                // AuthenticationController.handleLogout will make the server call.
                // Its onLogoutSuccess callback (set by MainApp) will trigger processLogoutNavigation.
                this.authControllerInstance.handleLogout(this.token);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception encountered during server-side logout attempt.", e);
            }
        }
        // If application is exiting, ensure local state is cleared regardless of server call success.
        // For user-initiated logouts from views, processLogoutNavigation handles clearing state after server call.
        if (appIsExiting) {
            this.username = null;
            this.token = null;
            cleanupGameResources();
            this.homeControllerInstance = null;
        }
    }

    private void showLogin() {
        logger.info("Navigating to Login View.");
        // Ensure any previous session data is thoroughly cleared before showing login
        this.username = null;
        this.token = null;
        cleanupGameResources();
        this.homeControllerInstance = null; // Reset home controller

        org.omg.CORBA.Object authServiceObj = getNamingRef("AuthenticationService");
        if (authServiceObj == null) {
            // getNamingRef now handles exit on critical failure
            return;
        }
        AuthenticationService authSvc = AuthenticationServiceHelper.narrow(authServiceObj);

        AuthenticationModel authModel = new AuthenticationModel(authSvc);
        // Create a new AuthenticationController instance for a fresh login session
        this.authControllerInstance = new AuthenticationController(authModel);
        AuthenticationView authenticationView = new AuthenticationView(this.authControllerInstance); // Your styled view

        this.authControllerInstance.setOnLoginSuccess((loggedInUser, receivedToken) -> {
            this.username = loggedInUser;
            this.token = receivedToken;
            logger.info("Login successful for user: " + this.username + ". Token acquired. Navigating to appropriate view.");
            // CRITICAL: Set the onLogoutSuccess for this authenticated session's controller
            this.authControllerInstance.setOnLogoutSuccess(this::processLogoutNavigation);

            if ("admin".equalsIgnoreCase(this.username)) {
                showAdminView();
            } else {
                showHome(null, null); // No special message on fresh login
            }
        });
        // Optional: Set onLoginFailure if MainApp needs to react beyond AuthenticationView's internal error display
        this.authControllerInstance.setOnLoginFailure(errMsg -> {
            logger.warning("MainApp's AuthController onLoginFailure: " + errMsg + ". AuthenticationView should display this.");
        });

        setScene(authenticationView.createLoginPane()); // Using method from your styled AuthenticationView
    }

    private void showAdminView() {
        logger.info("Navigating to Admin Dashboard for user: " + username);
        cleanupGameResources(); // Clean player game state if any
        this.homeControllerInstance = null;

        if (this.authControllerInstance == null || this.token == null) {
            logger.severe("AdminView: Critical authentication context missing. Redirecting to login.");
            showLogin();
            return;
        }

        org.omg.CORBA.Object adminServiceObj = getNamingRef("AdminService");
        if (adminServiceObj == null) return;
        AdminService adminSvc = AdminServiceHelper.narrow(adminServiceObj);

        AdminModel adminModel = new AdminModel(adminSvc);
        AdminController adminCtrl = new AdminController(adminModel); // Admin-specific tasks controller

        AdminView adminView = new AdminView(token, adminCtrl, this.authControllerInstance); // Your styled view
        // AdminView's internal logout button calls authControllerInstance.handleLogout.
        // The onLogoutSuccess callback on authControllerInstance (set during/after login)
        // will trigger processLogoutNavigation(), leading back to the login screen.
        // AdminView also has its own onLogout callback that MainApp sets.
        adminView.setOnLogout(this::processLogoutNavigation);

        setScene(adminView.getRootPane());
    }

    private void showHome(String initialMessage, AlertType alertType) {
        logger.info("Navigating to Home View for user: " + username +
                (initialMessage != null ? ". Initial message: '" + initialMessage + "'" : ""));
        cleanupGameResources();

        if (this.authControllerInstance == null || this.token == null) {
            logger.warning("HomeView: User not properly authenticated or token is missing. Redirecting to login.");
            showLogin();
            return;
        }

        org.omg.CORBA.Object gameServiceObj = getNamingRef("GameService");
        if (gameServiceObj == null) return;
        GameService gameSvc = GameServiceHelper.narrow(gameServiceObj);

        AuthenticationModel currentAuthModel = this.authControllerInstance.getModel();
        if (currentAuthModel == null) {
            logger.severe("HomeView: AuthenticationModel is null via authControllerInstance. Cannot proceed.");
            showLogin();
            return;
        }

        GameModel tempGameModelForLeaderboard = new GameModel(gameSvc, username, token);
        LeaderboardModel lbModel = new LeaderboardModel(tempGameModelForLeaderboard);

        // Create or reuse HomeController
        this.homeControllerInstance = new HomeController(currentAuthModel, tempGameModelForLeaderboard, lbModel, null);
        HomeView homeView = new HomeView(token, this.homeControllerInstance); // Your styled HomeView
        this.homeControllerInstance.setHomeView(homeView); // Link controller to view

        if (this.username != null) {
            homeView.setWelcomeMessage(this.username); // Set personalized welcome message
        }

        homeView.setOnStartGame(() -> showMatchmaking(gameSvc));
        homeView.setOnViewLeaderboard(() -> {
            try {
                logger.info("HomeView: Leaderboard view requested by user.");
                if (lbModel != null) {
                    homeView.showLeaderboard(lbModel.fetchTop5());
                } else {
                    logger.warning("HomeView: LeaderboardModel is null. Cannot show leaderboard.");
                    homeView.showError("Leaderboard data service is currently unavailable.");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "HomeView: Error occurred while fetching/displaying leaderboard.", e);
                Platform.runLater(() -> homeView.showError("Failed to display leaderboard: " + e.getMessage()));
            }
        });
        homeView.setOnLogout(() -> {
            logger.info("HomeView: Logout action initiated by user.");
            if (this.authControllerInstance != null) {
                // This call will trigger AuthenticationController's onLogoutSuccess,
                // which in turn calls processLogoutNavigation() in MainApp.
                this.authControllerInstance.handleLogout(this.token);
            } else {
                logger.warning("HomeView: authControllerInstance is null during logout. Forcing navigation to login.");
                processLogoutNavigation(); // Fallback
            }
        });
        // Ensure onLogoutSuccess of the central authControllerInstance is correctly set
        this.authControllerInstance.setOnLogoutSuccess(this::processLogoutNavigation);

        setScene(homeView.getRootPane()); // Use getRootPane from your styled HomeView

        // Display initial message/alert if provided (e.g., from matchmaking failure)
        if (initialMessage != null && this.homeControllerInstance != null) {
            final String finalMessage = initialMessage; // For use in lambda
            Platform.runLater(() -> { // Ensure UI update is on FX thread and after scene is shown
                if (this.homeControllerInstance != null) { // Check instance again inside lambda
                    if (alertType == AlertType.ERROR || alertType == AlertType.WARNING) {
                        this.homeControllerInstance.displayMatchmakingFailureMessage(finalMessage);
                    } else { // For INFORMATION or other types, show a generic alert
                        Alert infoAlert = new Alert(alertType != null ? alertType : AlertType.INFORMATION);
                        infoAlert.setTitle("Notification");
                        infoAlert.setHeaderText(null);
                        infoAlert.setContentText(finalMessage);
                        infoAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        infoAlert.showAndWait();
                    }
                } else {
                    logger.warning("Could not display initial message on HomeView as homeControllerInstance was null in lambda. Message: " + finalMessage);
                }
            });
        }
    }

    // Convenience overload for showHome without a message
    private void showHome() {
        showHome(null, null);
    }

    private void showMatchmaking(GameService gameSvc) {
        logger.info("Navigating to Matchmaking View for user: " + username);
        // ... (setup as before) ...
        // Create new instances for a new game/matchmaking session
        GameModel gameModel = new GameModel(gameSvc, username, token);
        this.gameControllerInstance = new GameController(gameModel, null); // View will be set later

        // Crucially, set the navigation callback on GameController *before* ClientCallback can invoke startGameFailed
        this.gameControllerInstance.setOnGameStartFailedForNavigation(reason -> {
            logger.info("MainApp: Received onGameStartFailedForNavigation from GameController (likely during/after matchmaking setup). Reason: " + reason);
            cleanupGameResources();
            // If matchmaking view is still active, this will navigate away from it to home.
            showHome(reason, AlertType.WARNING);
        });

        this.clientCallbackInstance = new ClientCallbackImpl(orb, gameSvc, token, this.gameControllerInstance);
        MatchmakingController mmCtrl = new MatchmakingController(gameModel, this.clientCallbackInstance, this.gameControllerInstance);
        // ... (setOnMatchReadyToProceed and setOnMatchmakingCancelledOrFailed for mmCtrl as before) ...

        mmCtrl.setOnMatchmakingCancelledOrFailed(reason -> {
            // ... (existing logic for timeout, server error, etc.) ...
            // If REASON_USER_CANCELLED, this part won't be hit due to button removal,
            // unless controller's cancelMatchmakingProcess is called by MainApp (e.g. on logout).
            // The timeout (REASON_TIMEOUT) or server error (REASON_SERVER_ERROR) from matchmaking itself
            // should lead here.
            logger.info("Matchmaking process concluded without finding a match. Reason: " + reason);
            isMatchmakingEffectivelyActive = false;
            cleanupGameResources(); // Clean game controller as game won't start from this path

            String userMessage;
            AlertType alertType = AlertType.INFORMATION;


            // ... (rest of the switch for other reasons like TIMEOUT, SERVER_ERROR)
            switch (reason) {
                case MatchmakingController.REASON_TIMEOUT:
                    userMessage = "No players found to start a game at this time. Please try again later.";
                    alertType = AlertType.WARNING;
                    break;
                // ... other cases
                default:
                    userMessage = "Matchmaking did not complete successfully. Please try again.";
                    alertType = AlertType.WARNING;
                    break;
            }
            showHome(userMessage, alertType);
        });


        setScene(mmCtrl.getView().getRootPane());
        mmCtrl.startMatchmaking();
    }

    private void showGame(GameService gameSvc) {
        if (this.gameControllerInstance == null) {
            logger.severe("Attempted to show Game View, but GameController instance is null! Returning to Home.");
            cleanupGameResources();
            showHome();
            return;
        }

        GameView gameView = new GameView();
        this.gameControllerInstance.setGameView(gameView);

        // Set the callback for when startGameFailed needs to navigate back to Home
        this.gameControllerInstance.setOnGameStartFailedForNavigation(reason -> {
            logger.info("MainApp: Received onGameStartFailedForNavigation from GameController. Reason: " + reason);
            cleanupGameResources(); // Clean up the failed game attempt
            showHome(reason, AlertType.WARNING); // Show HomeView with a warning popup
        });


        // If gameId is null (e.g., due to startGameFailed being called *before* this showGame method),
        // the setOnGameStartFailedForNavigation callback would be triggered by GameController when it processes the server event.
        // If showGame is called and gameId is already null due to an earlier startGameFailed,
        // the GameController's handleGameStartFailed will trigger the navigation.
        if (this.gameControllerInstance.getGameId() == null || this.gameControllerInstance.getGameId().isEmpty()) {
            logger.warning("GameID is not set in GameController when MainApp.showGame() is called. " +
                    "This indicates startGameFailed might have already been processed or will be shortly. " +
                    "Awaiting GameController to trigger navigation if needed.");
            // We still set the scene, and GameController's callbacks will handle the state.
            // If startGameFailed comes after this, the callback above will handle it.
        } else {
            logger.info("Displaying Game View for user=" + username + ", GameID: " + this.gameControllerInstance.getGameId());
        }

        gameView.setOnBackToMenu(() -> {
            logger.info("GameView: 'Back to Menu' action triggered.");
            cleanupGameResources();
            showHome();
        });
        gameView.setOnPlayAgain(() -> {
            logger.info("GameView: 'Play Again' action triggered.");
            cleanupGameResources();
            showMatchmaking(gameSvc);
        });

        setScene(gameView.getRootPane());
        logger.info("Game View scene is now set. Awaiting server callbacks or user interaction.");
    }

    // Helper to resolve CORBA object references
    private org.omg.CORBA.Object getNamingRef(String serviceName) {
        logger.fine("Attempting to resolve Naming Service reference for: '" + serviceName + "'");
        try {
            org.omg.CORBA.Object initialRef = orb.resolve_initial_references("NameService");
            NamingContextExt nceh = NamingContextExtHelper.narrow(initialRef);
            if (nceh == null) {
                String errorMsg = "Failed to narrow NamingContextExt from 'NameService' reference. Naming Service might be misconfigured or unavailable.";
                logger.severe(errorMsg);
                Platform.runLater(() -> showErrorAlertAndExit("Critical Naming Service Error", errorMsg));
                throw new RuntimeException(errorMsg); // Halt execution
            }
            org.omg.CORBA.Object serviceObj = nceh.resolve_str(serviceName);

            if (serviceObj == null) {
                String errorMsg = "Naming Service resolved a null object for service: '" + serviceName + "'. The service may be down, not registered, or misnamed.";
                logger.severe(errorMsg);
                Platform.runLater(() -> showErrorAlertAndExit("Critical Service Resolution Error", errorMsg));
                throw new RuntimeException(errorMsg); // Halt execution
            }
            logger.info("Successfully resolved Naming Service reference for: '" + serviceName + "'.");
            return serviceObj;
        } catch (org.omg.CORBA.ORBPackage.InvalidName e_orb) { // Specific exception for "NameService" not found
            String errorMsg = "ORB  Error: 'NameService' reference not found. ORB may not be configured correctly, or the Naming Service is not running at the expected location.";
            logger.log(Level.SEVERE, errorMsg, e_orb);
            Platform.runLater(() -> showErrorAlertAndExit("ORB Configuration/Naming Error", errorMsg));
            throw new RuntimeException(errorMsg, e_orb); // Halt execution
        } catch (Exception e_cos) { // Catches org.omg.CosNaming.NamingContextPackage.NotFound, .CannotProceed, .InvalidName
            String errorMsg = "Failed to resolve service '" + serviceName + "' from the Naming Service. Ensure the service is running and correctly registered with the expected name.";
            logger.log(Level.SEVERE, errorMsg, e_cos);
            Platform.runLater(() -> showErrorAlertAndExit("Service Name Resolution Error", errorMsg));
            throw new RuntimeException(errorMsg, e_cos); // Halt execution
        }
    }

    // Helper to show an error alert that allows continuing to a fallback action
    private void showErrorAlert(String title, String header, Runnable onOkAction) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText("Please try again. If the problem persists, check server status or contact support.");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); // Ensure content fits
            if (onOkAction != null) {
                // Use setOnCloseRequest if you want action on 'X' button too,
                // setOnHidden for after dialog is closed by any means (button, X)
                alert.setOnHidden(evt -> onOkAction.run());
            }
            alert.showAndWait(); // Use showAndWait for modal behavior
        });
    }

    // Helper to show a critical error alert and then exit the application
    private void showErrorAlertAndExit(String title, String header) {
        Runnable task = () -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Critical Error - Application Exit");
            alert.setHeaderText(title + (header != null ? ": " + header : ""));
            alert.setContentText("The application encountered a critical error and will now exit. Please check logs for details.");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.setOnHidden(evt -> { // Action after dialog is closed
                if (orb != null) {
                    try {
                        orb.shutdown(true);
                        orb.destroy();
                    } catch (Exception e_orb_shutdown) {
                        logger.log(Level.WARNING, "Exception during ORB shutdown on critical error exit.", e_orb_shutdown);
                    }
                }
                Platform.exit();
                System.exit(1); // Non-zero exit code indicates an error
            });
            alert.show(); // Show and let onHidden handle exit. Using showAndWait() here would block until closed by user.
        };

        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }

    // Helper to set the root of the primary stage's scene
    private void setScene(Parent rootPane) {
        if (rootPane == null) {
            logger.severe("CRITICAL: Attempted to set scene with a null root pane. Application integrity compromised.");
            showErrorAlertAndExit("Fatal UI Error", "Cannot display screen due to missing content (null root pane).");
            return;
        }
        Runnable task = () -> {
            if (primaryStage == null) {
                logger.severe("CRITICAL: PrimaryStage is null in setScene. UI cannot be updated. This indicates a severe application lifecycle issue.");
                // Cannot show alert if primaryStage is null. Log and exit.
                System.exit(2); // Different exit code for this specific failure
                return;
            }
            Scene scene = primaryStage.getScene();
            double currentWidth = (scene != null && scene.getWidth() > 0) ? scene.getWidth() : 1024;  // Default width
            double currentHeight = (scene != null && scene.getHeight() > 0) ? scene.getHeight() : 768; // Default height

            if (scene == null) {
                primaryStage.setScene(new Scene(rootPane, currentWidth, currentHeight));
            } else {
                scene.setRoot(rootPane);
                // Maintain current window size if a scene already exists
                primaryStage.setWidth(currentWidth);
                primaryStage.setHeight(currentHeight);
            }
            // primaryStage.sizeToScene(); // Optional: Resizes stage to fit the preferred size of the new root.
            // Can be useful, but might also shrink window unexpectedly.
            // If views have good preferred sizes, this is okay.
            primaryStage.centerOnScreen(); // Center the window on the screen
        };

        // Ensure UI updates happen on the JavaFX Application Thread
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }
}