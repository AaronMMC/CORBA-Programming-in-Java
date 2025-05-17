package com.cs9322.team05.server.main;

import ModifiedHangman.*;
import com.cs9322.team05.server.dao.DatabaseConnection;
import com.cs9322.team05.server.dao.WordDao;
import com.cs9322.team05.server.impl.AdminServiceImpl;
import com.cs9322.team05.server.impl.AuthenticationServiceImpl;
import com.cs9322.team05.server.impl.GameServiceImpl;
import com.cs9322.team05.server.dao.UserDao;
import com.cs9322.team05.server.dao.GameDao;
import com.cs9322.team05.server.manager.SessionManager;
import com.cs9322.team05.server.manager.PendingGameManager;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableServer.*;
import java.util.logging.Logger;

import java.sql.Connection;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static final long startTime = System.currentTimeMillis();

    public static void main(String[] args) {
        try {
            if (args.length > 0){
                try{
                    int port = Integer.parseInt(args[0]);
                    if (port < 1024 || port > 65535){
                        logger.warning("Invalid port range: "+port);

                    }
                } catch (NumberFormatException e){
                    logger.warning("Invalid port number format");
                }
            }
            // 1) Initialize ORB
            ORB orb = ORB.init(args, null);

            // 2) Get and activate RootPOA
            POA rootPOA = POAHelper.narrow(
                    orb.resolve_initial_references("RootPOA")
            );
            rootPOA.the_POAManager().activate();

            // 3) Resolve the Naming Service
            NamingContextExt nameRoot = NamingContextExtHelper.narrow(
                    orb.resolve_initial_references("NameService")
            );
            // Connection validation
            Connection databaseConnection = DatabaseConnection.getConnection();
            if (databaseConnection == null || databaseConnection.isClosed()){
                throw new IllegalStateException("Database connection failed");
            }

            // 4) Database connection, DAOs, managers
            Connection connection = DatabaseConnection.getConnection();
            UserDao userDao = UserDao.getInstance(connection);
            GameDao gameDao = new GameDao(connection);
            WordDao.getInstance(connection);
            SessionManager sessionManager = SessionManager.getInstance();
            PendingGameManager pendingGameManager = new PendingGameManager();

            // 5) Instantiate servants
            AdminServiceImpl adminServant =
                    new AdminServiceImpl(sessionManager, userDao, gameDao);
            AuthenticationServiceImpl authServant =
                    new AuthenticationServiceImpl(sessionManager, userDao);
            GameServiceImpl gameServant = GameServiceImpl.getInstance(
                    sessionManager, gameDao, userDao, pendingGameManager
            );

            // 6) Convert servants to CORBA object references
            org.omg.CORBA.Object adminRef =
                    rootPOA.servant_to_reference(adminServant);
            org.omg.CORBA.Object authRef =
                    rootPOA.servant_to_reference(authServant);
            org.omg.CORBA.Object gameRef =
                    rootPOA.servant_to_reference(gameServant);

            AdminService adminService = AdminServiceHelper.narrow(adminRef);
            AuthenticationService authService = AuthenticationServiceHelper.narrow(authRef);
            GameService gameService = GameServiceHelper.narrow(gameRef);

            // Service registration log
            logger.info("Registering CORBA services...");

            // 7) Bind each service in the Naming Service
            bindService(nameRoot, "AdminService", adminService);
            bindService(nameRoot, "AuthenticationService", authService);
            bindService(nameRoot, "GameService", gameService);

            logger.info("Server initialied in " + (System.currentTimeMillis() - startTime + "ms"));
            // 8) Print startup confirmation
            System.out.println("=== Server is up and running ===");
            System.out.println("Bound services in NameService:");
            System.out.println("  • AdminService");
            System.out.println("  • AuthenticationService");
            System.out.println("  • GameService");

            // 9) Wait for client invocations
            orb.run();

        } catch (Exception e) {
            System.err.println("Server exception: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Binds or rebinds a CORBA object under the given name.
     */
    private static void bindService(NamingContextExt rootCtx,
                                    String name,
                                    org.omg.CORBA.Object ref)
            throws NotFound, CannotProceed, InvalidName {
        NameComponent[] path = rootCtx.to_name(name);
        try {
            rootCtx.bind(path, ref);
        } catch (AlreadyBound ex) {
            rootCtx.rebind(path, ref);
        }
    }
}
