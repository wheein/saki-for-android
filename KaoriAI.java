package mahjong.riichi;

import java.util.Random;

import android.util.Log;

public class KaoriAI extends AI {
	private Random randGenerator;
	
	/**
	 * Kaori will have 3 modes
	 * 
	 * Beginner - 40% chance - Completely random discards, call at random, no shot at winning
	 * Normal - 50% chance - Follow the normal AI routine
	 * Lucky - 10% chance - We rig the deal to give her a good hand, normal AI after that
	 */
	private boolean bBeginnerMode;
	private boolean bNormalMode;
	private boolean bLuckyMode;
	/**
	 * Constructors
	 */
	KaoriAI(){
		super();
		randGenerator = new Random();
	}
	
	KaoriAI(Integer ID){
		super(ID);
		randGenerator = new Random();
	}
	
	/**
	 * Overrides for the special powers
	 */
	protected void handlePowersAtStart(){
		
		try{
			int randInt = randGenerator.nextInt(10);
			if(randInt <= 3){ //0-3, 40%
				bBeginnerMode = true;
				bNormalMode = false;
				bLuckyMode = false;
				Log.i("Kaori " + getName(), "Beginner Mode");
			}
			else if(randInt <= 8){ //4-8, 50%
				bBeginnerMode = false;
				bNormalMode = true;
				bLuckyMode = false;
				Log.i("Kaori " + getName(), "Normal Mode");
			}
			else if(randInt == 9){ //9, 10%
				bBeginnerMode = false;
				bNormalMode = false;
				bLuckyMode = true;
				Log.i("Kaori " + getName(), "Lucky Mode");
			}
			else{
				//Something went wrong, go with normal mode
				Globals.myAssert(false);
				bBeginnerMode = false;
				bNormalMode = true;
				bLuckyMode = false;
			}
			
			if(bLuckyMode){
				pMyPlayer.powerActivated[Globals.Powers.dealBased] = true;
				//Set up the tiles we want to recieve
				//I'm not sure the best way to do this, for the time being we will ask for 
				//Honors, winds, and 1 random suit
				pMyPlayer.powerTiles.clear();
				for(int thisTile = Tile.HONOR_START; thisTile <= Tile.LAST_TILE; thisTile++){
					pMyPlayer.powerTiles.add(thisTile);
				}
				
				//Pick a random suit
				int suit = randGenerator.nextInt(Globals.Suits.MAN);
				suit++;
				for(int thisTile = ((suit-1)*9)+1; thisTile < ((suit-1)*9)+10; thisTile++){
					pMyPlayer.powerTiles.add(thisTile);
				}
			}
			else{
				turnOffPowers();
			}
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("KaoriAI.PowersAtStart", WTFAmI);
		}
		
		super.handlePowersAtStart();
	}
	
	protected void handlePowersAtDraw(){
		//turnOffPowers();
		
		super.handlePowersAtDraw();
	}
	
	protected void handlePowersAtDiscard(){
		if(bLuckyMode && ShantanCount == 1){
			pMyPlayer.powerTiles.clear();
			for(int thisTile = 0; thisTile < pMyPlayer.myHand.tenpaiTiles.size(); thisTile++){
				if(pGameThread.mTable.isLeftInWall(pMyPlayer.myHand.tenpaiTiles.get(thisTile))){
					//pGameThread.mTable.reserveTile(pMyPlayer.myHand.tenpaiTiles.get(thisTile));
					pMyPlayer.powerActivated[Globals.Powers.drawBased] = true;
					pMyPlayer.powerTiles.add(pMyPlayer.myHand.tenpaiTiles.get(thisTile));
				}
			}
		}
		super.handlePowersAtDiscard();
	}
	
	protected void handlePowersAtCall(){
		super.handlePowersAtCall();
	}
	
	protected void handlePowersAtEnd(){
		turnOffPowers();
	}
	
	protected void handlePowersAtWin(){
		super.handlePowersAtWin();
	}

	protected void handlePowersAtLose(){
		super.handlePowersAtLose();
	}
	
	protected void handlePowersAtKan(){
		super.handlePowersAtKan();
	}
	
	protected void turnOffPowers(){
		pMyPlayer.powerActivated[Globals.Powers.dealBased] = false;
		pMyPlayer.powerActivated[Globals.Powers.drawBased] = false;
		pMyPlayer.powerTiles.clear();
	}
	
	/**
	 * Overrides
	 */
	protected int handleDiscard(){
		try{
			while(hasChanged){ //analyzeHand isn't done yet, if this happens a lot we should look into a change
				sleep(100);
			}
			if(bBeginnerMode){
				int idx = randGenerator.nextInt(pMyPlayer.myHand.activeHandMap.size());
				if(pMyPlayer.myHand.getTileFromActiveIdx(idx) != null)
					return idx;
			}
			
			return super.handleDiscard();
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("KaoriAI.handleDiscard", WTFAmI);
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
			
			if(bBeginnerMode){
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
				
				int randInt = randGenerator.nextInt(2);
				
				//50/50 odds of calling it
				if(randInt == 0)
					return -1;
				
				if(kan)
					return Globals.CMD.KAN;
				else if(pon)
					return Globals.CMD.PON;
				else if(chi){
					int[] tileCounts = pMyPlayer.myHand.getTileCounts();
					if(tileCounts[lastDiscard.rawNumber] == 0){
						if(Tile.convertRawToSuit(lastDiscard.rawNumber+1) == lastDiscard.getSuit()){
							if(Tile.convertRawToSuit(lastDiscard.rawNumber-1) == lastDiscard.getSuit()){
								if(tileCounts[lastDiscard.rawNumber-1] == 1 && tileCounts[lastDiscard.rawNumber+1] == 1){
									tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
									tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
									tilesToUse[2] = -1;
									return Globals.CMD.CHI;
								}
							}
							if(Tile.convertRawToSuit(lastDiscard.rawNumber+2) == lastDiscard.getSuit()){
								if(tileCounts[lastDiscard.rawNumber+2] == 1 && tileCounts[lastDiscard.rawNumber+1] == 1){
									tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
									tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+2);
									tilesToUse[2] = -1;
									return Globals.CMD.CHI;
								}
							}
						}
						if(Tile.convertRawToSuit(lastDiscard.rawNumber-1) == lastDiscard.getSuit()){
							if(Tile.convertRawToSuit(lastDiscard.rawNumber-2) == lastDiscard.getSuit()){
								if(tileCounts[lastDiscard.rawNumber-2] == 1 && tileCounts[lastDiscard.rawNumber-1] == 1){
									tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
									tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-2);
									tilesToUse[2] = -1;
									return Globals.CMD.CHI;
								}
							}
						}
					}
					return -1;
				}
				else{
					Globals.myAssert(false); //something went wrong
					return -1;
				}
			}
			else{
				return super.handleCall();
			}
			
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("KaoriAI.handleCall", WTFAmI);
			return 0;
		}
	}
}

