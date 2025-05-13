package com.cs9322.team05.client.main;

import ModifiedHangman.*;
import com.cs9322.team05.client.admin.model.AdminModel;
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
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class MainApp extends Application {
    private Stage primaryStage;
    private ORB orb;
    private POA poa;
    private String username, token;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {

        orb = ORB.init(new String[0], null);
        NamingContextExt nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
        poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        poa.the_POAManager().activate();
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

    private void showAdminLandingPage() {
        AdminService adminService = AdminServiceHelper.narrow(getNamingRef("AdminService"));
        AdminModel adminModel = new AdminModel(adminService);
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
                throw new RuntimeException(e);
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


        ClientCallbackImpl callback = new ClientCallbackImpl(null);
        GameController gameCtrl = new GameController(gameModel, null);
        callback.setController(gameCtrl);
        gameCtrl.registerCallback(callback, poa);


        MatchmakingController mmCtrl = new MatchmakingController(gameModel, callback);
        mmCtrl.setOnMatchFound(() -> showGame(gameModel, gameCtrl, callback));
        mmCtrl.setOnCancel(this::showHome);

        setScene(mmCtrl.getView().getRootPane());
    }

    private void showGame(GameModel gameModel, GameController gameCtrl, ClientCallbackImpl callback) {
        GameView view = new GameView();
        gameCtrl.setGameView(view);


        view.setOnStart(gameCtrl::startGame);
        view.setOnGuess(gameCtrl::submitGuess);
        view.setOnLeaderboard(gameCtrl::fetchLeaderboard);
        view.setOnPlayAgain(gameCtrl::resetAndStart);
        view.setOnBackToMenu(this::showHome);


        gameCtrl.registerCallback(callback, poa);
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
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root, 1000, 700));
        } else {
            primaryStage.getScene().setRoot(root);
        }
    }
}
