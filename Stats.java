package mahjong.riichi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

/**
 * 
 * The stats class
 * 
 * Responsive for the collection, loading, and saving 
 *
 */
public class Stats {
	private static Integer VERSION = 2;
	/**
	 * Game level stats
	 */
	public Integer totalGames;
	public Integer firstPlace;
	public Integer secondPlace;
	public Integer thirdPlace;
	public Integer fourthPlace;
	public Integer avgScore;
	
	/**
	 * Hand/Yaku
	 */
	public Integer totalHands;
	public Integer[] yakuCount;
	public Integer[] totalHanCount;
	public Integer wonByRon;
	public Integer wonByTsumo;
	public Integer lostByRon;
	
	/**
	 * Misc
	 */
	public Integer[] characterUsed;
	
	/**
	 * Calc'ed stats
	 * These don't get saved
	 */
	public double[] yakuPercentages;
	public double[] hanPercentages;
	public double[] placePercentages;
	public double[] characterPercentages;
	public double winPercentage;
	public double ronPercentage;
	public double tsumoPercentage;
	public double avgFinish;
	
	/**
	 * Flags/Other Controls variables
	 */
	boolean bLoadedFromFile;
	boolean bAI;
	boolean bDirty;
	
	Stats(){
		totalGames = 0;
		firstPlace = 0;
		secondPlace = 0;
		thirdPlace = 0;
		fourthPlace = 0;
		avgScore = 0;
		
		/**
		 * Hand/Yaku
		 */
		totalHands = 0;
		yakuCount = new Integer[Globals.ALLYAKUCOUNT];
		totalHanCount = new Integer[14];
		wonByRon = 0;
		wonByTsumo = 0;
		lostByRon = 0;
		
		/**
		 * Misc
		 */
		characterUsed = new Integer[Globals.Characters.COUNT];
		
		yakuPercentages = new double[Globals.ALLYAKUCOUNT];
		placePercentages = new double[4];
		hanPercentages = new double[10];
		characterPercentages = new double[Globals.Characters.COUNT];
		
		for(int thisYaku = 0; thisYaku < Globals.ALLYAKUCOUNT; thisYaku++){
			yakuCount[thisYaku] = new Integer(0);
		}
		for(int thisHan = 0; thisHan < 14; thisHan++){
			totalHanCount[thisHan] = new Integer(0);
		}
		for(int thisPlayer = 0; thisPlayer < Globals.Characters.COUNT; thisPlayer++){
			characterUsed[thisPlayer] = new Integer(0);
		}
		
		bAI = false;
		
	}
	
	public boolean loadFromFile(boolean AI, String err){
		try{
			bLoadedFromFile = false;
			bAI = AI;
			boolean mExternalStorageAvailable = false;
			boolean mExternalStorageWriteable = false;
			String state = Environment.getExternalStorageState();
	
			if (Environment.MEDIA_MOUNTED.equals(state)) {
			    // We can read and write the media
			    mExternalStorageAvailable = mExternalStorageWriteable = true;
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			    // We can only read the media
			    mExternalStorageAvailable = true;
			    mExternalStorageWriteable = false;
			} else {
			    // Something else is wrong. It may be one of many other states, but all we need
			    //  to know is we can neither read nor write
			    mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
			
			if(!mExternalStorageAvailable){
				err = "SD card not available";
				return false;
			}
			
			File root = Environment.getExternalStorageDirectory();
			String fileName = "stats.dat";
			if(AI)
				fileName = "aistats.dat";
			File myDir = new File(root.getPath()+"/Android/data/mahjong.riichi/files/");
			File myFile = new File(myDir, fileName);
			
			FileInputStream rawStream = new FileInputStream(myFile);
			
			ObjectInputStream serializer = new ObjectInputStream(rawStream);
			
			int VersionInFile = serializer.readInt();
			
			if(VersionInFile != VERSION){
				err = "Outdated stat file";
				return false;
			}
				
			//serializer.readInt(VERSION);
			totalGames = serializer.readInt();
			firstPlace = serializer.readInt();
			secondPlace = serializer.readInt();
			thirdPlace = serializer.readInt();
			fourthPlace = serializer.readInt();
			avgScore = serializer.readInt();
			totalHands = serializer.readInt();
			wonByRon = serializer.readInt();
			wonByTsumo = serializer.readInt();
			lostByRon = serializer.readInt();
			
			for(int thisYaku = 0; thisYaku < Globals.ALLYAKUCOUNT; thisYaku++){
				yakuCount[thisYaku] = serializer.readInt();
			}
			for(int thisHan = 0; thisHan < 14; thisHan++){
				totalHanCount[thisHan] = serializer.readInt();
			}
			for(int thisPlayer = 0; thisPlayer < Globals.Characters.COUNT; thisPlayer++){
				characterUsed[thisPlayer] = serializer.readInt();
			}
			
			serializer.close();
			
			bLoadedFromFile = true;
			return true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			
			if(e.getClass() == FileNotFoundException.class)
				err = "File not found";
			else if(e.getClass() == NullPointerException.class)
				err = "Bad code >_> sorry";
			else
				err = WTFAmI;
			
			Log.e("Stats.loadFromFile", WTFAmI);
			return false;
		}
	}
	
	public boolean saveToFile(boolean AI){
		try{
			boolean mExternalStorageAvailable = false;
			boolean mExternalStorageWriteable = false;
			String state = Environment.getExternalStorageState();
	
			if (Environment.MEDIA_MOUNTED.equals(state)) {
			    // We can read and write the media
			    mExternalStorageAvailable = mExternalStorageWriteable = true;
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			    // We can only read the media
			    mExternalStorageAvailable = true;
			    mExternalStorageWriteable = false;
			} else {
			    // Something else is wrong. It may be one of many other states, but all we need
			    //  to know is we can neither read nor write
			    mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
			
			if(!mExternalStorageWriteable)
				return false;
			
			if(!bLoadedFromFile){
				String err = "";
				Stats statsToMerge = new Stats();
				if(statsToMerge.loadFromFile(AI, err)){
					mergeStats(statsToMerge);
				}
				else{
					/**
					 * If this happens then we will be overwriting the stats file
					 * In Theory this will only happen when:
					 * 		1) There was no stats file to begin with
					 * 		2) The SD card isn't available and thus save will probably fail anyways
					 */
					Globals.myAssert(false);
				}
			}
			
			File root = Environment.getExternalStorageDirectory();
			String fileName = "stats.dat";
			if(AI)
				fileName = "aistats.dat";
			File myDir = new File(root.getPath()+"/Android/data/mahjong.riichi/files/");
			File myFile = new File(myDir, fileName);
			if(myFile.exists()){
				myFile.delete();
			}
			
			Globals.myAssert(myDir.mkdirs()); //Couldn't create the dir, they MIGHT already exist
			Globals.myAssert(myFile.createNewFile());
			
			FileOutputStream rawStream = new FileOutputStream(myFile);
			
			ObjectOutputStream serializer = new ObjectOutputStream(rawStream);
			
			serializer.writeInt(VERSION);
			serializer.writeInt(totalGames);
			serializer.writeInt(firstPlace);
			serializer.writeInt(secondPlace);
			serializer.writeInt(thirdPlace);
			serializer.writeInt(fourthPlace);
			serializer.writeInt(avgScore);
			serializer.writeInt(totalHands);
			serializer.writeInt(wonByRon);
			serializer.writeInt(wonByTsumo);
			serializer.writeInt(lostByRon);
			
			for(int thisYaku = 0; thisYaku < Globals.ALLYAKUCOUNT; thisYaku++){
				serializer.writeInt(yakuCount[thisYaku]);
			}
			for(int thisHan = 0; thisHan < 14; thisHan++){
				serializer.writeInt(totalHanCount[thisHan]);
			}
			for(int thisPlayer = 0; thisPlayer < Globals.Characters.COUNT; thisPlayer++){
				serializer.writeInt(characterUsed[thisPlayer]);
			}
			
			serializer.flush();
			serializer.close();
			
			return true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Stats.saveToFile", WTFAmI);
			return false;
		}
	}
	
	public boolean updateOnStart(int player1){
		try{
			if(player1 < 0 || player1 >= Globals.Characters.COUNT)
				return false;
			
			characterUsed[player1]++;
			totalGames++;
			
			bDirty = true;
			return true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Stats.OnStart", WTFAmI);
			return false;
		}
	}
	
	public boolean updateOnStart(int player2, int player3, int player4){
		try{
			if(player2 < 0 || player2 >= Globals.Characters.COUNT)
				return false;
			if(player3 < 0 || player3 >= Globals.Characters.COUNT)
				return false;
			if(player4 < 0 || player4 >= Globals.Characters.COUNT)
				return false;
			
			characterUsed[player2]++;
			if(player3 != player2)
				characterUsed[player3]++;
			if((player2 != player4) && (player3 != player4))
				characterUsed[player4]++;
			totalGames += 3;
			
			bDirty = true;
			
			return true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Stats.OnStart_AI", WTFAmI);
			return false;
		}
	}
	
	public void updateForEndOfHand(){
		totalHands++;
		bDirty = true;
	}
	
	public void updateForWin(int[] yaku, int totalHan, boolean tsumo){
		try{
			for(int thisYaku = 0; thisYaku < Globals.ALLYAKUCOUNT; thisYaku++){
				yakuCount[thisYaku] += Math.min(1, yaku[thisYaku]);
			}
			totalHanCount[totalHan]++;
			if(tsumo)
				wonByTsumo++;
			else
				wonByRon++;
			bDirty = true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Stats.EndOfHand", WTFAmI);
		}
		
	}
	
	public void updateForLoss(boolean tsumo){
		if(!tsumo)
			lostByRon++;
		bDirty = true;
	}
	
	public void updateForEnd(int points, int place){
		try{
			if(place == 1)
				firstPlace++;
			else if(place == 2)
				secondPlace++;
			else if(place == 3)
				thirdPlace++;
			else
				fourthPlace++;
			
			avgScore = avgScore * (totalGames-1);
			avgScore += points;
			avgScore = avgScore / totalGames;
			bDirty = true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Stats.ForEnd", WTFAmI);
		}
	}
	
	public void updateForEnd(int p1Points, int p1Place, int p2Points, int p2Place, int p3Points, int p3Place){
		try{
			if(p1Place == 1)
				firstPlace++;
			else if(p1Place == 2)
				secondPlace++;
			else if(p1Place == 3)
				thirdPlace++;
			else
				fourthPlace++;
			
			if(p2Place == 1)
				firstPlace++;
			else if(p2Place == 2)
				secondPlace++;
			else if(p2Place == 3)
				thirdPlace++;
			else
				fourthPlace++;
			
			if(p3Place == 1)
				firstPlace++;
			else if(p3Place == 2)
				secondPlace++;
			else if(p3Place == 3)
				thirdPlace++;
			else
				fourthPlace++;
			
			avgScore = avgScore * (totalGames-3);
			avgScore += p1Points + p2Points + p3Points;
			avgScore = avgScore / totalGames;
			bDirty = true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Stats.ForEnd_AI", WTFAmI);
		}
	}
	
	public void calcStats(){
		try{
			Integer totalHandsAdjusted = totalHands;
			if(bAI)
				totalHandsAdjusted = totalHandsAdjusted/3;
			
			if(totalHands >0){
				winPercentage = ((double)(wonByRon+wonByTsumo))/((double)totalHandsAdjusted)*100;
			}
			else{
				winPercentage = 0.0;
			}
			
			if((wonByRon+wonByTsumo) > 0){
				ronPercentage = ((double)(wonByRon))/((double)(wonByRon+wonByTsumo))*100;
				tsumoPercentage = ((double)(wonByTsumo))/((double)(wonByRon+wonByTsumo))*100;
				hanPercentages[1] = ((double)totalHanCount[1])/((double)(wonByRon+wonByTsumo))*100;
				hanPercentages[2] = ((double)totalHanCount[2])/((double)(wonByRon+wonByTsumo))*100;
				hanPercentages[3] = ((double)totalHanCount[3])/((double)(wonByRon+wonByTsumo))*100;
				hanPercentages[4] = ((double)totalHanCount[4])/((double)(wonByRon+wonByTsumo))*100;
				hanPercentages[5] = ((double)totalHanCount[5])/((double)(wonByRon+wonByTsumo))*100;
				hanPercentages[6] = ((double)totalHanCount[6]+totalHanCount[7])/((double)(wonByRon+wonByTsumo))*100;
				hanPercentages[7] = ((double)totalHanCount[8]+totalHanCount[9]+totalHanCount[10])/((double)(wonByRon+wonByTsumo))*100;
				hanPercentages[8] = ((double)totalHanCount[11]+totalHanCount[12])/((double)(wonByRon+wonByTsumo))*100;
				hanPercentages[9] = ((double)totalHanCount[13])/((double)(wonByRon+wonByTsumo))*100;
				
				for(int thisYaku = 0; thisYaku < Globals.ALLYAKUCOUNT; thisYaku++){
					yakuPercentages[thisYaku] = ((double)yakuCount[thisYaku])/((double)(wonByRon+wonByTsumo))*100;
				}
			}
			else{
				ronPercentage = 0.0;
				tsumoPercentage = 0.0;
				hanPercentages[1] = 0.0;
				hanPercentages[2] = 0.0;
				hanPercentages[3] = 0.0;
				hanPercentages[4] = 0.0;
				hanPercentages[5] = 0.0;
				hanPercentages[6] = 0.0;
				hanPercentages[7] = 0.0;
				hanPercentages[8] = 0.0;
				hanPercentages[9] = 0.0;
				
				for(int thisYaku = 0; thisYaku < Globals.ALLYAKUCOUNT; thisYaku++){
					yakuPercentages[thisYaku] = 0.0;;
				}
			}
			
			if(totalGames > 0){
				avgFinish = (double)((firstPlace)+(secondPlace*2)+(thirdPlace*3)+(fourthPlace*4))/((double)totalGames);
				
				placePercentages[0] = ((double)(firstPlace))/((double)totalGames)*100;
				placePercentages[1] = ((double)(secondPlace))/((double)totalGames)*100;
				placePercentages[2] = ((double)(thirdPlace))/((double)totalGames)*100;
				placePercentages[3] = ((double)(fourthPlace))/((double)totalGames)*100;
				
				for(int thisChar = 0; thisChar < Globals.Characters.COUNT; thisChar++){
					if(bAI)
						characterPercentages[thisChar] = ((double)characterUsed[thisChar])/((double)totalGames/3)*100;
					else
						characterPercentages[thisChar] = ((double)characterUsed[thisChar])/((double)totalGames)*100;
				}
			}
			else{
				avgFinish = 0.0;
				
				placePercentages[0] = 0.0;
				placePercentages[1] = 0.0;
				placePercentages[2] = 0.0;
				placePercentages[3] = 0.0;
				
				for(int thisChar = 0; thisChar < Globals.Characters.COUNT; thisChar++){
					if(bAI)
						characterPercentages[thisChar] = 0.0;
					else
						characterPercentages[thisChar] = 0.0;
				}
			}
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Stats.calcStats", WTFAmI);
		}
	}
	
	/**
	 * The idea here is that if the initial loading fails we will keep our stats as normal
	 * then at save time we will give the load another chance and if we are successful then
	 * we will merge the stats and save
	 * @param statsToMerge
	 */
	private void mergeStats(Stats statsToMerge){
		try{
			avgScore = avgScore * totalGames;
			statsToMerge.avgScore = statsToMerge.avgScore * statsToMerge.totalGames;
			
			totalGames += statsToMerge.totalGames;
			firstPlace += statsToMerge.firstPlace;
			secondPlace += statsToMerge.secondPlace;
			thirdPlace += statsToMerge.thirdPlace;
			fourthPlace += statsToMerge.fourthPlace;
			
			avgScore += statsToMerge.avgScore;
			avgScore = avgScore / totalGames;
	
			totalHands += statsToMerge.totalHands;
			wonByRon += statsToMerge.wonByRon;
			wonByTsumo += statsToMerge.wonByTsumo;
			lostByRon += statsToMerge.lostByRon;
			
			for(int thisYaku = 0; thisYaku < Globals.ALLYAKUCOUNT; thisYaku++){
				yakuCount[thisYaku] += statsToMerge.yakuCount[thisYaku];
			}
			for(int thisHan = 0; thisHan < 14; thisHan++){
				totalHanCount[thisHan] += statsToMerge.totalHanCount[thisHan];
			}
			for(int thisPlayer = 0; thisPlayer < Globals.Characters.COUNT; thisPlayer++){
				characterUsed[thisPlayer] += statsToMerge.characterUsed[thisPlayer];
			}
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("Stats.MergeStats", WTFAmI);
		}
	}
	
	public boolean hasLoaded(){
		return bLoadedFromFile;
	}
}

