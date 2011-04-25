package mahjong.riichi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.util.Log;

/**
 * 
 * This class is taking the place of GameManager which was poorly defined
 * 
 * Table will handle everything on the table that is accessible to all players
 * ie things like the wall, discards, dora, etc.
 * 
 * We are removing things like individual players and moving them to the thread
 * 
 * 
 *
 */
public class Table {
	private Tile[] TileList;
	private ArrayList<Integer> TileCountList;
	private Random randGenerator;
	private int[] discardCount; 
	private Tile[][] Discards;
	private Tile lastDiscard;
	private ArrayList<Tile> Dora;
	private int[] riichiTile;
	private int bonusCount;
	private int pointsOnTheTable;
	
	/**
	 * Pointers to other classes
	 */
	private MainGameThread pMainGameThread;
	
	Table(){
		TileList = new Tile[38];
		for(int i = 1; i < 35; i++){
			TileList[i] = new Tile(i);
		}
		TileList[35]/*.rawNumber*/ = new Tile(5);
		TileList[35].redTile = true;
		TileList[36]/*.rawNumber*/ = new Tile(14);
		TileList[36].redTile = true;
		TileList[37]/*.rawNumber*/ = new Tile(23);
		TileList[37].redTile = true;

		TileCountList = new ArrayList<Integer>();
		randGenerator = new Random();
		
		Dora = new ArrayList<Tile>();
		Discards = new Tile[4][22];
		discardCount = new int[] {0,0,0,0};
		riichiTile = new int[] {-1,-1,-1,-1};
		bonusCount = 0;
		pointsOnTheTable = 0;
	}
	
	//Public Access Functions
	public void init(){
		buildWall();
		
		//Initial Dora
		Dora.clear();
		addDora();
		//Tile doraTile = drawRandomTile();
		//Globals.myAssert(doraTile != null);
		//Dora.add(doraTile);
		
		//Clear out discards, there's no real reason to clear the Discards array
		discardCount[0] = 0;
		discardCount[1] = 0;
		discardCount[2] = 0;
		discardCount[3] = 0;
		
		riichiTile[0] = -1;
		riichiTile[1] = -1;
		riichiTile[2] = -1;
		riichiTile[3] = -1;
	}
	
	public void setGameThread(MainGameThread useThis){
		Globals.myAssert(useThis != null);
		pMainGameThread = useThis;
	}
	
	public Tile drawRandomTile(){
		if(TileCountList.size() <= (8-Dora.size())) //Dead Wall
			return null;
		
		if(TileCountList.size() <= 2)
			return null;
		
		int pos = randGenerator.nextInt(TileCountList.size()-2)+1;
		
		Globals.myAssert(pos != TileCountList.size() && pos >= 1);
		
		try{
			Tile ret = TileList[TileCountList.get(pos)];
			TileCountList.remove(pos);
			return ret;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Table.drawRandomTile", WTFAmI);
			return null;
		}
	}
	
	private Tile drawRandomTile(boolean ignoreDeadWall){
		if(!ignoreDeadWall){
			if(TileCountList.size() <= (8-Dora.size())) //Dead Wall
				return null;
		}
		else{
			if(TileCountList.size() <= 0) 
				return null;
		}
		
		if(TileCountList.size() <= 2)
			return null;
		
		int pos = randGenerator.nextInt(TileCountList.size()-2)+1;
		
		Globals.myAssert(pos != TileCountList.size() && pos >= 1);
		
		try{
			Tile ret = TileList[TileCountList.get(pos)];
			TileCountList.remove(pos);
			return ret;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Table.drawRandomTile_ignoreDeadWall", WTFAmI);
			return null;
		}
	}
	
	public Tile getTile(int idx){
		Globals.myAssert(idx > 0 && idx < 28 );
		try{
			Tile ret = TileList[idx];
			return ret;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Table.getTile", WTFAmI);
			return null;
		}
	}
	
	public ArrayList<Tile> getDora(){
		try{
			return Dora;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Table.getDora", WTFAmI);
			return null;
		}
	}
	
	public ArrayList<Tile> getDiscards(int player){
		try{
			ArrayList<Tile> ret = new ArrayList<Tile>();
			for(int i = 0; i < discardCount[player]; i++){
				ret.add(Discards[player][i]);
			}
			return ret;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Table.getDiscards", WTFAmI);
			return null;
		}
	}
	
	public Tile getLastDiscard(){
		Globals.myAssert(lastDiscard != null);
		return lastDiscard;
	}
	
	public void discardTile(Tile discardMe, int player){
		Globals.myAssert(player >= 0 && player <= 3);
		if(pMainGameThread.mPlayers[player].riichi && riichiTile[player] == -1)
			riichiTile[player] = discardCount[player];
		lastDiscard = discardMe;
		Discards[player][discardCount[player]++] = discardMe;
	}
	
	public Tile undiscardLastTile(int player){
		Tile ret = lastDiscard;
		discardCount[player]--;
		lastDiscard = null;
		return ret;
	}
	
	public int wallCount(){
		return (TileCountList.size() - (14-Dora.size()));
	}
	
	public boolean addDora(){
		if(Dora.size() >= 4)
			return false;
		Tile doraTile = drawRandomTile();
		Globals.myAssert(doraTile != null);
		Dora.add(doraTile);
		return true;
	}
	
	//Private 
	private void buildWall(){
		Integer[] TileCount = new Integer[] {1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4,5,5,5,6,6,6,6,7,7,7,7,8,8,8,8,9,9,9,9,
				 				   10,10,10,10,11,11,11,11,12,12,12,12,13,13,13,13,14,14,14,15,15,15,15,16,16,16,16,17,17,17,17,18,18,18,18,
				 				   19,19,19,19,20,20,20,20,21,21,21,21,22,22,22,22,23,23,23,24,24,24,24,25,25,25,25,26,26,26,26,27,27,27,27,
				 				   28,28,28,28,29,29,29,29,30,30,30,30,
				 				   31,31,31,31,32,32,32,32,33,33,33,33,34,34,34,34,
				 				   35,36,37};
		List<Integer> rawList = Arrays.asList(TileCount);
		TileCountList = new ArrayList<Integer>();
		TileCountList.addAll(rawList);
	}
	
	public boolean hasBeenDiscarded(int rawTileNumber, int player, boolean inclusive){
		if(inclusive){
			for(int i = 0; i < discardCount[player]; i++){
				if(Discards[player][i].rawNumber == rawTileNumber)
					return true;
			}
		}
		else{
			for(int thisPlayer = 0; thisPlayer < 4; thisPlayer++){
				if(player == thisPlayer)
					continue;
				for(int thisTile = 0; thisTile < discardCount[player]; thisTile++){
					if(Discards[thisPlayer][thisTile].rawNumber == rawTileNumber)
						return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasBeenDiscarded(Tile tileToFind, int player, boolean inclusive){
		return hasBeenDiscarded(tileToFind.rawNumber, player, inclusive);
	}
	
	//It would take the exact same amount of time to tally 1 as it would to tally them all
	//So yeah, we're just doing that
	public int[] getAllDiscardCounts(){
		int[] discardCounts = new int[] {0,
					  					 0,0,0,0,0,0,0,0,0,
					  					 0,0,0,0,0,0,0,0,0,
					  					 0,0,0,0,0,0,0,0,0,
					  					 0,0,0,0,
					  					 0,0,0};
		for(int player = 0; player < 4; player++){
			for(int i = 0; i < discardCount[player]; i++){
				discardCounts[Discards[player][i].rawNumber]++;
			}
		}
		return discardCounts;
	}
	
	public int[] getAllDiscardCounts(int everyoneButMe){
		int[] discardCounts = new int[] {0,
					 0,0,0,0,0,0,0,0,0,
					 0,0,0,0,0,0,0,0,0,
					 0,0,0,0,0,0,0,0,0,
					 0,0,0,0,
					 0,0,0};
		for(int player = 0; player < 4; player++){
			if(player == everyoneButMe)
				continue;
			for(int i = 0; i < discardCount[player]; i++){
				discardCounts[Discards[player][i].rawNumber]++;
			}
		}
		return discardCounts;
	}
	
	public int[] getDiscardCounts(int player){
		int[] discardCounts = new int[] {0,
					 0,0,0,0,0,0,0,0,0,
					 0,0,0,0,0,0,0,0,0,
					 0,0,0,0,0,0,0,0,0,
					 0,0,0,0,
					 0,0,0};
		for(int i = 0; i < discardCount[player]; i++){
			discardCounts[Discards[player][i].rawNumber]++;
		}
		return discardCounts;
	}
	
	boolean isDora(Tile thisTile){
		for(int i = 0; i < Dora.size(); i++){
			Tile temp = Dora.get(i);
			if(thisTile.rawNumber == Tile.convertRawToDora(temp.rawNumber))
				return true;
		}
		return false;
	}
	
	boolean isDora(int rawNumber){
		for(int i = 0; i < Dora.size(); i++){
			Tile temp = Dora.get(i);
			if(rawNumber == Tile.convertRawToDora(temp.rawNumber))
				return true;
		}
		return false;
	}
	
	public int getRiichiTile(int player){
		Globals.myAssert(player < 4 && player >= 0);
		return riichiTile[player];
	}
	
	public void addBonus(){
		bonusCount++;
	}
	
	public void clearBonus(){
		bonusCount = 0;
	}
	
	public int getBonusCount(){
		return bonusCount;
	}
	
	public void addPointsToTable(int points){
		pointsOnTheTable += points;
	}
	
	public int getPointsOnTable(){
		return pointsOnTheTable;
	}
	
	public void clearPointsOnTable(){
		pointsOnTheTable = 0;
	}
	
	public void addUraDora(){
		int doraSize = Dora.size();
		for(int i = 0; i < doraSize; i++){
			Tile temp = drawRandomTile(true);
			if(temp != null)
				Dora.add(temp);
		}
	}
	
}