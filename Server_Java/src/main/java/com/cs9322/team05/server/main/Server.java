package com.cs9322.team05.server.main;

import ModifiedHangman.*;
import com.cs9322.team05.server.dao.DatabaseConnection;
import com.cs9322.team05.server.impl.AdminServiceImpl;
import com.cs9322.team05.server.impl.AuthenticationServiceImpl;
import com.cs9322.team05.server.impl.GameServiceImpl;
import com.cs9322.team05.server.dao.PlayerDao;
import com.cs9322.team05.server.dao.GameDao;
import com.cs9322.team05.server.manager.SessionManager;
import com.cs9322.team05.server.manager.PendingGameManager;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import java.sql.Connection;

public class Server {
    public static void main(String[] args) {
        try {
            // Initialize the ORB
            ORB orb = ORB.init(args, null);

            // Get the database connection
            Connection connection = DatabaseConnection.getConnection();

            // DAOs and Managers
            PlayerDao playerDao = new PlayerDao(connection);
            GameDao gameDao = new GameDao(connection);
            SessionManager sessionManager = new SessionManager();
            PendingGameManager pendingGameManager = new PendingGameManager();

            // Servants (service implementations)
            AdminServiceImpl adminServiceImpl = new AdminServiceImpl(sessionManager, playerDao, gameDao);
            AuthenticationServiceImpl authServiceImpl = new AuthenticationServiceImpl(sessionManager, playerDao);
            GameServiceImpl gameServiceImpl = new GameServiceImpl(sessionManager, gameDao, playerDao, pendingGameManager);

            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

            org.omg.CORBA.Object adminRef = rootPOA.servant_to_reference(adminServiceImpl);
            org.omg.CORBA.Object authRef = rootPOA.servant_to_reference(authServiceImpl);
            org.omg.CORBA.Object gameRef = rootPOA.servant_to_reference(gameServiceImpl);

            AdminService adminService = AdminServiceHelper.narrow(adminRef);
            AuthenticationService authService = AuthenticationServiceHelper.narrow(authRef);
            GameService gameService = GameServiceHelper.narrow(gameRef);

            String adminIOR = orb.object_to_string(adminService);
            String authIOR = orb.object_to_string(authService);
            String gameIOR = orb.object_to_string(gameService);

            System.out.println("Server ready and waiting...");
            System.out.println("Admin Service IOR: " + adminIOR);
            System.out.println("Authentication Service IOR: " + authIOR);
            System.out.println("Game Service IOR: " + gameIOR);

            rootPOA.the_POAManager().activate();
            orb.run();

        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
