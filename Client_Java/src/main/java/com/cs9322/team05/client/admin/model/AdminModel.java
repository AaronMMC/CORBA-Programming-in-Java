package com.cs9322.team05.client.admin.model;

import ModifiedHangman.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;

public class AdminModel {

    private static AdminService adminServiceImpl;

    public AdminModel() {
        try {
            ORB orb = ORB.init(args, null);

            org.omg.CORBA.Object nameServiceObj = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(nameServiceObj);

            String name = "AdminService";
            adminServiceImpl = AdminServiceHelper.narrow(ncRef.resolve_str(name));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}