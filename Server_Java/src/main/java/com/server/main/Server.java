package com.server.main;

import ModifiedHangman.*;
import com.server.dao.DatabaseConnection;
import com.server.dao.WordDao;
import com.server.impl.AdminServiceImpl;
import com.server.impl.AuthenticationServiceImpl;
import com.server.impl.GameServiceImpl;
import com.server.dao.UserDao;
import com.server.dao.GameDao;
import com.server.manager.SessionManager;
import com.server.manager.PendingGameManager;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableServer.*;

import java.sql.Connection;

public class Server {

    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);

            POA rootPOA = POAHelper.narrow(
                    orb.resolve_initial_references("RootPOA")
            );
            rootPOA.the_POAManager().activate();

            NamingContextExt nameRoot = NamingContextExtHelper.narrow(
                    orb.resolve_initial_references("NameService")
            );

            Connection connection = DatabaseConnection.getConnection();
            UserDao userDao = UserDao.getInstance(connection);
            GameDao gameDao = new GameDao(connection);
            WordDao.getInstance(connection);
            SessionManager sessionManager = SessionManager.getInstance();
            PendingGameManager pendingGameManager = new PendingGameManager();

            AdminServiceImpl adminServant =
                    new AdminServiceImpl(sessionManager, userDao, gameDao);
            AuthenticationServiceImpl authServant =
                    new AuthenticationServiceImpl(sessionManager, userDao);
            GameServiceImpl gameServant = GameServiceImpl.getInstance(
                    sessionManager, gameDao, userDao, pendingGameManager
            );

            org.omg.CORBA.Object adminRef =
                    rootPOA.servant_to_reference(adminServant);
            org.omg.CORBA.Object authRef =
                    rootPOA.servant_to_reference(authServant);
            org.omg.CORBA.Object gameRef =
                    rootPOA.servant_to_reference(gameServant);

            AdminService adminService = AdminServiceHelper.narrow(adminRef);
            AuthenticationService authService = AuthenticationServiceHelper.narrow(authRef);
            GameService gameService = GameServiceHelper.narrow(gameRef);

            bindService(nameRoot, "AdminService", adminService);
            bindService(nameRoot, "AuthenticationService", authService);
            bindService(nameRoot, "GameService", gameService);

            System.out.println("=== Server is up and running ===");
            System.out.println("Bound services in NameService:");
            System.out.println("  • AdminService");
            System.out.println("  • AuthenticationService");
            System.out.println("  • GameService");

            orb.run();

        } catch (Exception e) {
            System.err.println("Server exception: " + e);
            e.printStackTrace();
        }
    }


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
