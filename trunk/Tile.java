package mahjong.riichi;

/*
 * The tiles will be numbered thusly:
 * 		1-9: The Bamboos
 * 		10-18: Dots
 * 		19-27: Characters
 * 		28-30: Dragons
 * 			28: Red
 * 			29: White
 * 			30: Green
 * 		31-34: Winds
 * 			31: East
 * 			32: South
 * 			33: West
 * 			34: North
 */

public class Tile {
	public int rawNumber;
	public boolean redTile;
	public boolean open;
	public boolean selfKan;
	
	//Constants
	public static int BAMBOO_START = 1;
	public static int BAMBOO_END = 9;
	public static int DOT_START = 10;
	public static int DOT_END = 18;
	public static int CHAR_START = 19;
	public static int CHAR_END = 27;
	public static int HONOR_START = 28;
	public static int DRAGON_START = 28;
	public static int DRAGON_END = 30;
	public static int WIND_START = 31;
	public static int WIND_END = 34;
	public static int LAST_TILE = 34;
	
	public Tile(){
		rawNumber = 0;
		redTile = false;
		selfKan = false;
	}
	public Tile(int tileNumber){
		rawNumber = tileNumber;
		redTile = false;
		selfKan = false;
	}
	
	public Tile(Tile CopyMe){
		rawNumber = CopyMe.rawNumber;
		redTile = CopyMe.redTile;
		open = CopyMe.open;
		selfKan = CopyMe.selfKan;
	}
	
	public boolean equals(Tile X){
		return X.rawNumber == rawNumber;
	}
	
	//Fetch functions
	public int getSuit(){
		if(rawNumber < 10)
			return Globals.Suits.BAMBOO;
		else if(rawNumber < 19)
			return Globals.Suits.PIN;
		else if(rawNumber < 28)
			return Globals.Suits.MAN;
		else if(rawNumber < 31)
			return Globals.Suits.SANGEN;
		return Globals.Suits.KAZE;
	}
	
	public int getType(){
		if((rawNumber == 1)||(rawNumber == 9)||(rawNumber == 10)||(rawNumber == 18)||
		   (rawNumber == 19)||(rawNumber == 27))
				return Globals.TERMINAL;
		else if(rawNumber > 27)
			return Globals.HONOR;
		return Globals.SIMPLE;
			
	}
	
	public int getNumber(){
		if(rawNumber < 10)
			return rawNumber;
		else if(rawNumber < 19)
			return rawNumber - 9;
		else if(rawNumber < 28)
			return rawNumber - 18;
		return 1;
	}
	
	//Comparisons
	public boolean isSameSuit(Tile X){
		if(X.getType() == Globals.HONOR)
			return false;
		return X.getSuit() == getSuit();
	}
	
	public boolean isSameWind(int wind){
		if(getSuit() != Globals.Suits.KAZE){
			return false;
		}
		return (wind+30)==rawNumber;
	}
	
	//static helpers
	public static int convertRawToSuit(int rawTileNum){
		int thisSuit = 0;
		if(rawTileNum <= 9 && rawTileNum > 0)
			thisSuit = Globals.Suits.BAMBOO;
		else if(rawTileNum <= 18)
			thisSuit = Globals.Suits.PIN;
		else if(rawTileNum <= 27) 
			thisSuit = Globals.Suits.MAN;
		else if(rawTileNum <= 30)
			thisSuit = Globals.Suits.SANGEN;
		else if(rawTileNum <= Globals.MAXTILE)
			thisSuit = Globals.Suits.KAZE;
		else{
			thisSuit = -1; //something has gone horribly wrong
			Globals.myAssert(false);
		}
		
		return thisSuit;
	}
	
	public static int convertRawToRelative(int rawTileNum){
		int thisSuit = convertRawToSuit(rawTileNum);
		if(thisSuit > Globals.Suits.MAN || thisSuit == -1){
			Globals.myAssert(false);
			return -1;
		}
		
		return rawTileNum - (9*(thisSuit-1));
	}
	
	public static int convertRawToWind(int rawTileNum){
		int thisSuit = convertRawToSuit(rawTileNum);
		if(thisSuit != Globals.Suits.KAZE){
			Globals.myAssert(false);
			return -1;
		}
		
		return rawTileNum - 31;
	}
	
	public static int convertWindToRaw(int wind){
		if(wind < Globals.Winds.EAST || wind > Globals.Winds.NORTH)
			return -1;
		
		int ret = 31 + wind;
		
		if(ret < 31 || ret > 34)
			return -1;
		
		return ret;
	}
	
	public static int convertRawToDora(int rawTileNum){
		int thisSuit = convertRawToSuit(rawTileNum);
		if(thisSuit <= Globals.Suits.MAN){
			if(Tile.convertRawToRelative(rawTileNum) == 9)
				return rawTileNum - 8;
			else
				return rawTileNum + 1;
		}
		else if(rawTileNum == 30)
			return 28;
		else if(rawTileNum == 34)
			return 31;
		else
			return rawTileNum + 1;
			
	}
}
