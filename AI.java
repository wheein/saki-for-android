package mahjong.riichi;

import java.util.ArrayList;
import java.util.Random;

import android.util.Log;
import android.util.Pair;

/**
 * Basic AI class.  In theory specialized AIs should be derived from this
 *
 */
public class AI extends /*Handler*/Thread {
	
	//Statics to be used for messages.  
	static int DISCARD = 1;
	static int TSUMO = 2;
	static int CALL = 3;
	static int RIICHI = 4;
	static int SELFKAN = 5;
	
	/**The idea is to track how far away we are from each yaku and go for whatever we are close to
	 * for the time being we will ignore yakuman, in 99% of games it won't make a difference anyways
	 * If/When everything else is working right we'll add them in
	 * 
	 * Other Notes:
	 * The following awayFrom calcs aren't 100% accurate: Chanta, Pinfu, Junchantayao
	 * 
	 * See: setupAwayFrom()
	 */
	protected int[] awayFrom;
	protected int[] Test_awayFrom;
	
	/**
	 * Connections (the name made more sense in the past >_>) is what will determine what 
	 * we discard.  We assign a value to each tile based on what yaku we are going for 
	 * (ie if we are going for ToiToi we will place a high value on pairs/pons and devalue
	 * chis) and what tiles are around it (ie value a 3/4 combo higher than a solitary 8)
	 * 
	 * See: setupConnections(ArrayList<Integer>)
	 * 
	 */
	protected int[] Connections;
	protected int[] Test_Connections;
	
	//Thread execution controls
	private boolean mRunning;
	protected boolean hasChanged;
	private boolean runDrawScript;
	private boolean runDiscardScript;
	private boolean runPostScript;
	
	/**
	 * Shantan/Tenpai Info
	 */
	public int ShantanCount;
	ArrayList<Set> optimalHand;
	
	/**
	 * Random switches
	 * 
	 * BailMode - pure defense
	 * LockMode - Only discard things that lower our shanten count
	 */
	private boolean inBailMode;
	protected boolean inLockMode;
	public boolean bCallEverythingMode;
	
	/**
	 * The idea here is that when we are within the threshold we will aim for that yaku
	 * If there is a conflict/more then one threshold hit: priority will determine which is more important
	 * 
	 * Thresholds:
	 * 		These are really finicky.  Set it too high and the yaku will never be used.
	 * 		Set it too low and it will always get used (ie if you set Yakuhai to 2 then 
	 * 		you will end up holding onto single dragon tiles forever)
	 * 
	 * 		Default Values (set to 3 unless I have a reason to change it):
	 * 			PINFU = 3
				TANYAO = 3
				IIPEIKOU = 1 (I don't really like it triggering for things like 1123)
				YAKUHAI = 1 (otherwise we will place a lot of value on single dragon tiles)
				SANSHOKUDOUJUN = 2 (Triggering too often IMO)
				ITSU = 2 (personal preference)
				CHANTA = 3
				HONROUTOU = 3
				TOITOI = 4 (at 3 it never gets used)
				SANANKOU = 2 (It interferes with Toitoi and Chitoi)
				SANKANTSU = 3
				SANSHOKUDOUKOU = 2 (I found it triggers too often at 3)
				CHIITOI = 1 (Good god, if it's set higher the AI will go for this wayyy too often)
				SHOUSANGEN = 3
				HONITSU = 3
				JUNCHANTAYAO = 3
				RYANPEIKOU = 3
				CHINITSU = 3
	 * 
	 * Priority:
	 * 		I'm not sure how exactly these will be used yet.  As of this moment it only
	 * 		effects our decision on what we will call.  We will only pay attention to
	 * 		the highest priority yaku when calling something.  May change...
	 * 		This causes issues because we will almost never open up our hand for 
	 * 		something like Yakuhai.  So we get stuck in places where we can't get
	 * 		any yaku
	 * 
	 * 		We may use it to scale the values in setupConnections.  TBD		
	 * 
	 * 		Default Order (yaku worth more should take priority...in theory):
	 * 			SHOUSANGEN
	 * 			CHINITSU
	 * 			JUNCHANTAYAO
	 * 			RYANPEIKOU
	 * 			SANSHOKUDOUKOU
	 * 			HONITSU
	 * 			SANANKOU
	 * 			TOITOI
	 * 			SANKANTSU
	 * 			CHIITOI
	 * 			HONROUTOU
	 * 	 		ITSU
	 * 			PINFU
	 * 			CHANTA
	 * 			IIPEIKOU
	 * 			SANSHOKUDOUJUN
	 * 			TANYAO
	 * 			YAKUHAI	
	 */
	protected int[] thresholds;
	protected int[] priority;
	
	//These get some special values to help us assign multiple cases to 1 tile
	private static int ITSUWEIGHT = 0x00000001;
	protected static int DOUJUNWEIGHT = 0x00000002;
	protected static int DOUKOUWEIGHT = 0x00000004;
	private static int IIPEIKOUWEIGHT = 0x00000008;
	private static int RYANPEIKOUWEIGHT = 0x00000016;
	protected int[] individualTileWeights; 
	protected int[] individualSuitValue; 
	
	/**
	 * Helpers for calling tiles
	 */
	protected int[] tilesToUse;
	
	//It's REALLY important that this gets set
	protected int myID;
	
	//Random number generator
	Random randGenerator;
	
	//Pointers to other classes
	MainGameThread pGameThread;
	Player pMyPlayer;
	
	/**
	 * We SHOULD be able to do this with messages but I'm having a lot of trouble with that
	 * So we will used these to pass info back and forth.
	 * Look into doing this properly in the future
	 */
	protected int output;
	protected boolean outputReady;
	public void requestOutput(int cmd){
		if(cmd == DISCARD){
			output = handleDiscard();
			outputReady = true;
		}
		else if(cmd == TSUMO){
			try{
				while(hasChanged)
					sleep(100);
			}
			catch(Exception e){
				
			}
			if(canCallTsumo()){
				output = 1;
				outputReady = true;
			}
			else{
				output = 0;
				outputReady = true;
			}
		}
		else if(cmd == CALL){
			output = handleCall();
			outputReady = true;
		}
		else if(cmd == RIICHI){
			try{
				while(hasChanged)
					sleep(100);
			}
			catch(Exception e){
				
			}
			if(ShantanCount == 1 && !pMyPlayer.myHand.openHand){
				if(!inLockMode){
					output = 0;
					outputReady = true;
				}
				else if(pGameThread.mTable.wallCount() > 7){
					output = 1;
					outputReady = true;
				}
				else{
					output = 0;
					outputReady = true;
				}
			}
			else{
				output = 0;
				outputReady = true;
			}
		}
		else if(cmd == SELFKAN){
			int[] tileCounts = pMyPlayer.myHand.getTileCounts();
			for(int thisMeld = 0; thisMeld < pMyPlayer.myHand.numberOfMelds; thisMeld++){
				if(pMyPlayer.myHand.rawHand[pMyPlayer.myHand.melds[thisMeld][1]].rawNumber == pMyPlayer.myHand.rawHand[pMyPlayer.myHand.melds[thisMeld][2]].rawNumber){
					tileCounts[pMyPlayer.myHand.rawHand[pMyPlayer.myHand.melds[thisMeld][1]].rawNumber] += 3;
				}
			}
			int tileToCall = -1;
			for(int thisTile = 1; thisTile <= Tile.LAST_TILE; thisTile++){
				if(tileCounts[thisTile] == 4){
					tileToCall = thisTile;
					break;
				}
			}
			if(tileToCall == -1){
				output = 0;
				outputReady = true;
				return;
			}
			
			//If we are in riichi we have to make sure we can actually do this
			//Treat Lock Mode/tenpai as riichi
			if(pMyPlayer.riichi || (pMyPlayer.myHand.inTenpai && inLockMode)){
				Globals.myAssert(false); //delete this, just a check to make sure this works
				if(!optimalHand.isEmpty()){
					boolean okToKan = true;
					for(int setIter = 0; setIter < optimalHand.size(); setIter++){
						Set thisSet = optimalHand.get(setIter);
						if(thisSet.isComplete() && thisSet.isChi()){
							//if it's part of a chi we can not call it
							if(thisSet.tiles[0] == tileToCall || thisSet.tiles[1] == tileToCall || thisSet.tiles[2] == tileToCall){
								okToKan = false;
								break;
							}
						}
					}
					
					if(okToKan){
						//We should be fine calling Kan
						output = 1;
						outputReady = true;
						return;
					}
					
					//There was a chi with the tile, we can't call kan
					output = 0;
					outputReady = true;
					return;
				}
				else{
					//Something went wrong, we don't know what our optimal hand is
					//Just discard it to be safe
					output = 0;
					outputReady = true;
					return;
				}
			}
			
			tileCounts = pMyPlayer.myHand.getTileCounts();
			Integer primaryYaku = -1;
			for(int i = 0; i < Globals.AIYAKUCOUNT; i++){
				int yakuToCheck = priority[i];
				if(awayFrom[yakuToCheck] <= thresholds[yakuToCheck]){
					if(primaryYaku == -1){
						primaryYaku = yakuToCheck;
						break;
					}
					//break;
				}
			}
			if(primaryYaku == Globals.TOITOI || primaryYaku == Globals.SANKANTSU || primaryYaku == Globals.SANANKOU || primaryYaku == Globals.HONROUTOU || primaryYaku == Globals.SHOUSANGEN){
				output = 1;
				outputReady = true;
				return;
			}
			//If it's a dragon/wind always do it
			if(Tile.convertRawToSuit(tileToCall) == Globals.Suits.SANGEN || Tile.convertRawToSuit(tileToCall) == Globals.Suits.KAZE){
				output = 1;
				outputReady = true;
				return;
			}
			if(Tile.convertRawToSuit(tileToCall - 1) == Tile.convertRawToSuit(tileToCall)){
				if(tileCounts[tileToCall - 1] > 0){
					output = 0;
					outputReady = true;
					return;
				}
			}
			if(Tile.convertRawToSuit(tileToCall + 1) == Tile.convertRawToSuit(tileToCall)){
				if(tileCounts[tileToCall + 1] > 0){
					output = 0;
					outputReady = true;
					return;
				}
			}
			output = 1;
			outputReady = true;
		}
	}
	
	public int getOutput(){
		if(!outputReady){
			Globals.myAssert(false);
			return -1;
		}
		outputReady = false;
		return output;
	}
	
	
	AI(){
		super("UnassignedAI");
		myID = 0;
		randGenerator = new Random();
	}
	
	AI(Integer ID){
		super("AI#"+ID.toString());
		myID = ID;
		mRunning = true;
		randGenerator = new Random();
		init();
	}
	
	public void setGameThread(MainGameThread useThis){
		Globals.myAssert(useThis != null);
		pGameThread = useThis;
		pMyPlayer = pGameThread.mPlayers[myID];
	}
	
	public void run(){
		try{
			while(mRunning){
				if(/*hasChanged*/runDrawScript){
					boolean ippatsuSaver = pMyPlayer.ippatsu;
					pMyPlayer.ippatsu = false;
					ShantanCount = pMyPlayer.myHand.getShantenCount_TreeVersion(3, false);
					pMyPlayer.ippatsu = ippatsuSaver;
					handlePowersAtDraw();
					setupAwayFrom();
					chooseApproach();
					runDrawScript = false;
					hasChanged = false;
				}
				else if(runDiscardScript){
					pMyPlayer.myHand.setupTilesToCall();
					handlePowersAtDiscard();
					runDiscardScript = false;
					hasChanged = false;
				}
				else if(runPostScript){
					inLockMode = false;
					inBailMode = false;
					ShantanCount = 3;
					runPostScript = false;
					hasChanged = false;
				}
				else if(hasChanged){
					//We got into some kind of thread-locked situation
					Globals.myAssert(false);
					hasChanged = false;
				}
				else{
					sleep(500); //only check every 0.5 seconds
				}
			}
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			WTFAmI.length();
		}
		//We aren't doing anything here, that's ok right?
	}
	
	public void init(){
		awayFrom = new int[] {14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14};
		Test_awayFrom = new int[] {14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14,14};
		Connections = new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		Test_Connections = new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		thresholds = new int[] {3, //Pinfu
								3, //Tanyao
								1, //Iipeikou
								1, //Yakuhai
								2, //Sanshoku Doujun
								2, //Itsu
								3, //Chanta
								3, //Honroutou
								4, //Toitoi
								2, //Sanankou
								3, //Sankantsu
								2, //Sanshoku Doukou
								1, //Chiitoi
								3, //Shousangen
								3, //Honitsu
								3, //Junchantayao
								3, //Ryanpeikou
								3};//Chinitsu
		
		//as the default we will set the bigger yaku to be higher priority
		priority = new int[] {Globals.SHOUSANGEN,
							  Globals.CHINITSU,
							  Globals.JUNCHANTAYAO,
							  Globals.RYANPEIKOU,
							  Globals.SANSHOKUDOUKOU,
							  Globals.SANANKOU,
							  Globals.TOITOI,
							  Globals.SANKANTSU,
							  Globals.CHIITOI,
							  Globals.HONROUTOU,
							  Globals.HONITSU, //Same principle as tanyao (ex. I had really good toitoi hand broken up for this.  We had 4 pairs and a completed pon of a different suit, we threw away all 3 of those tiles for now good reason)
							  Globals.ITSU,
							  Globals.PINFU,
							  Globals.CHANTA,
							  Globals.IIPEIKOU,
							  Globals.SANSHOKUDOUJUN,
							  Globals.TANYAO,//Dropped down here because it was causing us to break up more interesting hands (ex.  we had a 112233 iipeikou but broke it up for tanyao)
							  Globals.YAKUHAI};
		individualSuitValue = new int[] {0,0,0,0,0,0}; //Only important for flushes
		individualTileWeights = new int[] {0,
										   0,0,0,0,0,0,0,0,0,
										   0,0,0,0,0,0,0,0,0,
										   0,0,0,0,0,0,0,0,0,
										   0,0,0,0,
										   0,0,0};//These are important for things like ShouSanGen and SanShokuDouKou

		tilesToUse = new int[] {0,0,0,0};
		optimalHand = new ArrayList<Set>();
		inBailMode = false;
		inLockMode = false;
		bCallEverythingMode = false;
		hasChanged = false;
	}
	
	public void stopThread(){
		mRunning = false;
	}
	
	//analyzeHand has been replaced with setupAwayFrom()
	
	//Handlers to generate expected output
	protected int handleDiscard(){
		try{
			while(hasChanged){ //analyzeHand isn't done yet, if this happens a lot we should look into a change
				sleep(100);
			}
			
			/**
			 * OK the idea here is to see what yaku we are close to first
			 * Then use that in combination with the connections array
			 * to choose a discard
			 * 
			 * We will have 3 different "modes" so to speak
			 * Bail Mode: Discard only "safe" tiles
			 * General Mode: we are 3 or more away from winning or have no yaku, we will just use the connections array
			 * Lock Mode: If we are <= 2 away we will only consider tiles that are unused in SOME optimal hand
			 */
			if(inBailMode && !pMyPlayer.powerActivated[Globals.Powers.pureVision]){
				/**
				 * We Have 2 sub-modes ( >_> hurray for complicated things)
				 * 
				 * If only 1 person is in riichi we will specifically gaurd against them
				 * Otherwise play the safest tile in general
				 * 
				 * If we have the "See All" power activated we can keep playing offense
				 */
				if(pGameThread.getNumberInRiichi(myID) == 1){
					int player = myID;
					for(player = ((player+1)%4); player != myID; player = ((player+1)%4)){
						if(pGameThread.mPlayers[player].riichi)
							break;
					}
					int[] discardCounts = pGameThread.mTable.getDiscardCounts(player);
					for(int activeIdx = 0; activeIdx < pMyPlayer.myHand.activeHandMap.size(); activeIdx++){
						Tile thisTile = pMyPlayer.getActiveTileAt(activeIdx);
						if(discardCounts[thisTile.rawNumber] > 0)
							return activeIdx;
					}
				}
					
				int[] discardCounts = pGameThread.mTable.getAllDiscardCounts(myID);
				int highest = 0;
				int bestIdx = -1;
				for(int activeIdx = 0; activeIdx < pMyPlayer.myHand.activeHandMap.size(); activeIdx++){
					Tile thisTile = pMyPlayer.getActiveTileAt(activeIdx);
					if(discardCounts[thisTile.rawNumber] > 0){
						if(discardCounts[thisTile.rawNumber] > highest){
							highest = discardCounts[thisTile.rawNumber];
							bestIdx = activeIdx;
						}
					}
				}
				if(bestIdx != -1)
					return bestIdx; //if this doesn't trip then just use general mode
			}
			
			ArrayList<Integer> goingFor = new ArrayList<Integer>();
			int primaryYaku = -1;
			for(int i = 0; i < Globals.AIYAKUCOUNT; i++){
				int yakuToCheck = priority[i];
				if(awayFrom[yakuToCheck] <= thresholds[yakuToCheck]){
					//Special case for honitsu, don't include it if chinitsu is already there
					if(yakuToCheck == Globals.HONITSU && goingFor.contains(Globals.CHINITSU))
						continue;
					goingFor.add(yakuToCheck);
					if(primaryYaku == -1)
						primaryYaku = yakuToCheck;
				}
			}
			
			setupConnections(goingFor);
			//setupTestConnections(goingFor);
			
			/**
			 * If we can see people's hands we will play perfect defense
			 * 
			 * It SHOULD be in MihokoAI, but the way this function is set up makes that awkward.
			 * It was just easier to put it here.
			 */
			if(pMyPlayer.powerActivated[Globals.Powers.pureVision]){
				int rightPlayer = (myID+1)%4;
				int acrossPlayer = (myID+2)%4;
				int leftPlayer = (myID+3)%4;
				if(pGameThread.mPlayers[rightPlayer].myHand.inTenpai ||
				   pGameThread.mPlayers[acrossPlayer].myHand.inTenpai ||
				   pGameThread.mPlayers[leftPlayer].myHand.inTenpai)
				{
					for(int conIdx = 0; conIdx < 14; conIdx++){
						Tile thisTile = pMyPlayer.myHand.getTileFromActiveIdx(conIdx);
						if(thisTile == null)
							continue;
						if(pGameThread.mPlayers[rightPlayer].myHand.tenpaiTiles.contains(thisTile.rawNumber) ||
						   pGameThread.mPlayers[acrossPlayer].myHand.tenpaiTiles.contains(thisTile.rawNumber) ||
						   pGameThread.mPlayers[leftPlayer].myHand.tenpaiTiles.contains(thisTile.rawNumber))
						{
							//Never discard this tile, it's someone's ron tile
							Connections[conIdx] = 99;
						}
					}
				}
				
			}
			int lowest = 1000;
			int idx = 0;
			
			/**
			 * Case 2
			 */
			//if(pMyPlayer.riichi){
			ArrayList<Integer> UnusedTiles = new ArrayList<Integer>();
			//ArrayList<Set> PartialSets = new ArrayList<Set>();
			//ArrayList<Integer> FuritenEmptyOffsets = new ArrayList<Integer>();
			int[] tileCounts = pMyPlayer.myHand.getTileCounts();
			
			if(inLockMode){
				int i = 0;
				int numberOfPairs = 0;
				while(i < optimalHand.size()){
					Set thisSet = optimalHand.get(i);
					if(thisSet.size == 1){
						//Unused Tile
						if(!UnusedTiles.contains(thisSet.tiles[0]))
							UnusedTiles.add(thisSet.tiles[0]);
					}
					else if(thisSet.isPair()){
						numberOfPairs++;
					}
					else if(thisSet.size == 0 && (i+1)!=optimalHand.size()){
						//it's not 7 pairs
						if(numberOfPairs > 0)
							numberOfPairs = 2; //doesn't matter what it is as long as it's not 0 or 6
					}
					
					i++;
				}
				//}
				//Should only happen once, when riichi is first called
				if(UnusedTiles.size() < 1){
					Globals.myAssert(false);
				}
				else{
					
					if(ShantanCount == 1){
						/**
						 * Trying something different now
						 * We will assign a value to get potential discard based on what the tenpai tiles 
						 * would be.  
						 * 
						 * Plus: No more ridiculous furitens/terrible waits
						 * Minus: We aren't looking at potential yaku
						 */
						int[] discardCounts = pGameThread.mTable.getAllDiscardCounts(myID);
						int[] myDiscardCounts = pGameThread.mTable.getDiscardCounts(myID);
						int[] meldCounts = pGameThread.getMeldCounts();
						i = 0;
						int[] addToConnections = new int[UnusedTiles.size()];
						ArrayList<Pair<Integer, Integer>> alreadySeenTiles = new ArrayList<Pair<Integer, Integer>>();
						
						if(UnusedTiles.size() > 1){ //If we only have one choice then this is stupid
							int startAt = 0;
							for(int thisSet = 0; thisSet < optimalHand.size(); thisSet++){
								if(startAt == -1)
									startAt = thisSet;
								
								//OK startAt -> thisSet is one optimal hand
								if(optimalHand.get(thisSet).size == 0){
									
									boolean[] unusedTileForThisHand = new boolean[UnusedTiles.size()];
									//We need to count the pairs first, f-ing pairs
									int Pairs = 0;
									for(int setNum = startAt; setNum < thisSet; setNum++){
										Set tempSet = optimalHand.get(setNum);
										if(tempSet.isPair())
											Pairs++;
										if(tempSet.size == 1){
											//OK now figure out which of our unused tiles is in this hand
											//If both then, again, this is stupid
											int boolIdx = UnusedTiles.indexOf(tempSet.tiles[0]);
											if(boolIdx >= 0)
												unusedTileForThisHand[boolIdx] = true;
										}
									}
									
									ArrayList<Integer> localTenpaiTiles = new ArrayList<Integer>();
									//Ok now that that nonsense is out of the way figure out the tenpai tiles for this hand
									for(int setNum = startAt; setNum < thisSet; setNum++){
										Set tempSet = optimalHand.get(setNum);
										if(tempSet.size == 1 && (Pairs == 0 || Pairs >= 6))
											localTenpaiTiles.add(tempSet.tiles[0]);
										if((tempSet.size == 2)&&(Pairs < 6)){
											if(tempSet.tiles[0] == tempSet.tiles[1] && Pairs > 1)
												localTenpaiTiles.add(tempSet.tiles[0]);
											else if(tempSet.tiles[0] != tempSet.tiles[1]){
												if(tempSet.tiles[0]+1 == tempSet.tiles[1]){
													if(Tile.convertRawToRelative(tempSet.tiles[0])>1)
														localTenpaiTiles.add(tempSet.tiles[0]-1);
													if(Tile.convertRawToRelative(tempSet.tiles[1])<9)
														localTenpaiTiles.add(tempSet.tiles[1]+1);
												}
												else{
													localTenpaiTiles.add(tempSet.tiles[0]+1);
												}
											}
										}
									}
									
									//Special case for 7 pairs....it really is a stupid hand
									if(Pairs == 6){
										for(int thisTile = 0; thisTile < UnusedTiles.size(); thisTile++){
											if(tileCounts[UnusedTiles.get(thisTile)] == 3)
												addToConnections[thisTile] += 99;
										}
									}
									int giveMeSomeWhereToPutABreak = 1;
									boolean bPairWaits = true;
									for(int boolIter = 0; boolIter < UnusedTiles.size(); boolIter++){
										if(!unusedTileForThisHand[boolIter]){
											bPairWaits = false;
											break;
										}
									}
									if(bPairWaits){
										for(int boolIter = 0; boolIter < UnusedTiles.size(); boolIter++){
											//The values here have to be opposite
											//Normally if we discard a tile it will put us in furiten, here keeping it will
											int rawNum = UnusedTiles.get(boolIter);
											int tilesLeft = -(4 - tileCounts[rawNum] - discardCounts[rawNum] - meldCounts[rawNum]);
											if(myDiscardCounts[rawNum] > 0)
												tilesLeft = 25;
											if(!alreadySeenTiles.contains(new Pair<Integer, Integer>(boolIter, rawNum))){
												alreadySeenTiles.add(new Pair<Integer, Integer>(boolIter, rawNum));
												addToConnections[boolIter] += tilesLeft;
											}
										}
									}
									else{
										for(int thisTile = 0; thisTile < localTenpaiTiles.size(); thisTile++){
											int rawNum = localTenpaiTiles.get(thisTile);
											int tilesLeft = 4 - tileCounts[rawNum] - discardCounts[rawNum] - meldCounts[rawNum];
											if(myDiscardCounts[rawNum] > 0)
												tilesLeft = -25;
											for(int boolIter = 0; boolIter < UnusedTiles.size(); boolIter++){
												if(unusedTileForThisHand[boolIter]){
													if(!alreadySeenTiles.contains(new Pair<Integer, Integer>(boolIter, rawNum))){
														alreadySeenTiles.add(new Pair<Integer, Integer>(boolIter, rawNum));
														addToConnections[boolIter] += tilesLeft;
													}
												}
											}
										}
									}
									startAt = -1;
								}
							}
							//Delete this and use addToConnections
							Globals.myAssert(false);
							//Consider doubling these up =/ 
							//I think this will have minimal effect, besides in furitens.
							//But I also think that may be ok.  The difference between 3 and 4 available tiles really isn't a big deal
							for(int thisTile = 0; thisTile < pMyPlayer.myHand.activeHandMap.size(); thisTile++){
								ActiveHandPair thisPair = pMyPlayer.myHand.activeHandMap.get(thisTile);
								Tile tempTile = pMyPlayer.myHand.rawHand[thisPair.rawHandIdx];
								int unusedTileIdx = UnusedTiles.indexOf(tempTile.rawNumber);
								if(unusedTileIdx != -1){ //I this this should be negative
									Connections[thisTile] -= addToConnections[unusedTileIdx];
								}
							}
						}
						
						/*
						while(i < optimalHand.size()){
							Set thisSet = optimalHand.get(i);
							if(thisSet.size == 1){
								//Unused Tile
								if(!UnusedTiles.contains(thisSet.tiles[0]))
									UnusedTiles.add(thisSet.tiles[0]);
							}
							else if(thisSet.isPair()){
								numberOfPairs++;
							}
							else if(thisSet.size == 0 && (i+1)!=optimalHand.size()){
								//it's not 7 pairs
								if(numberOfPairs > 0)
									numberOfPairs = 2; //doesn't matter what it is as long as it's not 0 or 6
							}
							
							i++;
						}
						
						if(numberOfPairs == 6){ //does this need to be >= ?
							//First make sure we don't have a pon...if we have 2 pons, then well we're fucked, but odds are that we would have switched to toi toi at that point
							for(int thisSet = 0; thisSet < optimalHand.size(); thisSet++){
								Set tempSet = optimalHand.get(thisSet);
								if(tempSet.isPair() && UnusedTiles.contains(tempSet.tiles[0])){
									int rawIdx = pMyPlayer.myHand.getFirstTile(tempSet.tiles[0]);
									int activeHandIdx = pMyPlayer.myHand.rawToActiveIdx(rawIdx);
									return activeHandIdx;
								}
							}
						}
						
						//if we have a 7 pairs hand and the above did not return then it should flow into here
						if(numberOfPairs == 0 || numberOfPairs == 6){
							//In even case we must pick a single tile to wait for a pair
							//int[] myDiscardCounts = pGameThread.mTable.getDiscardCounts(myID);
							//First things first lets avoid furiten
							if(UnusedTiles.size() > 0){
								for(int thisTile = 0; thisTile < UnusedTiles.size(); thisTile++){
									if(myDiscardCounts[UnusedTiles.get(thisTile)] > 0){
										//found one return it
										int rawIdx = pMyPlayer.myHand.getFirstTile(UnusedTiles.get(thisTile));
										int activeHandIdx = pMyPlayer.myHand.rawToActiveIdx(rawIdx);
										return activeHandIdx;
									}
								}
								
								int[] theRestDiscardCounts = pGameThread.mTable.getAllDiscardCounts(myID);
								int lowestCount = theRestDiscardCounts[UnusedTiles.get(0)];
								int lowestTile = UnusedTiles.get(0);
								for(int thisTile = 1; thisTile < UnusedTiles.size(); thisTile++){
									if(theRestDiscardCounts[UnusedTiles.get(thisTile)] < lowestCount){
										lowestCount = theRestDiscardCounts[UnusedTiles.get(thisTile)];
										lowestTile = UnusedTiles.get(thisTile);
									}
								}
								int rawIdx = pMyPlayer.myHand.getFirstTile(lowestTile);
								int activeHandIdx = pMyPlayer.myHand.rawToActiveIdx(rawIdx);
								return activeHandIdx;
							}
						}*/
					}
					for(i = 0; i < UnusedTiles.size(); i++){
						int thisTile = UnusedTiles.get(i);
						if(thisTile == -1){
							continue;
						}
						for(int rawIdx = pMyPlayer.myHand.getFirstTile(thisTile); rawIdx != -1; rawIdx = pMyPlayer.myHand.getNextTile(thisTile, rawIdx)){
							int activeHandIdx = pMyPlayer.myHand.rawToActiveIdx(rawIdx);
							
							if(activeHandIdx < 0)
								continue; //Something went wrong =( 

							if(Connections[activeHandIdx] < lowest){
								idx = activeHandIdx;//pMyPlayer.myHand.activeHand[i]; 
								lowest = Connections[activeHandIdx];
							}
							
							//Special case for 7-Pairs.  We have to break up Pons
							if(primaryYaku == Globals.CHIITOI){
								if(tileCounts[pMyPlayer.getRawTileAt(rawIdx).rawNumber] == 3){
									idx = activeHandIdx;//pMyPlayer.myHand.activeHand[i]; 
									lowest = -99;;
								}
							}
						}
					}
					return idx;
				}
			}
			
			//General Mode
			//Special case for 1 away - don't break up the pair!
			int ignorePair = -1;
			if(ShantanCount == 1){
				int i = 0;
				while(i < optimalHand.size()){
					Set thisSet = optimalHand.get(i);
					if(thisSet.isPair()){
						if(ignorePair == -1)
							ignorePair = thisSet.tiles[0];
						else //If there are 2 pairs then ignore
							ignorePair = -1;
					}
					i++;
				}
			}
			
			int ignorePairCount = 2;
			for(int i = 0; i < pMyPlayer.myHand.activeHandMap.size(); i++){
				if(ignorePair > 0){
					if(ignorePair == pMyPlayer.myHand.getRawTileAt(i).rawNumber){
						if(ignorePairCount > 0){
							ignorePairCount--;
							continue;
						}
					}
				}
				if(Connections[i/*pMyPlayer.myHand.activeHand[i]*/] < lowest){
					idx = i;//pMyPlayer.myHand.activeHand[i]; 
					lowest = Connections[i];
				}
				else if(Connections[i] == lowest){
					int rand = randGenerator.nextInt(2);
					if(rand == 2){
						idx = i;//pMyPlayer.myHand.activeHand[i]; 
						lowest = Connections[i];
					}
				}
				//if(primaryYaku == Globals.CHIITOI){
				//	if(tileCounts[pMyPlayer.myHand.getRawTileAt(i).rawNumber] == 3){
				//		idx = i;//pMyPlayer.myHand.activeHand[i]; 
				//		lowest = -99;;
				//	}
				//}
			}
			
			Globals.myAssert(idx >= 0);
			return idx;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("AI.handleDiscard", WTFAmI);
			return 0;
		}
	}
	
	protected int handleCall(){
		/**
		 * This is going to be really limited for the time being.  I'm not sure exactly how to
		 * handle this intelligently.  Basically the only way they will open up is if Toi Toi,
		 * Chanta, Shou Sangen, or Yakuhai are your highest priority yaku
		 */
		try{
			while(hasChanged){ //analyzeHand isn't done yet, if this happens a lot we should look into a change
				sleep(100);
			}
			
			Tile lastDiscard = pGameThread.mTable.getLastDiscard();
			Globals.myAssert(lastDiscard != null);
			
			//Can we even call this?
			boolean pon = pMyPlayer.myHand.canCallPon(lastDiscard);
			boolean chi = pMyPlayer.myHand.canCallChi(lastDiscard) && (myID == ((pGameThread.curPlayer+1)%4));
			boolean kan = pMyPlayer.myHand.canCallKan(lastDiscard);
			boolean ron = pMyPlayer.myHand.canCallRon(lastDiscard);
			
			if(ron)
				return Globals.CMD.RON; //don't bother going farther
			
			if(pMyPlayer.riichi){
				pon = false;
				chi = false;
				kan = false;
			}
			
			if(!pon && !chi && !kan/* && !ron*/)
				return -1;
			
			Integer primaryYaku = -1;
			Integer subYaku = -1;
			for(int i = 0; i < Globals.AIYAKUCOUNT; i++){
				int yakuToCheck = priority[i];
				if(awayFrom[yakuToCheck] <= thresholds[yakuToCheck]){
					if(primaryYaku == -1)
						primaryYaku = yakuToCheck;
					else{
						subYaku = yakuToCheck;
						break;
					}
					//break;
				}
			}
			
			//int[] tiles = new int[] {-1,-1,-1};
			tilesToUse[0] = -1;
			tilesToUse[1] = -1;
			tilesToUse[2] = -1;
			
			if(bCallEverythingMode){
				if(pon)
					return Globals.CMD.PON;
				if(kan)
					return Globals.CMD.KAN;
				/*if(chi){
					int[] tileCounts = pMyPlayer.myHand.getTileCounts();
					if(tileCounts[lastDiscard.rawNumber] == 0){
						if(Tile.convertRawToSuit(lastDiscard.rawNumber+1) == lastDiscard.getSuit()){
							if(Tile.convertRawToSuit(lastDiscard.rawNumber-1) == lastDiscard.getSuit()){
								if(tileCounts[lastDiscard.rawNumber-1] == 1 && tileCounts[lastDiscard.rawNumber+1] == 1){
									tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
									tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
									return Globals.CMD.CHI;
								}
							}
							if(Tile.convertRawToSuit(lastDiscard.rawNumber+2) == lastDiscard.getSuit()){
								if(tileCounts[lastDiscard.rawNumber+2] == 1 && tileCounts[lastDiscard.rawNumber+1] == 1){
									tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
									tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+2);
									return Globals.CMD.CHI;
								}
							}
						}
						if(Tile.convertRawToSuit(lastDiscard.rawNumber-1) == lastDiscard.getSuit()){
							if(Tile.convertRawToSuit(lastDiscard.rawNumber-2) == lastDiscard.getSuit()){
								if(tileCounts[lastDiscard.rawNumber-2] == 1 && tileCounts[lastDiscard.rawNumber-1] == 1){
									tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
									tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-2);
									return Globals.CMD.CHI;
								}
							}
						}
					}
				}*/
			}

			if(primaryYaku != -1){
				int[] discardCounts = pGameThread.mTable.getAllDiscardCounts(myID);
				if(primaryYaku == Globals.SHOUSANGEN){
					if(lastDiscard.getSuit() == Globals.Suits.SANGEN){
						/*int counter = 0;
						int tileCounter = 0;
						while(pMyPlayer.myHand.suits[Globals.Suits.SANGEN][counter] != -1){
							Tile thisTile = pMyPlayer.myHand.getRawTileAt(pMyPlayer.myHand.suits[Globals.Suits.SANGEN][counter]);
							if(thisTile.equals(lastDiscard))
								tilesToUse[tileCounter++] = pMyPlayer.myHand.suits[Globals.Suits.SANGEN][counter];
							counter++;
						}*/
						if(kan){
							//pMyPlayer.myHand.meld(lastDiscard, tiles[0], tiles[1], tiles[2]);
							return Globals.CMD.KAN;
						}
						if(pon){
							//pMyPlayer.myHand.meld(lastDiscard, tiles[0], tiles[1], -1);
							return Globals.CMD.PON;
						}
					}
				}
				else if(primaryYaku == Globals.SANANKOU){
					if(subYaku == Globals.TOITOI){
						if(pGameThread.mTable.wallCount() < 16){
							if(pon && !kan){//ie we have 2 but not 3 of this tile
								return Globals.CMD.PON;
							}
						}
						else{
							if(pon && !kan){//ie we have 2 but not 3 of this tile
								if(discardCounts[lastDiscard.rawNumber] == 2){
									//We were trying for sanankou but this is the last tile
									return Globals.CMD.PON;
								}
							}
						}
					}
				}
				else if(primaryYaku == Globals.YAKUHAI){
					if(lastDiscard.getType() == Globals.HONOR){
						/*int counter = 0;
						int tileCounter = 0;
						for(int thisSuit = Globals.Suits.SANGEN; thisSuit <= Globals.Suits.KAZE; thisSuit++){
							counter = 0;
							
							while(pMyPlayer.myHand.suits[thisSuit][counter] != -1){
								Tile thisTile = pMyPlayer.myHand.getRawTileAt(pMyPlayer.myHand.suits[thisSuit][counter]);
								if(thisTile.equals(lastDiscard))
									tilesToUse[tileCounter++] = pMyPlayer.myHand.suits[thisSuit][counter];
								counter++;
							}
						}*/
						if(kan){
							//pMyPlayer.myHand.meld(lastDiscard, tiles[0], tiles[1], tiles[2]);
							return Globals.CMD.KAN;
						}
						if(pon){
							//pMyPlayer.myHand.meld(lastDiscard, tiles[0], tiles[1], -1);
							return Globals.CMD.PON;
						}
					}
				}
				else if(primaryYaku == Globals.TOITOI){
					if(pon || kan){
						/*int counter = 0;
						int tileCounter = 0;
						for(int thisSuit = Globals.Suits.BAMBOO; thisSuit <= Globals.Suits.KAZE; thisSuit++){
							counter = 0;
							
							while(pMyPlayer.myHand.suits[thisSuit][counter] != -1){
								Tile thisTile = pMyPlayer.myHand.getRawTileAt(pMyPlayer.myHand.suits[thisSuit][counter]);
								if(thisTile.equals(lastDiscard))
									tilesToUse[tileCounter++] = pMyPlayer.myHand.suits[thisSuit][counter];
								counter++;
							}
						}*/
						if(kan){
							//pMyPlayer.myHand.meld(lastDiscard, tiles[0], tiles[1], tiles[2]);
							return Globals.CMD.KAN;
						}
						else{
							//pMyPlayer.myHand.meld(lastDiscard, tiles[0], tiles[1], -1);
							return Globals.CMD.PON;
						}
					}
				}
				else if(primaryYaku == Globals.CHANTA){
					if(chi){
						tilesToUse[0] = -1;
						int lastDiscardNumber = lastDiscard.getNumber();
						//if it's one of these numbers then there's no chance
						if(lastDiscardNumber != 4 && lastDiscardNumber != 5 && lastDiscardNumber != 6){
							int[] tileCounts = pMyPlayer.myHand.getTileCounts();
							boolean left = false;
							boolean center = false; 
							boolean right = false;
							if(lastDiscardNumber >= 3){ //check left
								if((tileCounts[lastDiscard.rawNumber - 1] > 0)&&(tileCounts[lastDiscard.rawNumber - 2] > 0))
									left = true;
							}
							if(lastDiscardNumber <= 7){//check right
								if((tileCounts[lastDiscard.rawNumber + 1] > 0)&&(tileCounts[lastDiscard.rawNumber + 2] > 0))
									right = true;
							}
							if(lastDiscardNumber != 1 && lastDiscardNumber != 9){//check center
								if((tileCounts[lastDiscard.rawNumber - 1] > 0)&&(tileCounts[lastDiscard.rawNumber + 1] > 0))
									center = true;
							}
							
							if((lastDiscardNumber == 1 && right)||(lastDiscardNumber == 7 && right)){
								int tempTile1 = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
								int tempTile2 = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+2);
								if(tempTile1 != -1 && tempTile2 != -1){
									tilesToUse[0] = tempTile1;
									tilesToUse[1] = tempTile2;
								}
								
							}
							else if((lastDiscardNumber == 2 && center)||(lastDiscardNumber == 8 && center)){
								int tempTile1 = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
								int tempTile2 = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
								if(tempTile1 != -1 && tempTile2 != -1){
									tilesToUse[0] = tempTile1;
									tilesToUse[1] = tempTile2;
								}
								
							}
							else if((lastDiscardNumber == 3 && left)||(lastDiscardNumber == 9 && left)){
								int tempTile1 = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
								int tempTile2 = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-2);
								if(tempTile1 != -1 && tempTile2 != -1){
									tilesToUse[0] = tempTile1;
									tilesToUse[1] = tempTile2;
								}
								
							}
							
							if(tilesToUse[0] != -1 && (tileCounts[lastDiscard.rawNumber] == 0 || lastDiscardNumber == 9 || lastDiscardNumber == 1)){
								//pMyPlayer.myHand.meld(lastDiscard, tiles[0], tiles[1], -1);
								return Globals.CMD.CHI;
							}
						}
					}
					else if(pon || kan){
						if(lastDiscard.getNumber() == 1 || lastDiscard.getNumber() == 9 ||
						   lastDiscard.getType() == Globals.HONOR){
							/*int counter = 0;
							int tileCounter = 0;
							for(int thisSuit = Globals.Suits.BAMBOO; thisSuit <= Globals.Suits.KAZE; thisSuit++){
								counter = 0;
								
								while(pMyPlayer.myHand.suits[thisSuit][counter] != -1){
									Tile thisTile = pMyPlayer.myHand.getRawTileAt(pMyPlayer.myHand.suits[thisSuit][counter]);
									if(thisTile.equals(lastDiscard))
										tilesToUse[tileCounter++] = pMyPlayer.myHand.suits[thisSuit][counter];
									counter++;
								}
							}*/
							if(kan){
								//pMyPlayer.myHand.meld(lastDiscard, tiles[0], tiles[1], tiles[2]);
								return Globals.CMD.KAN;
							}
							else{
								//pMyPlayer.myHand.meld(lastDiscard, tiles[0], tiles[1], -1);
								return Globals.CMD.PON;
							}
						}
					}

				}//End Chanta
				else if(primaryYaku == Globals.HONITSU){
					if(lastDiscard.getType() == Globals.HONOR){
						if(pMyPlayer.myHand.openHand){
							if(kan)
								return Globals.CMD.KAN;
							else if(pon)
								return Globals.CMD.PON;
						}
						else{
							if(pon && !kan){
								if(discardCounts[lastDiscard.rawNumber] == 2){
									return Globals.CMD.PON;
								}
							}
						}
					}
					else if(individualSuitValue[lastDiscard.getSuit()] > 0){
						int[] tileCounts = pMyPlayer.myHand.getTileCounts();
						if(pMyPlayer.myHand.openHand){
							if(kan)
								return Globals.CMD.KAN;
							else if(pon)
								return Globals.CMD.PON;
							if(chi){
								if(tileCounts[lastDiscard.rawNumber] == 0){
									if(Tile.convertRawToSuit(lastDiscard.rawNumber+1) == lastDiscard.getSuit()){
										if(Tile.convertRawToSuit(lastDiscard.rawNumber-1) == lastDiscard.getSuit()){
											if(tileCounts[lastDiscard.rawNumber-1] == 1 && tileCounts[lastDiscard.rawNumber+1] == 1){
												tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
												tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
												return Globals.CMD.CHI;
											}
										}
										if(Tile.convertRawToSuit(lastDiscard.rawNumber+2) == lastDiscard.getSuit()){
											if(tileCounts[lastDiscard.rawNumber+2] == 1 && tileCounts[lastDiscard.rawNumber+1] == 1){
												tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
												tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+2);
												return Globals.CMD.CHI;
											}
										}
									}
									if(Tile.convertRawToSuit(lastDiscard.rawNumber-1) == lastDiscard.getSuit()){
										if(Tile.convertRawToSuit(lastDiscard.rawNumber-2) == lastDiscard.getSuit()){
											if(tileCounts[lastDiscard.rawNumber-2] == 1 && tileCounts[lastDiscard.rawNumber-1] == 1){
												tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
												tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-2);
												return Globals.CMD.CHI;
											}
										}
									}
								}
							}
						}
						else{
							if(pon && !kan){
								if(discardCounts[lastDiscard.rawNumber] == 2){
									return Globals.CMD.PON;
								}
							}
						}
					}
				}
				else if(primaryYaku == Globals.CHINITSU){
					if(individualSuitValue[lastDiscard.getSuit()] > 0){
						int[] tileCounts = pMyPlayer.myHand.getTileCounts();
						if(pMyPlayer.myHand.openHand){
							if(kan)
								return Globals.CMD.KAN;
							else if(pon)
								return Globals.CMD.PON;
							if(chi){
								if(tileCounts[lastDiscard.rawNumber] == 0){
									if(Tile.convertRawToSuit(lastDiscard.rawNumber+1) == lastDiscard.getSuit()){
										if(Tile.convertRawToSuit(lastDiscard.rawNumber-1) == lastDiscard.getSuit()){
											if(tileCounts[lastDiscard.rawNumber-1] == 1 && tileCounts[lastDiscard.rawNumber+1] == 1){
												tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
												tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
												return Globals.CMD.CHI;
											}
										}
										if(Tile.convertRawToSuit(lastDiscard.rawNumber+2) == lastDiscard.getSuit()){
											if(tileCounts[lastDiscard.rawNumber+2] == 1 && tileCounts[lastDiscard.rawNumber+1] == 1){
												tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
												tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+2);
												return Globals.CMD.CHI;
											}
										}
									}
									if(Tile.convertRawToSuit(lastDiscard.rawNumber-1) == lastDiscard.getSuit()){
										if(Tile.convertRawToSuit(lastDiscard.rawNumber-2) == lastDiscard.getSuit()){
											if(tileCounts[lastDiscard.rawNumber-2] == 1 && tileCounts[lastDiscard.rawNumber-1] == 1){
												tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
												tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-2);
												return Globals.CMD.CHI;
											}
										}
									}
								}
							}
						}
						else{
							if(pon && !kan){
								if(discardCounts[lastDiscard.rawNumber] == 2){
									return Globals.CMD.PON;
								}
							}
						}
					}
				}
				else if(primaryYaku == Globals.ITSU){
					if(lastDiscard.getType() == Globals.HONOR){
						//If it's worth a yaku just grab it
						if(lastDiscard.getSuit() == Globals.Suits.SANGEN){
							if(pon && !kan)
								return Globals.CMD.PON;
						}
						else{
							if(pMyPlayer.isMyWind(lastDiscard)){
								if(pon && !kan)
									return Globals.CMD.PON;
							}
						}
					}
					else if(individualSuitValue[lastDiscard.getSuit()] > 0){
						int[] tileCounts = pMyPlayer.myHand.getTileCounts();
						if(tileCounts[lastDiscard.rawNumber] == 0){
							//Only call it if we are already open or if one has already been discarded
							if(pMyPlayer.myHand.openHand || discardCounts[lastDiscard.rawNumber] > 1){
								int relNum = Tile.convertRawToRelative(lastDiscard.rawNumber);
								if(relNum == 1 || relNum == 4 || relNum == 7){ 
									if(tileCounts[lastDiscard.rawNumber+1] > 0 && tileCounts[lastDiscard.rawNumber+2] > 0){
										tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
										tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+2);
										return Globals.CMD.CHI;
									}
								}
								else if(relNum == 2 || relNum == 5 || relNum == 8){ 
									if(tileCounts[lastDiscard.rawNumber+1] > 0 && tileCounts[lastDiscard.rawNumber-1] > 0){
										tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
										tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
										return Globals.CMD.CHI;
									}
								}
								else{ 
									if(tileCounts[lastDiscard.rawNumber-1] > 0 && tileCounts[lastDiscard.rawNumber-2] > 0){
										tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
										tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-2);
										return Globals.CMD.CHI;
									}
								}
							}
						}
					}
				}
				else if(primaryYaku == Globals.SANKANTSU){
					//Can we call a kan? Do it
					if(kan)
						return Globals.CMD.KAN;
					
					//Well we still need another set
					//If it's a pon, do it.  Toitoi is fine, besides if it passes then we have 0 shot at a kan
					if(pon)
						return Globals.CMD.PON;
					
					//In theory we could call a chi and still get this hand, skip for now
					
				}
				else if(primaryYaku == Globals.HONROUTOU){
					if(pon || kan){
						if(lastDiscard.getType() != Globals.SIMPLE){
							if(kan)
								return Globals.CMD.KAN;
							return Globals.CMD.PON;
						}
					}
				}
				else if(primaryYaku == Globals.SANSHOKUDOUJUN){
					//Do we already have this tile?
					if(pMyPlayer.myHand.getFirstTile(lastDiscard) == -1){
						//Is this even our doujun tile?
						if(individualTileWeights[lastDiscard.rawNumber] != 0){
							if((individualTileWeights[lastDiscard.rawNumber] & DOUJUNWEIGHT) == DOUJUNWEIGHT){
								//OK we need this tile
								//We'll be super picky for now, are there only 2 left?
								if(discardCounts[lastDiscard.rawNumber] >= 2){
									int theOtherTiles[] = new int[] {-1,-1};
									int iter = 0;
									//Find the damn doujun tiles
									for(int thisTile = lastDiscard.rawNumber-2; thisTile < lastDiscard.rawNumber+3; thisTile++){
										if(thisTile < 1)
											continue;
										if(thisTile == lastDiscard.rawNumber)
											continue;
										
										if((individualTileWeights[thisTile] & DOUJUNWEIGHT) == DOUJUNWEIGHT){
											theOtherTiles[iter++] = thisTile;
										}
									}
									
									//F*** this has taken too long to get to this point
									if(pMyPlayer.myHand.getFirstTile(theOtherTiles[0]) != -1){
										if(pMyPlayer.myHand.getFirstTile(theOtherTiles[1]) != -1){
											tilesToUse[0] = pMyPlayer.myHand.getFirstTile(theOtherTiles[0]);
											tilesToUse[1] = pMyPlayer.myHand.getFirstTile(theOtherTiles[1]);
											return Globals.CMD.CHI;
										}
									}
								}
							}
						}
					}
				}
				else if(primaryYaku == Globals.SANSHOKUDOUKOU){
					//Same principle as Doujun but a lot simpler
					
					//Is this our doukou tile?
					if(individualTileWeights[lastDiscard.rawNumber] != 0){
						if((individualTileWeights[lastDiscard.rawNumber] & DOUKOUWEIGHT) == DOUKOUWEIGHT){
							if(pon && !kan){
								return Globals.CMD.PON;
							}
						}
					}
				}
			}
			
			return -1;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("AI.HandleCall", WTFAmI);
			return -1;
		}
	}
	
	/**
	 * I have way too many of these >_>
	 */
	protected void handlePowersAtStart(){
		if(!pGameThread.bPowers){
			turnOffPowers();
			return;
		}
	}
	
	protected void handlePowersAtDraw(){
		if(!pGameThread.bPowers){
			turnOffPowers();
			return;
		}
	}
	
	protected void handlePowersAtDiscard(){
		if(!pGameThread.bPowers){
			turnOffPowers();
			return;
		}
	}
	
	protected void handlePowersAtCall(){
		if(!pGameThread.bPowers){
			turnOffPowers();
			return;
		}
	}
	
	protected void handlePowersAtEnd(){
		if(!pGameThread.bPowers){
			turnOffPowers();
			return;
		}
	}
	
	protected void handlePowersAtWin(){
		if(!pGameThread.bPowers){
			turnOffPowers();
			return;
		}
	}

	protected void handlePowersAtLose(){
		if(!pGameThread.bPowers){
			turnOffPowers();
			return;
		}
	}
	
	protected void handlePowersAtKan(){
		if(!pGameThread.bPowers){
			turnOffPowers();
			return;
		}
	}
	
	protected void turnOffPowers(){
		
	}
	
	public boolean canCallTsumo(){
		if(ShantanCount != 0)
			return false;
		else 
			return pMyPlayer.myHand.scoreHand(true) > 0;
	}
	
	public void onDraw(){
		try{
			while(hasChanged){
				sleep(100);
			}
			runDrawScript = true;
			hasChanged = true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("AI.OnDraw", WTFAmI);
		}
	}
	
	public void onDiscard(){
		try{
			while(hasChanged){
				sleep(100);
			}
			runDiscardScript = true;
			hasChanged = true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("AI.OnDiscard", WTFAmI);
		}
	}
	
	public void onEnd(){
		try{
			while(hasChanged){
				sleep(100);
			}
			runPostScript = true;
			hasChanged = true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("AI.OnEnd", WTFAmI);
		}
	}

	//public void update(){
	//	hasChanged = true;
	//}
	
	public void setupConnections(ArrayList<Integer> goingFor){
		//Integer[][] pSuits = pMyPlayer.myHand.suits;
		//This will make it easier for any later passes
		int[] suitSize = {0,0,0,0,0,0};
		
		/**
		 * We will rank the tiles based on these values, the yaku we are going for will affect these
		 * 
		 * Values will be added, so 0 is neutral, + will make us more likely to keep it
		 * and likewise - will make it more likely to be released
		 * 
		 * For the time being we are going to use +-10 as a standard modifier
		 * 
		 * In theory we could let users play with these to tweak the AI
		 */
		int terminalWeight = 0;
		int simpleWeight = 0;
		int ourWindWeight = 1;
		int otherWindWeight = -1;
		int dragonWeight = 1;
		int doraWeight = 1;
		int pairWeight = 2;
		int twoSidedWeight = 2;
		int oneSidedWeight = 1;
		int ponWeight = 5;
		int chiWeight = 5;
		int kanWeight = 5;
		int emptyWaitWeight = -5; //ie what we need to complete this set is not available
		int singleWaitWeight = -1; //there is only 1 tile that could complete
		int[] discardCounts = pGameThread.mTable.getAllDiscardCounts();
		int[] meldCounts = pGameThread.getMeldCounts();
		int[] tileCounts = pMyPlayer.myHand.getTileCounts();
		int[] localTileWeights = new int[] {0,
											0,0,0,0,0,0,0,0,0,
				   							0,0,0,0,0,0,0,0,0,
				   							0,0,0,0,0,0,0,0,0,
				   							0,0,0,0,
				   							0,0,0}; 
		int[] localTileWeightCounts = new int[] {0,
												 0,0,0,0,0,0,0,0,0,
												 0,0,0,0,0,0,0,0,0,
												 0,0,0,0,0,0,0,0,0,
												 0,0,0,0,
												 0,0,0}; 
		
		/**
		 * OK we are going to try this:
		 * We will scale the values by priority
		 * 
		 * We will ignore 1's
		 * 
		 * Ex. Highest priority Yaku gets the full value
		 * 		2nd Highest will get 1/2
		 * 		3rd 1/3 and so on.
		 * 
		 * We still need to sit down with a pencil and paper and work out the exact 
		 * values we want for these
		 */
		for(int i = 0; i < goingFor.size(); i++){
			double scaler = 1.0/((double)(i+1));
			int yaku = goingFor.get(i);
			if(yaku == Globals.PINFU){
				terminalWeight += 0;
				simpleWeight += 1;
				ourWindWeight += Math.round(-10 * scaler);
				otherWindWeight += -1;
				dragonWeight += Math.round(-10 * scaler);
				doraWeight += 0;
				pairWeight += 1;
				twoSidedWeight += Math.round(5 * scaler);
				oneSidedWeight += Math.round(2 * scaler);
				ponWeight += Math.round(-10 * scaler);
				chiWeight += Math.round(10 * scaler);
				kanWeight += Math.round(-10 * scaler);
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.TANYAO){
				terminalWeight += Math.round(-10 * scaler);
				simpleWeight += Math.round(10 * scaler);
				ourWindWeight += Math.round(-10 * scaler);
				otherWindWeight += Math.round(-10 * scaler);
				dragonWeight += Math.round(-10 * scaler);
				doraWeight += 0;
				pairWeight += 0;
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += 0;
				chiWeight += 0;
				kanWeight += 0;
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.IIPEIKOU){
				for(int j = 1; j < 35; j++){
					if((individualTileWeights[j] & IIPEIKOUWEIGHT) == IIPEIKOUWEIGHT){
						localTileWeights[j] += Math.round(10 * scaler);
						if(localTileWeightCounts[j] < 2)
							localTileWeightCounts[j] = 2;
					}
				}
			}
			else if(yaku == Globals.YAKUHAI){
				for(int thisTile = 28; thisTile <= Tile.LAST_TILE; thisTile++){
					if(Tile.convertRawToSuit(thisTile) == Globals.Suits.SANGEN){
						if(tileCounts[thisTile] >= 2){
							localTileWeights[thisTile] += Math.round(10 * scaler);
							localTileWeightCounts[thisTile] = 3;
						}
						else{
							localTileWeights[thisTile] += Math.round(3 * scaler);
							localTileWeightCounts[thisTile] = 3;
						}
					}
					else{
						int rndWind = Tile.convertWindToRaw(pGameThread.curWind);
						int myWind = Tile.convertWindToRaw(pMyPlayer.currentWind);
						int tileWind = Tile.convertRawToWind(thisTile);
						if(rndWind == tileWind || myWind == tileWind){
							if(tileCounts[thisTile] >= 2){
								localTileWeights[thisTile] += Math.round(10 * scaler);
								localTileWeightCounts[thisTile] = 3;
							}
							else{
								localTileWeights[thisTile] += Math.round(3 * scaler);
								localTileWeightCounts[thisTile] = 3;
							}
						}
					}
				}
				/*
				individualTileWeights[28] += Math.round(10 * scaler);
				individualTileWeights[29] += Math.round(10 * scaler);
				individualTileWeights[30] += Math.round(10 * scaler);
				int rndWind = Tile.convertWindToRaw(pGameThread.curWind);
				int myWind = Tile.convertWindToRaw(pMyPlayer.currentWind);
				if(rndWind > 0)
					individualTileWeights[rndWind] += Math.round(10 * scaler);
				if(myWind > 0)
					individualTileWeights[myWind] += Math.round(10 * scaler);
					*/
			}
			else if(yaku == Globals.SANSHOKUDOUJUN){
				for(int j = 1; j < 35; j++){
					if(individualTileWeights[j] != 0){
						if((individualTileWeights[j] & DOUJUNWEIGHT) == DOUJUNWEIGHT){
							localTileWeights[j] += Math.round(10 * scaler);
							if(localTileWeightCounts[j] < 1)
								localTileWeightCounts[j] = 1;
						}
					}
				}
			}
			else if(yaku == Globals.ITSU){
				for(int j = 1; j < Globals.Suits.SANGEN; j++){
					if(individualSuitValue[j] > 0){
						int firstTile = 1+((j-1)*9);
						for(int thisTile = firstTile; thisTile < (firstTile+9); thisTile++){
							localTileWeights[thisTile] += Math.round(10 * scaler);
							if(localTileWeightCounts[thisTile] < 1)
								localTileWeightCounts[thisTile] = 1;
						}
						//individualSuitValue[j] += Math.round(10 * scaler);	
					}
				}
				/*
				for(int j = 1; j <= Globals.Suits.KAZE; j++){
					if(individualSuitValue[j] > 0)
						individualSuitValue[j] += Math.round(10 * scaler);	
				}
				*/
			}
			else if(yaku == Globals.CHANTA){
				terminalWeight += Math.round(10 * scaler);
				simpleWeight += 0;
				ourWindWeight += Math.round(10 * scaler);
				otherWindWeight += Math.round(5 * scaler);
				dragonWeight += Math.round(10 * scaler);
				doraWeight += 0;
				pairWeight += 0;
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += 0;
				chiWeight += 0;
				kanWeight += 0;
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.HONROUTOU){
				terminalWeight += Math.round(10 * scaler);
				simpleWeight += Math.round(-10 * scaler);
				ourWindWeight += Math.round(10 * scaler);
				otherWindWeight += Math.round(5 * scaler);
				dragonWeight += Math.round(10 * scaler);
				doraWeight += 0;
				pairWeight += Math.round(5 * scaler);
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += Math.round(10 * scaler);
				chiWeight += Math.round(-10 * scaler);
				kanWeight += Math.round(10 * scaler);
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.TOITOI){
				terminalWeight += 0;
				simpleWeight += 0;
				ourWindWeight += 0;
				otherWindWeight += 0;
				dragonWeight += 0;
				doraWeight += 0;
				pairWeight += Math.round(5 * scaler);
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += Math.round(10 * scaler);
				chiWeight += Math.round(-5 * scaler);//This was -10, but was lowered to make it less then a pair, but more then nothing
				kanWeight += Math.round(10 * scaler);
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.SANANKOU){
				terminalWeight += 0;
				simpleWeight += 0;
				ourWindWeight += 0;
				otherWindWeight += 0;
				dragonWeight += 0;
				doraWeight += 0;
				pairWeight += Math.round(5 * scaler);
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += Math.round(10 * scaler);
				chiWeight += 0;//Math.round(-2 * scaler);this was at -10 but that's not right, we can have a sanankou with a chi
				kanWeight += Math.round(10 * scaler);
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.SANKANTSU){
				terminalWeight += 0;
				simpleWeight += 0;
				ourWindWeight += 0;
				otherWindWeight += 0;
				dragonWeight += 0;
				doraWeight += 0;
				pairWeight += Math.round(5 * scaler);
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += Math.round(5 * scaler);
				chiWeight += Math.round(-10 * scaler);
				kanWeight += Math.round(10 * scaler);
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.SANSHOKUDOUKOU){
				for(int j = 1; j < 28; j++){
					if((individualTileWeights[j] & Globals.SANSHOKUDOUKOU) == Globals.SANSHOKUDOUKOU){
						localTileWeights[j] += Math.round(10 * scaler);
						localTileWeightCounts[j] = 3;
					}
				}
			}
			else if(yaku == Globals.CHIITOI){
				terminalWeight += 0;
				simpleWeight += 0;
				ourWindWeight += 0;
				otherWindWeight += 0;
				dragonWeight += 0;
				doraWeight += 0;
				pairWeight += Math.round(10 * scaler);
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += Math.round(-30 * scaler); //We realllly need to break up pons
				chiWeight += Math.round(-5 * scaler);
				kanWeight += Math.round(-5 * scaler);
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.SHOUSANGEN){
				terminalWeight += 0;
				simpleWeight += 0;
				ourWindWeight += 0;
				otherWindWeight += 0;
				dragonWeight += Math.round(10 * scaler);
				doraWeight += 0;
				pairWeight += 0;
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += 0;
				chiWeight += 0;
				kanWeight += 0;
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.HONITSU){
				for(int j = 1; j <= Globals.Suits.MAN; j++){
					if(individualSuitValue[j] > 0)
						individualSuitValue[j] += Math.round(10 * scaler);
					else
						individualSuitValue[j] += Math.round(-10 * scaler);	
				}
				ourWindWeight += Math.round(10 * scaler);
				otherWindWeight += Math.round(5 * scaler);
				dragonWeight += Math.round(10 * scaler);
			}
			else if(yaku == Globals.JUNCHANTAYAO){
				terminalWeight += Math.round(10 * scaler);
				simpleWeight += 0;
				ourWindWeight += Math.round(-10 * scaler);
				otherWindWeight += Math.round(-10 * scaler);
				dragonWeight += Math.round(-10 * scaler);
				doraWeight += 0;
				pairWeight += 0;
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += 0;
				chiWeight += 0;
				kanWeight += 0;
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.RYANPEIKOU){
				//Tile only
				for(int j = 1; j < 35; j++){
					if((individualTileWeights[j] & RYANPEIKOUWEIGHT) == RYANPEIKOUWEIGHT){
						localTileWeights[j] += Math.round(10 * scaler);
						if(localTileWeightCounts[j] < 2)
							localTileWeightCounts[j] = 2;
					}
				}
			}
			else if(yaku == Globals.CHINITSU){
				for(int j = 1; j <= Globals.Suits.KAZE; j++){
					if(individualSuitValue[j] > 0)
						individualSuitValue[j] += Math.round(10 * scaler);	
					else{
						//We are giving winds/dragons a slight edge so that we can fall back to honitsu
						if(j >= Globals.Suits.SANGEN)
							individualSuitValue[j] += Math.round(-5 * scaler);	
						else
							individualSuitValue[j] += Math.round(-10 * scaler);	
					}
				}
			}
		}
		try{
			//int[] tileCounts = pMyPlayer.myHand.getTileCounts();
			int[] unchangedTileCounts = pMyPlayer.myHand.getTileCounts();
			for(int thisTile = Tile.BAMBOO_START; thisTile <= Tile.LAST_TILE; thisTile++){
				if(tileCounts[thisTile] <= 0)
					continue;
				int thisSuit = Tile.convertRawToSuit(thisTile);
				
				int completeChis = 0;
				int oneSidedChis = 0;
				int twoSidedChis = 0;
				int changeCount = 0;
				if(thisSuit <= Globals.Suits.MAN){
					int[] temp = new int[Tile.LAST_TILE+1];
					boolean foundSomething = true;
					while(foundSomething){
						foundSomething = false;
						if(Tile.convertRawToSuit(thisTile+1) == thisSuit){
							if(tileCounts[thisTile+1] > 0){
								//XX000
								if(Tile.convertRawToSuit(thisTile+2) == thisSuit){
									if(tileCounts[thisTile+2] > 0){
										tileCounts[thisTile+1]--;
										tileCounts[thisTile+2]--;
										temp[thisTile+1]++;
										temp[thisTile+2]++;
										completeChis++;
										changeCount++;
										foundSomething = true;
										continue;
									}
								}
								//X000X
								if(Tile.convertRawToSuit(thisTile-1) == thisSuit){
									if(tileCounts[thisTile-1] > 0){
										tileCounts[thisTile+1]--;
										tileCounts[thisTile-1]--;
										temp[thisTile+1]++;
										temp[thisTile-1]++;
										completeChis++;
										changeCount++;
										foundSomething = true;
										continue;
									}
								}
								if(((Tile.convertRawToSuit(thisTile-1) == thisSuit)&&(discardCounts[thisTile-1]+unchangedTileCounts[thisTile-1] != 4)) ||
								   ((Tile.convertRawToSuit(thisTile+2) == thisSuit)&&(discardCounts[thisTile+2]+unchangedTileCounts[thisTile+2] != 4))	){
										//XX00X
										tileCounts[thisTile+1]--;
										temp[thisTile+1]++;
										if(Tile.convertRawToRelative(thisTile) != 1 && Tile.convertRawToRelative(thisTile+1) != 9)
											twoSidedChis++;
										else
											oneSidedChis++;
										changeCount++;
										foundSomething = true;
										continue;
								}
								//else //Delete this
								//	Globals.myAssert(false);
							}
							//XX0X0
							else if(Tile.convertRawToSuit(thisTile+2) == thisSuit){
								if(tileCounts[thisTile+2] > 0){
									if(discardCounts[thisTile+1]+unchangedTileCounts[thisTile+1] != 4){
										tileCounts[thisTile+2]--;
										temp[thisTile+2]++;
										oneSidedChis++;
										changeCount++;
										foundSomething = true;
										continue;
									}
									//else //Delete this
									//	Globals.myAssert(false);
								}
							}
						}
						if(Tile.convertRawToSuit(thisTile-1) == thisSuit){
							if(tileCounts[thisTile-1] > 0){
								//000XX
								if(Tile.convertRawToSuit(thisTile-2) == thisSuit){
									if(tileCounts[thisTile-2] > 0){
										tileCounts[thisTile-1]--;
										tileCounts[thisTile-2]--;
										temp[thisTile-1]++;
										temp[thisTile-2]++;
										completeChis++;
										changeCount++;
										foundSomething = true;
										continue;
									}
								}
								
								if(((Tile.convertRawToSuit(thisTile-2) == thisSuit)&&(discardCounts[thisTile-2]+unchangedTileCounts[thisTile-2] != 4)) ||
								   ((Tile.convertRawToSuit(thisTile+1) == thisSuit)&&(discardCounts[thisTile+1]+unchangedTileCounts[thisTile+1] != 4))	){
										//X00XX
										tileCounts[thisTile-1]--;
										temp[thisTile-1]++;
										if(Tile.convertRawToRelative(thisTile-1) != 1 && Tile.convertRawToRelative(thisTile) != 9)
											twoSidedChis++;
										else
											oneSidedChis++;
										changeCount++;
										foundSomething = true;
										continue;
								}
								//else //Delete this
								//	Globals.myAssert(false);
							}
							//0X0XX
							else if(Tile.convertRawToSuit(thisTile-2) == thisSuit){
								if(tileCounts[thisTile-2] > 0){
									if(discardCounts[thisTile-1]+unchangedTileCounts[thisTile-1] != 4){
										tileCounts[thisTile-2]--;
										temp[thisTile-2]++;
										oneSidedChis++;
										changeCount++;
										foundSomething = true;
										continue;
									}
									//else //Delete this
									//	Globals.myAssert(false);
								}
							}
						}
					}
					//Add those things back
					if(changeCount > 0){
						if(thisSuit == Globals.Suits.BAMBOO){
							for(int i = Tile.BAMBOO_START; i <= Tile.BAMBOO_END; i++){
								tileCounts[i] += temp[i];
							}
						}
						else if(thisSuit == Globals.Suits.PIN){
							for(int i = Tile.DOT_START; i <= Tile.DOT_END; i++){
								tileCounts[i] += temp[i];
							}
						}
						else if(thisSuit == Globals.Suits.MAN){
							for(int i = Tile.CHAR_START; i <= Tile.CHAR_END; i++){
								tileCounts[i] += temp[i];
							}
						}
					}
				}
				//Set the values
				for(int rawIdx = pMyPlayer.myHand.getFirstTile(thisTile); rawIdx != -1; rawIdx = pMyPlayer.myHand.getNextTile(thisTile, rawIdx)){
					int activeHandIdx = pMyPlayer.myHand.rawToActiveIdx(rawIdx);
					Connections[activeHandIdx] = 0;
					Connections[activeHandIdx] += individualSuitValue[thisSuit];
					/**
					 * Fix for san shoku doujun and others
					 * 
					 * We were having an issue where it would hold onto pair for way too long
					 * EX: if we had 5677 both the 7's would get the doujun bonus so
					 * 		we ended up keeping both 7's forever
					 * 
					 * Problem would still theoretically exist if there was a case where both
					 * sanshoku doujun and doukou were triggered, but the likelyhood of that is slim
					 */
					if(localTileWeightCounts[thisTile] > 0){
						Connections[activeHandIdx] += localTileWeights[thisTile];
						localTileWeightCounts[thisTile]--;
					}
					if(tileCounts[thisTile] == 2){
						Connections[activeHandIdx] += pairWeight;
						if(discardCounts[thisTile]+meldCounts[thisTile] == 1)
							Connections[activeHandIdx] += singleWaitWeight;
						else if(discardCounts[thisTile]+meldCounts[thisTile] >= 2)
							Connections[activeHandIdx] += emptyWaitWeight;
					}
					else if(tileCounts[thisTile] == 3)
						Connections[activeHandIdx] += ponWeight;
					else if(tileCounts[thisTile] == 4)
						Connections[activeHandIdx] += kanWeight;
					
					if(completeChis > 0){
						Connections[activeHandIdx] += chiWeight;
						completeChis--;
						
						/**
						 * This is here so that we value middle tiles more
						 * EX. if we have 3456, 4 & 5 should be worth more so that we don;t 
						 * break up the chi 
						 */
						if(oneSidedChis +  twoSidedChis > 0){
							if(twoSidedChis > 0){
								Connections[activeHandIdx] += twoSidedWeight;
								//twoSidedChis--;
							}
							else if(oneSidedChis > 0){
								Connections[activeHandIdx] += oneSidedWeight;
								//oneSidedChis--;
							}
						}
					}
					else if(oneSidedChis +  twoSidedChis > 0){
						if(twoSidedChis > 0){
							Connections[activeHandIdx] += twoSidedWeight;
							twoSidedChis--;
						}
						else if(oneSidedChis > 0){
							Connections[activeHandIdx] += oneSidedWeight;
							oneSidedChis--;
						}
					}
						
					if(thisSuit == Globals.Suits.SANGEN){
						Connections[activeHandIdx] += dragonWeight;
						
						if(discardCounts[thisTile]+meldCounts[thisTile] == 1)
							Connections[activeHandIdx] += singleWaitWeight;
						else if(discardCounts[thisTile]+meldCounts[thisTile] >= 2)
							Connections[activeHandIdx] += emptyWaitWeight;
					}
					else if(thisSuit == Globals.Suits.KAZE){
						if(pMyPlayer.isMyWind(thisTile))
							Connections[activeHandIdx] += ourWindWeight;
						else
							Connections[activeHandIdx] += otherWindWeight;
						
						if(discardCounts[thisTile]+meldCounts[thisTile] == 1)
							Connections[activeHandIdx] += singleWaitWeight;
						else if(discardCounts[thisTile]+meldCounts[thisTile] >= 2)
							Connections[activeHandIdx] += emptyWaitWeight;
					}
					else{
						if(Tile.convertRawToRelative(thisTile) == 1 || Tile.convertRawToRelative(thisTile) == 9){
							Connections[activeHandIdx] += terminalWeight;
						}
						else
							Connections[activeHandIdx] += simpleWeight;
					}
					if(pGameThread.mTable.isDora(thisTile) || pMyPlayer.getRawTileAt(rawIdx).redTile)
						Connections[activeHandIdx] += doraWeight;
					
				//lastTile = thisTile;
				//counter++;
				}
			}
		}
		catch(Exception e){
			String WhatAmI = e.toString();
			WhatAmI.length();
		}
		
	}
	
	public void setupTestConnections(ArrayList<Integer> goingFor){
		//Integer[][] pSuits = pMyPlayer.myHand.suits;
		//This will make it easier for any later passes
		//int[] suitSize = {0,0,0,0,0,0};
		
		/**
		 * We will rank the tiles based on these values, the yaku we are going for will affect these
		 * 
		 * Values will be added, so 0 is neutral, + will make us more likely to keep it
		 * and likewise - will make it more likely to be released
		 * 
		 * For the time being we are going to use +-10 as a standard modifier
		 * 
		 * In theory we could let users play with these to tweak the AI
		 */
		int terminalWeight = 0;
		int simpleWeight = 0;
		int ourWindWeight = 1;
		int otherWindWeight = -1;
		int dragonWeight = 1;
		int doraWeight = 1;
		int pairWeight = 2;
		int twoSidedWeight = 2;
		int oneSidedWeight = 1;
		int ponWeight = 5;
		int chiWeight = 5;
		int kanWeight = 5;
		int emptyWaitWeight = -5; //ie what we need to complete this set is not available
		int singleWaitWeight = -1; //there is only 1 tile that could complete
		int[] discardCounts = pGameThread.mTable.getAllDiscardCounts();
		int[] meldCounts = pGameThread.getMeldCounts();
		int[] localTileWeights = new int[] {0,
											0,0,0,0,0,0,0,0,0,
				   							0,0,0,0,0,0,0,0,0,
				   							0,0,0,0,0,0,0,0,0,
				   							0,0,0,0,
				   							0,0,0}; 
		int[] localTileWeightCounts = new int[] {0,
												 0,0,0,0,0,0,0,0,0,
												 0,0,0,0,0,0,0,0,0,
												 0,0,0,0,0,0,0,0,0,
												 0,0,0,0,
												 0,0,0}; 
		
		int[] tileCounts = pMyPlayer.myHand.getTileCounts();
		boolean bDisableSuitBonus = true;
		/**
		 * OK we are going to try this:
		 * We will scale the values by priority
		 * 
		 * We will ignore 1's
		 * 
		 * Ex. Highest priority Yaku gets the full value
		 * 		2nd Highest will get 1/2
		 * 		3rd 1/3 and so on.
		 * 
		 * We still need to sit down with a pencil and paper and work out the exact 
		 * values we want for these
		 */
		for(int i = 0; i < goingFor.size(); i++){
			double scaler = 1.0/((double)(i+1));
			int yaku = goingFor.get(i);
			if(yaku == Globals.PINFU){
				terminalWeight += 0;
				simpleWeight += 1;
				ourWindWeight += Math.round(-10 * scaler);
				otherWindWeight += -1;
				dragonWeight += Math.round(-10 * scaler);
				doraWeight += 0;
				pairWeight += 1;
				twoSidedWeight += Math.round(5 * scaler);
				oneSidedWeight += Math.round(2 * scaler);
				ponWeight += Math.round(-10 * scaler);
				chiWeight += Math.round(10 * scaler);
				kanWeight += Math.round(-10 * scaler);
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.TANYAO){
				terminalWeight += Math.round(-10 * scaler);
				simpleWeight += Math.round(10 * scaler);
				ourWindWeight += Math.round(-10 * scaler);
				otherWindWeight += Math.round(-10 * scaler);
				dragonWeight += Math.round(-10 * scaler);
				doraWeight += 0;
				pairWeight += 0;
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += 0;
				chiWeight += 0;
				kanWeight += 0;
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.IIPEIKOU){
				for(int j = 1; j < 35; j++){
					if((individualTileWeights[j] & IIPEIKOUWEIGHT) == IIPEIKOUWEIGHT){
						localTileWeights[j] += Math.round(10 * scaler);
						if(localTileWeightCounts[j] < 2)
							localTileWeightCounts[j] = 2;
					}
				}
			}
			else if(yaku == Globals.YAKUHAI){
				//Changed
				for(int thisTile = 28; thisTile <= Tile.LAST_TILE; thisTile++){
					if(Tile.convertRawToSuit(thisTile) == Globals.Suits.SANGEN){
						if(tileCounts[thisTile] >= 2){
							localTileWeights[thisTile] += Math.round(10 * scaler);
							localTileWeightCounts[thisTile] = 3;
						}
						else{
							localTileWeights[thisTile] += Math.round(3 * scaler);
							localTileWeightCounts[thisTile] = 3;
						}
					}
					else{
						int rndWind = Tile.convertWindToRaw(pGameThread.curWind);
						int myWind = Tile.convertWindToRaw(pMyPlayer.currentWind);
						int tileWind = Tile.convertRawToWind(thisTile);
						if(rndWind == tileWind || myWind == tileWind){
							if(tileCounts[thisTile] >= 2){
								localTileWeights[thisTile] += Math.round(10 * scaler);
								localTileWeightCounts[thisTile] = 3;
							}
							else{
								localTileWeights[thisTile] += Math.round(3 * scaler);
								localTileWeightCounts[thisTile] = 3;
							}
						}
					}
				}
				/*
				individualTileWeights[28] += Math.round(10 * scaler);
				individualTileWeights[29] += Math.round(10 * scaler);
				individualTileWeights[30] += Math.round(10 * scaler);
				int rndWind = Tile.convertWindToRaw(pGameThread.curWind);
				int myWind = Tile.convertWindToRaw(pMyPlayer.currentWind);
				if(rndWind > 0)
					individualTileWeights[rndWind] += Math.round(10 * scaler);
				if(myWind > 0)
					individualTileWeights[myWind] += Math.round(10 * scaler);*/
			}
			else if(yaku == Globals.SANSHOKUDOUJUN){
				for(int j = 1; j < 35; j++){
					if(individualTileWeights[j] != 0){
						if((individualTileWeights[j] & DOUJUNWEIGHT) == DOUJUNWEIGHT){
							localTileWeights[j] += Math.round(10 * scaler);
							if(localTileWeightCounts[j] < 1)
								localTileWeightCounts[j] = 1;
						}
					}
				}
			}
			else if(yaku == Globals.ITSU){
				//changed
				for(int j = 1; j <= Globals.Suits.SANGEN; j++){
					if(individualSuitValue[j] > 0){
						int firstTile = 1+((j-1)*9);
						for(int thisTile = firstTile; thisTile < (firstTile+9); thisTile++){
							localTileWeights[thisTile] += Math.round(10 * scaler);
							if(localTileWeightCounts[thisTile] < 1)
								localTileWeightCounts[thisTile] = 1;
						}
						//individualSuitValue[j] += Math.round(10 * scaler);	
					}
				}
			}
			else if(yaku == Globals.CHANTA){
				terminalWeight += Math.round(10 * scaler);
				simpleWeight += 0;
				ourWindWeight += Math.round(10 * scaler);
				otherWindWeight += Math.round(5 * scaler);
				dragonWeight += Math.round(10 * scaler);
				doraWeight += 0;
				pairWeight += 0;
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += 0;
				chiWeight += 0;
				kanWeight += 0;
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.HONROUTOU){
				terminalWeight += Math.round(10 * scaler);
				simpleWeight += Math.round(-10 * scaler);
				ourWindWeight += Math.round(10 * scaler);
				otherWindWeight += Math.round(5 * scaler);
				dragonWeight += Math.round(10 * scaler);
				doraWeight += 0;
				pairWeight += Math.round(5 * scaler);
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += Math.round(10 * scaler);
				chiWeight += Math.round(-10 * scaler);
				kanWeight += Math.round(10 * scaler);
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.TOITOI){
				//changed
				terminalWeight += 0;
				simpleWeight += 0;
				ourWindWeight += 0;
				otherWindWeight += 0;
				dragonWeight += 0;
				doraWeight += 0;
				pairWeight += Math.round(7 * scaler);
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += Math.round(10 * scaler);
				chiWeight += Math.round(-5 * scaler);//This was -10, but was lowered to make it less then a pair, but more then nothing
				kanWeight += Math.round(10 * scaler);
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.SANANKOU){
				terminalWeight += 0;
				simpleWeight += 0;
				ourWindWeight += 0;
				otherWindWeight += 0;
				dragonWeight += 0;
				doraWeight += 0;
				pairWeight += Math.round(5 * scaler);
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += Math.round(10 * scaler);
				chiWeight += 0;//Math.round(-2 * scaler);this was at -10 but that's not right, we can have a sanankou with a chi
				kanWeight += Math.round(10 * scaler);
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.SANKANTSU){
				terminalWeight += 0;
				simpleWeight += 0;
				ourWindWeight += 0;
				otherWindWeight += 0;
				dragonWeight += 0;
				doraWeight += 0;
				pairWeight += Math.round(5 * scaler);
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += Math.round(5 * scaler);
				chiWeight += Math.round(-10 * scaler);
				kanWeight += Math.round(10 * scaler);
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.SANSHOKUDOUKOU){
				for(int j = 1; j < 28; j++){
					if((individualTileWeights[j] & Globals.SANSHOKUDOUKOU) == Globals.SANSHOKUDOUKOU){
						localTileWeights[j] += Math.round(10 * scaler);
						localTileWeightCounts[j] = 3;
					}
				}
			}
			else if(yaku == Globals.CHIITOI){
				terminalWeight += 0;
				simpleWeight += 0;
				ourWindWeight += 0;
				otherWindWeight += 0;
				dragonWeight += 0;
				doraWeight += 0;
				pairWeight += Math.round(10 * scaler);
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += Math.round(-30 * scaler); //We realllly need to break up pons
				chiWeight += Math.round(-5 * scaler);
				kanWeight += Math.round(-5 * scaler);
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.SHOUSANGEN){
				terminalWeight += 0;
				simpleWeight += 0;
				ourWindWeight += 0;
				otherWindWeight += 0;
				dragonWeight += Math.round(10 * scaler);
				doraWeight += 0;
				pairWeight += 0;
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += 0;
				chiWeight += 0;
				kanWeight += 0;
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.HONITSU){
				//Changed
				bDisableSuitBonus = false;
				for(int j = 1; j <= Globals.Suits.MAN; j++){
					if(individualSuitValue[j] > 0)
						individualSuitValue[j] += Math.round(10 * scaler);
					else
						individualSuitValue[j] += Math.round(-10 * scaler);	
				}
				individualSuitValue[Globals.Suits.SANGEN] += Math.round(10 * scaler);
				individualSuitValue[Globals.Suits.KAZE] += Math.round(10 * scaler);
			}
			else if(yaku == Globals.JUNCHANTAYAO){
				terminalWeight += Math.round(10 * scaler);
				simpleWeight += 0;
				ourWindWeight += Math.round(-10 * scaler);
				otherWindWeight += Math.round(-10 * scaler);
				dragonWeight += Math.round(-10 * scaler);
				doraWeight += 0;
				pairWeight += 0;
				twoSidedWeight += 0;
				oneSidedWeight += 0;
				ponWeight += 0;
				chiWeight += 0;
				kanWeight += 0;
				emptyWaitWeight += 0; 
				singleWaitWeight += 0;
			}
			else if(yaku == Globals.RYANPEIKOU){
				//Tile only
				for(int j = 1; j < 35; j++){
					if((individualTileWeights[j] & RYANPEIKOUWEIGHT) == RYANPEIKOUWEIGHT){
						localTileWeights[j] += Math.round(10 * scaler);
						if(localTileWeightCounts[j] < 2)
							localTileWeightCounts[j] = 2;
					}
				}
			}
			else if(yaku == Globals.CHINITSU){
				//Changed
				bDisableSuitBonus = false;
				for(int j = 1; j <= Globals.Suits.KAZE; j++){
					if(individualSuitValue[j] > 0)
						individualSuitValue[j] += Math.round(10 * scaler);	
					else{
						//We are giving winds/dragons a slight edge so that we can fall back to honitsu
						if(j >= Globals.Suits.SANGEN)
							individualSuitValue[j] += Math.round(-6 * scaler);	
						else
							individualSuitValue[j] += Math.round(-10 * scaler);	
					}
				}
			}
		}
		try{
			//int[] tileCounts = pMyPlayer.myHand.getTileCounts();
			int[] unchangedTileCounts = pMyPlayer.myHand.getTileCounts();
			for(int thisTile = Tile.BAMBOO_START; thisTile <= Tile.LAST_TILE; thisTile++){
				if(tileCounts[thisTile] <= 0)
					continue;
				int thisSuit = Tile.convertRawToSuit(thisTile);
				
				int completeChis = 0;
				int oneSidedChis = 0;
				int twoSidedChis = 0;
				int changeCount = 0;
				if(thisSuit <= Globals.Suits.MAN){
					int[] temp = new int[Tile.LAST_TILE+1];
					boolean foundSomething = true;
					while(foundSomething){
						foundSomething = false;
						if(Tile.convertRawToSuit(thisTile+1) == thisSuit){
							if(tileCounts[thisTile+1] > 0){
								//XX000
								if(Tile.convertRawToSuit(thisTile+2) == thisSuit){
									if(tileCounts[thisTile+2] > 0){
										tileCounts[thisTile+1]--;
										tileCounts[thisTile+2]--;
										temp[thisTile+1]++;
										temp[thisTile+2]++;
										completeChis++;
										changeCount++;
										foundSomething = true;
										continue;
									}
								}
								//X000X
								if(Tile.convertRawToSuit(thisTile-1) == thisSuit){
									if(tileCounts[thisTile-1] > 0){
										tileCounts[thisTile+1]--;
										tileCounts[thisTile-1]--;
										temp[thisTile+1]++;
										temp[thisTile-1]++;
										completeChis++;
										changeCount++;
										foundSomething = true;
										continue;
									}
								}
								if(((Tile.convertRawToSuit(thisTile-1) == thisSuit)&&(discardCounts[thisTile-1]+unchangedTileCounts[thisTile-1] != 4)) ||
								   ((Tile.convertRawToSuit(thisTile+2) == thisSuit)&&(discardCounts[thisTile+2]+unchangedTileCounts[thisTile+2] != 4))	){
										//XX00X
										tileCounts[thisTile+1]--;
										temp[thisTile+1]++;
										if(Tile.convertRawToRelative(thisTile) != 1 && Tile.convertRawToRelative(thisTile+1) != 9)
											twoSidedChis++;
										else
											oneSidedChis++;
										changeCount++;
										foundSomething = true;
										continue;
								}
								//else //Delete this
								//	Globals.myAssert(false);
							}
							//XX0X0
							else if(Tile.convertRawToSuit(thisTile+2) == thisSuit){
								if(tileCounts[thisTile+2] > 0){
									if(discardCounts[thisTile+1]+unchangedTileCounts[thisTile+1] != 4){
										tileCounts[thisTile+2]--;
										temp[thisTile+2]++;
										oneSidedChis++;
										changeCount++;
										foundSomething = true;
										continue;
									}
									//else //Delete this
									//	Globals.myAssert(false);
								}
							}
						}
						if(Tile.convertRawToSuit(thisTile-1) == thisSuit){
							if(tileCounts[thisTile-1] > 0){
								//000XX
								if(Tile.convertRawToSuit(thisTile-2) == thisSuit){
									if(tileCounts[thisTile-2] > 0){
										tileCounts[thisTile-1]--;
										tileCounts[thisTile-2]--;
										temp[thisTile-1]++;
										temp[thisTile-2]++;
										completeChis++;
										changeCount++;
										foundSomething = true;
										continue;
									}
								}
								
								if(((Tile.convertRawToSuit(thisTile-2) == thisSuit)&&(discardCounts[thisTile-2]+unchangedTileCounts[thisTile-2] != 4)) ||
								   ((Tile.convertRawToSuit(thisTile+1) == thisSuit)&&(discardCounts[thisTile+1]+unchangedTileCounts[thisTile+1] != 4))	){
										//X00XX
										tileCounts[thisTile-1]--;
										temp[thisTile-1]++;
										if(Tile.convertRawToRelative(thisTile-1) != 1 && Tile.convertRawToRelative(thisTile) != 9)
											twoSidedChis++;
										else
											oneSidedChis++;
										changeCount++;
										foundSomething = true;
										continue;
								}
								//else //Delete this
								//	Globals.myAssert(false);
							}
							//0X0XX
							else if(Tile.convertRawToSuit(thisTile-2) == thisSuit){
								if(tileCounts[thisTile-2] > 0){
									if(discardCounts[thisTile-1]+unchangedTileCounts[thisTile-1] != 4){
										tileCounts[thisTile-2]--;
										temp[thisTile-2]++;
										oneSidedChis++;
										changeCount++;
										foundSomething = true;
										continue;
									}
									//else //Delete this
									//	Globals.myAssert(false);
								}
							}
						}
					}
					//Add those things back
					if(changeCount > 0){
						if(thisSuit == Globals.Suits.BAMBOO){
							for(int i = Tile.BAMBOO_START; i <= Tile.BAMBOO_END; i++){
								tileCounts[i] += temp[i];
							}
						}
						else if(thisSuit == Globals.Suits.PIN){
							for(int i = Tile.DOT_START; i <= Tile.DOT_END; i++){
								tileCounts[i] += temp[i];
							}
						}
						else if(thisSuit == Globals.Suits.MAN){
							for(int i = Tile.CHAR_START; i <= Tile.CHAR_END; i++){
								tileCounts[i] += temp[i];
							}
						}
					}
				}
				//Set the values
				for(int rawIdx = pMyPlayer.myHand.getFirstTile(thisTile); rawIdx != -1; rawIdx = pMyPlayer.myHand.getNextTile(thisTile, rawIdx)){
					int activeHandIdx = pMyPlayer.myHand.rawToActiveIdx(rawIdx);
					Test_Connections[activeHandIdx] = 0;
					if(!bDisableSuitBonus)
						Test_Connections[activeHandIdx] += individualSuitValue[thisSuit];
					/**
					 * Fix for san shoku doujun and others
					 * 
					 * We were having an issue where it would hold onto pair for way too long
					 * EX: if we had 5677 both the 7's would get the doujun bonus so
					 * 		we ended up keeping both 7's forever
					 * 
					 * Problem would still theoretically exist if there was a case where both
					 * sanshoku doujun and doukou were triggered, but the likelyhood of that is slim
					 */
					if(localTileWeightCounts[thisTile] > 0){
						Test_Connections[activeHandIdx] += localTileWeights[thisTile];
						localTileWeightCounts[thisTile]--;
					}
					if(tileCounts[thisTile] == 2){
						Test_Connections[activeHandIdx] += pairWeight;
						if(discardCounts[thisTile]+meldCounts[thisTile] == 1)
							Test_Connections[activeHandIdx] += singleWaitWeight;
						else if(discardCounts[thisTile]+meldCounts[thisTile] >= 2)
							Test_Connections[activeHandIdx] += emptyWaitWeight;
					}
					else if(tileCounts[thisTile] == 3)
						Test_Connections[activeHandIdx] += ponWeight;
					else if(tileCounts[thisTile] == 4)
						Test_Connections[activeHandIdx] += kanWeight;
					
					if(completeChis > 0){
						Test_Connections[activeHandIdx] += chiWeight;
						completeChis--;
						
						/**
						 * This is here so that we value middle tiles more
						 * EX. if we have 3456, 4 & 5 should be worth more so that we don;t 
						 * break up the chi 
						 */
						if(oneSidedChis +  twoSidedChis > 0){
							if(twoSidedChis > 0){
								Test_Connections[activeHandIdx] += twoSidedWeight;
								//twoSidedChis--;
							}
							else if(oneSidedChis > 0){
								Test_Connections[activeHandIdx] += oneSidedWeight;
								//oneSidedChis--;
							}
						}
					}
					else if(oneSidedChis +  twoSidedChis > 0){
						if(twoSidedChis > 0){
							Test_Connections[activeHandIdx] += twoSidedWeight;
							twoSidedChis--;
						}
						else if(oneSidedChis > 0){
							Test_Connections[activeHandIdx] += oneSidedWeight;
							oneSidedChis--;
						}
					}
						
					if(thisSuit == Globals.Suits.SANGEN){
						Test_Connections[activeHandIdx] += dragonWeight;
						
						if(discardCounts[thisTile]+meldCounts[thisTile] == 1)
							Test_Connections[activeHandIdx] += singleWaitWeight;
						else if(discardCounts[thisTile]+meldCounts[thisTile] >= 2)
							Test_Connections[activeHandIdx] += emptyWaitWeight;
					}
					else if(thisSuit == Globals.Suits.KAZE){
						if(pMyPlayer.isMyWind(thisTile))
							Test_Connections[activeHandIdx] += ourWindWeight;
						else
							Test_Connections[activeHandIdx] += otherWindWeight;
						
						if(discardCounts[thisTile]+meldCounts[thisTile] == 1)
							Test_Connections[activeHandIdx] += singleWaitWeight;
						else if(discardCounts[thisTile]+meldCounts[thisTile] >= 2)
							Test_Connections[activeHandIdx] += emptyWaitWeight;
					}
					else{
						if(Tile.convertRawToRelative(thisTile) == 1 || Tile.convertRawToRelative(thisTile) == 9){
							Test_Connections[activeHandIdx] += terminalWeight;
						}
						else
							Test_Connections[activeHandIdx] += simpleWeight;
					}
					if(pGameThread.mTable.isDora(thisTile) || pMyPlayer.getRawTileAt(rawIdx).redTile)
						Test_Connections[activeHandIdx] += doraWeight;
					
				//lastTile = thisTile;
				//counter++;
				}
			}
		}
		catch(Exception e){
			String WhatAmI = e.toString();
			WhatAmI.length();
		}
		
	}
	
	public void AIMeld(Tile newTile, int from){
		if(tilesToUse[2] != -1)
			pMyPlayer.myHand.meld(newTile, tilesToUse[0], tilesToUse[1], tilesToUse[2], from);
		else
			pMyPlayer.myHand.meld(newTile, tilesToUse[0], tilesToUse[1], -1, from);
		
		hasChanged = true;
		runDrawScript = true;
	}
	
	/**
	 * Current implementation of awayFrom is really haphazard/scattered
	 * We are going to try and condense as much of it as possible here
	 * It will make it easier to test/debug/tinker with
	 * 
	 * TODO: Reevaluate these for accuracy
	 */
	private void setupAwayFrom(){
		try{
			//By default set it to 99 (ie never even look at this)
			for(int i = 0; i < Globals.AIYAKUCOUNT; i++){
				awayFrom[i] = 99;
			}
			
			for(int i = 1; i < 35; i++){
				individualTileWeights[i] = 0;
			}
			
			for(int i = Globals.Suits.BAMBOO; i <= Globals.Suits.KAZE; i++){
				individualSuitValue[i] = 0;
			}
			
			int[][] kouChecker = new int[][] {{0,0,0,0,0,0,0,0,0,0},
					  				      	  {0,0,0,0,0,0,0,0,0,0},
					  				      	  {0,0,0,0,0,0,0,0,0,0},
					  				      	  {0,0,0,0,0,0,0,0,0,0}};
			int[][] junChecker = new int[][] {{0,0,0,0,0,0,0,0},
											  {0,0,0,0,0,0,0,0},
											  {0,0,0,0,0,0,0,0},
											  {0,0,0,0,0,0,0,0}};
			int[][] peikoChecker = new int[][] {{0,0,0,0,0,0,0,0},
					  							{0,0,0,0,0,0,0,0},
					  							{0,0,0,0,0,0,0,0},
					  							{0,0,0,0,0,0,0,0}};
			int[] suitCount = {0,0,0,0,0,0};
			
			int[] tileCounts = pMyPlayer.myHand.getTileCounts();
			int[] allDiscardCounts = pGameThread.mTable.getAllDiscardCounts();
			//int[] rawTileCounts = pMyPlayer.myHand.getRawTileCounts();
			
			int numberOfTerminals = tileCounts[1]+tileCounts[9]+tileCounts[10]+tileCounts[18]+tileCounts[19]+tileCounts[27];
			int numberOfHonors = tileCounts[28]+tileCounts[29]+tileCounts[30]+tileCounts[31]+tileCounts[32]+tileCounts[33]+tileCounts[34];
			int numberOfSimples = pMyPlayer.myHand.activeHandMap.size() - numberOfTerminals - numberOfHonors;
			int numberOfPairs = 0;
			int numberOfPons = 0;
			int numberOfKans = 0;
			//int numberOfChis = 0; Has to be taken care of in ShantanCount or else we are doing double work
	
			for(int i = 1; i <= Globals.MAXTILE; i++){
				if(tileCounts[i] >= 3)
					numberOfPons++;
				if(tileCounts[i] == 2)
					numberOfPairs++;
			}
			
			awayFrom[Globals.CHIITOI] = 7 - numberOfPairs - numberOfPons;
			
			awayFrom[Globals.TANYAO] = numberOfTerminals + numberOfHonors;
			awayFrom[Globals.HONROUTOU] = numberOfSimples;
			awayFrom[Globals.SANANKOU] = 9 - (numberOfPons*3);
			for(int i = 0; i < (3-numberOfPons); i++){
				if(numberOfPairs > i)
					awayFrom[Globals.SANANKOU] -= 2;
				else
					awayFrom[Globals.SANANKOU] -= 1;
			}
			
			int awayFromFlush = 14;
			int awayFromPeiko = 6;
			int awayFromRyanPeiko = 6;
			int awayFromDouKou = 9;
			int awayFromDouJun = 9;
			int suitUsed = 0; 
			int iipeikouTile = 0;
			int doujunTile = 0;
			int doukouTile = 0;
			for(int thisSuit = 1; thisSuit <= Globals.Suits.MAN; thisSuit++){
				//int awayFromItsu = 9;
				//int suitCount = 0;
				for(int num = 1; num < 10; num++){
					int rawNum = ((thisSuit-1)*9)+num;
					suitCount[thisSuit] += tileCounts[rawNum];
					//if(tileCounts[rawNum] > 0){
					//	awayFromItsu--;
					//	suitCount += tileCounts[rawNum];
					//}
					if(num <= 7){
						junChecker[thisSuit][num] = Math.min(1, tileCounts[rawNum]) + Math.min(1, tileCounts[rawNum+1]) + Math.min(1, tileCounts[rawNum+2]);
						peikoChecker[thisSuit][num] = Math.min(2, tileCounts[rawNum]) + Math.min(2, tileCounts[rawNum+1]) + Math.min(2, tileCounts[rawNum+2]);
						
						//Old
						//Iipeikou/Ryanpeikou
						/*int away = 6 - Math.min(2, tileCounts[rawNum]) - Math.min(2, tileCounts[rawNum+1]) - Math.min(2, tileCounts[rawNum+2]);
						if(away < awayFromPeiko){
							iipeikouTile = rawNum;
							awayFromRyanPeiko = awayFromPeiko;
							awayFromPeiko = away;
						}
						else if(away < awayFromRyanPeiko)
							awayFromRyanPeiko = away;
						*/
						//San Shoku Dou Jun
						
						int away = 6;
						if(thisSuit == Globals.Suits.BAMBOO){//Only do this once
							away = 9 - Math.min(1, tileCounts[rawNum]) - Math.min(1, tileCounts[rawNum+1]) - Math.min(1, tileCounts[rawNum+2]) - 
									   Math.min(1, tileCounts[rawNum+9]) - Math.min(1, tileCounts[rawNum+9+1]) - Math.min(1, tileCounts[rawNum+9+2]) - 
									   Math.min(1, tileCounts[rawNum+18]) - Math.min(1, tileCounts[rawNum+18+1]) - Math.min(1, tileCounts[rawNum+18+2]);
							if(away < awayFromDouJun){
								awayFromDouJun = away;
								doujunTile = rawNum;
							}
						}
					}
					kouChecker[thisSuit][num] = Math.min(3, tileCounts[rawNum]);
					if(thisSuit == Globals.Suits.BAMBOO){//Only do this once
						//kouChecker[rawNum] = tileCounts[rawNum] + tileCounts[rawNum+9] + tileCounts[rawNum+18];
						
						//Old
						int away = 9 - tileCounts[rawNum] - tileCounts[rawNum+9] - tileCounts[rawNum+18];
						if(away < awayFromDouKou){
							awayFromDouKou = away;
							doukouTile = rawNum;
						}
					}
				}
				//if(awayFromItsu < awayFrom[Globals.ITSU])
				//	awayFrom[Globals.ITSU] = awayFromItsu;
				//if((14-suitCount) < awayFromFlush){
				//	awayFromFlush = (14-suitCount);
				//	suitUsed = thisSuit;
				//}
			}
			
			//awayFrom[Globals.CHINITSU] = awayFromFlush;
			//individualSuitValue[suitUsed] += 1;
			
			//New methods
			//Ii/Ryan-Peikou
			awayFromPeiko = 6;
			awayFromRyanPeiko = 6;
			iipeikouTile = 0;
			int ryanpeikouTile = 0;
			int disableRyanCheck = 0;
			for(int thisSuit = Globals.Suits.BAMBOO; thisSuit <= Globals.Suits.MAN; thisSuit++){
				for(int thisTile = 1; thisTile < 8; thisTile++){
					int away = 6 - peikoChecker[thisSuit][thisTile];
					if(away < awayFromPeiko){
						iipeikouTile = ((thisSuit-1)*9)+thisTile;
						awayFromPeiko = away;
						disableRyanCheck = 2;
						continue;
					}
					else if((away < awayFromRyanPeiko)&& (disableRyanCheck <= 0)){
						ryanpeikouTile = ((thisSuit-1)*9)+thisTile;
						awayFromRyanPeiko = away;
						//disableRyanCheck = 2;
						continue;
					}
					if(disableRyanCheck > 0)
						disableRyanCheck--;
				}
			}
			awayFrom[Globals.IIPEIKOU] = awayFromPeiko;
			awayFrom[Globals.RYANPEIKOU] = awayFromPeiko + awayFromRyanPeiko;
			individualTileWeights[iipeikouTile] = individualTileWeights[iipeikouTile] | IIPEIKOUWEIGHT;
			individualTileWeights[iipeikouTile+1] = individualTileWeights[iipeikouTile+1] | IIPEIKOUWEIGHT;
			individualTileWeights[iipeikouTile+2] = individualTileWeights[iipeikouTile+2] | IIPEIKOUWEIGHT;
			individualTileWeights[ryanpeikouTile] = individualTileWeights[ryanpeikouTile] | RYANPEIKOUWEIGHT;
			individualTileWeights[ryanpeikouTile+1] = individualTileWeights[ryanpeikouTile+1] | RYANPEIKOUWEIGHT;
			individualTileWeights[ryanpeikouTile+2] = individualTileWeights[ryanpeikouTile+2] | RYANPEIKOUWEIGHT;
			
			//Old methods
			/*
			awayFrom[Globals.IIPEIKOU] = awayFromPeiko;
			awayFrom[Globals.RYANPEIKOU] = awayFromPeiko + awayFromRyanPeiko;
			individualTileWeights[iipeikouTile] = individualTileWeights[iipeikouTile] | IIPEIKOUWEIGHT;
			individualTileWeights[iipeikouTile+1] = individualTileWeights[iipeikouTile+1] | IIPEIKOUWEIGHT;
			individualTileWeights[iipeikouTile+2] = individualTileWeights[iipeikouTile+2] | IIPEIKOUWEIGHT;
			*/
			/*
			awayFrom[Globals.SANSHOKUDOUKOU] = awayFromDouKou;
			individualTileWeights[doukouTile] = individualTileWeights[doukouTile] | DOUKOUWEIGHT;
			individualTileWeights[doukouTile+9] = individualTileWeights[doukouTile+9] | DOUKOUWEIGHT;
			individualTileWeights[doukouTile+18] = individualTileWeights[doukouTile+18] | DOUKOUWEIGHT;
			
			awayFrom[Globals.SANSHOKUDOUJUN] = awayFromDouJun;
			individualTileWeights[doujunTile] = individualTileWeights[doujunTile] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+1] = individualTileWeights[doujunTile+1] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+2] = individualTileWeights[doujunTile+2] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+9] = individualTileWeights[doujunTile+9] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+1+9] = individualTileWeights[doujunTile+1+9] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+2+9] = individualTileWeights[doujunTile+2+9] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+18] = individualTileWeights[doujunTile+18] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+1+18] = individualTileWeights[doujunTile+1+18] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+2+18] = individualTileWeights[doujunTile+2+18] | DOUJUNWEIGHT;
			*/
			awayFrom[Globals.SHOUSANGEN] = 8;
			for(int i = 28; i <= 30; i++){
				if((3-tileCounts[i])< awayFrom[Globals.YAKUHAI])
					awayFrom[Globals.YAKUHAI] = (3-tileCounts[i]);
				awayFrom[Globals.SHOUSANGEN] -= tileCounts[i];
			}
			int rndWind = Tile.convertWindToRaw(pGameThread.curWind);
			int myWind = Tile.convertWindToRaw(pMyPlayer.currentWind);
			if(rndWind > 0){
				if((3-tileCounts[rndWind])< awayFrom[Globals.YAKUHAI])
					awayFrom[Globals.YAKUHAI] = (3-tileCounts[rndWind]);
			}
			if(myWind > 0){
				if((3-tileCounts[myWind])< awayFrom[Globals.YAKUHAI])
					awayFrom[Globals.YAKUHAI] = (3-tileCounts[myWind]);
			}
			
			//Stupid Chi-based hands
			int completeChis = 0;
			int partialChis = 0;
			int pairs = 0;
			int completeWithout = 0;
			int partialWithout = 0;
			int pairsWithout = 0;
			int completeHonors = 0;
			int partialHonors = 0;
			int pairHonors = 0;
			
			for(int thisTile = 1; thisTile <= Tile.LAST_TILE; thisTile++){
				int thisSuit = Tile.convertRawToSuit(thisTile);
				//int relNum = Tile.convertRawToRelative(thisTile);
				
				while(tileCounts[thisTile] > 0){
					boolean tileUsed = false;
					if(thisSuit > Globals.Suits.MAN){
						//Pons
						if(tileCounts[thisTile] >= 3){
							tileCounts[thisTile] -= 3;
							completeHonors++;
							tileUsed = true;
							continue;
						}
						if(tileCounts[thisTile] >= 2){
							tileCounts[thisTile] -= 2;
							pairs++;
							pairHonors++;
							tileUsed = true;
							continue;
						}
					}
					else{
						int relNum = Tile.convertRawToRelative(thisTile);
						//Pons
						if(tileCounts[thisTile] >= 3){
							tileCounts[thisTile] -= 3;
							if(relNum == 1 || relNum == 9)
								completeHonors++;
							else
								completeWithout++;
							tileUsed = true;
							continue;
						}
						//Chis
						if(relNum < 9){
							if(relNum < 8){
								if(tileCounts[thisTile+1] > 0 && tileCounts[thisTile+2] > 0){
									tileCounts[thisTile]--;
									tileCounts[thisTile+1]--;
									tileCounts[thisTile+2]--;
									if(relNum == 1 || relNum == 7)
										completeHonors++;
									else
										completeWithout++;
									completeChis++;
									tileUsed = true;
									continue;
								}
							}
						}
						
						//Pairs
						if(tileCounts[thisTile] >= 2){
							tileCounts[thisTile] -= 2;
							pairs++;
							if(relNum == 1 || relNum == 9)
								pairHonors++;
							else
								pairsWithout++;
							tileUsed = true;
							continue;
						}
						
						//Partial Chis
						if(relNum < 9){
							if(tileCounts[thisTile+1] > 0){
								tileCounts[thisTile]--;
								tileCounts[thisTile+1]--;
								if(relNum == 1 || relNum == 2 || relNum == 7 ||relNum == 8)
									partialHonors++;
								else
									partialWithout++;
								partialChis++;
								tileUsed = true;
								continue;
							}
							if(relNum < 8){
								if(tileCounts[thisTile+2] > 0){
									tileCounts[thisTile]--;
									tileCounts[thisTile+2]--;
									if(relNum == 1 || relNum == 7)
										partialHonors++;
									else
										partialWithout++;
									partialChis++;
									tileUsed = true;
									continue;
								}
							}
						}
					}
					
					if(!tileUsed)
						break;
				}
			}
			
			//Pinfu
			int awayFromPinfu = 9 - (2 * completeChis) - partialChis;
			if(pairs > 0)
				awayFromPinfu--;
			awayFrom[Globals.PINFU] = awayFromPinfu;
			
			int awayFromChanta = 9 - (2 * completeHonors) - partialHonors;
			if(pairHonors > 0)
				awayFromChanta--;
			awayFrom[Globals.CHANTA] = awayFromChanta;
			
			//We are doing this the cheap/stupid way for now
			//awayFrom[Globals.CHANTA] = tileCounts[4]+tileCounts[5]+tileCounts[6]+
			//						   tileCounts[13]+tileCounts[14]+tileCounts[15]+
			//						   tileCounts[22]+tileCounts[23]+tileCounts[24];
			awayFrom[Globals.JUNCHANTAYAO] = awayFrom[Globals.CHANTA] + numberOfHonors;
			
			if(pMyPlayer.myHand.numberOfMelds > 0){
				//shut down closed only hands
				awayFrom[Globals.TANYAO] = 99;
				awayFrom[Globals.IIPEIKOU] = 99;
				awayFrom[Globals.RYANPEIKOU] = 99;
				awayFrom[Globals.PINFU] = 99;
				for(int i = 0; i < pMyPlayer.myHand.numberOfMelds; i++){
					// >_> we have to grab/check all 3 tiles because Chi's could be in any order
					Tile tile1 = pMyPlayer.myHand.getRawTileAt(pMyPlayer.myHand.melds[i][1]);
					Tile tile2 = pMyPlayer.myHand.getRawTileAt(pMyPlayer.myHand.melds[i][2]);
					Tile tile3 = pMyPlayer.myHand.getRawTileAt(pMyPlayer.myHand.melds[i][3]);
					
					int thisSuit = tile1.getSuit();
					suitCount[thisSuit] += pMyPlayer.myHand.melds[i][0];
					
					if(tile1.equals(tile2)){
						numberOfPons++;
						if(tile1.getType() == Globals.HONOR)
							numberOfHonors += pMyPlayer.myHand.melds[i][0];
						else if(tile1.getType() == Globals.TERMINAL)
							numberOfTerminals += pMyPlayer.myHand.melds[i][0];
						else
							numberOfSimples += pMyPlayer.myHand.melds[i][0];
						
						if(tile1.getSuit() == Globals.Suits.SANGEN)
							awayFrom[Globals.SHOUSANGEN] -= 3;
						
						if(pMyPlayer.myHand.melds[i][0] == 4){
							numberOfKans++;
						}
						if(tile1.getType() != Globals.HONOR)
							kouChecker[thisSuit][tile1.getNumber()] = 3;
					}
					else{ //Chis
						int lowest = tile1.getNumber();
						if(tile2.getNumber() < lowest)
							lowest = tile2.getNumber();
						if(tile3.getNumber() < lowest)
							lowest = tile3.getNumber();
						junChecker[thisSuit][lowest] = 3;
						awayFrom[Globals.HONROUTOU] = 99;
					}
				}
				
			}
			
			awayFromDouKou = 9;
			awayFromDouJun = 9;
			for(int thisTile = 1; thisTile < 10; thisTile++){
				int away = 9;
				if(thisTile <= 7){
					away = 9 - Math.min(3, junChecker[Globals.Suits.BAMBOO][thisTile]) - Math.min(3, junChecker[Globals.Suits.PIN][thisTile]) - Math.min(3, junChecker[Globals.Suits.MAN][thisTile]);
					if(away < awayFromDouJun){
						awayFromDouJun = away;
						doujunTile = thisTile;
					}
				}
				
				away = 9 - Math.min(3, kouChecker[Globals.Suits.BAMBOO][thisTile]) - Math.min(3, kouChecker[Globals.Suits.PIN][thisTile]) - Math.min(3, kouChecker[Globals.Suits.MAN][thisTile]);
				if(away < awayFromDouKou){
					awayFromDouKou = away;
					doukouTile = thisTile;
				}
			}
			
			awayFrom[Globals.SANSHOKUDOUKOU] = awayFromDouKou;
			individualTileWeights[doukouTile] = individualTileWeights[doukouTile] | DOUKOUWEIGHT;
			individualTileWeights[doukouTile+9] = individualTileWeights[doukouTile+9] | DOUKOUWEIGHT;
			individualTileWeights[doukouTile+18] = individualTileWeights[doukouTile+18] | DOUKOUWEIGHT;
			
			awayFrom[Globals.SANSHOKUDOUJUN] = awayFromDouJun;
			individualTileWeights[doujunTile] = individualTileWeights[doujunTile] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+1] = individualTileWeights[doujunTile+1] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+2] = individualTileWeights[doujunTile+2] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+9] = individualTileWeights[doujunTile+9] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+1+9] = individualTileWeights[doujunTile+1+9] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+2+9] = individualTileWeights[doujunTile+2+9] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+18] = individualTileWeights[doujunTile+18] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+1+18] = individualTileWeights[doujunTile+1+18] | DOUJUNWEIGHT;
			individualTileWeights[doujunTile+2+18] = individualTileWeights[doujunTile+2+18] | DOUJUNWEIGHT;
			
			//Itsu/Flush
			int awayFromItsu = 9;
			awayFromFlush = 14;
			for(int thisSuit = Globals.Suits.BAMBOO; thisSuit <= Globals.Suits.MAN; thisSuit++){
				int temp = 9 - Math.min(3, junChecker[thisSuit][1]) - Math.min(3, junChecker[thisSuit][4]) - Math.min(3, junChecker[thisSuit][7]);
				if(temp < awayFromItsu)
					awayFromItsu = temp;
				
				if((14-suitCount[thisSuit]) < awayFromFlush){
					awayFromFlush = 14-suitCount[thisSuit];
					suitUsed = thisSuit;
				}
			}
			awayFrom[Globals.ITSU] = awayFromItsu;
			
			awayFrom[Globals.CHINITSU] = awayFromFlush;
			awayFrom[Globals.HONITSU] = awayFromFlush - numberOfHonors;
			individualSuitValue[suitUsed] += 1;
			
			awayFrom[Globals.TOITOI] = 9 - (2 * numberOfPons);
			for(int i = 0; i < (5-numberOfPons); i++){
				if(numberOfPairs > i)
					awayFrom[Globals.TOITOI]--;
			}
			
			awayFrom[Globals.SANKANTSU] = 12 - (numberOfKans*4);
			for(int i = 0; i < (3-numberOfKans); i++){
				if(numberOfPons > i)
					awayFrom[Globals.SANKANTSU] -= 3;
			}
			for(int i = 0; i < (3-numberOfKans-numberOfPons); i++){
				if(numberOfPairs > i)
					awayFrom[Globals.SANKANTSU] -= 2;
			}
			
			/**
			 * The "impossible" conditions
			 * 
			 * There are certain, failry easy to see situations where a yaku is impossible to complete
			 * We should try and stop it off here if thats the case
			 * 
			 */
			if((allDiscardCounts[28]+allDiscardCounts[29]+allDiscardCounts[30]) >= 5){
				awayFrom[Globals.SHOUSANGEN] = 99;
				//Globals.myAssert(false);
			}
			if(allDiscardCounts[doukouTile] > 1 || allDiscardCounts[doukouTile+9] > 1 || allDiscardCounts[doukouTile+18] > 1){
				awayFrom[Globals.SANSHOKUDOUKOU] = 99;
				//Globals.myAssert(false);
			}
			int suitOffset = 9*(suitUsed-1);
			for(int i = 1; i < 10; i++){
				if(allDiscardCounts[i+suitOffset] == 4){
					awayFrom[Globals.ITSU] = 99;
					//Globals.myAssert(false);
				}
				if(i == doujunTile || i == doujunTile+1 || i == doujunTile+2){
					if(allDiscardCounts[i] == 4 || allDiscardCounts[i+9] == 4 || allDiscardCounts[i+18] == 4){
						awayFrom[Globals.SANSHOKUDOUJUN] = 99;
						//Globals.myAssert(false);
					}
				}
			}
			if(allDiscardCounts[iipeikouTile] >= 3 || allDiscardCounts[iipeikouTile+1] >= 3 || allDiscardCounts[iipeikouTile+2] >= 3){
				awayFrom[Globals.IIPEIKOU] = 99;
				//Globals.myAssert(false);
			}
	
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("AI.setupAwayFrom", WTFAmI);
		}
		                              
	}
	
	/*private void setupTestAwayFrom(){
		//By default set it to 99 (ie never even look at this)
		for(int i = 0; i < Globals.AIYAKUCOUNT; i++){
			awayFrom[i] = 99;
		}
	}*/
	
	void setOptimalHand(ArrayList<Set> newList){
		optimalHand = newList;
	}
	
	void chooseApproach(){
		if(pMyPlayer.riichi){
			inBailMode = false;
			inLockMode = true;
			return;
		}
		if(ShantanCount >= 3 && 
		  ((pGameThread.getNumberInRiichi(pMyPlayer.ID)>0)||pGameThread.mTable.wallCount() < 16)){
			if(!inBailMode)
				Log.i("AI.chooseApproach", getName() + ": Bail Mode");
			inBailMode = true;
			inLockMode = false;
		}
		else{
			inBailMode = false;
			int haveSomething = 0;
			boolean closeToSomethingElse = false;
			for(int i = 0; i < Globals.AIYAKUCOUNT; i++){
				/**
				 * Originally I had this set at 0, but it causes issues with things like
				 * Honitsu and Tanyao (ie you draw a terminal and you are no longer in tanyao)
				 * 
				 * Instead we will have a boatload of special cases
				 */
				if(awayFrom[i] == 0){
					haveSomething++;
					//break;
				}
				if(awayFrom[i] == 1 &&
				  (i == Globals.CHINITSU || i == Globals.HONITSU || i == Globals.TANYAO || i == Globals.JUNCHANTAYAO || i == Globals.CHIITOI)	
				   ){
					haveSomething++;
					//break;
				}
				
				//If we have 1 yaku already and we are close to one of these, we may want to keep going
				if(awayFrom[i] == 1 &&
						  (i == Globals.ITSU || i == Globals.SANSHOKUDOUJUN || i == Globals.SANSHOKUDOUKOU || i == Globals.IIPEIKOU || i == Globals.SANKANTSU)	
						   ){
					closeToSomethingElse = true;
							//break;
						}
					
			}
			
			if(ShantanCount < 3 && (haveSomething > 1)){ 
				//less than 24 tiles means there are only 6 more go arounds...kind of arbitrary
				//Changed to 35 (still arbitrary), it was waiting a bit too long to call riichi
				if(closeToSomethingElse && (pGameThread.mTable.wallCount() > 35) && (haveSomething == 1)){
					//Only invoke this when we are at a single yaku
					//We were having issues with people throwing away 2+ yaku hands for no good reason
					if(inBailMode || inLockMode)
						Log.i("AI.chooseApproach", getName() + ": General Mode");
					inBailMode = false;
					inLockMode = false;
				}
				else{
					if(!inLockMode)
						Log.i("AI.chooseApproach", getName() + ": Lock Mode");
					inLockMode = true;
				}
			}
			else if(ShantanCount < 3 && pGameThread.mTable.wallCount() < 24){
				//Even if we don;t have a yaku, just get into tenpai when we are close to the end
				if(!inLockMode)
					Log.i("AI.chooseApproach", getName() + ": Lock Mode");
				inLockMode = true;
			}
			else{
				if(inBailMode || inLockMode)
					Log.i("AI.chooseApproach", getName() + ": General Mode");
				inBailMode = false;
				inLockMode = false;
			}
		}
	}
	
	public void terminateThread(){
		mRunning = false;
	}
	
	public void waitForThread(){
		try{
			while(hasChanged){
				sleep(100);
			}
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("AI.waitForThread", WTFAmI);
		}
	}
}

