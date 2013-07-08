package poker;
import java.util.*;


public class Suit
{
  public static final int    NUM_SUIT = 4;
  public static final int CLUBS    = 0;
  public static final int DIAMONDS = 1;
  public static final int HEARTS   = 2;
  public static final int SPADES   = 3;

  private int _val;
  
  public Suit ()
  {
    _val = 0;
  }

  public Suit (int n)
  {
    if (n >= 0 && n < NUM_SUIT)
      _val = n;
    else
      _val = 0;
  }

  public Suit (char c)
  {
      switch (c)
	  {
	  case 'c':
	  case 'C':
	      _val = CLUBS;
	      return;
	  case 'd':
	  case 'D':
	      _val = DIAMONDS;
	      return;
	  case 'h':
	  case 'H':
	      _val = HEARTS;
	      return;
	  case 's':
	  case 'S':
	      _val = SPADES;
	      return;
	  default:
	      _val = -1;
      }
    return;
  }

  public Suit (Suit s)
  {
    _val = s._val;
  }

  public int value ()
  {
    return _val;
  }

  public boolean lessThan (Suit r)
  {
    if (_val < r._val)
      return true;
    return false;
  }

  public boolean greaterThan (Suit r)
  {
    if (_val > r._val)
      return true;
    return false;
  }

  public boolean equals (Suit r)
  {
    if (_val == r._val)
      return true;
    return false;
  }

  public String toString ()
  {
    switch (_val)
      {
      case 0:
	return "c";
      case 1:
	return "d";
      case 2:
	return "h";
      case 3:
	return "s";
      }
    return null;
  }

}



  


