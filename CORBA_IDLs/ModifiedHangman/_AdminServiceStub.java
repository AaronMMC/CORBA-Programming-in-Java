package ModifiedHangman;


/**
* ModifiedHangman/_AdminServiceStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ModifiedHangman/AdminService.idl
* Thursday, May 15, 2025 9:19:34 PM SGT
*/

public class _AdminServiceStub extends org.omg.CORBA.portable.ObjectImpl implements ModifiedHangman.AdminService
{

  public void create_player (String username, String password, String token) throws ModifiedHangman.AdminNotLoggedInException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("create_player", true);
                $out.write_string (username);
                $out.write_string (password);
                $out.write_string (token);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:ModifiedHangman/AdminNotLoggedInException:1.0"))
                    throw ModifiedHangman.AdminNotLoggedInExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                create_player (username, password, token        );
            } finally {
                _releaseReply ($in);
            }
  } // create_player

  public void update_player (String username, String new_password, String token) throws ModifiedHangman.AdminNotLoggedInException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("update_player", true);
                $out.write_string (username);
                $out.write_string (new_password);
                $out.write_string (token);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:ModifiedHangman/AdminNotLoggedInException:1.0"))
                    throw ModifiedHangman.AdminNotLoggedInExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                update_player (username, new_password, token        );
            } finally {
                _releaseReply ($in);
            }
  } // update_player

  public void delete_player (String username, String token) throws ModifiedHangman.AdminNotLoggedInException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("delete_player", true);
                $out.write_string (username);
                $out.write_string (token);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:ModifiedHangman/AdminNotLoggedInException:1.0"))
                    throw ModifiedHangman.AdminNotLoggedInExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                delete_player (username, token        );
            } finally {
                _releaseReply ($in);
            }
  } // delete_player

  public ModifiedHangman.Player search_player (String keyword, String token) throws ModifiedHangman.AdminNotLoggedInException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("search_player", true);
                $out.write_string (keyword);
                $out.write_string (token);
                $in = _invoke ($out);
                ModifiedHangman.Player $result = ModifiedHangman.PlayerHelper.read ($in);
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:ModifiedHangman/AdminNotLoggedInException:1.0"))
                    throw ModifiedHangman.AdminNotLoggedInExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return search_player (keyword, token        );
            } finally {
                _releaseReply ($in);
            }
  } // search_player

  public ModifiedHangman.Player[] get_all_player (String token) throws ModifiedHangman.AdminNotLoggedInException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("get_all_player", true);
                $out.write_string (token);
                $in = _invoke ($out);
                ModifiedHangman.Player $result[] = ModifiedHangman.PlayerListHelper.read ($in);
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:ModifiedHangman/AdminNotLoggedInException:1.0"))
                    throw ModifiedHangman.AdminNotLoggedInExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return get_all_player (token        );
            } finally {
                _releaseReply ($in);
            }
  } // get_all_player

  public void set_waiting_time (int seconds, String token) throws ModifiedHangman.AdminNotLoggedInException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("set_waiting_time", true);
                $out.write_long (seconds);
                $out.write_string (token);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:ModifiedHangman/AdminNotLoggedInException:1.0"))
                    throw ModifiedHangman.AdminNotLoggedInExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                set_waiting_time (seconds, token        );
            } finally {
                _releaseReply ($in);
            }
  } // set_waiting_time

  public void set_round_duration (int seconds, String token) throws ModifiedHangman.AdminNotLoggedInException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("set_round_duration", true);
                $out.write_long (seconds);
                $out.write_string (token);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:ModifiedHangman/AdminNotLoggedInException:1.0"))
                    throw ModifiedHangman.AdminNotLoggedInExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                set_round_duration (seconds, token        );
            } finally {
                _releaseReply ($in);
            }
  } // set_round_duration

  public int get_waiting_time (String token) throws ModifiedHangman.AdminNotLoggedInException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("get_waiting_time", true);
                $out.write_string (token);
                $in = _invoke ($out);
                int $result = $in.read_long ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:ModifiedHangman/AdminNotLoggedInException:1.0"))
                    throw ModifiedHangman.AdminNotLoggedInExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return get_waiting_time (token        );
            } finally {
                _releaseReply ($in);
            }
  } // get_waiting_time

  public int get_round_duration (String token) throws ModifiedHangman.AdminNotLoggedInException
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("get_round_duration", true);
                $out.write_string (token);
                $in = _invoke ($out);
                int $result = $in.read_long ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                if (_id.equals ("IDL:ModifiedHangman/AdminNotLoggedInException:1.0"))
                    throw ModifiedHangman.AdminNotLoggedInExceptionHelper.read ($in);
                else
                    throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return get_round_duration (token        );
            } finally {
                _releaseReply ($in);
            }
  } // get_round_duration

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:ModifiedHangman/AdminService:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }

  private void readObject (java.io.ObjectInputStream s) throws java.io.IOException
  {
     String str = s.readUTF ();
//     com.sun.corba.se.impl.orbutil.IORCheckImpl.check(str, "ModifiedHangman._AdminServiceStub");
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     org.omg.CORBA.Object obj = orb.string_to_object (str);
     org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();
     _set_delegate (delegate);
   } finally {
     orb.destroy() ;
   }
  }

  private void writeObject (java.io.ObjectOutputStream s) throws java.io.IOException
  {
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     String str = orb.object_to_string (this);
     s.writeUTF (str);
   } finally {
     orb.destroy() ;
   }
  }
} // class _AdminServiceStub
