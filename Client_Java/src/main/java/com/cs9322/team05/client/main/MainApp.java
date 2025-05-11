import com.cs9322.team05.client.player.view.LoginView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private LoginView loginView;

    public static void main(String[] args) {

        ORB orb = ORB.init(args, null);
        NamingContextExt ncRef = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

        AdminService adminService = AdminServiceHelper.narrow(ncRef.resolve_str("AdminService"));
        // Dito nalang mag dagdag nung mga servies na gagamitin.
        // AuthenticationService authService = AuthenticationServiceHelper.narrow(ncRef.resolve_str("AuthenticationService"));

        AdminModel adminModel = new AdminModel(adminService);

        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();


        loginView = new LoginView();

    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("What's The Word - Player Login");


        Parent loginRoot = loginView.createLoginPane();

        Scene scene = new Scene(loginRoot, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}