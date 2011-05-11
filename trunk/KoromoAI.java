package mahjong.riichi;

import android.util.Log;

public class KoromoAI extends AI {
	int wallCountAtDraw;
	
	/**
	 * Constructors
	 */
	KoromoAI(){
		super();
	}
	
	KoromoAI(Integer ID){
		super(ID);
	}
	
	/**
	 * Overrides for the special powers
	 */
	protected void handlePowersAtStart(){
		super.handlePowersAtStart();
	}
	
	protected void handlePowersAtDraw(){
		wallCountAtDraw = pGameThread.mTable.wallCount();
		super.handlePowersAtDraw();
	}
	
	protected void handlePowersAtDiscard(){
		super.handlePowersAtDiscard();
		
		for(int i = 0; i < pMyPlayer.powerTiles.size(); i++){
			pGameThread.mTable.unreserveTile(pMyPlayer.powerTiles.get(i));
		}
		pMyPlayer.powerTiles.clear();
		pMyPlayer.powerActivated[Globals.Powers.drawBased] = false;
		
		if(ShantanCount == 1){
			for(int thisTile = 0; thisTile < pMyPlayer.myHand.tenpaiTiles.size(); thisTile++){
				if(pGameThread.mTable.isLeftInWall(pMyPlayer.myHand.tenpaiTiles.get(thisTile))){
					//pMyPlayer.powerActivated[Globals.Powers.drawBased] = true;
					pMyPlayer.powerTiles.add(pMyPlayer.myHand.tenpaiTiles.get(thisTile));
				}
			}
		}
		
		for(int i = 0; i < pMyPlayer.powerTiles.size(); i++){
			pGameThread.mTable.reserveTile(pMyPlayer.powerTiles.get(i));
		}
		
		if(wallCountAtDraw == 4 && pMyPlayer.powerTiles.size() > 0)
			pMyPlayer.powerActivated[Globals.Powers.drawBased] = true;
	}
	
	protected void handlePowersAtCall(){
		wallCountAtDraw = pGameThread.mTable.wallCount();
		super.handlePowersAtCall();
	}
	
	protected void handlePowersAtEnd(){
		super.handlePowersAtEnd();
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
		pMyPlayer.powerActivated[Globals.Powers.drawBased] = false;
		
		for(int i = 0;i < pMyPlayer.powerTiles.size(); i++){
			pGameThread.mTable.unreserveTile(pMyPlayer.powerTiles.get(i));
		}
		pMyPlayer.powerTiles.clear();
	}
	
	/**
	 * Other Overrides
	 */
	protected int handleCall(){
		/**
		 * This will require a bit of tweaking.
		 * The general idea is that when the wall starts getting low we want to try and angle
		 * for a haitei.  We don't want to throw away good hands just for a haitei for no reason though
		 *
		 */
		try{
			while(hasChanged){ //analyzeHand isn't done yet, if this happens a lot we should look into a change
				sleep(100);
			}
			
			Tile lastDiscard = pGameThread.mTable.getLastDiscard();
			
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
			
			//If this will put us in line for the last tile
			if((pGameThread.mTable.wallCount()%4) == 0){
				//For now only do this in the last 6 go-arounds
				if(pGameThread.mTable.wallCount() <= 24){
					//Only do this if we are close
					if(ShantanCount < 3){
						//It doesn't help our yaku but call it if it lowers our count
						boolean doWhatever = false;
						//Is there a reason we need to stay concealed?
						if(!Globals.needsToBeConcealed(primaryYaku)){
							//int[] discardCounts = pGameThread.mTable.getAllDiscardCounts(myID);
							//How many conditions will we have >_>
							if(primaryYaku == Globals.TOITOI){
								if(kan)
									return Globals.CMD.KAN;
								if(pon)
									return Globals.CMD.PON;
							}
							else if(primaryYaku == Globals.CHANTA){
								if(lastDiscard.getType() == Globals.HONOR){
									if(kan)
										return Globals.CMD.KAN;
									if(pon)
										return Globals.CMD.PON;
								}
								else if(lastDiscard.getType() == Globals.TERMINAL){
									if(kan)
										return Globals.CMD.KAN;
									if(pon)
										return Globals.CMD.PON;
									if(chi){
										if(lastDiscard.getNumber() == 1){
											tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
											tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+2);
											if(tilesToUse[0] > 0 && tilesToUse[1] > 0)
												return Globals.CMD.CHI;
										}
										else{
											tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
											tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-2);
											if(tilesToUse[0] > 0 && tilesToUse[1] > 0)
												return Globals.CMD.CHI;
										}
									}
								}
								else{
									if(chi){
										if(lastDiscard.getNumber() == 7){
											tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
											tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+2);
											if(tilesToUse[0] > 0 && tilesToUse[1] > 0)
												return Globals.CMD.CHI;
										}
										else if(lastDiscard.getNumber() == 2 || lastDiscard.getNumber() == 8){
											tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
											tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
											if(tilesToUse[0] > 0 && tilesToUse[1] > 0)
												return Globals.CMD.CHI;
										}
										else if(lastDiscard.getNumber() == 3){
											tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
											tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-2);
											if(tilesToUse[0] > 0 && tilesToUse[1] > 0)
												return Globals.CMD.CHI;
										}
									}
								}
							}
							else if(primaryYaku == Globals.HONROUTOU){
								if(lastDiscard.getType() != Globals.SIMPLE){
									if(pon && !kan)
										return Globals.CMD.PON;
								}
								
							}
							else if(primaryYaku == Globals.ITSU){
								if(individualSuitValue[lastDiscard.getSuit()] > 0){
									int[] tileCounts = pMyPlayer.myHand.getTileCounts();
									//if(tileCounts[lastDiscard.rawNumber] == 0){
										
									//}
									//Whatever, just call it
									if(lastDiscard.getNumber() == 1 || lastDiscard.getNumber() == 4 || lastDiscard.getNumber() == 7){
										tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
										tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+2);
										if(tilesToUse[0] > 0 && tilesToUse[1] > 0)
											return Globals.CMD.CHI;
									}
									else if(lastDiscard.getNumber() == 2 || lastDiscard.getNumber() == 5 || lastDiscard.getNumber() == 8){
										tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
										tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber+1);
										if(tilesToUse[0] > 0 && tilesToUse[1] > 0)
											return Globals.CMD.CHI;
									}
									else{
										tilesToUse[0] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-1);
										tilesToUse[1] = pMyPlayer.myHand.getFirstTile(lastDiscard.rawNumber-2);
										if(tilesToUse[0] > 0 && tilesToUse[1] > 0)
											return Globals.CMD.CHI;
									}
								}
								else
									doWhatever = true;
							}
							else if(primaryYaku == Globals.SANKANTSU){
								if(kan)
									return Globals.CMD.KAN;
							}
							else if(primaryYaku == Globals.SANSHOKUDOUJUN){
								//Do we already have this tile?
								if(pMyPlayer.myHand.getFirstTile(lastDiscard) == -1){
									//Is this even our doujun tile?
									if(individualTileWeights[lastDiscard.rawNumber] != 0){
										if((individualTileWeights[lastDiscard.rawNumber] & DOUJUNWEIGHT) == DOUJUNWEIGHT){
											//OK we need this tile
											//We'll be super picky for now, are there only 2 left?
											//if(discardCounts[lastDiscard.rawNumber] >= 2){
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
											//}
										}
									}
								}
							}
							else if(primaryYaku == Globals.SANSHOKUDOUKOU){
								//Is this our doukou tile?
								if(individualTileWeights[lastDiscard.rawNumber] != 0){
									if((individualTileWeights[lastDiscard.rawNumber] & DOUKOUWEIGHT) == DOUKOUWEIGHT){
										if(kan)
											return Globals.CMD.KAN;
										if(pon){
											return Globals.CMD.PON;
										}
									}
								}
								doWhatever = true;
							}
							else if(primaryYaku == Globals.SHOUSANGEN){
								if(lastDiscard.getSuit() == Globals.Suits.SANGEN){
									if(kan)
										return Globals.CMD.KAN;
									if(pon)
										return Globals.CMD.PON;
								}
								doWhatever = true;
							}
							else if(primaryYaku == Globals.YAKUHAI){
								if(lastDiscard.getSuit() == Globals.Suits.SANGEN){
									if(kan)
										return Globals.CMD.KAN;
									if(pon)
										return Globals.CMD.PON;
								}
								else if(lastDiscard.getSuit() == Globals.Suits.KAZE){
									if(pMyPlayer.isMyWind(lastDiscard)){
										if(kan)
											return Globals.CMD.KAN;
										if(pon)
											return Globals.CMD.PON;
									}
								}
								else{
									doWhatever = true;
								}
							}
							else if(primaryYaku == Globals.HONITSU){
								if(individualSuitValue[lastDiscard.getSuit()] > 0){
									doWhatever = true;
								}
								else if(lastDiscard.getType() == Globals.HONOR){
									if(kan)
										return Globals.CMD.KAN;
									if(pon)
										return Globals.CMD.PON;
								}
							}
							else if(primaryYaku == Globals.CHINITSU){
								if(individualSuitValue[lastDiscard.getSuit()] > 0){
									doWhatever = true;
								}
							}
							
						}
						else if(primaryYaku == Globals.SANANKOU){
							//Screw sanankou
							if(pon && !kan)
								return Globals.CMD.PON;
						}
						else if(primaryYaku == -1)
							doWhatever = true;
						
						if(doWhatever){
							//Think about adding something similar to the main AI
							for(int thisSet = 0; thisSet < optimalHand.size(); thisSet++){
								Set set = optimalHand.get(thisSet);
								if(set.size == 2){
									if(set.isPair()){
										if(set.tiles[0] == lastDiscard.rawNumber){
											if(pon)
												return Globals.CMD.PON;
										}
									}
									else{
										if(lastDiscard.getSuit() == Tile.convertRawToSuit(set.tiles[0])){
											if(set.tiles[0] == set.tiles[1]+1){
												if(lastDiscard.rawNumber == (set.tiles[0]+1)){
													tilesToUse[0] = pMyPlayer.myHand.getFirstTile(set.tiles[1]);
													tilesToUse[1] = pMyPlayer.myHand.getFirstTile(set.tiles[0]);
													if(tilesToUse[0] != -1 && tilesToUse[1] != -1)
														return Globals.CMD.CHI;
												}
												else if(lastDiscard.rawNumber == (set.tiles[1]-1)){
													tilesToUse[0] = pMyPlayer.myHand.getFirstTile(set.tiles[1]);
													tilesToUse[1] = pMyPlayer.myHand.getFirstTile(set.tiles[0]);
													if(tilesToUse[0] != -1 && tilesToUse[1] != -1)
														return Globals.CMD.CHI;
												}
											}
											else if(set.tiles[0] == set.tiles[1]-1){
												if(lastDiscard.rawNumber == (set.tiles[0]-1)){
													tilesToUse[0] = pMyPlayer.myHand.getFirstTile(set.tiles[0]);
													tilesToUse[1] = pMyPlayer.myHand.getFirstTile(set.tiles[1]);
													if(tilesToUse[0] != -1 && tilesToUse[1] != -1)
														return Globals.CMD.CHI;
												}
												else if(lastDiscard.rawNumber == (set.tiles[1]+1)){
													tilesToUse[0] = pMyPlayer.myHand.getFirstTile(set.tiles[0]);
													tilesToUse[1] = pMyPlayer.myHand.getFirstTile(set.tiles[1]);
													if(tilesToUse[0] != -1 && tilesToUse[1] != -1)
														return Globals.CMD.CHI;
												}
											}
											else if(set.tiles[0] == set.tiles[1]+2){
												if(lastDiscard.rawNumber == (set.tiles[0]-1)){
													tilesToUse[0] = pMyPlayer.myHand.getFirstTile(set.tiles[1]);
													tilesToUse[1] = pMyPlayer.myHand.getFirstTile(set.tiles[0]);
													if(tilesToUse[0] != -1 && tilesToUse[1] != -1)
														return Globals.CMD.CHI;
												}
											}
											else if(set.tiles[0] == set.tiles[1]-2){
												if(lastDiscard.rawNumber == (set.tiles[1]-1)){
													tilesToUse[0] = pMyPlayer.myHand.getFirstTile(set.tiles[0]);
													tilesToUse[1] = pMyPlayer.myHand.getFirstTile(set.tiles[1]);
													if(tilesToUse[0] != -1 && tilesToUse[1] != -1)
														return Globals.CMD.CHI;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			return super.handleCall();
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("KoromoAI.HandleCall", WTFAmI);
			return -1;
		}
	}
}
