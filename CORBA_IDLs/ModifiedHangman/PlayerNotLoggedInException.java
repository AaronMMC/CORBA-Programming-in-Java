package ModifiedHangman;


/**
* ModifiedHangman/PlayerNotLoggedInException.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ModifiedHangman/GameService.idl
* Thursday, May 15, 2025 9:19:59 PM SGT
*/

public final class PlayerNotLoggedInException extends org.omg.CORBA.UserException
{
  public String message = null;

  public PlayerNotLoggedInException ()
  {
    super(PlayerNotLoggedInExceptionHelper.id());
  } // ctor

  public PlayerNotLoggedInException (String _message)
  {
    super(PlayerNotLoggedInExceptionHelper.id());
    message = _message;
  } // ctor


  public PlayerNotLoggedInException (String $reason, String _message)
  {
    super(PlayerNotLoggedInExceptionHelper.id() + "  " + $reason);
    message = _message;
  } // ctor

} // class PlayerNotLoggedInException
