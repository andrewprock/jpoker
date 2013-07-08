
package poker;
import java.util.*;
import utils.*;


public class Valuation
{
  public static final int NO_PAIR		       = 0;
  public static final int ONE_PAIR		       = 1;
  public static final int TWO_PAIR		       = 2;
  public static final int THREE_OF_A_KIND	       = 3;
  public static final int STRAIGHT		       = 4;
  public static final int FLUSH			       = 5;
  public static final int FULL_HOUSE		       = 6;
  public static final int FOUR_OF_A_KIND	       = 7;
  public static final int STRAIGHT_FLUSH	       = 8;

  private static final int NO_PAIR_MASK         = 0x01 << 23;
  private static final int ONE_PAIR_MASK        = 0x01 << 24;
  private static final int TWO_PAIR_MASK        = 0x01 << 25;
  private static final int THREE_OF_A_KIND_MASK = 0x01 << 26;
  private static final int STRAIGHT_MASK        = 0x01 << 27;
  private static final int FLUSH_MASK           = 0x01 << 28;
  private static final int FULL_HOUSE_MASK      = 0x01 << 29;
  private static final int FOUR_OF_A_KIND_MASK  = 0x01 << 30;
  private static final int STRAIGHT_FLUSH_MASK  = 0x01 << 31;

  public static final int NUM_RANKINGS                 = 9;
  public static final int NO_LOW = ONE_PAIR_MASK;
  //public static final int NUM_RANKINGS_DRAWS           =15;

  public static final String StringRanking[] = {
    "No Pair       ",
    "Pair          ",
    "Two Pair      ",
    "Trips         ",
    "Straight      ",
    "Flush         ",
    "Full House    ",
    "Quads         ",
    "Straight Flush"
  };



  /*
   *
   * The encoding is kind of tricky.  Basically it works like this:
   * 
   * bits [23,31] store a single bit which determines which valuation
   * the hand is.  bits [0,15] store valuation dependent info which
   * is a proper ordering for that valuation.  This is for quick
   * testing of the valuation (e.g. isFlush())
   *
   * Each valuation requires from 1 to 5 ranks to fully specify it.
   * e.g. straights need one rank (ten high striaght)
   *      flushes need five ranks (AK742 flush in spade)
   *      pairs need four ranks (AA pair with three kickers)
   *
   * if the valuation needs four or less ranks, they are stored in
   * four bit blocks with bits [0,3] representing the least significant
   * rank.
   *
   * if the valuation need five ranks, bits [0,12] are used to 
   * mark whether each rank is used, bit 12 corresponds to Ace, bit
   * zero correpsonds to 2.
   *
   * bug: suits cannont be stored without messing up flush ties.
   * bug: because straight flushes are handeld as flushes, not
   *      straights, they use the 13 bit encoding
   *     
   *
   */
  /*
   * old code
  public static String decode (int n)
  {
    StringBuffer str = new StringBuffer ();

    Debug.out ("Valuation.decode : " + n);

    for (int i=31; i>=0; i--)
      {
	if ((n & (0x01<<i)) > 0)
	  str.append ("1");
	else
	  str.append ("0");
	if (i%8==0)
	  str.append (" ");
      }
    str.append (" ");
    
    
    if ((n&NO_PAIR_MASK         ) > 0)
      {
	str.append("high card:       ");
	int k=0;
	for (int i=Rank.NUM_RANK-1; i>=0; i--)
	  {
	    if ((n&(0x01<<i)) > 0)
	      {
		str.append(" "+(new Rank(i)).toString());
		k++;
	      }
	    if (k>=5)
	      break;
	  }
      }
    if ((n&ONE_PAIR_MASK        ) > 0)
      {
	str.append("one pair:        ");
	str.append(" "+(new Rank( ((n>>12) & 0x0F)) ).toString());
	str.append(" "+(new Rank( ((n>> 8) & 0x0F)) ).toString());
	str.append(" "+(new Rank( ((n>> 4) & 0x0F)) ).toString());
	str.append(" "+(new Rank( ((n    ) & 0x0F)) ).toString());
      }
    if ((n&TWO_PAIR_MASK        ) > 0)
      {
	str.append("two pair:        ");
	str.append(" "+(new Rank( ((n>> 8) & 0x0F)) ).toString());
	str.append(" "+(new Rank( ((n>> 4) & 0x0F)) ).toString());
	str.append(" "+(new Rank( ((n    ) & 0x0F)) ).toString());
      }
    if ((n&THREE_OF_A_KIND_MASK ) > 0)
      {
	str.append("three of a kind: ");
	str.append(" "+(new Rank( ((n>> 8) & 0x0F)) ).toString());
	str.append(" "+(new Rank( ((n>> 4) & 0x0F)) ).toString());
	str.append(" "+(new Rank( ((n    ) & 0x0F)) ).toString());
      }
    if ((n&STRAIGHT_MASK        ) > 0)
      {
	str.append("straight:        ");
	str.append(" "+(new Rank( ((n    ) & 0x0F)) ).toString());
      }
    if ((n&FLUSH_MASK           ) > 0)
      {
	str.append("flush:           ");
	int k=0;
	for (int i=Rank.NUM_RANK-1; i>=0; i--)
	  {
	    if ((n&(0x01<<i)) > 0)
	      {
		str.append(" "+(new Rank(i)).toString());
		k++;
	      }
	    if (k>=5)
	      break;
	  }
      }
    if ((n&FULL_HOUSE_MASK      ) > 0)
      {
	str.append("full house:      ");
	str.append(" "+(new Rank( ((n>> 4) & 0x0F)) ).toString());
	str.append(" "+(new Rank( ((n    ) & 0x0F)) ).toString());
      }
    if ((n&FOUR_OF_A_KIND_MASK  ) > 0)
      {
	str.append("four of a kind:  ");
	str.append(" "+(new Rank( ((n>> 4) & 0x0F)) ).toString());
	str.append(" "+(new Rank( ((n    ) & 0x0F)) ).toString());
      }
    if ((n&STRAIGHT_FLUSH_MASK  ) > 0)
      {
	str.append("straight flush:  ");
	int k=0;
	for (int i=Rank.NUM_RANK-1; i>=0; i--)
	  {
	    if ((n&(0x01<<i)) > 0)
	      {
		str.append(" "+(new Rank(i)).toString());
		k++;
	      }
	    if (k>=1)
	      break;
	  }
      }

    return str.toString ();

  }
  */

  private static void addKickers (StringBuffer str, int n)
  {
    for (int i=Rank.NUM_RANK-1; i>=0; i--)
      if ((n&(0x01<<i)) > 0)
	str.append(" "+(new Rank(i)).toString());
  }
  private static void addTop (StringBuffer str, int n)
  {
    int i = (n >> 20) & 0x0F;
    str.append(" "+(new Rank(i)).toString());
  }
  private static void addBot (StringBuffer str, int n)
  {
    int i = (n >> 16) & 0x0F;
    str.append(" "+(new Rank(i)).toString());
  }

  public static String decode (int n)
  {
    StringBuffer str = new StringBuffer ();

    Debug.out ("Valuation.decode : " + n);

    for (int i=31; i>=0; i--)
      {
	if ((n & (0x01<<i)) > 0)
	  str.append ("1");
	else
	  str.append ("0");
	if (i%8==0)
	  str.append (" ");
      }
    str.append (" ");
    
    int val = n>>24;

    switch (val)
      {
      case NO_PAIR:               
	str.append("high card:       ");
	addKickers (str,n);
	break;

      case ONE_PAIR:              
	str.append("one pair:        ");
	addTop (str,n);
	addKickers (str,n);
	break;

      case TWO_PAIR:              
	str.append("two pair:        ");
	addTop (str,n);
	addBot (str,n);
	addKickers (str,n);
	break;

      case THREE_OF_A_KIND:       
	str.append("three of a kind: ");
	addTop (str,n);
	addKickers (str,n);
	break;

      case STRAIGHT:              
	str.append("straight:        ");
	addTop (str,n);
	break;

      case FLUSH:                 
	str.append("flush:           ");
	addKickers (str,n);
	break;

      case FULL_HOUSE:            
	str.append("full house:      ");
	addTop (str,n);
	addBot (str,n);
	break;

      case FOUR_OF_A_KIND:        
	str.append("four of a kind:  ");
	addTop (str,n);
	addKickers (str,n);
	break;

      case STRAIGHT_FLUSH:        
	str.append("straight flush:  ");
	addTop (str,n);
	break;

      }
    return str.toString ();

  }

    public static int valueLow (int n)
    {
	if ((n&NO_PAIR_MASK         ) > 0)
	    {
		int k=0;
		for (int i=Rank.NUM_RANK-1; i>=0; i--)
		    {
			if ((n&(0x01<<i)) > 0)
			    {
				k++;
				return i+2;
			    }
			if (k>=5)
			    break;
		    }
	    }
	return 0;
    }

  public static int value (int val)
  {
    return (val>>24) & 0x0F;
  }
    



}

