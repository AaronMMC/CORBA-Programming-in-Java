package ModifiedHangman;


/**
* ModifiedHangman/Player.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ModifiedHangman/GameService.idl
* Thursday, May 15, 2025 9:19:59 PM SGT
*/

public final class Player implements org.omg.CORBA.portable.IDLEntity
{
  public String username = null;
  public String password = null;
  public int wins = (int)0;

  public Player ()
  {
  } // ctor

  public Player (String _username, String _password, int _wins)
  {
    username = _username;
    password = _password;
    wins = _wins;
  } // ctor

} // class Player
