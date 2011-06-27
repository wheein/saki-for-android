package mahjong.riichi;

import android.util.Log;

public class SakiAI extends AI{
	SakiAI(){
		super();
	}
	
	SakiAI(Integer ID){
		super(ID);
	}
	
	public void init(){
		super.init();
		//We are skewing this toward more pon friendly hands
		/*
		thresholds = new int[] {2, //Pinfu 				-1
								3, //Tanyao			    +0
								1, //Iipeikou			+0
								1, //Yakuhai			+0
								1, //Sanshoku Doujun	-1
								2, //Itsu				+0
								4, //Chanta				+1
								4, //Honroutou			+1
								5, //Toitoi				+1
								3, //Sanankou			+1
								3, //Sankantsu			+0
								3, //Sanshoku Doukou	+1
								1, //Chiitoi			+0
								3, //Shousangen			+0
								3, //Honitsu			+0
								3, //Junchantayao		+0
								2, //Ryanpeikou			-1
								3};//Chinitsu			+0*/
		
		priority = new int[] {Globals.SHOUSANGEN,
							  Globals.CHINITSU,
							  Globals.JUNCHANTAYAO,
							  Globals.SANSHOKUDOUKOU,
							  Globals.SANANKOU,
							  Globals.TOITOI,
							  Globals.SANKANTSU,
							  Globals.HONROUTOU,
							  Globals.HONITSU, 
							  Globals.CHANTA,
							  Globals.IIPEIKOU,
							  Globals.ITSU,
							  Globals.TANYAO,
							  Globals.YAKUHAI,
							  Globals.SANSHOKUDOUJUN,
							  Globals.CHIITOI,
							  Globals.PINFU,
							  Globals.RYANPEIKOU};
	}
	
	/*public void requestOutput(int cmd){
		/*if(cmd == DISCARD){
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
		}*/
		/*else if(cmd == RIICHI){
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
		}*/
		/*else *//*if(cmd == SELFKAN){
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
			if(primaryYaku == Globals.TOITOI || primaryYaku == Globals.SANKANTSU || primaryYaku == Globals.SANANKOU){
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
		else{
			super.requestOutput(cmd);
			return;
		}
	}*/
	
	protected int handleCall(){
		/**
		 * The only real change here is that we want to call more kans
		 */
		try{
			while(hasChanged){ //analyzeHand isn't done yet, if this happens a lot we should look into a change
				sleep(100);
			}
			
			Tile lastDiscard = pGameThread.mTable.getLastDiscard();
			
			//Can we even call this?
			boolean kan = pMyPlayer.myHand.canCallKan(lastDiscard);
			
			if(!kan){
				return super.handleCall();
			}
			
			if(ShantanCount == 1){
				//We have a shot at rinshan, do it every time
				return Globals.CMD.KAN;
			}
			
			if(pMyPlayer.myHand.openHand){
				//We're already open, do it for the sake of doing it
				return Globals.CMD.KAN;
			}
			
			return super.handleCall();
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("SakiAI.HandleCall", WTFAmI);
			return -1;
		}
	}
	
	protected void handlePowersAtDiscard(){
		try{
			//Reserve our Kan Tiles
			//if(ShantanCount <= 2){
				//Clear it out first...
				//I don;t like doing this, it seems too costly, but I don't want to end up with a list full of 
				//reserved tiles that no one needs
				int i = 0;
				for(;i < pMyPlayer.powerTiles.size(); i++){
					pGameThread.mTable.unreserveTile(pMyPlayer.powerTiles.get(i));
				}
				pMyPlayer.powerTiles.clear();
				
				i = 0;
				/*while(i < optimalHand.size()){
					Set thisSet = optimalHand.get(i);
					if(thisSet.size == 3){
						if(!thisSet.isChi()){
							if(pGameThread.mTable.isLeftInWall(thisSet.tiles[0])){
								if(!pMyPlayer.myHand.tenpaiTiles.contains(thisSet.tiles[0])){
									//pGameThread.mTable.reserveTile(thisSet.tiles[0]);
									//pMyPlayer.powerActivated[Globals.Powers.drawBased] = true;
									pMyPlayer.powerTiles.add(thisSet.tiles[0]);
								}
							}
						}
					}
					i++;
				}*/
				/**
				 * We're changing this up a bit
				 * Saki's powers are almost never triggering.  o we will reserve tiles wayyyyy
				 * before we ever need it
				 */
				int[] tileCounts = pMyPlayer.myHand.getTileCounts();
				for(int thisTile = 1; thisTile <= Tile.LAST_TILE; thisTile++){
					if(tileCounts[thisTile] == 3){
						if(pGameThread.mTable.isLeftInWall(thisTile)){
							pMyPlayer.powerTiles.add(thisTile);
						}
					}
				}
				//Check the melds too
				int numberOfMelds = pMyPlayer.myHand.numberOfMelds;
	        	if(numberOfMelds > 0){
		        	for(int meldIdx = 0; meldIdx < numberOfMelds; meldIdx++){
		        		if(pMyPlayer.myHand.melds[meldIdx][0]==3){
		        			Tile tile1 = pMyPlayer.myHand.rawHand[pMyPlayer.myHand.melds[meldIdx][1]];
		        			if(tile1.equals(pMyPlayer.myHand.rawHand[pMyPlayer.myHand.melds[meldIdx][2]])){
		        				if(pGameThread.mTable.isLeftInWall(tile1.rawNumber)){
									if(!pMyPlayer.myHand.tenpaiTiles.contains(tile1.rawNumber)){
										//pGameThread.mTable.reserveTile(thisSet.tiles[0]);
										//pMyPlayer.powerActivated[Globals.Powers.drawBased] = true;
										pMyPlayer.powerTiles.add(tile1.rawNumber);
									}
								}
		        			}
		        		}
		        	}
	        	}
			//}
			if(ShantanCount == 1){
				if(!pMyPlayer.powerTiles.isEmpty()){
					//OK we can call a kan, let's see if we can rinshan
					for(int thisTile = 0; thisTile < pMyPlayer.myHand.tenpaiTiles.size(); thisTile++){
						if(pGameThread.mTable.isLeftInWall(pMyPlayer.myHand.tenpaiTiles.get(thisTile))){
							//pGameThread.mTable.reserveTile(pMyPlayer.myHand.tenpaiTiles.get(thisTile));
							pMyPlayer.powerActivated[Globals.Powers.drawBased] = true;
							pMyPlayer.powerTiles.add(pMyPlayer.myHand.tenpaiTiles.get(thisTile));
						}
					}
				}
			}

			//Just reserve the kan tiles without turning on our power
			for(i = 0; i < pMyPlayer.powerTiles.size(); i++){
				pGameThread.mTable.reserveTile(pMyPlayer.powerTiles.get(i));
			}
			
			//if(!pMyPlayer.powerActivated[Globals.Powers.drawBased])
			//	pMyPlayer.powerTiles.clear();
			//else{
			//	for(i = 0; i < pMyPlayer.powerTiles.size(); i++){
			//		pGameThread.mTable.reserveTile(pMyPlayer.powerTiles.get(i));
			//	}
			//}
			
			super.handlePowersAtDiscard();
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("SakiAI.HandlePowersDiscard", WTFAmI);
		}
	}
	
	protected void handlePowersAtKan(){
		try{
			if(pMyPlayer.rinshan && ShantanCount == 1){
				
				//This should only happen after a self kan =/
				if(pMyPlayer.myHand.tenpaiTiles.isEmpty()){
					pMyPlayer.myHand.getShantenCount_TreeVersion(1, true);
				}
				
				//Activate powers 
				for(int thisTile = 0; thisTile < pMyPlayer.myHand.tenpaiTiles.size(); thisTile++){
					if(pGameThread.mTable.isLeftInWall(pMyPlayer.myHand.tenpaiTiles.get(thisTile))){
						//pGameThread.mTable.reserveTile(pMyPlayer.myHand.tenpaiTiles.get(thisTile));
						pMyPlayer.powerActivated[Globals.Powers.drawBased] = true;
						pMyPlayer.powerTiles.add(pMyPlayer.myHand.tenpaiTiles.get(thisTile));
					}
				}
			}
			
			if(!pMyPlayer.powerActivated[Globals.Powers.drawBased])
				pMyPlayer.powerTiles.clear();
			else{
				for(int i = 0; i < pMyPlayer.powerTiles.size(); i++){
					pGameThread.mTable.reserveTile(pMyPlayer.powerTiles.get(i));
				}
			}
			
			super.handlePowersAtKan();
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("SakiAI.HandlePowersForce", WTFAmI);
		}
	}
	
	protected void handlePowersAtStart(){
		super.handlePowersAtStart();
		pMyPlayer.powerActivated[Globals.Powers.drawBased] = false;
	}
	
	protected void turnOffPowers(){
		pMyPlayer.powerActivated[Globals.Powers.drawBased] = false;
		
		for(int i = 0;i < pMyPlayer.powerTiles.size(); i++){
			pGameThread.mTable.unreserveTile(pMyPlayer.powerTiles.get(i));
		}
		pMyPlayer.powerTiles.clear();
	}
	
	protected void handlePowersAtEnd(){
		turnOffPowers();
	}
}
