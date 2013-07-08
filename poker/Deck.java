package poker;
import utils.*;
import java.util.*;


public class Deck
{
  public static int NUM_CARDS = 52;

  public Random _rand;  // this is done to give access to the rng, 
  //should not be used outside the packages
  int _cards[];
  int _currentCard;
  int _cardsRemoved;
  boolean _shuffled;
  boolean _inDeck[];

  public Deck ()
  {
    _rand  = new Random (0);
    //_rand  = new Random ((new Date()).getTime());

    _cards = new int [NUM_CARDS];
    _inDeck = new boolean [NUM_CARDS];
    for (int i=0; i<NUM_CARDS; i++)
      {
	_cards[i] = i;
	_inDeck[i] = true;
      }
    _currentCard = -1;
    _shuffled = false;
    _cardsRemoved = 0;
  }

  public int getNumCardsLeft ()
  {
    int ret = 0;
    for (int i=0; i<_inDeck.length; i++)
      if (_inDeck[i])
	ret++;
    return ret;
  }

  public void replaceAllCards ()
  {
    //_inDeck = new boolean [NUM_CARDS];
    for (int i=0; i<NUM_CARDS; i++)
      {
	_inDeck[i] = true;
      }
  }

  /**
   * Knuth Shuffle -- O(n) where n is size of deck.  The shuffle
   * is guaranteed random if the underlying RNG is random.
   */
  public void shuffle ()
  {
    Debug.out ("Deck.shuffle");
    for (int i=0; i<NUM_CARDS; i++)
      {
	int t = _rand.nextInt (NUM_CARDS-i) + i;
	if (t==i)
	  continue;
	int tmp   = _cards[i];
	_cards[i] = _cards[t];
	_cards[t] = tmp;
      }
    _currentCard = -1;
    _cardsRemoved = 0;
  }

  public int [] getRemainingCards ()
  {
    int cards[] = new int[NUM_CARDS - _cardsRemoved];
    int n=0;
    for (int i=0; i<NUM_CARDS; i++)
      {
	if (_inDeck[i])
	  cards[n++] = i;
      }
    return cards;
  }    

  int randomHalfIndex (int low)
  {
    int val = low + (int)Math.floor(_rand.nextFloat ()*NUM_CARDS/2);
    //if (val <= 0 || val >= NUM_CARDS-1) return -1;
    return val;
  }

  void subShuffle ()
  {
    for (int i=0; i<NUM_CARDS/2; i++)
      {
	int t     = randomHalfIndex (NUM_CARDS/2);
	int tmp   = _cards[i];
	_cards[i] = _cards[t];
	_cards[t] = tmp;
      }
    for (int i=NUM_CARDS/2; i<NUM_CARDS; i++)
      {
	int t     = randomHalfIndex (0);
	int tmp   = _cards[i];
	_cards[i] = _cards[t];
	_cards[t] = tmp;
      }
  }

  public void print ()
  {
    for (int i=0; i<NUM_CARDS; i++)
      {
	Card c = new Card(_cards[i]);
	System.out.println (i + ": " + c.rank () + " " + c.suit() + 
			    " " + _inDeck[_cards[i]]);
      }
  }

  public void removeCard (Card c)
  {
    removeCard(c.value());
  }

  public void removeCards (Hand h)
  {
    if (h == null)
      return;

    for (int i=0; i<h.getNumCards (); i++)
      removeCard (h.getCard (i));
  }

  public void removeCard (int c)
  {
    if (c < 0 || c >= NUM_CARDS)
      return;
    _inDeck[c] = false;
    _cardsRemoved++;
  }

  public int nextcard ()
  {
    _currentCard++;
    //Debug.out ("current card: " + _currentCard);
    if (_currentCard >= NUM_CARDS)
      return -1;

    if (_inDeck[_cards[_currentCard]])
      {
	//Debug.out (_currentCard);
	_inDeck[_cards[_currentCard]] = false;
	_cardsRemoved++;
	return _cards[_currentCard];
      }
    else 
      return nextcard ();
  }

  //      public Card nextCard ()
  //      {
  //  	_currentCard++;
  //  	if (_currentCard >= NUM_CARDS)
  //  	    return null;
  //  	Card c = new Card(_cards[_currentCard]);
  //  	if (_inDeck[_cards[_currentCard]])
  //  	    return c;
  //  	else 
  //  	    return nextCard ();
  //      }
    

  public static void main (String args[])
  {
    Deck d = new Deck ();
    d.print ();
    d.shuffle ();
    d.print ();

    
    d = new Deck ();
    int ar[] = new int[26];

    for (int i=0; i<10000; i++)
      {
	ar[d.randomHalfIndex (0)]++;
      }
  
    for (int i=0; i<ar.length; i++)
      System.out.println (ar[i]);
    
  }
}

