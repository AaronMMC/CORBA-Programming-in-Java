package ModifiedHangman;

/**
* ModifiedHangman/RoundResultHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ModifiedHangman/GameService.idl
* Thursday, May 15, 2025 9:19:59 PM SGT
*/

public final class RoundResultHolder implements org.omg.CORBA.portable.Streamable
{
  public ModifiedHangman.RoundResult value = null;

  public RoundResultHolder ()
  {
  }

  public RoundResultHolder (ModifiedHangman.RoundResult initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = ModifiedHangman.RoundResultHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    ModifiedHangman.RoundResultHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return ModifiedHangman.RoundResultHelper.type ();
  }

}
