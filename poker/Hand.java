package poker;
import utils.*;
import java.util.*;


public class Hand implements Cloneable
{
  public static final int NO_PAIR               = Valuation.NO_PAIR          ;
  public static final int ONE_PAIR              = Valuation.ONE_PAIR         ;
  public static final int TWO_PAIR              = Valuation.TWO_PAIR         ;
  public static final int THREE_OF_A_KIND       = Valuation.THREE_OF_A_KIND  ;
  public static final int STRAIGHT              = Valuation.STRAIGHT         ;
  public static final int FLUSH                 = Valuation.FLUSH            ;
  public static final int FULL_HOUSE            = Valuation.FULL_HOUSE       ;
  public static final int FOUR_OF_A_KIND        = Valuation.FOUR_OF_A_KIND   ;
  public static final int STRAIGHT_FLUSH        = Valuation.STRAIGHT_FLUSH   ;

  private static final long NO_PAIR_MASK         = 0x01 << 21;
  private static final long ONE_PAIR_MASK        = 0x01 << 22;
  private static final long TWO_PAIR_MASK        = 0x01 << 23;
  private static final long THREE_OF_A_KIND_MASK = 0x01 << 24;
  private static final long STRAIGHT_MASK        = 0x01 << 25;
  private static final long FLUSH_MASK           = 0x01 << 26;
  private static final long FULL_HOUSE_MASK      = 0x01 << 27;
  private static final long FOUR_OF_A_KIND_MASK  = 0x01 << 28;
  private static final long STRAIGHT_FLUSH_MASK  = 0x01 << 29;

  
  //public static final int FLUSH_DRAW            = Valuation.FLUSH_DRAW         ;
  //public static final int STRAIGHT_DRAW         = Valuation.STRAIGHT_DRAW      ;

  public static final int NUM_HAND_CARDS	= 5;
  public static final int MAX_CARDS		=10;
  
  int _cards[];
  int _cardsDelt;
  int _intval;
  int _card_hash[];
  int _rankbits;
  int _suithash;		// need at least  4 suits * 4 bits = 16 bits
  long _Rankhash;		// need at least 13 ranks * 4 bits = 52 bits 
				// each 4 bits stores the total number of
				// cards for a particular rank
  //int _rankhash[] = new int[Rank.NUM_RANK];

  /*
   *
   *		Various constructors and clone
   *
   *		Hand ()
   *		Hand (int, int)
   *		Hand (int [])
   *		Hand (String)
   */

  public Hand () 
  {
    _cards = new int[7];
    _cardsDelt = 0;
    _intval = 0;
    _card_hash = new int[4];
    _rankbits = 0;
    _suithash = 0;
    _Rankhash = 0;
  }

  public void copy (Hand h)
  {
    _cardsDelt      = h._cardsDelt;
    for (int i=0; i<_cardsDelt; i++)
      _cards[i]     = h._cards[i];
    _intval         = h._intval;
    for (int i=0; i<_card_hash.length; i++)
      _card_hash[i] = h._card_hash[i];
    _rankbits       = h._rankbits;
    _Rankhash       = h._Rankhash;
    _suithash       = h._suithash;
  }

  public Object clone ()
  {
    Hand h = new Hand ();
    h.copy (this);
    return h;
  }
  
  public Hand (int i, int j)
  {
    this ();

    _cards[0] = i;
    _cards[1] = j;
    _cardsDelt = 2;
  }

  public Hand (int [] c)
  {
    this ();

    if (c.length == 7)
      _cards = c;
    else
      {
	_cards = new int[7];
	for (int i=0; i<c.length; i++)
	  _cards[i] = c[i];
      }
    _cardsDelt = c.length;
  }

  /**
   * The string which is passed in must contain
   * standard character card encodings [2-9TJQK][cdhs]
   * catenated with no spaces in between.  All valid
   * input strings will have an even number of characters.
   */
  public Hand (String s)
  {
    this ();

    StringTokenizer st = new StringTokenizer (s);
    while (st.hasMoreTokens())
      {
	String str = st.nextToken();
	for (int i=0; i<str.length(); i+=2)
	  addCard (str.substring (i));
      }
  }

  /*
   *
   *	UTILITY FUNCTIONS
   *
   *
   */

  public void setLength (int n)
  {
    _cardsDelt = n;
  }

  //    public Hand (int n) throws NumberOfCardsException
  //    {
  //      //_val = null;
  //      _intval = 0;
  //      if (n>MAX_CARDS)
  //        {
  //  	throw new NumberOfCardsException ("To Many Cards in Hand");
  //        }
  //      if (n<=0)
  //        {
  //  	throw new NumberOfCardsException ("To Few Cards in Hand");
  //        }
  //      _cards = new int[n];
  //    }

  /*
   *
   *
   *	Simple Utilities and interfaces
   *
   *
   */

  public void addCard (int n)
  {
    //System.out.println ("addCard: :" + this + ": " + (new Card (n)));
    //_val = null;
    if (n >= 0 && n < Deck.NUM_CARDS)
      {
	_intval = 0;
	_cardsDelt++;
	_cards[_cardsDelt-1] = n;
      }
  }

  public void addCard (Card c)
  {
    addCard (c.value());
  }

  /**
   * These functions doens't do any kind of bounds
   * checking so it's use is DANGEROUS
   */
  public void addcard (int c1)
  {
    _cards[_cardsDelt++] = c1;
  }
  public void addcards (int c1, int c2)
  {
    _cards[_cardsDelt++] = c1;
    _cards[_cardsDelt++] = c2;
  }

  public void addCard (String s)
  {
    addCard (new Card (s));
  }

  /**
   * This is an unsafe version of addHand, it does not check if the
   * card already exists in the hand.
   *
   * Normally this version should be avoided except in portions of the
   * code where fast evaluation need to occur.
   *
   * */
  public void addhand (Hand h)
  {
    try 
      {
	for (int i=0; i<h._cardsDelt; i++)
	  {
	    _cards[_cardsDelt] = h._cards[i];
	    _cardsDelt++;
	  }
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
	_cardsDelt = 7;
      }
  }
  /**
   * Empty out the hand
   */
  public void clear ()
  {
    _intval = 0;
    _cardsDelt = 0;
  }

      
  /**
   * A safer version of addHand, checks for duplicate cards
   */
  public void addHand (Hand h)
  {
    if (h==null)
      return;  
    for (int i=0; i<h._cardsDelt; i++)
      {
	try
	  {
	    boolean flag_add = true;
	    for (int j=0; j<_cardsDelt; j++)
	      if (_cards[j] == h._cards[i])
		flag_add = false;
	    if (flag_add)
	      addCard (h._cards[i]);
	  }
	catch (ArrayIndexOutOfBoundsException e)
	  {
	    //System.err.println ("Hand.java : " +h + " : " + e);
	    _cardsDelt = 7;
	  }
      }
  }
  
  public int getNumCards ()
  {
    return _cardsDelt;
  }

  /**
   * return the card in position i
   */
  public Card getCard (int i)
  {
    return new Card(_cards[i]);
  }

  /**
   * return the int value of the card in position i
   */
  public int getcard (int i)
  {
    return _cards[i];
  }

  /**
   * No bounds checking !!!
   */
  public void setcards (int [] c,int n)
  {
    for (int i=0; i<n; i++)
      _cards[i] = c[i];
    _cardsDelt = n;
  }

  public String toString ()
  {
    int i = 0;
    StringBuffer sb = new StringBuffer ();
    while (i < _cards.length && i<_cardsDelt)
      {
	Card c = new Card (_cards[i]);
	if (i == _cardsDelt-1)
	  sb.append (c.toString ());
	else
	  sb.append (c.toString () + " ");
	i++;
      }
    return sb.toString ();
  }



  /*
   *
   *
   *		Old hand evaluation code.  The evaluate1
   *		function should always provide the correct
   *		answer to any evaluation.
   *
   *		The new code uses the HandEvaluator class
   *		for all evaluation.  A hook is provided
   *		below for backwards compatability.
   *
   *
   */



  /** there is an invariant that the cards in the hand
   * must be consecutivly correct [0,52) starting with _cards[0],
   *
   * The value returned is an integer value which uniuqely determines
   * the value of the hand, such that there is a total ordering on
   * all hands.
   */
  public int evaluate ()
  {
    return HandEvaluator.evaluate (this);
  }
  public int evaluate1 ()
  {
    if (_intval==0)
      {
	hashCards ();

	if (evaluateFlush())
	  return _intval;
	if (evaluateStraight())
	  return _intval;

	evaluatePairing();
      }
    return _intval;
  }

  /*
   * In order to comile the JNI part of this
   * code:
   *
   * 1) recompile this file: % javac poker/Hand.java
   * 2) use javah on class:  % javah poker.Hand
   * 3) move to appropriate dir:
   */
  /*
  public native void evaluateJNI ();     
  static { System.loadLibrary("jni");  }

  public int evaluateC ()
  {
//      System.out.println ("evaluateC");
//      _intval = 0;
//      if (_intval==0)
      {
	evaluateJNI ();
//  	hashCards ();
//  	if (evaluateFlush())
//  	  return _intval;
//  	if (evaluateStraight())
//  	  return _intval;
//  	evaluatePairing();
      }
    return _intval;
  }
  */

  public int evaluateLow (int n)
  {
    hashCards ();		// probably wasting time here, oh well
    int hash = _card_hash[0] | _card_hash[1] | 
      _card_hash[2] | _card_hash[3];
	
    int lowcards = 0;
    int result = 0;

    // first check for the ace
    if ((hash & (0x01 << 12)) > 0)
      {
	lowcards ++;
	//result |= (0x01 << 12);
      }

    for (int i=0; i<n-1; i++)
      {
	if ((hash & (0x01 << i)) > 0)
	  {
	    lowcards ++;
	    result |= (0x01 << i);
	  }
	if (lowcards >= 5)
	  break;
      }
    if (lowcards == 5)
      {
	result ^= NO_PAIR_MASK;
	result ^= NO_PAIR << 16;
	return result;
      }
    return Valuation.NO_LOW;
  }

  private void hashCards ()
  {
    //Debug.out (".");
    // the card_hash array is sorted by suits
    if (_card_hash == null)
      _card_hash = new int [4];
    else
      _card_hash[0] = 
	_card_hash[1] = 
	_card_hash[2] = 
	_card_hash[3] = 0;

    //    	System.arraycopy (_suitzero, 0, _suithash, 0, _suitzero.length);
    //    	System.arraycopy (_rankzero, 0, _rankhash, 0, _rankzero.length);

    _suithash = 0;
    //_rankhash = new int[Rank.NUM_RANK];
    _Rankhash = 0;
//      System.out.println (this);
    for (int i=0; i<_cardsDelt; i++)
      {
	//Debug.out (_cards[i]%13 + " " +_cards[i]/13);
	int tmp_suit = _cards[i]/13;
	int tmp_rank = _cards[i]%13;
	_suithash += (0x01<<(tmp_suit*4));
	//_rankhash [(int)tmp_rank]++;
	_Rankhash += ((long)0x01<<(tmp_rank*4));
	//System.out.println (tmp_rank + " " +((long)0x01<<(tmp_rank*4)));
	_card_hash[tmp_suit] |= (0x01<<tmp_rank);
      }
//      for (int i=12; i>=0; i--)
//        System.out.print (_rankhash[i]);
//      Format.print (System.out, "\n%o\n",_Rankhash);
  }

  private boolean evaluateFlush()
  {
    //Debug.out ("evaluateFlush");
    int suitindex=-1;
    for (int i=0; i<Suit.NUM_SUIT; i++)
      {
	// note: this assumes a hand can hold at most
	// one flush
	//if (_suithash[i] >= NUM_HAND_CARDS)
	if (((_suithash>>(4*i)) & 0xF) >= NUM_HAND_CARDS)
	  {
	    suitindex = i;
	    break;
	  }
      }

    if (suitindex >= 0)
      {
	// we *know* we have some kind of a flush

	// note: we aren't just looking for
	// straight flushes here, we are also
	// ordering the flushes by card values
	int run = 0;
	int start = 0;
	for (int i=Rank.NUM_RANK-1,j=0; i>=0; i--)
	  {
	    if ((_card_hash[suitindex] & (0x01<<i)) > 0)
	      {
		run++;
				// 		if (j<_val.orderedRank.length)
				// 		  _val.orderedRank[j] = i;
		j++;
		_intval ^= (_card_hash[suitindex] & (0x01<<i));
	      }
	    else 
	      run = 0;
	    if (run == 5)
	      {
		_intval ^= STRAIGHT_FLUSH_MASK;
		_intval ^= STRAIGHT_FLUSH << 16;
		return true;
	      }
	  }

	if (run == 4 && (_card_hash[suitindex] & (0x01<<Rank.NUM_RANK-1)) > 0)
	  {
	    _intval ^= STRAIGHT_FLUSH_MASK;
	    _intval ^= STRAIGHT_FLUSH << 16;
	    return true;
	  }

	_intval ^= FLUSH << 16;
	_intval ^= FLUSH_MASK;
	return true;
      }

    return false;
  }

  //    public boolean openEndedStraight ()
  //    {
  //      return false;
  //    }

  private boolean evaluateStraight ()
  {
    int hash = _card_hash[0] | _card_hash[1] | 
      _card_hash[2] | _card_hash[3];
    int val = evaluateStraightHashMash (hash);
    if (val >= 0)
      {
	_intval ^= STRAIGHT_MASK;
	_intval ^= STRAIGHT << 16;
	_intval ^= val;
	return true;
      }
    return false;
  }
  
  private int evaluateStraightHashMash (int hash)
  {
    // we have to do this in order otherwise we get
    // bit by 6 straights and 7 straights
    if ((hash & 0x1F00)==0x1F00)
      return 12;
    if ((hash & 0xF80)==0xF80)
      return 11;
    if ((hash & 0x7C0)==0x7C0)
      return 10;
    if ((hash & 0x3E0)==0x3E0)
      return 9;
    if ((hash & 0x1F0)==0x1F0)
      return 8;
    if ((hash & 0xF8)==0xF8)
      return 7;
    if ((hash & 0x7C)==0x7C)
      return 6;
    if ((hash & 0x3E)==0x3E)
      return 5;
    if ((hash & 0x1F)==0x1F)
      return 4;
    if ((hash & 0x100F)==0x100F)
      return 3;
    
    return -1;
  }

  private boolean evaluatePairing ()
  {
    int toprank = 0;
    int botrank = 0;
    int topind  = 0;
    int botind  = 0;
    
    for (int i=Rank.NUM_RANK-1; i>=0; i--)
      {
	//int rankhashi = _rankhash[i];
	int rankhashi = (int)((_Rankhash>>(4*i)) & 0xF);
	if (rankhashi > toprank)
	  {
	    botrank = toprank;
	    botind  = topind;
	    toprank = rankhashi;
	    topind  = i;
	  }
	else if (rankhashi > botrank)
	  {
	    botrank = rankhashi;
	    botind  = i;
	  }
      }

    switch (toprank)
      {
      case 4:
	_intval ^= FOUR_OF_A_KIND_MASK;
	_intval ^= FOUR_OF_A_KIND << 16;
	_intval ^= (topind << 4);
	_intval ^=  botind;
	//Debug.out ("fish " + this + " -r");
	return true;
	//break;

      case 3:
	if (botrank > 1)
	  {
	    _intval ^= FULL_HOUSE_MASK;
	    _intval ^= FULL_HOUSE << 16;
	    _intval ^= (topind << 4);
	    _intval ^=  botind;
	    return true;
	  }
	else
	  {
	    _intval ^= THREE_OF_A_KIND_MASK;
	    _intval ^= THREE_OF_A_KIND << 16;
	    _intval ^= (topind << 8);
	    //_rankhash[topind] = 0;  // hack so 3 pair works
	    _Rankhash -= (((_Rankhash>>(4*topind)) & 0xF)<<(4*topind));
	    fillHand (1,3);
	  }
	break;

      case 2:
	if (botrank > 1)
	  {
	    _intval ^= TWO_PAIR_MASK;
	    _intval ^= TWO_PAIR << 16;
	    _intval ^= (topind << 8);
	    _intval ^= (botind << 4);
	    //_rankhash[topind] = _rankhash[botind] = 0; // hack so 3 pr works
	    _Rankhash -= (((_Rankhash>>(4*topind)) & 0xF)<<(4*topind));
	    _Rankhash -= (((_Rankhash>>(4*botind)) & 0xF)<<(4*botind));
	    fillHand (2,3);
	  }
	else
	  {
	    _intval ^= ONE_PAIR_MASK;
	    _intval ^= ONE_PAIR << 16;
	    _intval ^= (topind << 12);
	    //_rankhash[topind] = 0;  // hack so 3 pair works
	    _Rankhash -= (((_Rankhash>>(4*topind)) & 0xF)<<(4*topind));
	    fillHand(1,4);
	  }
	break;

      default:
	_intval ^= NO_PAIR_MASK;
	_intval ^= NO_PAIR << 16;
	fillHand(0,5);
	return false;
      }
    return false;

  }

  private void fillHand(int n, int tot)
  {
    for (int i=Rank.NUM_RANK-1; i>=0; i--)
      {
	//int rankhashi = _rankhash[i];
	int rankhashi = (int)((_Rankhash>>(4*i)) & 0xF);
	if (rankhashi > 0)
	  if (n < tot)
	    {
	      if (tot==5)
		_intval ^= (0x01 << i);
	      else
		_intval ^= (i<< (4*(tot-n-1)));
	      n++;
	    }
	  else
	    break;
      }
  }




  /*
   *
   *
   * Beginning of static functions
   *
   */

  /**
   * Used to generate code for a giant switch statement for
   * evaluating straights.  It wasn't very efficient in java.
   */
  private static void makeStraightCode ()
  {
    Debug.debug = true;

    System.out.println("    switch (n)\n    {");

    int max = 0;

    int strval [] = new int[8192];

    for (int i=0; i<8192; i++)
      {
	generateStraightValue (strval,i);
      }

    for (int i=4; i<13; i++)
      {
	for (int j=0; j<8192; j++)
	  {
	    if (strval[j]==i)
	      {
		System.out.println ("      case " + j + ":");
	      }
	  }
	System.out.println ("        return " + i + ";");
      }

    System.out.println ("    }");
  }

  private static void generateStraightValue (int ar[], int n)
  {
    int run=0;
    int start=0;
    for (int i=Rank.NUM_RANK-1; i>=0; i--)
      {
	if ((n & 0x01<<i) > 0)
	  run++;
	else
	  run = 0;
	if (run == 5)
	  ar[n] = i+4;
      }

    if (run == 4 && (n & 0x01<<(Rank.NUM_RANK-1)) > 0)
      {
	ar[n] = 4;
      }
  }

  /*
   *
   *	TEST DRIVER
   *
   */
  // if a hand in the array is set to true, then
  // it has a value of at least a PAIR
  /*
    public static void setGood (boolean[] good, Hand [] h)
    {
    for (int i=0; i<h.length; i++)
    {
    //Debug.out (h[i].evaluate().toString());
    if (h[i].evaluate() > NO_PAIR)
    good[i]=true;
    else
    good[i]=false;
    }
    }



    public static void main (String args[]) throws Exception
    {
    Debug.debug = true;

    Hand h = new Hand ();
    h.addCard ("9c");
    h.addCard ("8d");
    h.addCard ("9d");
    h.addCard ("Qh");
    h.addCard ("Jd");
    h.addCard ("Tc");
    h.addCard ("Kc");

    Debug.out (h.evaluate ());

    Debug.out (Valuation.decode (h.evaluate()));
      
    }

    public static void main2 (String args[]) throws Exception
    {
    Debug.debug = true;
    int CARDS = 7;
    int OPEN  = 4;
    int HANDS = 7;
    int PLAYERS = 8;
    int TRIALS = 500;
    
    int c1 = (new Card ("As")).value();
    int c2 = (new Card ("Kc")).value();
    int c3 = (new Card ("9s")).value();
    int c4 = (new Card ("8s")).value();

    StudTable st = new StudTable ();
    st.getPlayer(5).fold();
    st.deck.removeCard (c1);
    st.deck.removeCard (c2);
    //     st.deck.removeCard (c3);
    //     st.deck.removeCard (c4);

    int winnah[] = new int [PLAYERS];
    for (int i=0; i<TRIALS; i++)
    {
    Hand test = new Hand();
    test.addCard (c1);
    test.addCard (c2);
    //  	test.addCard (c3);
    //  	test.addCard (c4);

    st.newGame ();
    st.getPlayer(5).fold();
    st.setHand (0,test);
    st.deck.shuffle ();
    st.dealToRiver ();
    st.determineWinningHand ();
    //st.print ();

    winnah[st.getWinningPlayer()]++;

    // 	if (TRIALS > 0)
    // 	  {
    // 	    Debug.out (st.getPlayer(0).hand + " " + st.getPlayer(0).hand.evaluate());
    // 	    Debug.out (st.getPlayer(st.getWinningPlayer()).hand 
    // 		       + " " + st.getPlayer(st.getWinningPlayer()).hand.evaluate());
    // 	    Debug.out ("\n");
    // 	  }
    }

    for (int i=0; i<winnah.length; i++)
    {
    double pct = (double)winnah[i]/(double)TRIALS;
    double odds = 0;
    if (pct>0) odds = 1/pct - 1;
    Format.print (System.out, "player: %2d",i);
    Format.print (System.out, " : %5d", winnah[i]);
    Format.print (System.out, " : %5.2f\n", odds);
    }
    }

    public static void main3 (String args[]) throws Exception
    { 
    int counts[] = new int[10];
    //for (int i=2; i<52; i++)
    for (int j=3; j<52; j++)
    for (int k=j+1; k<52; k++)
    for (int l=k+1; l<52; l++)
    for (int m=l+1; m<52; m++)
    {
    Hand h = new Hand ();
    h.addCard (0);
    h.addCard (1);
    h.addCard (2);
    h.addCard (j);
    h.addCard (k);
    h.addCard (l);
    h.addCard (m);
    //counts[h.evaluate().value()]++;
    //System.out.println (h.evaluate().value());
    //System.out.println (h);
    }

    System.out.println("");
    int sum = 0;
    for (int i=0; i<9; i++)
    {
    Format.print (System.out, "%8d",counts[i]);
    sum += counts[i];
    }
    System.out.println(" : " + sum);
    }
  */

}

