package mahjong.riichi;
import java.util.*;

import android.util.Log;
import android.util.Pair;

public class Hand extends Object{
	
	/**
	 * rawHand is the basis for everything we do.  It is an unsorted collection of every
	 * tile we have both open and closed.  Once a tile is inserted in rawHand it should
	 * never be moved.  activeHand, suits, melds, etc all rely on the fact that a tile's 
	 * location in rawHand is never changed (unless it's discarded)
	 * 
	 * Tile14 - a special pointer to the last tile we picked up.
	 * rawHandSize - Depreciated, there could very well be gaps in rawHand
	 * 				 Always check from 0 to rawHandMax
	 * 
	 */
	public Tile[] rawHand;
	public int Tile14;
	public int rawHandSize;
	public static int rawHandMax = 19;
	
	//Turns on and off the AI
	public boolean AIControlled;
	
	/**
	 * Sorting arrays
	 * Both of these contain "pointers" to the tiles in rawHand
	 * IE if activeHand[0] = 1, it does not mean that it's 1-Bamboo, it points to
	 * whatever is in rawHand[1]
	 * 
	 * suits[suit][tile] - Tiles sorted by suit, -1 means the end of the array
	 * activeHand - Based on suits, tiles arranged in sequential order.  
	 * 				Used to display the hand on screen 
	 */
	//public Integer[][] suits;
	//public int[] activeHand;
	//public int activeHandSize;
	public ArrayList<ActiveHandPair> activeHandMap;
	
	/**
	 * Melds
	 * 
	 * The melds array is set up as follows:
	 * 		melds[Meld Number][0] = Number of tiles in the meld
	 * 		melds[Meld Number][1-4] = Indexes of the tiles in rawHand
	 * 		melds[Meld Number][5] = The player it came from
	 */
	public int[][] melds;
	public int numberOfMelds;
	public boolean openHand;
	public static int MELD_TILE_FROM = 5;
	
	/**
	 * Tenpai Info
	 * 
	 * tenpaiTiles - If we are in tenpai it will contain the tiles we could win with
	 * 				 Setup in getShantenCount_TreeVersion
	 * inTenpai - boolean, are we in tenpai
	 * 			  Setup in getShantenCount_TreeVersion
	 * inFuriten - boolean, are we in furiten
	 * 
	 * unusedTiles - Borrowed from AI, used only to tell what is legal to discard after calling riichi
	 */
	public ArrayList<Integer> tenpaiTiles;
	public boolean inTenpai;
	public boolean inFuriten;
	public ArrayList<Integer> riichiTiles;
	
	/**
	 * This will contain every tile we could call.  The numbers in here are set up where
	 * the High Word is the command (pon, chi, etc) and the Low Word is the actual tile number
	 * 
	 * Can/Should be used my the user controlled player
	 * 
	 * See: 
	 * 		setupTilesToCall()
	 * 		canCallPon(int)
	 * 		canCallChi(int)
	 * 		canCallKan(int)
	 * 		canCallRon(int)
	 * 		
	 */
	private ArrayList<Integer> tilesWeCouldCall;
	private static int PON = 0x00010000;
	private static int CHI = 0x00020000;
	private static int KAN = 0x00040000;
	private static int RON = 0x00080000;
	private static int GETTILE = 0x0000FFFF;
	private static int GETCMD = 0x000F0000;
	
	/**
	 * Scoring info, publicly available unless otherwise stated
	 */
	public int fu; 
	public int han;
	public int[] yaku;
	int dora;
	private ArrayList<Set> winningHand;
	private ArrayList<Set> optimalHand;
	
	//Pointers to other classes
	Player pMyPlayer;
	
	Hand(Player pPlayer){
		//super();
		pMyPlayer = pPlayer;
		rawHand = new Tile[rawHandMax]; //extra tiles are possible through Kans
		rawHandSize = 0;
		
		/*bamboo = new int[14];
		dot = new int[14];
		character = new int[14];
		honor = new int[14];*/
		//suits = new Integer[][] {{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
		//					 	 {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
		//					 	 {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
		//					 	 {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
		//					 	 {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1},
		//					 	 {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1}};
		/*vSuits = new ArrayList<Integer>[7];
		for ( int i = 0; i < 7; i++ )
		    vSuits[i] = new ArrayList<Integer>();*/
		melds = new int[4][6];
		numberOfMelds = 0;
		//activeHand = new int[15];
		activeHandMap = new ArrayList<ActiveHandPair>();
		//activeHandSize = 0;
		AIControlled = true;
		tenpaiTiles = new ArrayList<Integer>();
		inTenpai = false;
		inFuriten = false;
		tilesWeCouldCall = new ArrayList<Integer>();
		riichiTiles = new ArrayList<Integer>();
		
		fu = 0; 
		han = 0;
		dora = 0;
		yaku = new int[Globals.ALLYAKUCOUNT];
		winningHand = new ArrayList<Set>();;
		optimalHand = new ArrayList<Set>();
	}
	
	//Actions!  How Exciting!
	//Sort should really only be called at the start of a hand.  The insert/discard/meld functions will auto sort after that
	public void rebuildActiveHand(){
		try{
			activeHandMap.clear();
			for(int thisTile = 0; thisTile < rawHandMax; thisTile++){
				Tile tempTile = rawHand[thisTile];
				if(tempTile == null)
					continue;
				if(tempTile.open)
					continue;
				activeHandMap.add(new ActiveHandPair(tempTile.rawNumber, thisTile));
			}
			
			if(rawHandSize <= 1)
				return;
			
			//Clear it out, it screws it up if you try overriding a previous sort
			//for(int i = 0; i <= Globals.Suits.KAZE; i++){
			//	for(int j = 0; j < 16; j++){
			//		suits[i][j] = -1;
			//	}
			//}
			
			//for(int i = 0; i < rawHandMax; i++){
			//	Tile thisTile = rawHand[i];
			//	if(thisTile == null)
			//		continue;
			//	int thisSuit = thisTile.getSuit();
				
				//Skip open tiles (only relevant after a meld)
			//	if(thisTile.open)
			//		continue;
				
				/*if(suits[thisSuit][0] == -1){//special case for first entry
					suits[thisSuit][0] = i;
				}
				else{
					boolean Inserted = false;
					int counter = 0;
					int holder = 0;
					while(suits[thisSuit][counter]!=-1){
						if(Inserted){
							int temp = suits[thisSuit][counter];
							suits[thisSuit][counter] = holder;
							holder = temp;
						}
						else{
							if(thisTile.rawNumber < rawHand[(suits[thisSuit][counter])].rawNumber){
								holder = suits[thisSuit][counter];
								suits[thisSuit][counter] = i;
								Inserted = true;
							}
						}
						counter++;
					}
					if(Inserted)
						suits[thisSuit][counter] = holder;
					else
						suits[thisSuit][counter] = i;
				}*/
				
				//List version
				/*if(vSuits[thisSuit].isEmpty()){//special case for first entry
					vSuits[thisSuit].add(i);
				}
				else{
	
					int size = vSuits[thisSuit].size();
					int addAt = 0;
					for(; addAt < size; addAt++){
						if(rawHand[i].rawNumber < rawHand[(vSuits[thisSuit].get(addAt))].rawNumber){
							break;
						}
					}
					vSuits[thisSuit].add(addAt, i);
				}*/
			//}
			
			//Active hand seems unnecessary, but without it the discard function becomes a mess
			sort();
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.Sort", WTFAmI);
		}
	}
	
	public Tile discard(int thisTile, boolean isRawTile){
		if(thisTile == -1){ //just throw out the new tile
			Globals.myAssert(false);
			thisTile = Tile14;
			if(thisTile < 0){
				thisTile = 0; //Something went very wrong, just make sure we don;t crash
				isRawTile = false;
			}
				
		}
		
		int rawPos = 0;
		if(!isRawTile){
			if(thisTile < 0 || thisTile >= activeHandMap.size()){
				Globals.myAssert(false); //Something went terribly wrong, just discard the first tile
				thisTile = 0;
			}
			ActiveHandPair toDiscard = activeHandMap.get(thisTile);
			if(toDiscard == null)
				toDiscard = activeHandMap.get(0);
			
			Globals.myAssert(toDiscard != null);
			rawPos = toDiscard.rawHandIdx;
		}
		else{
			rawPos = thisTile;
		}

		//int thisSuit = rawHand[rawPos].getSuit();
		//boolean Deleted = false;
		//int counter = 0;
		//while(suits[thisSuit][counter]!=-1){
		//	if(Deleted){
		//		suits[thisSuit][counter] = suits[thisSuit][counter+1];
		//	}
		//	else{
		//		if(/*activeHand[thisTile]*/rawPos == suits[thisSuit][counter]){
		//			suits[thisSuit][counter] = suits[thisSuit][counter+1];
		//			Deleted = true;
		//		}
		//	}
		//	counter++;
		//}
		//if(!Deleted)
		//	return null;
		if(isRawTile)
			Globals.myAssert(activeHandMap.remove(new ActiveHandPair(0, rawPos)));
		else
			activeHandMap.remove(thisTile);
			
		Tile ret = rawHand[rawPos];
		rawHand[rawPos] = null;
		rawHandSize--;
		//int indexToRemove = activeHandMap.indexOf(new ActiveHandPair(ret.rawNumber, rawPos));
		//Globals.myAssert(indexToRemove >=0 && indexToRemove < activeHandMap.size());
		//if(Tile14.rawNumber != -1){
		//	insert(rawPos);//Add the 14th tile in
		//}
		sort();
		return ret;
	}
	
	public int rawToActiveIdx(int rawIdx){
		if(rawIdx < 0)
			return -1;
		if(rawHand[rawIdx] == null)
			return -1;
		int ret = activeHandMap.indexOf(new ActiveHandPair(0, rawIdx));
		//for(int i = 0; i < activeHandSize; i++){
		//	if(activeHand[i] == rawIdx)
		//		return i;
		//}
		
		return ret;//-1;
	}
	
	public void deal(Tile newTile){
		//We have to clone tiles here or else it will break everything when someone makes
		//a call
		ActiveHandPair pairToAdd = new ActiveHandPair(newTile.rawNumber, rawHandSize);
		rawHand[rawHandSize++] = new Tile(newTile);
		Globals.myAssert(activeHandMap.add(pairToAdd));
	}
	
	public void clear(){
		for(int i = 0; i < 19; i++){
			rawHand[i] = null;
		}
		rawHandSize = 0;
		Tile14 = -1;
		numberOfMelds = 0;
		//for(int i = 0; i < 6; i++){
		//	for(int j = 0; j < 16; j++){
		//		suits[i][j] = -1;
		//	}
		//}
		activeHandMap.clear();
		openHand = false;
		tenpaiTiles.clear();
		tilesWeCouldCall.clear();
		inTenpai = false;
		inFuriten = false;
	}
	
	public boolean draw(Tile newTile, boolean sort){
		try{
			//Tile14 = newTile;
			//int pos = 0;
			if((newTile.rawNumber < 0)||(newTile.rawNumber > 34))
				return false;
			for(int i = 0; i < 18; i++){
				if(rawHand[i] == null){
					Tile14 = i; break;
				}	
			}
			rawHand[Tile14] = newTile;
			ActiveHandPair pairToAdd = new ActiveHandPair(newTile.rawNumber, Tile14);
			Globals.myAssert(activeHandMap.add(pairToAdd));
			rawHandSize++;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.Draw_1", WTFAmI);
			//return false;
		}
		try{
			if(sort){
				//Collections.sort(activeHandMap);
				/*int thisSuit = newTile.getSuit();
		
				boolean Inserted = false;
				int counter = 0;
				int holder = 0;
				
				if(suits[thisSuit][0] == -1){//special case for first entry
					suits[thisSuit][0] = Tile14;
				}
				else{
					while(suits[thisSuit][counter]!=-1){
						if(Inserted){
							int temp = suits[thisSuit][counter];
							suits[thisSuit][counter] = holder;
							holder = temp;
						}
						else{
							if(rawHand[Tile14].rawNumber < rawHand[(suits[thisSuit][counter])].rawNumber){
								holder = suits[thisSuit][counter];
								suits[thisSuit][counter] = Tile14;
								Inserted = true;
							}
						}
						counter++;
					}
				}
				if(!Inserted){
					suits[thisSuit][counter] = Tile14;
					//Globals.myAssert(false);
					//return false;
				}
				else
					suits[thisSuit][counter] = holder;
				
				//Tile14.rawNumber = -1;*/
				sort();
			}
			return true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.Draw_Sort", WTFAmI);
			return false;
		}
	}
	
	public void meld(Tile newTile, int tile1, int tile2, int tile3, int from){
		//It doesn't matter to us what kind it is, validation will be handled elsewhere
		draw(newTile, false);
		
		try{
			if(tile1 < 0){//Only time this will happen is a promoted kan
				Globals.myAssert(false); //Self kans should use the other meld function
				boolean found = false;
				for(int i = 0; i < numberOfMelds; i++){
					if((rawHand[melds[i][1]].rawNumber == rawHand[Tile14].rawNumber)&&
					   (rawHand[melds[i][2]].rawNumber == rawHand[Tile14].rawNumber)){
							found = true;
							melds[i][0] = 4;
							melds[i][4] = Tile14;
							rawHand[Tile14].open = true;
					}
				}
				Globals.myAssert(found);
				rebuildActiveHand();
				return;
			}
			
			Globals.myAssert(rawHand[Tile14] != null);
			Globals.myAssert(rawHand[tile1] != null);
			Globals.myAssert(rawHand[tile2] != null);
			
			melds[numberOfMelds][0] = 3;
			melds[numberOfMelds][1] = Tile14;
			rawHand[Tile14].open = true;
			melds[numberOfMelds][2] = tile1;
			rawHand[tile1].open = true;
			melds[numberOfMelds][3] = tile2;
			rawHand[tile2].open = true;
			if(tile3 >= 0){
				melds[numberOfMelds][0] = 4;
				melds[numberOfMelds][4] = tile3;
				rawHand[tile3].open = true;
			}
			melds[numberOfMelds][MELD_TILE_FROM] = from;
			numberOfMelds++;
			openHand = true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.Meld", WTFAmI);
		}
		rebuildActiveHand();
	}
	
	/**
	 * Should only be used for self kans
	 */
	public void meld(int tile1, int tile2, int tile3, int tile4){
		//It doesn't matter to us what kind it is, validation will be handled elsewhere
		//draw(newTile, false);
		
		try{
			if(tile2 < 0){//Only time this will happen is a promoted kan
				boolean found = false;
				for(int i = 0; i < numberOfMelds; i++){
					if((rawHand[melds[i][1]].rawNumber == rawHand[tile1].rawNumber)&&
					   (rawHand[melds[i][2]].rawNumber == rawHand[tile1].rawNumber)){
							found = true;
							melds[i][0] = 4;
							melds[i][4] = tile1;
							rawHand[tile1].open = true;
					}
				}
				Globals.myAssert(found);
				rebuildActiveHand();
				return;
			}
			
			Globals.myAssert(rawHand[tile1] != null);
			Globals.myAssert(rawHand[tile2] != null);
			Globals.myAssert(rawHand[tile3] != null);
			
			melds[numberOfMelds][0] = 3;
			melds[numberOfMelds][1] = tile1;
			rawHand[tile1].open = true;
			rawHand[tile1].selfKan = true;
			melds[numberOfMelds][2] = tile2;
			rawHand[tile2].open = true;
			rawHand[tile2].selfKan = true;
			melds[numberOfMelds][3] = tile3;
			rawHand[tile3].open = true;
			rawHand[tile1].selfKan = true;
			if(tile4 >= 0){
				melds[numberOfMelds][0] = 4;
				melds[numberOfMelds][4] = tile4;
				rawHand[tile4].open = true;
				rawHand[tile1].selfKan = true;
			}
			
			numberOfMelds++;
			if(rawHand[tile1].selfKan != true)
				openHand = true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.Meld_SelfKan", WTFAmI);
		}
		rebuildActiveHand();
	}
	
	//Helpers
	private void sort(){
		try{
			Collections.sort(activeHandMap);
			//int iter = 0;
			//for(int i = 1; i < 6; i++){
			//	for(int j = 0; suits[i][j]!=-1; j++){
			//		if(!rawHand[suits[i][j]].open)
			//			activeHand[iter++] = suits[i][j];
			//	}
			//}
			//activeHandSize = iter;
			//while(iter != 14){
			//	activeHand[iter++] = -1;
			//}

		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.buildActiveHand", WTFAmI);
		}
	}
	
	/**
	 * 
	 * Getters and Setters
	 * 
	 */
	public Tile getRawTileAt(int idx){
		if(idx < 0 || idx >= 19){
			Globals.myAssert(false);
			return null;
		}
		
		Tile retTile = rawHand[idx];	
		return retTile;
		
	}
	
	public Tile getTileFromActiveIdx(int idx){
		try{
			if(idx < 0 || idx >= activeHandMap.size())
				return null;
			
			ActiveHandPair thisTile = activeHandMap.get(idx);
			
			if(thisTile == null)
				return null;
			if(thisTile.rawHandIdx < 0)
				return null;
			
			
			Tile ret = rawHand[thisTile.rawHandIdx];
			return ret;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.getTileFromActiveIdx", WTFAmI);
			return null;
		}
	}
	
	//These return the first instance of a tile
	public int getFirstTile(int rawNumber){
		int thisSuit = Tile.convertRawToSuit(rawNumber);
		if(thisSuit < Globals.Suits.BAMBOO || thisSuit > Globals.Suits.KAZE)
			return -1;
		int counter = 0;
		
		for(int thisPair = 0; thisPair < activeHandMap.size(); thisPair++){
			ActiveHandPair tempPair = activeHandMap.get(thisPair);
			if(tempPair.rawNumber == rawNumber)
				return tempPair.rawHandIdx;
		}
		//while(suits[thisSuit][counter] != -1){
		//	if(rawHand[suits[thisSuit][counter]].rawNumber == rawNumber)
		//		return suits[thisSuit][counter];
		//	counter++;
		//}
		return -1;
	}
	
	public int getFirstTile(Tile tileToFind){
		int thisSuit = tileToFind.getSuit();
		if(thisSuit < Globals.Suits.BAMBOO || thisSuit > Globals.Suits.KAZE)
			return -1;
		int counter = 0;
		for(int thisPair = 0; thisPair < activeHandMap.size(); thisPair++){
			ActiveHandPair tempPair = activeHandMap.get(thisPair);
			if(tempPair.rawNumber == tileToFind.rawNumber)
				return tempPair.rawHandIdx;
		}
		//while(suits[thisSuit][counter] != -1){
		//	if(rawHand[suits[thisSuit][counter]].equals(tileToFind))
		//		return suits[thisSuit][counter];
		//	counter++;
		//}
		return -1;
	}
	
	public int getNextTile(int rawNumber, int lastIdx){
		int thisSuit = Tile.convertRawToSuit(rawNumber);
		if(thisSuit < Globals.Suits.BAMBOO || thisSuit > Globals.Suits.KAZE)
			return -1;
		int counter = 0;
		boolean foundOld = false;
		for(int thisPair = 0; thisPair < activeHandMap.size(); thisPair++){
			ActiveHandPair tempPair = activeHandMap.get(thisPair);
			if(tempPair.rawNumber == rawNumber){
				if(foundOld)
					return tempPair.rawHandIdx;
				else if(tempPair.rawHandIdx == lastIdx)
					foundOld = true;
			}
				
		}
		/*while(suits[thisSuit][counter] != -1){
			if(suits[thisSuit][counter] == lastIdx)
				foundOld = true;
			else if(rawHand[suits[thisSuit][counter]].rawNumber == rawNumber && foundOld)
				return suits[thisSuit][counter];
			counter++;
		}*/
		return -1;
	}
	
	public int getNextTile(Tile tileToFind, int lastIdx){
		return getNextTile(tileToFind.rawNumber, lastIdx);
		//int thisSuit = tileToFind.getSuit();
		//if(thisSuit < Globals.Suits.BAMBOO || thisSuit > Globals.Suits.KAZE)
		//	return -1;
		//int counter = 0;
		//boolean foundOld = false;
		//while(suits[thisSuit][counter] != -1){
		//	if(suits[thisSuit][counter] == lastIdx)
		//		foundOld = true;
		//	else if(rawHand[suits[thisSuit][counter]].equals(tileToFind) && foundOld)
		//		return suits[thisSuit][counter];
		//	counter++;
		//}
		//return -1;
	}
	
	/**
	 * Stuff coming over from the AI class
	 * We had a bunch of random crap in the AI thread that didn't make any logical
	 * sense to put there (both because it wasn't AI related and the user needed
	 * access to it as well).
	 * 
	 * getTileCounts - returns an array of size [35] containing the number of each tile
	 * 					in your hand
	 * 
	 * setupTilesToCall - Call after each discard, sets up which tiles we can call
	 */
	public int[] getTileCounts(){
		int[] tileCounts = new int[] {0,
   									  0,0,0,0,0,0,0,0,0,
   									  0,0,0,0,0,0,0,0,0,
   									  0,0,0,0,0,0,0,0,0,
   									  0,0,0,0,
   									  0,0,0};
		
		try{
			for(int i = 0; i < rawHandMax; i++){
				if(rawHand[i] != null){
					if(!rawHand[i].open)
						tileCounts[ rawHand[i].rawNumber ]++;
				}
			}
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.GetTileCounts", WTFAmI);
		}
		
		return tileCounts;
	}
	
	/**
	 * A special case of getTileCounts where we want all tiles including melds
	 */
	public int[] getRawTileCounts(){
		int[] tileCounts = new int[] {0,
   									  0,0,0,0,0,0,0,0,0,
   									  0,0,0,0,0,0,0,0,0,
   									  0,0,0,0,0,0,0,0,0,
   									  0,0,0,0,
   									  0,0,0};
		
		try{
			for(int i = 0; i < rawHandMax; i++){
				if(rawHand[i] != null){
					//if(!rawHand[i].open)
						tileCounts[ rawHand[i].rawNumber ]++;
				}
			}
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.getRawTileCounts", WTFAmI);
		}
		
		return tileCounts;
	}
	
	/**
	 * Functions relating to calls
	 */
	public void setupTilesToCall(){
		tilesWeCouldCall.clear();
		int[] tileCounts = getTileCounts();
		
		try{
			for(int i = 1; i <= Globals.MAXTILE; i++){
				int thisSuit = Tile.convertRawToSuit(i);
				if(thisSuit <= Globals.Suits.MAN){
					if(tileCounts[i] >= 2){
						int packedCmd = i | PON;
						tilesWeCouldCall.add(packedCmd);
					}
					if(tileCounts[i] == 3){
						int packedCmd = i | KAN;
						tilesWeCouldCall.add(packedCmd);
					}
					
					if(Tile.convertRawToSuit(i+1) == thisSuit){
						if(tileCounts[i+1] > 0){
							if(Tile.convertRawToSuit(i+2) == thisSuit){
								if(tileCounts[i+2] > 0){
									int packedCmd = i | CHI;
									tilesWeCouldCall.add(packedCmd);
									continue;
								}
							}
							if(Tile.convertRawToSuit(i-1) == thisSuit){
								if(tileCounts[i-1] > 0){
									int packedCmd = i | CHI;
									tilesWeCouldCall.add(packedCmd);
									continue;
								}
							}
						}
					}
					if(Tile.convertRawToSuit(i-1) == thisSuit){
						if(tileCounts[i-1] > 0){
							if(Tile.convertRawToSuit(i-2) == thisSuit){
								if(tileCounts[i-2] > 0){
									int packedCmd = i | CHI;
									tilesWeCouldCall.add(packedCmd);
									continue;
								}
							}
						}
					}
				}
				else{
					if(tileCounts[i] >= 2){
						int packedCmd = i | PON;
						tilesWeCouldCall.add(packedCmd);
					}
					if(tileCounts[i] == 3){
						int packedCmd = i | KAN;
						tilesWeCouldCall.add(packedCmd);
					}
				}
			}
			for(int i = 0; i < tenpaiTiles.size(); i++){
				int packedCmd = tenpaiTiles.get(i) | RON;
				tilesWeCouldCall.add(packedCmd);
			}
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.setupTilesToCall", WTFAmI);
		}
		
	}
	
	public boolean canCallPon(Tile tileToCall){
		try{
			for(int i = 0; i < tilesWeCouldCall.size(); i++){
				int tilePlusCmd = tilesWeCouldCall.get(i);
				int tile = tilePlusCmd & GETTILE;
				int cmd = tilePlusCmd & GETCMD;
				if(tileToCall.rawNumber == tile){
					if((cmd & PON) == PON)
						return true;
				}
			}
			return false;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.canCallPon", WTFAmI);
			return false;
		}
	}
	public boolean canCallChi(Tile tileToCall){
		try{
			for(int i = 0; i < tilesWeCouldCall.size(); i++){
				int tilePlusCmd = tilesWeCouldCall.get(i);
				int tile = tilePlusCmd & GETTILE;
				int cmd = tilePlusCmd & GETCMD;
				if(tileToCall.rawNumber == tile){
					if((cmd & CHI) == CHI)
						return true;
				}
			}
			return false;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.canCallChi", WTFAmI);
			return false;
		}
	}
	public boolean canCallKan(Tile tileToCall){
		try{
			for(int i = 0; i < tilesWeCouldCall.size(); i++){
				int tilePlusCmd = tilesWeCouldCall.get(i);
				int tile = tilePlusCmd & GETTILE;
				int cmd = tilePlusCmd & GETCMD;
				if(tileToCall.rawNumber == tile){
					if((cmd & KAN) == KAN)
						return true;
				}
			}
			return false;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.canCallKan", WTFAmI);
			return false;
		}
	}
	
	public boolean canCallSelfKan(){
		try{
			int[] tileCounts = getTileCounts();
			//for(int thisMeld = 0; thisMeld < numberOfMelds; thisMeld++){
			//	if(rawHand[melds[thisMeld][1]].rawNumber == rawHand[melds[thisMeld][2]].rawNumber){
			//		tileCounts[rawHand[melds[thisMeld][1]].rawNumber] += 3;
			//	}
			//}
			for(int thisTile = 1; thisTile <= Tile.LAST_TILE; thisTile++){
				if(tileCounts[thisTile] == 4)
					return true;
			}
			return false;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.canCallSelfKan", WTFAmI);
			return false;
		}
	}
	
	public int canCallPromotedKan(){
		try{
			if(numberOfMelds == 0)
				return -1;
			
			int[] tileCounts = getTileCounts();
			int[] possibleKanCounts = new int[Tile.LAST_TILE+1];
			for(int thisMeld = 0; thisMeld < numberOfMelds; thisMeld++){
				if(melds[thisMeld][0] == 3 && rawHand[melds[thisMeld][1]].rawNumber == rawHand[melds[thisMeld][2]].rawNumber){
					possibleKanCounts[rawHand[melds[thisMeld][1]].rawNumber] = 3;
				}
			}
			for(int thisTile = 0; thisTile < activeHandMap.size(); thisTile++){
				ActiveHandPair thisPair = activeHandMap.get(thisTile);
				if(possibleKanCounts[thisPair.rawNumber] == 3)
					return thisTile;
			}
			/*for(int thisTile = 1; thisTile <= Tile.LAST_TILE; thisTile++){
				if(possibleKanCounts[thisTile] == 3){
					if(tileCounts[thisTile] == 1)
						return getFirstTile(thisTile);
				}
			}*/
			return -1;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.canCallPromotedKan", WTFAmI);
			return -1;
		}
	}
	
	public boolean canCallRon(Tile tileToCall){
		try{
			if(inFuriten)
				return false;
			for(int i = 0; i < tilesWeCouldCall.size(); i++){
				int tilePlusCmd = tilesWeCouldCall.get(i);
				int tile = tilePlusCmd & GETTILE;
				int cmd = tilePlusCmd & GETCMD;
				if(tileToCall.rawNumber == tile){
					if((cmd & RON) == RON){
						//Ok we have to do an extra check here to make sure we have a yaku
						int potentialScore = scoreHand(tileToCall, false);
						if(potentialScore <= 0)
							return false;
						return true;
					}
				}
			}
			return false;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.canCallRon", WTFAmI);
			return false;
		}
	}
	
	/**
	 * The terrible, frightening Decision Tree
	 *           /\     _____________________
	 *          /  \   /                      \
	 *         /    \  \    Grrr, I hate your  |
	 *        /      \  \    CPU cycles!       |
	 *       /  \  /  \  \  -------------------
	 *      /   0  0   \  \|
	 *     /     __     \
	 *    /              \
	 *   /                \
	 *  /__________________\
	 *          |  |
	 *          |__|
	 *         
	 * Taihen desu nya!
	 * 
	 * We are expanding this to handle the full hand
	 * I really hate doing this, but it's the only way to really guarantee 
	 * an accurate count
	 * 
	 * DO NOT CALL MULTIPLE TIMES!
	 * This thing will take a long time if the hand is complicated.  Do not dare
	 * try doing something like calling it for each potential discard
	 * 
	 * If we find any 0 branch we should return immediately.
	 * Likewise we should prune any branch that has over 4 (subject to change) unused tiles
	 * The logic behind that:
	 * 		2 Unused tiles could be a tenpai (choice between 2 pair waits)
	 * 		3 Could be 1 or more away, and 4 could be 2 or more 
	 * 		So if we get to 5 we are at least 3 away and any more than that who cares
	 * 
	 * Input: maxAwayFrom - I should change the name of the variable because that's not accurate
	 * 						In reality it sets the pruning aggression of the tree.
	 * 						It is however accurate when you send 0.  It will look for a winning hand or nothing
	 * Output: shanten count
	 */
	public int getShantenCount_TreeVersion(int maxAwayFrom, boolean getTenpaiTiles){
		int[] tileCounts = getTileCounts();
		Tree theEvilTree = new Tree();
		int unusedTiles = 0;
		int ret = 6;
		
		//0 is sort of a special case
		//if(maxAwayFrom != 0){
		//	maxAwayFrom++;
		//}
		if(pMyPlayer.ippatsu || !pMyPlayer.riichi){
			tenpaiTiles.clear();
		}
		/**
		 * We could have multiple hands that are optimal (Ex: we have 1234, it doesn't matter if we discard the 1 or 4)
		 * In the future we should document all of them, but for the time being I just 
		 * want to see what one of them is for testing purposes
		 */

		try{
			optimalHand.clear();
			riichiTiles.clear();
			
			/**
			 * Special Check for stupid hands
			 * 
			 * 13 orphans breaks just about everything we do in the tree calc
			 * So instead of creating a billion exceptions in that code we will just take care
			 * of it before hand.  If we are ever close to 13 orphans then there is no
			 * possible way that the tree will produce a better result
			 * 
			 * That said it will still break the AI, so until something changes
			 * the AI should never, ever try to use 13 orphans
			 * 
			 */
			if(!openHand){
				int awayFrom13Orphans = 14 - Math.min(1, tileCounts[1]) - Math.min(1, tileCounts[9]) - Math.min(1, tileCounts[10]) - Math.min(1, tileCounts[18])
									 	   - Math.min(1, tileCounts[19]) - Math.min(1, tileCounts[27]) - Math.min(1, tileCounts[28]) - Math.min(1, tileCounts[29])
									 	   - Math.min(1, tileCounts[30]) - Math.min(1, tileCounts[31]) - Math.min(1, tileCounts[32]) - Math.min(1, tileCounts[33]) - Math.min(1, tileCounts[34]);
				boolean hasPair = false;
				if(tileCounts[1] > 1){
					awayFrom13Orphans--;
					hasPair = true;
				}
				else if(tileCounts[9] > 1){
					awayFrom13Orphans--;
					hasPair = true;
				}
				else if(tileCounts[10] > 1){
					awayFrom13Orphans--;
					hasPair = true;
				}
				else if(tileCounts[18] > 1){
					awayFrom13Orphans--;
					hasPair = true;
				}
				else if(tileCounts[19] > 1){
					awayFrom13Orphans--;
					hasPair = true;
				}
				else if(tileCounts[27] > 1){
					awayFrom13Orphans--;
					hasPair = true;
				}
				else if(tileCounts[28] > 1){
					awayFrom13Orphans--;
					hasPair = true;
				}
				else if(tileCounts[29] > 1){
					awayFrom13Orphans--;
					hasPair = true;
				}
				else if(tileCounts[30] > 1){
					awayFrom13Orphans--;
					hasPair = true;
				}
				else if(tileCounts[31] > 1){
					awayFrom13Orphans--;
					hasPair = true;
				}
				else if(tileCounts[32] > 1){
					awayFrom13Orphans--;
					hasPair = true;
				}
				else if(tileCounts[33] > 1){
					awayFrom13Orphans--;
					hasPair = true;
				}
				else if(tileCounts[34] > 1){
					awayFrom13Orphans--;
					hasPair = true;
				}
				
				if(awayFrom13Orphans == 1 && getTenpaiTiles){
					if(!pMyPlayer.riichi  || pMyPlayer.ippatsu){
						if(!hasPair){
							tenpaiTiles.add(1);
							tenpaiTiles.add(9);
							tenpaiTiles.add(10);
							tenpaiTiles.add(18);
							tenpaiTiles.add(19);
							tenpaiTiles.add(27);
							tenpaiTiles.add(28);
							tenpaiTiles.add(29);
							tenpaiTiles.add(30);
							tenpaiTiles.add(31);
							tenpaiTiles.add(32);
							tenpaiTiles.add(33);
							tenpaiTiles.add(34);
						}
						else{
							if(tileCounts[1] == 0){
								tenpaiTiles.add(1);
							}
							else if(tileCounts[9] == 0){
								tenpaiTiles.add(9);
							}
							else if(tileCounts[10] == 0){
								tenpaiTiles.add(10);
							}
							else if(tileCounts[18] == 0){
								tenpaiTiles.add(18);
							}
							else if(tileCounts[19] == 0){
								tenpaiTiles.add(19);
							}
							else if(tileCounts[27] == 0){
								tenpaiTiles.add(27);
							}
							else if(tileCounts[28] == 0){
								tenpaiTiles.add(28);
							}
							else if(tileCounts[29] == 0){
								tenpaiTiles.add(29);
							}
							else if(tileCounts[30] == 0){
								tenpaiTiles.add(30);
							}
							else if(tileCounts[31] == 0){
								tenpaiTiles.add(31);
							}
							else if(tileCounts[32] == 0){
								tenpaiTiles.add(32);
							}
							else if(tileCounts[33] == 0){
								tenpaiTiles.add(33);
							}
							else if(tileCounts[34] == 0){
								tenpaiTiles.add(34);
							}
						}
					}
					return awayFrom13Orphans;
				}
				
				if(awayFrom13Orphans < maxAwayFrom){
					return awayFrom13Orphans;
				}
			}
			
			Node root = theEvilTree.getRoot();
			ret = recursiveTreeBuildAndTraverse(root, tileCounts, unusedTiles, maxAwayFrom, getTenpaiTiles, 0, 0);
			pMyPlayer.myAI.setOptimalHand(optimalHand);
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.getShantenCount", WTFAmI);
		}
		
		return ret;
	}
	
	int recursiveTreeBuildAndTraverse(Node parent, int[] tileCounts, int unusedTileCount, int maxAwayFrom, boolean getTenpaiTiles, int incompleteSets, int incPairs){
		try{
			//First things first lets take out the tiles in the parent
			for(int i = 0; i < parent.mySet.size; i++){
				tileCounts[parent.mySet.tiles[i]]--;
			}
			
			//Unused Tile
			if(parent.mySet.size == 1){
				unusedTileCount++;
			}
			
			int maxUnusedTiles = maxAwayFrom;
			if(maxUnusedTiles != 0)
				maxUnusedTiles++;
			
			/**
			 * Experimental for now
			 * I'm not sure if the benefits of pruning will make up for the additional
			 * cost in figuring if it's a mirror
			 * 
			 * The idea is to prune branches that are mirrors of each other
			 * Ex: if we have 11123, We would end up with a 11->123 and 123->11 branch
			 */
			boolean doMirrorPruning = false;
			boolean doAdvancedPruning = true;
			
			if(unusedTileCount > maxUnusedTiles){
				//Prune this branch
				//Do I need to decrement unusedTileCount?
				//Ans: Doesn't seem so, it looks like int's get passed by value
				
				//We're done here, the last thing to do is undo the changes to tileCounts
				for(int i = 0; i < parent.mySet.size; i++){
					tileCounts[parent.mySet.tiles[i]]++;
				}
				return 6;
			}
			if(doAdvancedPruning){
				int alreadyAway = Math.max(0, (unusedTileCount - 1));
				if(incPairs !=  incompleteSets){//ignore 7 pairs
					if(incPairs > 0)
						alreadyAway += Math.max(0, incompleteSets-1);
					else
						alreadyAway += Math.max(0, incompleteSets);
					if(alreadyAway > maxAwayFrom){
						int spacer = 1;
						for(int i = 0; i < parent.mySet.size; i++){
							tileCounts[parent.mySet.tiles[i]]++;
						}
						spacer--;
						return 6;
					}
				}
			}
			
			boolean somethingFound = false;
			ArrayList<Set> stuffToAdd = new ArrayList<Set>();
			for(int thisTile = Tile.BAMBOO_START; thisTile <= Tile.LAST_TILE; thisTile++){
				if(tileCounts[thisTile] == 0)
					continue;
				//Do everything possible!
				somethingFound = true;
				//boolean needUnusedBranch = false;
				
				if(tileCounts[thisTile] >= 3){
					if(doMirrorPruning)
						stuffToAdd.add(new Set(thisTile, thisTile, thisTile));
					else
						parent.addChild(new Set(thisTile, thisTile, thisTile));
				}
				/*Old, we are switching the order to raise efficiency a bit.  Every little bit counts!
				if(tileCounts[thisTile] >= 2){
					if(doMirrorPruning)
						stuffToAdd.add(new Set(thisTile, thisTile));
					else
						parent.addChild(new Set(thisTile, thisTile));
				}
				
				//If there is no possible Chi use do we need the unusedTile branch?
				//Ans: Yes, yes we do
				
				if(Tile.convertRawToSuit(thisTile) <= Globals.Suits.MAN){
					if(Tile.convertRawToRelative(thisTile) <= 8){
						if(Tile.convertRawToRelative(thisTile) <= 7){
							if(tileCounts[thisTile+2] > 0){
								//needUnusedBranch = true;
								if(tileCounts[thisTile+1] > 0){
									if(doMirrorPruning)
										stuffToAdd.add(new Set(thisTile, thisTile+1, thisTile+2));
									else
										parent.addChild(new Set(thisTile, thisTile+1, thisTile+2));
								}
								
								if(doMirrorPruning)
									stuffToAdd.add(new Set(thisTile, thisTile+2));
								else
									parent.addChild(new Set(thisTile, thisTile+2));
							}
						}
						if(tileCounts[thisTile+1] > 0){
							//needUnusedBranch = true;
							if(doMirrorPruning)
								stuffToAdd.add(new Set(thisTile, thisTile+1));
							else
								parent.addChild(new Set(thisTile, thisTile+1));
						}
						
					}
				}*/
				
				//Full Chi
				if(Tile.convertRawToSuit(thisTile) <= Globals.Suits.MAN){
					if(Tile.convertRawToRelative(thisTile) <= 8){
						if(tileCounts[thisTile+1] > 0){
							if(Tile.convertRawToRelative(thisTile) <= 7){
								if(tileCounts[thisTile+2] > 0){
									if(doMirrorPruning)
										stuffToAdd.add(new Set(thisTile, thisTile+1, thisTile+2));
									else
										parent.addChild(new Set(thisTile, thisTile+1, thisTile+2));
								}
							}
						}
					}
				}
				
				//Then Pairs
				if(tileCounts[thisTile] >= 2){
					if(doMirrorPruning)
						stuffToAdd.add(new Set(thisTile, thisTile));
					else
						parent.addChild(new Set(thisTile, thisTile));
				}
				
				//Then partial Chis
				if(Tile.convertRawToSuit(thisTile) <= Globals.Suits.MAN){
					if(Tile.convertRawToRelative(thisTile) <= 8){
						if(tileCounts[thisTile+1] > 0){			
							if(doMirrorPruning)//Partial Chi
								stuffToAdd.add(new Set(thisTile, thisTile+1));
							else
								parent.addChild(new Set(thisTile, thisTile+1));
						}
						if(Tile.convertRawToRelative(thisTile) <= 7){
							if(tileCounts[thisTile+2] > 0){//Middle chi wait
								if(doMirrorPruning)
									stuffToAdd.add(new Set(thisTile, thisTile+2));
								else
									parent.addChild(new Set(thisTile, thisTile+2));
							}
						}
					}
				}
				
				//if(needUnusedBranch)
				if(doMirrorPruning)
					stuffToAdd.add(new Set(thisTile));
				else
					parent.addChild(new Set(thisTile));
				
				break;
			}
			
			int best = maxAwayFrom;
			if(somethingFound){
				
				if(doMirrorPruning){
					Node parentOfMyParent = null;
					if(parent != null)
						parentOfMyParent = parent.myParent;
					
					for(int thisSet = 0; thisSet < stuffToAdd.size(); thisSet++){
						if(parentOfMyParent == null || stuffToAdd.get(thisSet).size == 1){
							parent.addChild(stuffToAdd.get(thisSet));
						}
						else{
							boolean addMe = true;
							//Iterating through this normally screws up the recursion
							for(int idx = 0; idx < parentOfMyParent.getNumChildren(); idx++){
								Node n = parentOfMyParent.getChild(idx);
								if(n == null)
									continue;
								if(n == parent)
									continue;
								if(n.mySet.equals(stuffToAdd.get(thisSet)))
									addMe = false;
							}
							if(addMe)
								parent.addChild(stuffToAdd.get(thisSet));
						}
					}
				}
				
				//My god....the recursiveness...
				for(Node n = parent.getFirst(); n != null; n = parent.getNext()){
					int addToInc = 0;
					int addToPair = 0;
					if(doAdvancedPruning){
						if(n.mySet.size == 2){
							addToInc++;
							if(n.mySet.isPair())
								addToPair++;
						}
					}
					int thisBranch = recursiveTreeBuildAndTraverse(n, tileCounts, unusedTileCount, best, getTenpaiTiles, incompleteSets+addToInc, incPairs+addToPair);
					if(thisBranch < best){
						best = thisBranch;
					}
				}
			}
			else{//We are at a leaf node
				int CompleteSets = numberOfMelds;
				int PartialSets = 0; 
				int Pairs = 0;
				//Ok, let's backtrace this (I need to stop using that term >_> )
				for(Node n = parent; n.myParent != null; n = n.myParent){
					if(n.mySet.isComplete())
						CompleteSets++;
					if(n.mySet.size == 2){
						PartialSets++;
						if(n.mySet.isPair())
							Pairs++;
					}
				}
				
				//Calc the shanten count 
				int tilesAway = 0;
				
				//Normal Hands
				int setsNeeded = 4 - CompleteSets;
				for(int i = 0; i < setsNeeded; i++){
					if(PartialSets > 0){
						tilesAway++;
						PartialSets--;
					}
					else{
						tilesAway += 2;
					}
				}
			
				boolean hasPair = false;
				if(PartialSets >= 1 && Pairs >= 1){
					tilesAway += 0;
					hasPair = true;
				}
				else
					tilesAway += 1;
				
				//7 Pairs is a weird hand >_>
				if(Pairs == 7)
					tilesAway = 0;
				if(Pairs == 6){
					//backtrace!
					/*for(Node n = parent.myParent; n != null; n = n.myParent){
						if(n.mySet.size == 1)
							tenpaiTiles.add(n.mySet.tiles[0]);
					}*/
					if(tilesAway > 1)
						tilesAway = 1;
				}
				if((Pairs == 5)&&(tilesAway > 2))
					tilesAway = 2;
				if((Pairs == 4)&&(tilesAway > 3))
					tilesAway = 3;
				
				//This is counter inuitive but once we are in riichi we do not need to do this anymore
				if(tilesAway == 1 && !pMyPlayer.riichi){
					for(Node n = parent; n != null; n = n.myParent){
						if(n.mySet.size == 1){
							if(!riichiTiles.contains(n.mySet.tiles[0]))
								riichiTiles.add(n.mySet.tiles[0]);
						}
					}
				}
				
				if(getTenpaiTiles){
					if((tilesAway == 1)&&(!pMyPlayer.riichi || pMyPlayer.ippatsu)){
						//backtrace!
						for(Node n = parent; n != null; n = n.myParent){
							if(n.mySet.size == 1 && (!hasPair || Pairs >= 6))
								tenpaiTiles.add(n.mySet.tiles[0]);
							if((n.mySet.size == 2)&&(Pairs < 6)){
								if(n.mySet.tiles[0] == n.mySet.tiles[1] && Pairs > 1)
									tenpaiTiles.add(n.mySet.tiles[0]);
								else if(n.mySet.tiles[0] != n.mySet.tiles[1]){
									if(n.mySet.tiles[0]+1 == n.mySet.tiles[1]){
										if(Tile.convertRawToRelative(n.mySet.tiles[0])>1)
											tenpaiTiles.add(n.mySet.tiles[0]-1);
										if(Tile.convertRawToRelative(n.mySet.tiles[1])<9)
											tenpaiTiles.add(n.mySet.tiles[1]+1);
									}
									else{
										tenpaiTiles.add(n.mySet.tiles[0]+1);
									}
								}
							}
						}
					}
				}
				if(tilesAway == 0){
					//winningHand.add(parent.mySet);
					for(Node n = parent; n != null; n = n.myParent){
						winningHand.add(n.mySet);
					}
				}
				
				//if(best < 3){ 
					if(tilesAway < best){
						optimalHand.clear();
						for(Node n = parent; n != null; n = n.myParent){
							optimalHand.add(n.mySet);
						}
					}
					else if(tilesAway == best){
						//optimalHand.clear();
						for(Node n = parent; n != null; n = n.myParent){
							optimalHand.add(n.mySet);
						}
					}
				//}
				
				//We're done here, the last thing to do is undo the changes to tileCounts
				for(int i = 0; i < parent.mySet.size; i++){
					tileCounts[parent.mySet.tiles[i]]++;
				}
				
				return tilesAway;
				
			}
			
			//We're done here, the last thing to do is undo the changes to tileCounts
			for(int i = 0; i < parent.mySet.size; i++){
				tileCounts[parent.mySet.tiles[i]]++;
			}
			
			return best;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.recursiveTreeBuildAndTraverse", WTFAmI);
			return 6;
		}
	}
	
	
	/**
	 * Replacement for the Player.scoreHand() function
	 * It makes more sense here and all the tree stuff is here anyways
	 * 
	 * We have 3 functions which all do the same thing:
	 * 	scoreHand(tileCounts) is the main one.  The others will just call this
	 * 	scoreHand() will use whatever is in the current hand.  Use it for tsumos.
	 * 	scoreHand(Tile) will be used to verify that a potential Ron has a yaku
	 * 
	 * These are all expensive functions but theoretically they should be called so rarely 
	 * that it won't matter
	 */

	int scoreHand(boolean selfDraw){
		return scoreHand(getTileCounts(), selfDraw, null);
	}
	
	int scoreHand(Tile newTile, boolean selfDraw){
		int[] tileCounts = getTileCounts();
		tileCounts[newTile.rawNumber]++;
		return scoreHand(tileCounts, selfDraw, newTile);
	}
	
	int scoreHand(int[] tileCounts, boolean selfDraw, Tile winningTile){
		Tree theEvilTree = new Tree();
		int unusedTiles = 0;
		
		boolean isOpen = false;
		
		if(winningTile == null && Tile14 != -1)
			winningTile = rawHand[Tile14];
		
		int Pons = 0;
		int Chis = 0;
		int Kans = 0;
		int Pairs = 0;
		int OpenPons = 0;
		int OpenChis = 0;
		int OpenKans = 0;
		int Honors = 0;
		int Dragons = 0;
		int Winds = 0;
		int Terminals = 0;
		int Simples = 0;
		boolean everySetHasTerminal = true;
		boolean twoSidedWait = true;
		boolean valuelessPair = true;
		
		int[] suitCount = new int[] {0,0,0,0,0,0};
		int[] kouChecker = new int[] {0,0,0,0,0,0,0,0,0,0};
		int[][] junChecker = new int[][] {{0,0,0,0,0,0,0,0},
										  {0,0,0,0,0,0,0,0},
										  {0,0,0,0,0,0,0,0},
										  {0,0,0,0,0,0,0,0}};
		
		fu = 0;
		han = 0;
		dora = 0;
		//int[] yaku = new int[Globals.ALLYAKUCOUNT];
		for(int i = 0; i < Globals.ALLYAKUCOUNT; i++){
			yaku[i] = 0;
		}
		int points = 0;
		
		/**
		 * We could have multiple hands that are optimal (Ex: we have 1234, it doesn't matter if we discard the 1 or 4)
		 * In the future we should document all of them, but for the time being I just 
		 * want to see what one of them is for testing purposes
		 */
		
		//try{
			/**
			 * 
			 * Just like before we are going to handle the honors first because
			 * they are simple.  There's no reason this has to be in a tree since it's 
			 * fairly easy to optimize.
			 * 
			 */
		/*	for(int thisTile = Tile.HONOR_START; thisTile <= Tile.LAST_TILE; thisTile++){
				if(tileCounts[thisTile] >= 3){
					Pons++;
					Honors += 3;
					fu += 8;
					
					if(Tile.convertTileToSuit(thisTile) == Globals.SANGEN){
						yaku[Globals.YAKUHAI] += 1;
						Dragons += 3;
					}
					else{
						if(pMyPlayer.currentWind == Tile.convertRawToWind(thisTile))
							yaku[Globals.YAKUHAI] += 1;
						Winds += 3;
					}
				}
				else if(tileCounts[thisTile] == 2){
					Pairs++;
					Honors += 2;
					valuelessPair = false;
					fu += 2;
				}
				else if(tileCounts[thisTile] == 1){
					return 0;
				}
			}
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			WTFAmI.length();
		}*/
		
		try{
			/**
			 * Same deal with the melds
			 */
			for(int i = 0; i < numberOfMelds; i++){
				Tile tile1 = getRawTileAt(melds[i][1]);
				Tile tile2 = getRawTileAt(melds[i][2]);
				Tile tile3 = getRawTileAt(melds[i][3]);
				
				int thisSuit = tile1.getSuit();
				
				/*if(pMyPlayer.pMainGameThread.mTable.isDora(tile1))
					dora += 1;
				if(pMyPlayer.pMainGameThread.mTable.isDora(tile2))
					dora += 1;
				if(pMyPlayer.pMainGameThread.mTable.isDora(tile3))
					dora += 1;
				if(tile1.redTile)
					dora++;
				if(tile2.redTile)
					dora++;
				if(tile3.redTile)
					dora++;*/
				
				boolean anyNonSimples = false;
				if(thisSuit == Globals.Suits.SANGEN){
					yaku[Globals.YAKUHAI] += 1;
					Dragons += 3;
					Honors += 3;
					anyNonSimples = true;
				}
				else if(thisSuit == Globals.Suits.KAZE){
					if(pMyPlayer.isMyWind(tile1))
						yaku[Globals.YAKUHAI] += 1;
					Winds += 3;
					Honors += 3;
					anyNonSimples = true;
				}
				else{
					if(tile1.getType() == Globals.TERMINAL){
						anyNonSimples = true;
						Terminals++;
					}
					else{
						Simples++;
					}
					
					if(tile2.getType() == Globals.TERMINAL){
						anyNonSimples = true;
						Terminals++;
					}
					else{
						Simples++;
					}
					
					if(tile3.getType() == Globals.TERMINAL){
						anyNonSimples = true;
						Terminals++;
					}
					else{
						Simples++;
					}
				}
				if(!anyNonSimples)
					everySetHasTerminal = false;
				
				suitCount[thisSuit] += 3;
					
				if(!tile1.selfKan)
					isOpen = true;
				
				if(tile1.equals(tile2)){//Pons & Kans
					if(melds[i][0] == 4){
						Kans++;
						if(!tile1.selfKan)
							OpenKans++;
						
						suitCount[thisSuit] += 1;
						
						//Tile tile4 = getRawTileAt(melds[i][4]);

						//if(pMyPlayer.pMainGameThread.mTable.isDora(tile4))
						//	dora += 1;
						//if(tile4.redTile)
						//	dora++;
						
						if(thisSuit == Globals.Suits.SANGEN){
							//Dragons += 1; //Don't do this, it screws up yaku checks
							Honors += 1;
							if(!tile1.selfKan)
								fu += 16;
							else
								fu += 32;
						}
						else if(thisSuit == Globals.Suits.KAZE){
							//Winds += 1; //Don't do this, it screws up yaku checks
							Honors += 1;
							if(!tile1.selfKan)
								fu += 16;
							else
								fu += 32;
						}
						else if(tile1.getType() == Globals.TERMINAL){
							Terminals += 1;
							kouChecker[tile1.getNumber()]++;
							if(!tile1.selfKan)
								fu += 16;
							else
								fu += 32;
						}
						else{
							Simples += 1;
							everySetHasTerminal = false;
							kouChecker[tile1.getNumber()]++;
							
							if(!tile1.selfKan)
								fu += 8;
							else
								fu += 16;
						}
						
					}
					else{
						Pons++;
						OpenPons++;
						if(thisSuit == Globals.Suits.SANGEN || thisSuit == Globals.Suits.KAZE){
							fu += 4;
						}
						else if(tile1.getType() == Globals.TERMINAL){
							kouChecker[tile1.getNumber()]++;
							fu += 4;
						}
						else{
							kouChecker[tile1.getNumber()]++;
							fu += 2;
						}
						
					}
				}
				else{//Chis
					Chis++;
					OpenChis++;
					int lowest = tile1.getNumber();
					if(tile2.getNumber() < lowest)
						lowest = tile2.getNumber();
					if(tile3.getNumber() < lowest)
						lowest = tile3.getNumber();
					junChecker[thisSuit][lowest]++;
					
					Simples += 2;
					if(lowest != 1 && lowest != 7){
						Simples++;
						everySetHasTerminal = false;
					}
					else
						Terminals++;
				}
				
			}
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.scoreHand_1", WTFAmI);
		}
		
		try{
			Node root = theEvilTree.getRoot();
			winningHand.clear();
			recursiveTreeBuildAndTraverse/*_CompleteOnly*/(root, tileCounts, unusedTiles, 0, false, 0, 0);
			
			
			//traverseTree/*_CompleteOnly*/(root, Pons+Kans+Chis, 0, Pairs, false);
			
			if(winningHand.isEmpty())
				return 0;
			
			twoSidedWait = false;
			for(int i = 0; i < winningHand.size(); i++){
				Set thisSet = winningHand.get(i);
				if(thisSet.size == 0){
					Globals.myAssert(i == (winningHand.size()-1));
					break;
				}
				int thisSuit = Tile.convertRawToSuit(thisSet.tiles[0]);
				suitCount[thisSuit] += thisSet.size;
				
				if(thisSet.size == 2){
					Pairs++;
					if(thisSuit == Globals.Suits.KAZE){
						Honors += 2;
						Winds += 2;
						if(pMyPlayer.isMyWind(thisSet.tiles[0]))
							valuelessPair = false;
					}
					else if(thisSuit == Globals.Suits.SANGEN){
						Dragons += 2;
						Honors += 2;
						valuelessPair = false;
					}
					else if((Tile.convertRawToRelative(thisSet.tiles[0]) == 1)||
					   (Tile.convertRawToRelative(thisSet.tiles[0]) == 9))
						Terminals += 2;
					else{
						Simples += 2;
						everySetHasTerminal = false;
					}
					
					/*if(Tile14 != -1){
						if(rawHand[Tile14] != null){
							if(thisSet.tiles[0] == rawHand[Tile14].rawNumber)
								twoSidedWait = false;
						}
					}*/
						
				}
				else{
					if(thisSet.tiles[0] == thisSet.tiles[1]){//Pons
						Pons++;
						if(thisSuit == Globals.Suits.KAZE){
							Honors += 3;
							Winds += 3;
							if(pMyPlayer.isMyWind(thisSet.tiles[0]))
								yaku[Globals.YAKUHAI] += 1;
						}
						else if(thisSuit == Globals.Suits.SANGEN){
							Dragons += 3;
							Honors += 3;
							yaku[Globals.YAKUHAI] += 1;
						}
						else if((Tile.convertRawToRelative(thisSet.tiles[0]) == 1)||
						   (Tile.convertRawToRelative(thisSet.tiles[0]) == 9)){
								Terminals += 3;
								fu += 8;
								kouChecker[Tile.convertRawToRelative(thisSet.tiles[0])]++;
						}
						else{
							Simples += 3;
							everySetHasTerminal = false;
							fu += 4;
							kouChecker[Tile.convertRawToRelative(thisSet.tiles[0])]++;
						}
						//kouChecker[Tile.convertRawToRelative(thisSet.tiles[0])]++;
						
						/*if(Tile14 != -1){
							if(rawHand[Tile14] != null){
								if(thisSet.tiles[0] == rawHand[Tile14].rawNumber)
									twoSidedWait = true; //technically true, but not for pinfu
							}
						}*/
						
					}
					else{//Chis
						Chis++;
						int lowestRaw = thisSet.tiles[0];
						int lowest = Tile.convertRawToRelative(thisSet.tiles[0]);
						if(Tile.convertRawToRelative(thisSet.tiles[1]) < lowest){
							lowest = Tile.convertRawToRelative(thisSet.tiles[1]);
							lowestRaw = thisSet.tiles[1];
						}
						if(Tile.convertRawToRelative(thisSet.tiles[2]) < lowest){
							lowest = Tile.convertRawToRelative(thisSet.tiles[2]);
							lowestRaw = thisSet.tiles[2];
						}
						
						junChecker[thisSuit][lowest]++;
						Simples += 2;
						if(lowest != 1 && lowest != 7){
							Simples++;
							everySetHasTerminal = false;
						}
						else
							Terminals++;
						
						if(winningTile != null){
							int middle = lowestRaw + 1;
							int high = middle + 1;
							if(winningTile.rawNumber == lowestRaw){
								if(9 != Tile.convertRawToRelative(high))
									twoSidedWait = true;
							}
							else if(winningTile.rawNumber == high){
								if(1 != Tile.convertRawToRelative(lowestRaw))
									twoSidedWait = true;
							}
						}
						
					}
				}
			}
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.scoreHand_2", WTFAmI);
		}
		
		try{
			//Yaku Time
			if((suitCount[Globals.Suits.BAMBOO] > 0 && suitCount[Globals.Suits.PIN] == 0 && suitCount[Globals.Suits.MAN] == 0)||
			   (suitCount[Globals.Suits.BAMBOO] == 0 && suitCount[Globals.Suits.PIN] > 0 && suitCount[Globals.Suits.MAN] == 0)||
			   (suitCount[Globals.Suits.BAMBOO] == 0 && suitCount[Globals.Suits.PIN] == 0 && suitCount[Globals.Suits.MAN] > 0)){
					if(suitCount[Globals.Suits.SANGEN] == 0 && suitCount[Globals.Suits.KAZE] == 0){
						if(isOpen)
							yaku[Globals.CHINITSU] = 5;
						else 
							yaku[Globals.CHINITSU] = 6;
					}
					else{
						if(isOpen)
							yaku[Globals.HONITSU] = 2;
						else 
							yaku[Globals.HONITSU] = 3;
					}
			}
							
			if(!isOpen && (Terminals == 0) && (Honors == 0))
				yaku[Globals.TANYAO] = 1;
							
			if(Simples == 0)
				yaku[Globals.HONROUTOU] = 2;
					
			if((Chis == 4) && valuelessPair && twoSidedWait && !isOpen)
				yaku[Globals.PINFU] = 1;
					
			if(Pons-OpenPons >= 3)
				yaku[Globals.SANANKOU] = 2;
					
			if(Kans >= 3)
				yaku[Globals.SANKANTSU] = 2;
					
			if(Dragons >= 8)
				yaku[Globals.SHOUSANGEN] = 2;
					
			if(Pairs == 7){
				yaku[Globals.CHIITOI] = 2;
				//fu = 25;
			}
			if(everySetHasTerminal){
				if(Honors == 0){
					if(isOpen)
						yaku[Globals.JUNCHANTAYAO] = 2;
					else
						yaku[Globals.JUNCHANTAYAO] = 3;
				}
				else{
					if(isOpen)
						yaku[Globals.CHANTA] = 1;
					else
						yaku[Globals.CHANTA] = 2;
				}
			}
			if(Pons+Kans == 4){
				yaku[Globals.TOITOI] += 2;
			}
			
			for(int i = 1; i < 10; i++){
				if(kouChecker[i] == 3)
					yaku[Globals.SANSHOKUDOUKOU] = 2;
			}
			
			for(int thisSuit = Globals.Suits.BAMBOO; thisSuit <= Globals.Suits.MAN; thisSuit++){
				if((junChecker[thisSuit][1] > 0)&&(junChecker[thisSuit][4] > 0)&&(junChecker[thisSuit][7] > 0)){
					if(isOpen)
						yaku[Globals.ITSU] = 1;
					else
						yaku[Globals.ITSU] = 2;
				}
					
			}
			
			for(int thisTile = 1; thisTile < 8; thisTile++){
				if((junChecker[Globals.Suits.BAMBOO][thisTile] > 0)&&(junChecker[Globals.Suits.PIN][thisTile] > 0)&&(junChecker[Globals.Suits.MAN][thisTile] > 0)){
					yaku[Globals.SANSHOKUDOUJUN] = 1;
				}
				if(!isOpen){
					if(junChecker[Globals.Suits.BAMBOO][thisTile] >= 2){
						yaku[Globals.IIPEIKOU] += 1;
					}
					if(junChecker[Globals.Suits.PIN][thisTile] >= 2){
						yaku[Globals.IIPEIKOU] += 1;
					}
					if(junChecker[Globals.Suits.MAN][thisTile] >= 2){
						yaku[Globals.IIPEIKOU] += 1;
					}
				}
			}
			
			if(yaku[Globals.IIPEIKOU] == 2)
				yaku[Globals.RYANPEIKOU] = 1;
			
			//Situational Yaku
			if(pMyPlayer.riichi)
				yaku[Globals.RIICHI] = 1;
			if(pMyPlayer.ippatsu)
				yaku[Globals.IPPATSU] = 1;
			if(pMyPlayer.rinshan)
				yaku[Globals.RINSHAN] = 1;
			if(pMyPlayer.robbing)
				yaku[Globals.CHANKAN] = 1;
			
			if(selfDraw && !isOpen)
				yaku[Globals.MENZEN] = 1;
			
			//int wallCount = pMyPlayer.pMainGameThread.mTable.wallCount();
			if(pMyPlayer.pMainGameThread.mTable.wallCount() == 0){
				if(selfDraw)
					yaku[Globals.HAITEI] = 1;
				else
					yaku[Globals.HOUTEI] = 1;
			}
			
			//Lastly count the dora
			for(int thisTile = 0; thisTile < rawHandMax; thisTile++){
				if(rawHand[thisTile] == null)
					continue;
				if(rawHand[thisTile].redTile)
					dora++;
				if(pMyPlayer.pMainGameThread.mTable.isDora(rawHand[thisTile]))
					dora++;
			}
			
			//Yakuman checks
			if(pMyPlayer.pMainGameThread.mTable.getDiscards(pMyPlayer.ID).size() == 0)
				yaku[Globals.TENHOU] = 13;
			if(Dragons >= 9)
				yaku[Globals.DAISANGEN] = 13;
			if(Winds == 11)
				yaku[Globals.SHOUSUUSHII] = 13;
			else if(Winds >= 12)
				yaku[Globals.DAISUUSHII] = 13;
			if(!isOpen && Pons == 4)
				yaku[Globals.SUUANKOU] = 13;
			if(Kans == 4)
				yaku[Globals.SUUKANTSU] = 13;
			if(Simples == 0 && Terminals == 0)
				yaku[Globals.TSUUIISOU] = 13;
			if(Simples == 0 && Honors == 0)
				yaku[Globals.CHINROUTOU] = 13;
			//These yaku are dumb, we have to do extra work to check them
			tileCounts = getTileCounts();
			if(!isOpen){
				if((tileCounts[1] > 0)&&(tileCounts[9] > 0)&&(tileCounts[10] > 0)&&(tileCounts[18] > 0)&&
				   (tileCounts[19] > 0)&&(tileCounts[27] > 0)&&(tileCounts[28] > 0)&&(tileCounts[29] > 0)&&
				   (tileCounts[30] > 0)&&(tileCounts[31] > 0)&&(tileCounts[32] > 0)&&(tileCounts[33] > 0)&&(tileCounts[34] > 0)){
					yaku[Globals.KOKUSHUMUSOU] = 13;
				}
				if((tileCounts[1] >= 3)&&(tileCounts[2] > 0)&&(tileCounts[3] > 0)&&(tileCounts[4] > 0)&&(tileCounts[5] > 0)&&
				   (tileCounts[6] > 0)&&(tileCounts[7] > 0)&&(tileCounts[8] > 0)&&(tileCounts[9] >= 3)){
					yaku[Globals.CHUURENPOUTOU] = 13;
				}
				if((tileCounts[10] >= 3)&&(tileCounts[11] > 0)&&(tileCounts[12] > 0)&&(tileCounts[13] > 0)&&(tileCounts[14] > 0)&&
				   (tileCounts[15] > 0)&&(tileCounts[16] > 0)&&(tileCounts[17] > 0)&&(tileCounts[18] >= 3)){
							yaku[Globals.CHUURENPOUTOU] = 13;
						}
				if((tileCounts[19] >= 3)&&(tileCounts[20] > 0)&&(tileCounts[21] > 0)&&(tileCounts[22] > 0)&&(tileCounts[23] > 0)&&
				   (tileCounts[24] > 0)&&(tileCounts[25] > 0)&&(tileCounts[26] > 0)&&(tileCounts[27] >= 3)){
							yaku[Globals.CHUURENPOUTOU] = 13;
						}
			}
			boolean allGreenChecker = true;
			tileCounts = getRawTileCounts();
			for(int thisTile = 1; thisTile <= Tile.LAST_TILE; thisTile++){
				if(thisTile == 2 || thisTile == 3 || thisTile == 4 || thisTile == 6 || thisTile == 8 || thisTile == 30)
					continue;
				if(tileCounts[thisTile] > 0){
					allGreenChecker = false;
					break;
				}	
			}
			if(allGreenChecker)
				yaku[Globals.RYUUIISOU] = 13;
			
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.scoreHand_3", WTFAmI);
		}
		
		try{
			//Man there are a lot of try blocks in this function
			//Time for the actual score yay!
			int totalYaku = 0;
			for(int i = 0; i < Globals.ALLYAKUCOUNT; i++){
				totalYaku += yaku[i];
			}
			
			if(totalYaku == 0)
				return 0;
			
			han = Math.min(totalYaku + dora, 13);
			
			if(!selfDraw && !isOpen)
				fu += 30;
			else if(yaku[Globals.CHIITOI] != 0)
				fu += 25;
			else
				fu += 20;
			
			if(han >= 5){
				int hanIdx = 0;
				if(han == 5)
					hanIdx = 0;
				else if(han <= 7)
					hanIdx = 1;
				else if(han <= 9)
					hanIdx = 2;
				else if(han <= 12)
					hanIdx = 3;
				else if(han >= 13)
					hanIdx = 4;
				
				if(pMyPlayer.currentWind == Globals.Winds.EAST)
					points = Globals.eastLimitTable[hanIdx];
				else
					points = Globals.otherLimitTable[hanIdx];
			}
			else{
				int fuIdx = 0;
				if(fu == 20)
					fuIdx = 0;
				else if(fu <= 25)
					fuIdx = 1;
				else if(fu <= 30)
					fuIdx = 2;
				else if(fu <= 40)
					fuIdx = 3;
				else if(fu <= 50)
					fuIdx = 4;
				else if(fu <= 60)
					fuIdx = 5;
				else if(fu <= 70)
					fuIdx = 6;
				else if(fu <= 80)
					fuIdx = 7;
				else if(fu <= 90)
					fuIdx = 8;
				else if(fu <= 100)
					fuIdx = 9;
				
				//If this happens then our score is 0 and something went wrong
				//The only case I can think of where this has happened is:
				//Open pinfu, haitei as the only yaku
				if((fuIdx == 0)&&(han == 1)){
					Globals.myAssert(false);
					fuIdx = 1;
				}
				
				if(pMyPlayer.currentWind == Globals.Winds.EAST)
					points = Globals.eastScoreTable[han-1][fuIdx];
				else
					points = Globals.otherScoreTable[han-1][fuIdx];
			}
			
			points += (pMyPlayer.pMainGameThread.mTable.getBonusCount() * 300);
			
			
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Hand.scoreHand_4", WTFAmI);
		}
		
		return points;
	}
}

