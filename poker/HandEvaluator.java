package poker;
import utils.*;
//import poker.sim.HandDistribution;
import java.util.*;


public class HandEvaluator
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

  /*
   * old stuff
  private static final int NO_PAIR_MASK         = 0x01 << 23;
  private static final int ONE_PAIR_MASK        = 0x01 << 24;
  private static final int TWO_PAIR_MASK        = 0x01 << 25;
  private static final int THREE_OF_A_KIND_MASK = 0x01 << 26;
  private static final int STRAIGHT_MASK        = 0x01 << 27;
  private static final int FLUSH_MASK           = 0x01 << 28;
  private static final int FULL_HOUSE_MASK      = 0x01 << 29;
  private static final int FOUR_OF_A_KIND_MASK  = 0x01 << 30;
  private static final int STRAIGHT_FLUSH_MASK  = 0x01 << 31;
  */
  private static final int NO_PAIR_MASK         = NO_PAIR		<< 24;
  private static final int ONE_PAIR_MASK        = ONE_PAIR		<< 24;
  private static final int TWO_PAIR_MASK        = TWO_PAIR		<< 24;
  private static final int THREE_OF_A_KIND_MASK = THREE_OF_A_KIND	<< 24;
  private static final int STRAIGHT_MASK        = STRAIGHT		<< 24;
  private static final int FLUSH_MASK           = FLUSH			<< 24;
  private static final int FULL_HOUSE_MASK      = FULL_HOUSE		<< 24;
  private static final int FOUR_OF_A_KIND_MASK  = FOUR_OF_A_KIND	<< 24;
  private static final int STRAIGHT_FLUSH_MASK  = STRAIGHT_FLUSH	<< 24;

  public static final int NUM_HAND_CARDS	= 5;
  public static final int MAX_CARDS		=10;


  private static int _cards[];
  private static int _cardsDelt;
  private static int _intval;
  private static long _rankhash;// need at least 13 ranks * 4 bits = 52 bits 
				// each 4 bits stores the total number of
				// cards for a particular rank
  private static int _suithash; // need at least  4 suits * 4 bits = 16 bits
  //    private static int _suitzero[] = new int[Suit.NUM_SUIT];
  //    private static int _rankzero[] = new int[Rank.NUM_RANK];

  private static int _card_hash[] = new int[4];
  private static int _rankbits;

  /*
   *
   *		Standard "fast" evaluation code
   *
   *
   */

  /** there is an invariant that the cards in the hand
   * must be consecutivly correct [0,52) starting with _cards[0],
   *
   * The value returned is an integer value which uniuqely determines
   * the value of the hand, such that there is a total ordering on
   * all hands.
   *
   * This code has been devloped fairly independently, but there
   * have been significant "inspirations" from other sources.  In
   * particular the liberal use of lookup tables and the newer
   * method of evaluating "pairing" hands was first encountered
   * in the poker_eval source.
   */
  public static int evaluate (Hand h)
  {
    // we short circuit the evaluation if we've done it before
    if (h._intval > 0)
      return h._intval;

    h._intval = doFullEvaluation (h);

    return _intval;
  }
  /**
   * do the actual evaluation, wrapped up since we might
   * want to store some of the residual evaluation data.
   */
  public static int doFullEvaluation (Hand h)
  {
    _intval = 0;
    _cards = h._cards;
    _cardsDelt = h._cardsDelt;

    hashCards ();
    int nranks = HandEvaluatorTables.nRanksTable[_rankbits];
    if (nranks >= 5)
      {
      	if (evaluateFlush())
      	  return _intval;
	if (evaluateStraight())
    	  return _intval;
      }
    evaluatePairing();
    return _intval;
  }
  private static int doFlushEvaluation (Hand h)
  {
    _intval = 0;
    _cards = h._cards;
    _cardsDelt = h._cardsDelt;

    hashCards ();
    if (HandEvaluatorTables.nRanksTable[_rankbits] >= 5)
      if (evaluateFlush())
	return _intval;
    return _intval;
  }

  /**
   * A "fast" evaluator which stores no transient data in
   * the input hand.
   *
   * Note:	while we store some flush into in the hash data
   *		we don't actually use it for anything here.
   *		However, that data may be used for partial evaluation.
   */
  private static int evaluateRanksTwoBits (Hand h, int rbits)
  {
    _intval = 0;
    _rankhash = 0;
    _rankbits = 0;
    _cardsDelt = h._cardsDelt;
    _cards = h._cards;

    hashCards ();	// hash the real cards
    _cardsDelt += 2;	// add the "rank only" cards

    // we need to set the hashing data for the two ranks that
    // rbits represents
    _rankbits |= rbits;
    int toprank = HandEvaluatorTables.topRankTable [rbits];
    if (HandEvaluatorTables.nRanksTable [rbits]==1)
      {
	// one bit set? need TWO of that rank
	_rankhash += 2*((long)0x01<<(toprank*4));
      }
    else
      {
	_rankhash += ((long)0x01<<(toprank*4));
	toprank = HandEvaluatorTables.topRankTable[rbits-(0x01<<toprank)];
	_rankhash += ((long)0x01<<(toprank*4));
      }

    if (HandEvaluatorTables.nRanksTable[_rankbits] >= 5)
      if (evaluateStraight())
	return _intval;

    evaluatePairing();
    return _intval;
  }

  /**
   * A "fast" evaluator which stores no transient data in
   * the input hand.
   *
   * Note:	while we store some flush into in the hash data
   *		we don't actually use it for anything here.
   *		However, that data may be used for partial evaluation.
   */
  private static int evaluateRanksFourBits (Hand h, int rbits, int sbits)
  {
    _intval = 0;
    _rankhash = 0;
    _rankbits = 0;
    _cardsDelt = h._cardsDelt;
    _cards = h._cards;

    hashCards ();	// hash the real cards
    _cardsDelt += 4;	// add the "rank only" cards

    // we need to set the hashing data for the two ranks that
    // rbits represents
    _rankbits |= rbits | sbits;
    int toprank = 0;

    // add the rbits first
    toprank = HandEvaluatorTables.topRankTable [rbits];
    if (HandEvaluatorTables.nRanksTable [rbits]==1)
      {
	// one bit set? need TWO of that rank
	_rankhash += 2*((long)0x01<<(toprank*4));
      }
    else
      {
	_rankhash += ((long)0x01<<(toprank*4));
	toprank = HandEvaluatorTables.topRankTable[rbits-(0x01<<toprank)];
	_rankhash += ((long)0x01<<(toprank*4));
      }

    // then the sbits
    toprank = HandEvaluatorTables.topRankTable [sbits];
    if (HandEvaluatorTables.nRanksTable [sbits]==1)
      {
	// one bit set? need TWO of that rank
	_rankhash += 2*((long)0x01<<(toprank*4));
      }
    else
      {
	_rankhash += ((long)0x01<<(toprank*4));
	toprank = HandEvaluatorTables.topRankTable[sbits-(0x01<<toprank)];
	_rankhash += ((long)0x01<<(toprank*4));
      }

    if (HandEvaluatorTables.nRanksTable[_rankbits] >= 5)
      if (evaluateStraight())
	return _intval;

    evaluatePairing();
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

  public static int evaluateLow (int n)
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
//  	result ^= NO_PAIR << 16;
	return result;
      }
    return Valuation.NO_LOW;
  }

  static int SUIT_TABLE [] = {0,0,0,0,0,0,0,0,0,0,0,0,0,
			      1,1,1,1,1,1,1,1,1,1,1,1,1,
			      2,2,2,2,2,2,2,2,2,2,2,2,2,
			      3,3,3,3,3,3,3,3,3,3,3,3,3};
  static int RANK_TABLE [] = {0,1,2,3,4,5,6,7,8,9,10,11,12,
			      0,1,2,3,4,5,6,7,8,9,10,11,12,
			      0,1,2,3,4,5,6,7,8,9,10,11,12,
			      0,1,2,3,4,5,6,7,8,9,10,11,12};

  /**
   * We hash the values of the input cards into various
   * integers.  Due to storage limitations, this implementation
   * will break down if more than 7 cards are passed into the
   * HandEvaluator.  The key breaking point is the _suithash
   * which stores the number of each suit in 3 bit chunks.  Thus
   * the maximum number of each suit that can be correctly stored
   * is 7 (0b0111 = 0x7).
   *
   * For a general (8+ cards) evaluator, a case needs to be included
   * to handle that situation.
   */
  private static void hashCards ()
  {
    // the card_hash array is sorted by suits
    // hopefully this allocation won't be happening too often
    //      if (_card_hash == null)
    //        _card_hash = new int [4];
    //      else
    _card_hash[0] = _card_hash[1] = _card_hash[2] = _card_hash[3] = 0;

    //      _suithash = 0;
    //      _rankhash = 0;
    //  	int tmp_suit = _cards[0]/13;
    //  	int tmp_rank = _cards[0]%13;
      
    int tmp_suit = SUIT_TABLE[_cards[0]];
    int tmp_rank = RANK_TABLE[_cards[0]];
    _suithash = (0x01<<(tmp_suit*3));
    _rankhash = ((long)0x01<<(tmp_rank*4));
    _card_hash[tmp_suit] = (0x01<<tmp_rank);

    for (int i=1; i<_cardsDelt; i++)
      {
	tmp_suit = SUIT_TABLE[_cards[i]];
	tmp_rank = RANK_TABLE[_cards[i]];
	_suithash += (0x01<<(tmp_suit*3));
	_rankhash += ((long)0x01<<(tmp_rank*4));
	_card_hash[tmp_suit] |= (0x01<<tmp_rank);
      }
    _rankbits = _card_hash[0] | _card_hash[1] | 
      _card_hash[2] | _card_hash[3];

  }

  private static boolean evaluateFlush()
  {
    int suitindex = HandEvaluatorTables.flushTable[_suithash];
    if (suitindex > 0)
      {
	suitindex--;
	// we *know* we have some kind of a flush, straight flush?
	int sranks = _card_hash[suitindex];
	int sval = HandEvaluatorTables.straightTable[sranks];
	if (sval > 0)
	  setStraightFlush (sval);
	else
	  setFlush (sranks);
	return true;
      }
    return false;
  }

  private static boolean evaluateStraight ()
  {
    int val = HandEvaluatorTables.straightTable [_rankbits];
    if (val > 0)
      {
	setStraight (val);
	return true;
      }
    return false;
  }
  
  /**
   * If you don't want to use the lookup table, you
   * can use this function instead.
   */
  static int evaluateStraightHashMash (int hash)
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
    
    return 0;
  }

  private static boolean evaluatePairing ()
  {
    int toprank = 0;
    int botrank = 0;
    int topind  = 0;
    int botind  = 0;

    // gather the rank count data
    for (int i=Rank.NUM_RANK-1; i>=0; i--)
      {
	//int rankhashi = _rankhash[i];
	int rankhashi = (int)((_rankhash>>(4*i)) & 0xF);
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

    // now pick the right hand from the data
    switch (toprank)
      {
      case 4:
	setFourOfAKind (topind, botind);
	return true;

      case 3:
	if (botrank > 1)
	  {
	    setFullHouse (topind, botind);
	    return true;
	  }
	else
	  {
	    setThreeOfAKind (topind);
	    return true;
	  }

      case 2:
	if (botrank > 1)
	  {
	    setTwoPair (topind, botind);
	    return true;
	  }
	else
	  {
	    setOnePair (topind);
	    return true;
	  }

      default:
	setNoPair ();
	return false;
      }
  }

  //
  //
  //
  // encoding of the hand
  //
  //
  //
  private static void fillHand(int n, int tot)
  {
    for (int i=Rank.NUM_RANK-1; i>=0; i--)
      {
	//int rankhashi = _rankhash[i];
	int rankhashi = (int)((_rankhash>>(4*i)) & 0xF);
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


  /**
   * Encode the value of the hand into the _intval
   */

  /*
   * Old versions
   *
  private static void setFourOfAKind (int topind, int botind)
  {
    _intval  = FOUR_OF_A_KIND_MASK;
//      _intval ^= FOUR_OF_A_KIND << 16;
    _intval ^= (topind << 4);
    _intval ^=  botind;
  }
  private static void setFullHouse (int topind, int botind)
  {
    _intval  = FULL_HOUSE_MASK;
//      _intval ^= FULL_HOUSE << 16;
    _intval ^= (topind << 4);
    _intval ^=  botind;
  }
  private static void setThreeOfAKind (int topind)
  {
    _intval  = THREE_OF_A_KIND_MASK;
//      _intval ^= THREE_OF_A_KIND << 16;
    _intval ^= (topind << 8);
    long thash = _rankhash;
    _rankhash -= (((_rankhash>>(4*topind)) & 0xF)<<(4*topind));
    fillHand (1,3);
    _rankhash = thash;
  }
  private static void setTwoPair (int topind, int botind)
  {
    _intval  = TWO_PAIR_MASK;
//      _intval ^= TWO_PAIR << 16;
    _intval ^= (topind << 8);
    _intval ^= (botind << 4);
    long thash = _rankhash;
    _rankhash -= (((_rankhash>>(4*topind)) & 0xF)<<(4*topind));
    _rankhash -= (((_rankhash>>(4*botind)) & 0xF)<<(4*botind));
    fillHand (2,3);
    _rankhash = thash;
  }
  private static void setOnePair (int topind)
  {
    _intval  = ONE_PAIR_MASK;
//      _intval ^= ONE_PAIR << 16;
    _intval ^= (topind << 12);
    long thash = _rankhash;
    _rankhash -= (((_rankhash>>(4*topind)) & 0xF)<<(4*topind));
    fillHand(1,4);
    _rankhash = thash;
  }
  private static void setNoPair ()
  {
    _intval  = NO_PAIR_MASK;
//      _intval ^= NO_PAIR << 16;
    _intval ^= HandEvaluatorTables.topFiveTable[_rankbits];
  }			
  private static void setStraight (int val)
  {
    _intval  = STRAIGHT_MASK;
//      _intval ^= STRAIGHT << 16;
    _intval ^= val;
  }
  private static void setStraightFlush (int val)
  {
//      Hand h = new Hand ();
//      for (int i=0; i<_cards.length; i++)
//        h.addcard (_cards[i]);
//      System.out.println (h);

    _intval  = STRAIGHT_FLUSH_MASK;
//      _intval ^= STRAIGHT_FLUSH << 16;
    _intval ^= val;
  }
  private static void setFlush (int sranks)
  {
//      _intval  = FLUSH << 16;
    _intval ^= FLUSH_MASK;
    _intval ^= HandEvaluatorTables.topFiveTable[sranks];
  }
  */


  private static void setFourOfAKind (int topind, int botind)
  {
    _intval  = FOUR_OF_A_KIND_MASK;
    _intval ^= (topind << 20);
    _intval ^=  0x01<<botind;
  }
  private static void setFullHouse (int topind, int botind)
  {
    _intval  = FULL_HOUSE_MASK;
    _intval ^= (topind << 20);
    _intval ^= (botind << 16);
  }
  private static void setThreeOfAKind (int topind)
  {
    _intval  = THREE_OF_A_KIND_MASK;
    _intval ^= (topind << 20);
    int kickers = _rankbits;
    kickers ^= (0x01<<topind);
    int k1 = HandEvaluatorTables.topRankTable[kickers];
    _intval ^= 0x01<<k1;
    int k2 = HandEvaluatorTables.topRankTable[kickers^0x01<<k1];
    _intval ^= 0x01<<k2;
  }
  private static void setTwoPair (int topind, int botind)
  {
    _intval  = TWO_PAIR_MASK;
    _intval ^= (topind << 20);
    _intval ^= (botind << 16);
    int kickers = _rankbits;
    kickers ^= (0x01<<topind);
    kickers ^= (0x01<<botind);
    _intval ^= 0x01<<HandEvaluatorTables.topRankTable[kickers];
  }
  private static void setOnePair (int topind)
  {
    _intval  = ONE_PAIR_MASK;
    _intval ^= (topind << 20);
    int kickers = _rankbits;
    kickers ^= (0x01<<topind);
    _intval ^= HandEvaluatorTables.topThreeTable[kickers];
  }
  private static void setNoPair ()
  {
    _intval  = NO_PAIR_MASK;
    _intval ^= HandEvaluatorTables.topFiveTable[_rankbits];
  }			
  private static void setStraight (int val)
  {
    _intval  = STRAIGHT_MASK;
    _intval ^= val << 20;
  }
  private static void setStraightFlush (int val)
  {
    _intval  = STRAIGHT_FLUSH_MASK;
    _intval ^= val << 20;
  }
  private static void setFlush (int sranks)
  {
    _intval ^= FLUSH_MASK;
    _intval ^= HandEvaluatorTables.topFiveTable[sranks];
  }
 

  /*
   *
   *
   *		Flop equity evaluators
   *
   *
   */

  /**
   * Take two hands as input and record the number of wins, loses
   * and ties that the first hand has, return each value in the 0,1,2
   * elements of the return array.
   *
   * This is a brute force version which does absolutly no algorithm
   * optimization at all.
   */
  public static float[] evaluateFlopEquity (Hand h1, Hand h2, Hand board)
  {
    int deadcards[] = new int[2+2+3+1];
    int cards[] = new int[52-2-2-3];
    float results[] = new float[3];

    deadcards[0] = h1.getcard (0);
    deadcards[1] = h1.getcard (1);
    deadcards[2] = h2.getcard (0);
    deadcards[3] = h2.getcard (1);
    deadcards[4] = board.getcard (0);
    deadcards[5] = board.getcard (1);
    deadcards[6] = board.getcard (2);


//      System.out.println (h1 + " / " + h2);

    // short list, just do a bubble sort
    for (int i=0; i<deadcards.length-2; i++)
      for (int j=i+1; j<deadcards.length-1; j++)
	if (deadcards[j] < deadcards[i])
	  {
	    int tmp = deadcards[j];
	    deadcards[j] = deadcards[i];
	    deadcards[i] = tmp;
	  }

    // now load the array of live cards
    int cardindex = 0;
    int deadindex = 0;
    for (int i=0; i<52; i++)
      {
	if (deadcards[deadindex] == i)
	  deadindex++;
	else
	  {
	    cards[cardindex] = i;
	    cardindex++;
	  }
      }

    Hand hb1 = (Hand)h1.clone ();  hb1.addHand (board);
    Hand hb2 = (Hand)h2.clone ();  hb2.addHand (board);

    for (int i=0; i<cards.length-1; i++)
      for (int j=i+1; j<cards.length; j++)
	{
	  int ic = cards[i];
	  int jc = cards[j];
	  Hand ht1 = (Hand)hb1.clone ();  ht1.addCard (ic);  ht1.addCard (jc);
	  Hand ht2 = (Hand)hb2.clone ();  ht2.addCard (ic);  ht2.addCard (jc);

	  int hval1 = ht1.evaluate ();
	  int hval2 = ht2.evaluate ();
//  	  System.out.println (hb1);
//  	  System.out.println (ht1);
//  	  System.out.println (Valuation.decode (hval1));
//  	  System.out.println (hb2);
//  	  System.out.println (ht2);
//  	  System.out.println (Valuation.decode (hval2));

	  //  	  System.out.println ("h1: " + ht1 + " " + Valuation.decode (hval1));
	  //  	  System.out.println ("h2: " + ht2 + " " + Valuation.decode (hval2));
	  //  	  System.out.println ();

	  if (hval1 > hval2)
	    results [0]++;
	  else if (hval1 < hval2)
	    results [1]++;
	  else
	    results [2]++;
	}

//      System.out.println (results[0] + "/" + results[1] + "/" + results[2] + "/");

    return results;
  }


  /**
   * This is for HOLD'EM hands only, each input hand must have two and
   * only two cards.
   * 
   * This function has NOT been tested !!!
   */
  public static float[] evaluateFlopEquity (Hand livehands[], Hand deadhands[], Hand board)
  {
    long deadcards = 0;

    // check for internal consistency
    // * non-null hands
    // * hands with two cards
    // * no repeated cards
    deadcards = recordCards (livehands, deadcards);
    if (deadcards == -1)
      return null;
    deadcards = recordCards (deadhands, deadcards);
    if (deadcards == -1)
      return null;

    float results[] = new float[livehands.length];
    float win_loss[][] = new float[livehands.length][2];

    Hand l_hands[] = new Hand[livehands.length];
    for (int i=0; i<livehands.length; i++)
      {
	l_hands[i].addhand(livehands[i]);
	l_hands[i].addhand(board);
      }
    
    // we don't try to bail out on optimizations here, but
    // we could rewrite this loop to look for flush draws and
    // bail.  To do this we would need to write a "general"
    // version of evaluateOppHandsF which takes two input
    // arrays instead of just two hands.
    for (int i=0; i<52; i++)
      {
	if ((deadcards & (0x01L<<i)) == 0)
	  for (int j=i+1; j<52; j++)
	    {
	      if ((deadcards & (0x01L<<j)) == 0)
		{
		  for (int h=0; h<l_hands.length; h++)
		    l_hands[i].addcards(i,j);
		 
		  evaluateAllHands (l_hands,win_loss);
 
		  for (int h=0; h<l_hands.length; h++)
		    l_hands[i].setLength (5);
		}
	    } 
      }

    for (int i=0; i<win_loss.length; i++)
      results[i] = win_loss[i][0]/(win_loss[i][0] + win_loss[i][1]);

    return results;
  }

  /**
   * Check the consistency of the input hands with respect to
   * the cards seen, as well as the general concept of two card
   * starting hands for Hold'em
   *
   * return the updated deadcards variable if all goes well,
   * otherwise return -1
   */
  private static long recordCards (Hand hands[], long deadcards)
  {
    int card;
    for (int i=0; i<hands.length; i++)
      {
	if (hands[i] == null)
	  return -1;
	else if (hands[i].getNumCards () != 2)
	  return -1;
	for (int n=0; n<2; n++)
	  {
	    card = hands[i].getcard (0);	
	    if ((deadcards & (0x01L<<card)) != 0)
	      return -1;
	    else
	      deadcards |= 0x01L << card;
	  }
      }
    return deadcards;
  }

  /**
   * wlt - win loss tie record for each hand
   */
  private static void evaluateAllHands (Hand hs[], float [][] wlt)
  {
    int hval[] = new int[hs.length];
    int maxval = 0;
    float maxnum = 0;
    for (int i=0; i<hs.length; i++)
      {
	hval[i] = HandEvaluator.doFullEvaluation (hs[i]);
	if (hval[i] > maxval)
	  {
	    maxval = hval[i];
	    maxnum = 1;
	  }
	else if (hval[i] == maxval)
	  {
	    maxnum++;
	  }
      }

    for (int i=0; i<hs.length; i++)
      {
	if (hval[i] == maxval)
	  wlt[i][0] += 1.0/maxnum;
	else
	  wlt[i][1] += 1.0;
      }
//      if (h1val > h2val)
//        results[0]+=weight;
//      else if (h1val < h2val)
//        results[1]+=weight;
//      else
//        results[2]+=weight;
  }



  private static void printbits (int bits)
  {
	for (int b=31; b>=0; b--)
	  {
	    System.out.print ((bits>>b)&0x01);
	    if ((b%3)==0)
	      System.out.print (" ");
	  }
	System.out.println ();
  }


  /**
   * This is one crazy function, inner loop, so mucho opto
   *
   * One thing to note about the approach taken, I tend to
   * short circuit "later" in the innermost loops.  The main
   * reason for this is that these loops are very short, so
   * doing the calculations to avoid them is probably almost
   * as expensive as executing them.
   *
   * The key to the speed of this inner loop function is the
   * partial evaluation of the flush information.  By incrementing
   * and decrementing the "suit" variables, we can quicky determing
   * whether or not we need to consider a flush for this turn/river
   * suit combination using the precalculated lookup table.
   *
   * Full flush evaluation is avoided in all cases except:
   * - flush vs. flush
   * - flush (from straight) vs. full house
   *
   * This takes advantage of the fact that there are only 7 cards
   * in a hand, and will break down if we try to uses these shortcuts
   * in other games (say Omaha).
   */
  private static void evaluateOppHandsF (Hand h1, int h1val, Hand h2, int h2val,
					long deadcards,
					int suits1, int suits2,
					int tf_toprank, int tf_botrank,
					float [] results, float weight)
  {
    int h1suit = getFlushSuit (suits1);
    int h2suit = getFlushSuit (suits2);
    int count = 0;
    int h1valuation = Valuation.value (h1val);
    int h2valuation = Valuation.value (h2val);
    
    // bail on the inner loop if we have a clear case
    if ((h1suit < 0 && h2suit < 0)          ||   // no flush draws
	(h1valuation >  Valuation.FLUSH &&	 // full house, 
	 h2valuation != Valuation.STRAIGHT) ||   //   no straight flush possible
	(h2valuation >  Valuation.FLUSH &&	 // full house, 
	 h1valuation != Valuation.STRAIGHT))      //   no straight flush possible
      {
	count = countCombinations (deadcards, tf_toprank, tf_botrank);
  	tabulate (h1val, h2val, results, weight, count);
  	return;
      }
      
    int h1fnum = (suits1>>(h1suit*3))&0x07;
    int h2fnum = (suits2>>(h2suit*3))&0x07;
    
    // two essentially different cases, running pair on the board,
    // and a turn and flush of two different ranks.
    if (tf_botrank < 0 || tf_botrank == tf_toprank)
      {
	// we've got a running board pair, only 4 flushes can be
	if (h1fnum < 4 && h2fnum < 4)
	  {
	    count = countCombinations (deadcards, tf_toprank, tf_botrank);
	    tabulate (h1val, h2val, results, weight, count);
	    return;
	  }
    
	for (int s1=0; s1<3; s1++)
	  {
	    int topcard = tf_toprank+13*s1;
	    if ((deadcards & (0x01L<<topcard)) == 0)
	      {
		suits1 += (0x01<<(s1*3));
		suits2 += (0x01<<(s1*3));
		for (int s2=s1+1; s2<4; s2++)
		  {
		    int botcard = tf_toprank+13*s2;
		    if ((deadcards & (0x01L<<botcard)) == 0)
		      {
			suits1 += (0x01<<(s2*3));
			suits2 += (0x01<<(s2*3));

			int f1 = HandEvaluatorTables.flushTable [suits1];
			int f2 = HandEvaluatorTables.flushTable [suits2];
			if (f1 > 0 || f2 > 0)
			  {
			    if (f1<0 && h1valuation < Valuation.FULL_HOUSE)
			      results[1]+=weight;
			    else if (f2<0 && h2valuation < Valuation.FULL_HOUSE)
			      results[0]+=weight;
			    else
			      {
				h1.addcards (topcard, botcard);
				h2.addcards (topcard, botcard);
				evaluateTwoHands (h1,h1val,h2,h2val,results,weight);
				h1.setLength (5);
				h2.setLength (5);
			      }
			  }
			else
			  {
			    count++;
			  }

			suits1 -= (0x01<<(s2*3));
			suits2 -= (0x01<<(s2*3));
		      }
		  }
		suits1 -= (0x01<<(s1*3));
		suits2 -= (0x01<<(s1*3));
	      }
	  }
	tabulate (h1val, h2val, results, weight, count);
      }
    else
      {
	count = 0;
	for (int s1=0; s1<4; s1++)
	  {
	    int topcard = tf_toprank+13*s1;
	    if ((deadcards & (0x01L<<topcard)) == 0)
	      {
		suits1 += (0x01<<(s1*3));
		suits2 += (0x01<<(s1*3));
		for (int s2=0; s2<4; s2++)
		  {
		    int botcard = tf_botrank+13*s2;
		    if ((deadcards & (0x01L<<botcard)) == 0)
		      {
			suits1 += (0x01<<(s2*3));
			suits2 += (0x01<<(s2*3));

			int f1 = HandEvaluatorTables.flushTable [suits1];
			int f2 = HandEvaluatorTables.flushTable [suits2];
			if (f1 > 0 || f2 > 0)
			  {
			    if (f1<0 && h1valuation < Valuation.FULL_HOUSE)
			      results[1]+=weight;
			    else if (f2<0 && h2valuation < Valuation.FULL_HOUSE)
			      results[0]+=weight;
			    else
			      {
				h1.addcards (topcard, botcard);
				h2.addcards (topcard, botcard);
				evaluateTwoHands (h1,h1val,h2,h2val,results,weight);
				h1.setLength (5);
				h2.setLength (5);
			      }
			  }
			else
			  {
			    count++;
			  }

			suits1 -= (0x01<<(s2*3));
			suits2 -= (0x01<<(s2*3));
		      }
		  }
		suits1 -= (0x01<<(s1*3));
		suits2 -= (0x01<<(s1*3));
	      }
	  }
	tabulate (h1val, h2val, results, weight, count);
      }
  }

  private static void tabulate (int v1, int v2, float [] results, 
			       float weight, int count)
  {
    if (v1 > v2)
      results[0]+=count*weight;
    else if (v1 < v2)
      results[1]+=count*weight;
    else
      results[2]+=count*weight;
  }

//    public static int countCombinations (long deadcards, 
//  				       int turncard, int riverrank, 
//  				       int toprank, int botrank)
//    {
//      int total = 0;
//      deadcards |= turncard;
//      for (int s=0; s<4; s++)
//        if ( (deadcards & (0x01L<<(toprank+13*s))) == 0)
//  	{
//  	  long dead2 = deadcards | (0x01L<<(riverrank+13*s));
//  	  total += countCombinations (dead2, toprank, botrank);
//  	}
//      return total;
//    }

  private static int countCombinations (long deadcards, int toprank, int botrank)
  {
    int topcount = 0;
    int botcount = 0;

    for (int s=0; s<4; s++)
      if ( (deadcards & (0x01L<<(toprank+13*s))) == 0)
  	topcount++;
    if (botrank == -1)
      {
  	switch (topcount)
  	  {
  	  case 2:
  	    return 1;
  	  case 3:
  	    return 3;
  	  case 4:
  	    return 6;
  	  default:
  	    // there must be at least two cards for this case to work.
  	    // other values indicate 0 combinations
  	    return 0;
  	  }
      }
    else
      {
  	for (int s=0; s<4; s++)
  	  if ( (deadcards & (0x01L<<(botrank+13*s))) == 0)
  	    botcount++;
  	return topcount*botcount;
      }
  }

  private static void evaluateTwoHands (Hand h1, Hand h2, 
				       float [] results, float weight)
  {
    int h1val = HandEvaluator.doFullEvaluation (h1);
    int h2val = HandEvaluator.doFullEvaluation (h2);

    if (h1val > h2val)
      results[0]+=weight;
    else if (h1val < h2val)
      results[1]+=weight;
    else
      results[2]+=weight;
  }

  public static void evaluateTwoHands (Hand h1, int h1vb, Hand h2, int h2vb, 
				       float [] results, float weight)
  {
    int h1val = HandEvaluator.doFlushEvaluation (h1);
    int h2val = HandEvaluator.doFlushEvaluation (h2);
    h1val = h1val > h1vb ? h1val : h1vb;
    h2val = h2val > h2vb ? h2val : h2vb;

    if (h1val > h2val)
      results[0]+=weight;
    else if (h1val < h2val)
      results[1]+=weight;
    else
      results[2]+=weight;
  }

  private static int getFlushSuit (int suithash)
  {
    switch (HandEvaluatorTables.flushTable[suithash])
      {
      case 4:
	return 3;
      case 3:
	return 2;
      case 2:
	return 1;
      case 1:
	return 0;
      case 0:
	return -1;
      case -1:
	return 0;
      case -2:
	return 1;
      case -3:
	return 2;
      case -4:
	return 3;
      case -16:
	return 0;
      case -32:
	return 1;
      case -48:
	return 2;
      case -64:
	return 3;
      }
    return -1;
  }


}



