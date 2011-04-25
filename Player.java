package mahjong.riichi;

import java.util.ArrayList;

import android.util.Log;

//Wrapper for Hand class.  It didn't feel right to put the AI, score, wind, etc in the Hand class
public class Player {
	public Hand myHand;
	public int ID;
	public int currentWind;
	public int score;
	public AI myAI;
	public boolean AIControlled;
	public boolean riichi;
	public boolean ippatsu;
	public boolean rinshan;
	public int characterID;
	
	//Pointers to other classes
	MainGameThread pMainGameThread;
	
	Player(){
		Globals.myAssert(false); //you should always pass in an ID
	}
	
	Player(int newID, MainGameThread mainThread){
		ID = newID;
		pMainGameThread = mainThread;
		myHand = new Hand(this);
		score = 25000;
		
		//Create and start the AI thread
		//The thread should remain dorment unless we actually call on it to do something
		//it's in a seperate thread so that the UI/GameThread won't be blocked
		//by us doing random AI calculations
		myAI = new AI(ID); //default AI
		myAI.start();
		
		currentWind = Globals.Winds.EAST;
		AIControlled = true;
		
		characterID = Globals.Characters.GENERIC;
		
		//yaku = new int[Globals.ALLYAKUCOUNT];
	}
	
	public void clearHand(int newWind){
		setWind(newWind);
		riichi = false;
		ippatsu = false;
		rinshan = false;
		myHand.clear();
	}
	
	//Setters/getters
	public boolean setWind(int Wind){
		if((Wind < Globals.Winds.EAST) || (Wind > Globals.Winds.NORTH)){
			Globals.myAssert(false);
			return false;
		}
		currentWind = Wind;
		return true;
	}
	
	public void nextWind(){
		currentWind = (currentWind+1)%4;
	}
	
	public void setScore(int newScore){
		score = newScore;
	}
	
	public void setAIControl(boolean isCom){
		if(!isCom && AIControlled){
			myAI.stopThread();
		}
		AIControlled = isCom;
	}
	
	public void offsetScore(int change){
		score += change;
	}
	
	public Tile getActiveTileAt(int pos){
		//I should be doing some error checking here, but we are skipping it for time
		return myHand.rawHand[myHand.activeHand[pos]];
		
	}
	
	public Tile getRawTileAt(int pos){
		if(pos < 0 || pos > 18)
			assert false;
		if(myHand.rawHand[pos] == null)
			assert false;
		return myHand.rawHand[pos];
	}
	
	/**
	 * 
	 * This list will have the following format:
	 * [0] - number of possibilities
	 * then it's pairs of 2
	 * 
	 */
	public ArrayList<Set> getPossibleChiList(Tile tileToCall){
		ArrayList<Set> ret = new ArrayList<Set>();
		try{
			Globals.myAssert(tileToCall.getType() != Globals.HONOR);
			int[] up = new int[] {-1, -1};
			int[] down = new int[] {-1, -1};
			if(tileToCall.getNumber() > 1){
				down[0] = myHand.getFirstTile(tileToCall.rawNumber - 1);
				if(tileToCall.getNumber() > 2)
					down[1] = myHand.getFirstTile(tileToCall.rawNumber - 2);
			}
			if(tileToCall.getNumber() < 9){
				up[0] = myHand.getFirstTile(tileToCall.rawNumber + 1);
				if(tileToCall.getNumber() < 8)
					up[1] = myHand.getFirstTile(tileToCall.rawNumber + 2);
			}
			if((down[0] >= 0)&&(down[1] >= 0)){
				ret.add(new Set(down[0], down[1], -1));
			}
			if((down[0] >= 0)&&(up[0] >= 0)){
				ret.add(new Set(down[0], up[0], -1));
			}
			if((up[0] >= 0)&&(up[1] >= 0)){
				ret.add(new Set(up[0], up[1], -1));
			}
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Player.getPossibleChiList", WTFAmI);
		}
		
		return ret;
	}
	
	/**
	 * Use when there is only 1 thing we can do
	 */
	public void autoPon(Tile tileToCall, int from){
		try{
			int[] tiles = new int[] {-1,-1};;
			int counter = 0;
			for(int i = 0; i < myHand.rawHandMax; i++){
				Tile tempTile = myHand.getRawTileAt(i);
				
				//Globals.myAssert(tempTile != null);
				if(tempTile == null)
					continue;
				if(tempTile.open)
					continue;
				
				if(tempTile.equals(tileToCall)){
					tiles[counter++] = i;
				}
				if(counter >= 2)
					break;
			}
			
			Globals.myAssert(!((tiles[0] < 0) || (tiles[1] < 0)));
			myHand.meld(tileToCall, tiles[0], tiles[1], -1, from);
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Player.autoPon", WTFAmI);
		}
	}
	
	/**
	 * Use when there is only 1 thing we can do
	 */
	public void autoKan(Tile tileToCall, int from){
		try{
			int[] tiles = new int[] {-1,-1, -1};
			int counter = 0;
			for(int i = 0; i <= myHand.activeHandSize; i++){
				Tile tempTile = myHand.getRawTileAt(myHand.activeHand[i]);
				//Globals.myAssert(tempTile != null);
				if(tempTile == null)
					continue;
				if(tempTile.equals(tileToCall)){
					tiles[counter++] = myHand.activeHand[i];
				}
				if(counter >= 3)
					break;
			}
			
			Globals.myAssert(!((tiles[0] < 0) || (tiles[1] < 0) || (tiles[2] < 0)));
			myHand.meld(tileToCall, tiles[0], tiles[1], tiles[2], from);
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Player.autoKan", WTFAmI);
		}
	}
	
	public void autoSelfKan(){
		try{
			int[] tiles = new int[] {-1,-1, -1, -1};
			int counter = 0;
			int[] tileCounts = myHand.getTileCounts();
			for(int thisMeld = 0; thisMeld < myHand.numberOfMelds; thisMeld++){
				if(myHand.rawHand[myHand.melds[thisMeld][1]].rawNumber == myHand.rawHand[myHand.melds[thisMeld][2]].rawNumber){
					tileCounts[myHand.rawHand[myHand.melds[thisMeld][1]].rawNumber] += 3;
				}
			}
			Tile tileToCall = new Tile();
			for(int thisTile = 1; thisTile <= Tile.LAST_TILE; thisTile++){
				if(tileCounts[thisTile] == 4){
					tileToCall.rawNumber = thisTile;
					break;
				}
			}
			for(int i = 0; i <= myHand.activeHandSize; i++){
				Tile tempTile = myHand.getRawTileAt(myHand.activeHand[i]);
				Globals.myAssert(tempTile != null);
				if(tempTile == null)
					continue;
				if(tempTile.equals(tileToCall)){
					tiles[counter++] = myHand.activeHand[i];
				}
				if(counter >= 4)
					break;
			}
			
			Globals.myAssert(counter == 4 || counter == 1);
			myHand.meld(tiles[0], tiles[1], tiles[2], tiles[3]);
				
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Player.autoKan", WTFAmI);
		}
	}
	
	boolean isMyWind(Tile tileToCheck){
		if(tileToCheck.getSuit() != Globals.Suits.KAZE)
			return false;
		if(Tile.convertRawToWind(tileToCheck.rawNumber) == currentWind)
			return true;
		if(Tile.convertRawToWind(tileToCheck.rawNumber) == pMainGameThread.curWind)
			return true;
		return false;
	}
	
	boolean isMyWind(int rawNumber){
		if(Tile.convertRawToSuit(rawNumber) != Globals.Suits.KAZE)
			return false;
		if(Tile.convertRawToWind(rawNumber) == currentWind)
			return true;
		if(Tile.convertRawToWind(rawNumber) == pMainGameThread.curWind)
			return true;
		return false;
	}
	
}
