import com.cs9322.team05.client.player.view.LoginView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private LoginView loginView;

    public static void main(String[] args) {
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