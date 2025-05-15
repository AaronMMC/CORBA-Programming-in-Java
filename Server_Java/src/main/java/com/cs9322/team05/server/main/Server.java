package com.cs9322.team05.server.main;

import ModifiedHangman.*;
import com.cs9322.team05.server.dao.DatabaseConnection;
import com.cs9322.team05.server.impl.AdminServiceImpl;
import com.cs9322.team05.server.impl.AuthenticationServiceImpl;
import com.cs9322.team05.server.impl.GameServiceImpl;
import com.cs9322.team05.server.dao.UserDao;
import com.cs9322.team05.server.dao.GameDao;
import com.cs9322.team05.server.manager.SessionManager;
import com.cs9322.team05.server.manager.PendingGameManager;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import java.sql.Connection;
import java.util.Properties;

public class Server {
    public static void main(String[] args) {
        try {
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBInitialPort", "2634");
            props.put("org.omg.CORBA.ORBInitialHost", "localhost");

            ORB orb = ORB.init(args, props);

            // Get Root POA and activate the POAManager
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            // Connect to DB and prepare services
            Connection connection = DatabaseConnection.getConnection();

            UserDao playerDao = new UserDao(connection);
            GameDao gameDao = new GameDao(connection);
            SessionManager sessionManager = SessionManager.getInstance();
            PendingGameManager pendingGameManager = new PendingGameManager();

            // Instantiate service implementations
            AdminServiceImpl adminImpl = new AdminServiceImpl(sessionManager, playerDao, gameDao);
            AuthenticationServiceImpl authImpl = new AuthenticationServiceImpl(sessionManager, playerDao);
            GameServiceImpl gameImpl = GameServiceImpl.getInstance(sessionManager, gameDao, playerDao, pendingGameManager);

            // Convert servants to CORBA object references
            org.omg.CORBA.Object adminRef = rootPOA.servant_to_reference(adminImpl);
            org.omg.CORBA.Object authRef = rootPOA.servant_to_reference(authImpl);
            org.omg.CORBA.Object gameRef = rootPOA.servant_to_reference(gameImpl);

            // Register with Naming Service
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt namingContext = NamingContextExtHelper.narrow(objRef);

            namingContext.rebind(namingContext.to_name("AdminService"), adminRef);
            namingContext.rebind(namingContext.to_name("AuthenticationService"), authRef);
            namingContext.rebind(namingContext.to_name("GameService"), gameRef);

            System.out.println("Server ready and services bound to Naming Service.");
            orb.run();

        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
