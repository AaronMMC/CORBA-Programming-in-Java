package ModifiedHangman;


/**
* ModifiedHangman/AdminServiceHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ModifiedHangman/AdminService.idl
* Thursday, May 15, 2025 9:19:34 PM SGT
*/

abstract public class AdminServiceHelper
{
  private static String  _id = "IDL:ModifiedHangman/AdminService:1.0";

  public static void insert (org.omg.CORBA.Any a, ModifiedHangman.AdminService that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static ModifiedHangman.AdminService extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (ModifiedHangman.AdminServiceHelper.id (), "AdminService");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static ModifiedHangman.AdminService read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_AdminServiceStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, ModifiedHangman.AdminService value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static ModifiedHangman.AdminService narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof ModifiedHangman.AdminService)
      return (ModifiedHangman.AdminService)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      ModifiedHangman._AdminServiceStub stub = new ModifiedHangman._AdminServiceStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static ModifiedHangman.AdminService unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof ModifiedHangman.AdminService)
      return (ModifiedHangman.AdminService)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      ModifiedHangman._AdminServiceStub stub = new ModifiedHangman._AdminServiceStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
