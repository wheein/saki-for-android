package mahjong.riichi;

import java.util.ArrayList;
import java.util.Random;

import android.os.Message;
import android.util.Log;

public class MainGameThread extends Thread implements Runnable{
	//TODO Prevent Chombo
	//TODO Prevent Bad Riichi Discard
	
	/**
	 * Current Game conditions
	 */
	public int curWind;
	public int curPlayer;
	public int curEast;
	public int curRound;
	public boolean selfDrawWin;
	private boolean roundInProgress;
	private int endWind;
	
	/**
	 * Thread Control Variables
	 */
	private boolean mRunning;
	private boolean bWaitingForCharSelect;
	
	/**
	 * Statics
	 */
	private static int DRAW = -1;
	private static int ABORTIVE = -2;
	
	/**
	 * Settings and preferences
	 */
	public boolean bPowers;
	public boolean bKeepStats;
	
	/**
	 * Other classes
	 */
	public Table mTable;
	public Player[] mPlayers;
	public Stats playerStats;
	public Stats AIStats;
	private SakiView mUI;
	
	//Messages from the UI Thread (should be set to -1 or null when not in use)
	int discardIdx;
	public int callCmd; 
	
	public MainGameThread(){
		super("MainGameThread");
		
		try{
			mTable = new Table();
			mTable.setGameThread(this);
			mPlayers = new Player[4];
			for(int i = 0; i < 4; i++){
				mPlayers[i] = new Player(i, this);
				//mPlayers[i].myAI.setGameThread(this);
				//Remove this
				//mPlayers[i].myAI.bCallEverythingMode = true;
			}
			mPlayers[0].setAIControl(false);
			//mPlayers[1].characterID = Globals.Characters.KANA;
			//mPlayers[2].characterID = Globals.Characters.SAKI;
			//mPlayers[3].characterID = Globals.Characters.KOROMO;
			
			playerStats = new Stats();
			AIStats = new Stats();
			
			init();
			
			mRunning = true;
			discardIdx = -1;
			callCmd = -1;
			bPowers = true;
			bWaitingForCharSelect = true;
			endWind = Globals.Winds.WEST;
			
			//randGenerator = new Random();
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("MainGameThread()", WTFAmI);
		}
	}
	
	public void init(){
		try{
			mTable.init();
			curPlayer = 0;
			/* Normal Settings*/
			curWind = Globals.Winds.EAST;
			curEast = 0;
			curRound = 1;
			/*Single hand game
			curWind = Globals.Winds.SOUTH;
			curEast = 0;
			curRound = 4; */
			
			mPlayers[0].setScore(25000);
			mPlayers[1].setScore(25000);
			mPlayers[2].setScore(25000);
			mPlayers[3].setScore(25000);

			roundInProgress = false;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("MainGameThread.init", WTFAmI);
		}
	}
	
	/**
	 * Various Game functions
	 */
	
	public void startRound(){
		try{
			
			mTable.init();

			selfDrawWin = false;
			
			int windIter = curEast;
			for(int iter = 0; iter < 4; iter++){
				mPlayers[windIter].clearHand(Globals.Winds.EAST + iter);
				windIter = (windIter+1)%4;
				mPlayers[windIter].myAI.handlePowersAtStart();
			}
			//pGameEngine.deal();
			int wallCount = mTable.wallCount();
			
			//Deal the starting hands
			for(int tileNum = 0; tileNum < 13; tileNum++){
				for(int dealTo = 0; dealTo < 4; dealTo++){
					Tile temp = getTileFromWall(dealTo);//mTable.drawRandomTile();
					
					mPlayers[dealTo].myHand.deal(temp);
				}
			}
			
			wallCount = mTable.wallCount();
			
			mPlayers[0].myHand.rebuildActiveHand();
			mPlayers[1].myHand.rebuildActiveHand();
			mPlayers[2].myHand.rebuildActiveHand();
			mPlayers[3].myHand.rebuildActiveHand();
			
			mUI.newRound();
			
			roundInProgress = true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("MainGameThread.StartRound", WTFAmI);
		}
	}
	
	public void endRound(boolean shiftEast){
		try{
			if(shiftEast){
				curEast = (curEast+1)%4;
				mTable.clearBonus();
				curRound++;
			}
			else{
				mTable.addBonus();
			}
			
			curPlayer = curEast;
			//doraCount = 0;
			
			//discardCount[0] = 0;
			//discardCount[1] = 0;
			//discardCount[2] = 0;
			//discardCount[3] = 0;
			if(curRound > 4){
				curRound = 1;
				curWind = (curWind+1)%4;
			}
			
			for(int i = 0; i < 4; i++){
				//Check if anyone is negative
				if(mPlayers[i].score < 0){
					//End the game
					curWind = endWind;
				}
				
				if(mPlayers[i].AIControlled)
					mPlayers[i].myAI.onEnd();
				mPlayers[i].myAI.handlePowersAtEnd();
			}
			roundInProgress = false;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("MainGameThread.EndRound", WTFAmI);
		}
	}
	
	//Yes this is kind of stupid, it used to be one function; but was split into drawFromWall
	//and getTileFromWall in order to accommodate dealBased powers
	public void drawFromWall(int player){
		try{
			Tile temp = getTileFromWall(player);//new Tile(mTable.drawNonRandomTile(mPlayers[player].powerTiles));
			mPlayers[player].myHand.draw(temp, true);
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("MainGameThread.DrawFromWall", WTFAmI);
		}
	}
	
	/**
	 * Main Loop
	 */
	public void run(){
		
		while(mRunning){
			try{
				
				
				while(bWaitingForCharSelect && mRunning){
					sleep(200);
				}
				
				init();
				startAIThreads();
				
				if(bKeepStats){
					String error = "";
					if(!playerStats.hasLoaded())
						playerStats.loadFromFile(false, error);
					if(!AIStats.hasLoaded())
						AIStats.loadFromFile(true, error);
				}
				
				playerStats.updateOnStart(mPlayers[0].characterID);
				AIStats.updateOnStart(mPlayers[1].characterID, mPlayers[2].characterID, mPlayers[3].characterID);
			}
			catch(Exception e){
				String WTFAmI = e.toString();
				Log.e("MainGameThread.run_init",WTFAmI);
			}
				//The only time this won't be tru is if someone calls something
				boolean needDraw = true;
				while(curWind != endWind && mRunning){
					try{
						if(!roundInProgress){
							startRound();
						}
						
						//pGameEngine.mSakiView.triggerRedraw();
						
						int result = DRAW;
						
						//Until the wall is empty
						//The only exception to this is when someone calls the last discard
						while((mTable.wallCount() > 0 || !needDraw) && mRunning){
							
							int wallCount = mTable.wallCount();
							
							//Check for abortive draws
							if(mPlayers[0].riichi && mPlayers[1].riichi && mPlayers[2].riichi && mPlayers[3].riichi){
								//4 Riichis
								result = ABORTIVE;
								break;
							}
							if(mTable.getDora().size() == 5){
								//4 Kans
								result = ABORTIVE;
								break;
							}
							if(wallCount == 166){
								//all 4 discards the same
								int[] discardCounts = mTable.getAllDiscardCounts();
								for (int thisTile = 1; thisTile <= Tile.LAST_TILE; thisTile++){
									//This would fail in a situation where say discards go West-West-East, someone
									//calls pon then discards west, and then the next player discards west.  
									//I think the odds of that are low enough to ignore
									if(discardCounts[thisTile] == 4){
										result = ABORTIVE;
										break;
									}
								}
								if(result == ABORTIVE)
									break;
								
							}
							
							//Draw from the wall
							if(needDraw)
								drawFromWall(curPlayer);
							
							if(mPlayers[curPlayer].AIControlled){
								mPlayers[curPlayer].myAI.onDraw();
							}
							else{
								mPlayers[curPlayer].myAI.handlePowersAtDraw();
							}
							
							Tile tileToDiscard = null;
							
							boolean RiichiCalled = false;
							int tilesAway = 6;
							
							//Delete Later
							if(mPlayers[curPlayer].riichi && mPlayers[curPlayer].myHand.tenpaiTiles.isEmpty())
								Globals.myAssert(false);
							
							if(mPlayers[curPlayer].AIControlled){
								mUI.triggerRedraw();
								
								tilesAway = mPlayers[curPlayer].myAI.ShantanCount;
								
								boolean canKan = mPlayers[curPlayer].myHand.canCallSelfKan();
								int promotedKan = mPlayers[curPlayer].myHand.canCallPromotedKan();
								
								if(canKan){
									mPlayers[curPlayer].myAI.requestOutput(AI.SELFKAN);
									if(mPlayers[curPlayer].myAI.getOutput() == 1){
										try{
											mUI.showCall(curPlayer, Globals.CMD.SELFKAN, false);
											mUI.triggerRedraw();
											sleep(1000);
											
											mPlayers[curPlayer].autoSelfKan();
											if(!mTable.addDora()){
												//abortive hand, do...something
											}
											mPlayers[curPlayer].rinshan = true;
											mPlayers[curPlayer].myAI.handlePowersAtKan();
											needDraw = true;
										}
										catch(Exception e){
											String WTFAmI = e.toString();
											Log.e("MainGemThread.run.selfKan", WTFAmI);
										}
										continue;
									}
								}
								
								if(promotedKan != -1){
									mPlayers[curPlayer].myAI.requestOutput(AI.SELFKAN);
									if(mPlayers[curPlayer].myAI.getOutput() == 1){
										try{
											mUI.showCall(curPlayer, Globals.CMD.SELFKAN, false);
											mUI.triggerRedraw();
											sleep(1000);
											
											//Ok this is kind of goofy, we discard it then reclaim it afterwards
											tileToDiscard = mPlayers[curPlayer].myHand.discard(promotedKan, false);
											mTable.discardTile(tileToDiscard, curPlayer);
											
											//We have to check if anyone can ron this
											boolean ronCalled = false;
											for(int playerToCheck = (curPlayer+1)%4; playerToCheck != curPlayer; playerToCheck = (playerToCheck+1)%4){
												mPlayers[playerToCheck].robbing = true;
												if(mPlayers[playerToCheck].myHand.canCallRon(tileToDiscard)){
													if(mPlayers[playerToCheck].AIControlled){
														
														mPlayers[curPlayer].currentState = Globals.Characters.Graphics.SAD;
														mPlayers[playerToCheck].currentState = Globals.Characters.Graphics.HAPPY;
														mUI.showCall(playerToCheck, Globals.CMD.RON, true);
														mUI.triggerRedraw();
														sleep(1000);
														
														mPlayers[playerToCheck].myHand.draw(tileToDiscard, true);
														result = playerToCheck;
														ronCalled = true;
														break;
													}
													else{
														//mPlayers[playerToCheck].myHand.setupTilesToCall();
														mUI.waitingForCallInput(false, false, false, true, false, false, false);
														mUI.triggerRedraw();
														while(callCmd == -1){
															//mUI.waitingForCallInput(mPlayers[playerToCheck].myAI.canCallPon(tileToDiscard), mPlayers[playerToCheck].myAI.canCallChi(tileToDiscard), mPlayers[playerToCheck].myAI.canCallKan(tileToDiscard), mPlayers[playerToCheck].myAI.canCallRon(tileToDiscard), false);
															//I don't know a good way to do this without polling
															sleep(500);
														}
														int whatToDo = callCmd;
														if(whatToDo != -1){
															
															mPlayers[curPlayer].currentState = Globals.Characters.Graphics.SAD;
															mPlayers[playerToCheck].currentState = Globals.Characters.Graphics.HAPPY;
															mUI.showCall(playerToCheck, Globals.CMD.RON, true);
															mUI.triggerRedraw();
															sleep(1000);
															
															//Display something to indicate the call
															mPlayers[playerToCheck].myHand.draw(tileToDiscard, true);
															result = playerToCheck;
															ronCalled = true;
															break;
														}
													}
												}
												mPlayers[playerToCheck].robbing = false;
											}
											if(ronCalled)
												break;
											
											//OK we made it through now kan it
											Tile tileToCall = mTable.undiscardLastTile(curPlayer);
											mPlayers[curPlayer].autoPromotedKan(tileToCall);
											
											//mPlayers[curPlayer].autoSelfKan();
											if(!mTable.addDora()){
												//abortive hand, do...something
											}
											mPlayers[curPlayer].rinshan = true;
											mPlayers[curPlayer].myAI.handlePowersAtKan();
											needDraw = true;
										}
										catch(Exception e){
											String WTFAmI = e.toString();
											Log.e("MainGemThread.run.promotedKan", WTFAmI);
										}
										continue;
									}
								}
								
								//Moved this before riichi, for ippatsu
								mPlayers[curPlayer].myAI.requestOutput(AI.TSUMO);
								if(mPlayers[curPlayer].myAI.getOutput() == 1 && needDraw){//needDraw is there to prevent a tsumo after a pon/chi
									//we won!
									result = curPlayer;  //or something
									selfDrawWin = true;
									
									mPlayers[curPlayer].currentState = Globals.Characters.Graphics.HAPPY;
									for(int tempChar = (curPlayer+1)%4; tempChar != curPlayer; tempChar = (tempChar+1)%4){
										mPlayers[tempChar].currentState = Globals.Characters.Graphics.SAD;
									}
									mUI.showCall(curPlayer, Globals.CMD.TSUMO, true);
									mUI.triggerRedraw();
									sleep(1000);
									//mPlayers[curPlayer].scoreHand();
									//Add in situational yaku
									break;
								}
								
								if(!mPlayers[curPlayer].riichi){
									mPlayers[curPlayer].myAI.requestOutput(AI.RIICHI);
									if(mPlayers[curPlayer].myAI.getOutput() == 1){
										mPlayers[curPlayer].riichi = true;
										mPlayers[curPlayer].ippatsu = true;
										mTable.addPointsToTable(1000);
										mPlayers[curPlayer].offsetScore(-1000);
										//A bit clumsy but this is to make sure it's in Lock Mode after a riichi
										mPlayers[curPlayer].myAI.chooseApproach();
										mUI.showCall(curPlayer, Globals.CMD.RIICHI, false);
										mUI.triggerRedraw();
										sleep(1000);
										
										RiichiCalled = true;
									}
								}
								else{
									mPlayers[curPlayer].ippatsu = false;
								}
								
								
								//else{
										
									if(!mPlayers[curPlayer].riichi || RiichiCalled){ //let it pass when riichi is first called
										mPlayers[curPlayer].myAI.requestOutput(AI.DISCARD);
										int discardThis = mPlayers[curPlayer].myAI.getOutput();
										if(discardThis == -1)
											discardThis = 0;
										tileToDiscard = mPlayers[curPlayer].myHand.discard(discardThis, false);
									}
									else{
										tileToDiscard = mPlayers[curPlayer].myHand.discard(mPlayers[curPlayer].myHand.Tile14, true);
									}
								//}
							}
							else{
								/**We need to get user input...somehow
								 * The UI is in a seperate thread so in theory we should just wait
								 * until it sends us a message, but we'll worry about that later 
								 */
								boolean canRiichi = false;
								boolean canKan = false;
								boolean canTsumo = false;
								//int tilesAway = mPlayers[curPlayer].myAI.getShantanCount(false);
								
								//if we are in riichi we just want to check for a winning hand
								mUI.triggerRedraw(); //Delete this 
								
								//This is a hack, if we leave ippatsu on it will screw up TenpaiTiles
								boolean ippatsuSaver = mPlayers[curPlayer].ippatsu;
								if(mPlayers[curPlayer].ippatsu)
									mPlayers[curPlayer].ippatsu = false;
								
								if(mPlayers[curPlayer].riichi)
									tilesAway = mPlayers[curPlayer].myHand.getShantenCount_TreeVersion(1, false);
								else
									tilesAway = mPlayers[curPlayer].myHand.getShantenCount_TreeVersion(3, false);
								
								//Needed for powers to work
								mPlayers[curPlayer].myAI.ShantanCount = tilesAway;
								
								//Other end of the hack
								mPlayers[curPlayer].ippatsu = ippatsuSaver;
								
								if(!mPlayers[curPlayer].myHand.openHand && !mPlayers[curPlayer].riichi){
									if((tilesAway == 1)&&(mPlayers[curPlayer].score >= 1000)){
										canRiichi = true;
									}
								}
								if(tilesAway == 0){
									if(mPlayers[curPlayer].myHand.scoreHand(true) > 0 && needDraw)
										canTsumo = true;
								}
								
								canKan = mPlayers[curPlayer].myHand.canCallSelfKan();
								int promotedKan = mPlayers[curPlayer].myHand.canCallPromotedKan();
								
								callCmd = -1;
								if(canTsumo || (canKan || promotedKan != -1) || (canRiichi && !mPlayers[curPlayer].riichi)){
									
									mUI.waitingForCallInput(false, false, false, false, canTsumo, canRiichi, (canKan || promotedKan != -1));
									mUI.triggerRedraw();
									while(callCmd == -1 && mRunning){
										//mUI.waitingForCallInput(mPlayers[playerToCheck].myAI.canCallPon(tileToDiscard), mPlayers[playerToCheck].myAI.canCallChi(tileToDiscard), mPlayers[playerToCheck].myAI.canCallKan(tileToDiscard), mPlayers[playerToCheck].myAI.canCallRon(tileToDiscard), false);
										//I don't know a good way to do this without polling
										sleep(500);
									}
									if(callCmd != Globals.CMD.PASS){
										mUI.showCall(curPlayer, callCmd, callCmd == Globals.CMD.TSUMO);
										mUI.triggerRedraw();
										sleep(1000);
									}
									
									if(callCmd == Globals.CMD.TSUMO){
										mPlayers[curPlayer].currentState = Globals.Characters.Graphics.HAPPY;
										for(int tempChar = (curPlayer+1)%4; tempChar != curPlayer; tempChar = (tempChar+1)%4){
											mPlayers[tempChar].currentState = Globals.Characters.Graphics.SAD;
										}
										mUI.showCall(curPlayer, callCmd, callCmd == Globals.CMD.TSUMO);
										mUI.triggerRedraw();
										sleep(1000);
										//mPlayers[curPlayer].scoreHand();
										selfDrawWin = true;
										result = curPlayer;
										break;
									}
									else if(callCmd == Globals.CMD.SELFKAN){
										//Two possibilities here
										
										//Self Kan
										if(canKan){
											mUI.showCall(curPlayer, callCmd, callCmd == Globals.CMD.TSUMO);
											mUI.triggerRedraw();
											sleep(1000);
											mPlayers[curPlayer].autoSelfKan();
											if(!mTable.addDora()){
												//abortive hand, do...something
											}
											mPlayers[curPlayer].rinshan = true;
											mPlayers[curPlayer].myAI.handlePowersAtKan();
											needDraw = true;
											continue;
										}
										//Promoted Kan
										else{
											try{
												mUI.showCall(curPlayer, Globals.CMD.SELFKAN, false);
												mUI.triggerRedraw();
												sleep(1000);
												
												//Ok this is kind of goofy, we discard it then reclaim it afterwards
												tileToDiscard = mPlayers[curPlayer].myHand.discard(promotedKan, false);
												mTable.discardTile(tileToDiscard, curPlayer);
												
												//We have to check if anyone can ron this
												boolean ronCalled = false;
												for(int playerToCheck = (curPlayer+1)%4; playerToCheck != curPlayer; playerToCheck = (playerToCheck+1)%4){
													mPlayers[playerToCheck].robbing = true;
													if(mPlayers[playerToCheck].myHand.canCallRon(tileToDiscard)){
														if(mPlayers[playerToCheck].AIControlled){
															
															mPlayers[curPlayer].currentState = Globals.Characters.Graphics.SAD;
															mPlayers[playerToCheck].currentState = Globals.Characters.Graphics.HAPPY;
															mUI.showCall(playerToCheck, Globals.CMD.RON, true);
															mUI.triggerRedraw();
															sleep(1000);
															
															mPlayers[playerToCheck].myHand.draw(tileToDiscard, true);
															result = playerToCheck;
															ronCalled = true;
															break;
														}
													}
													mPlayers[playerToCheck].robbing = false;
												}
												if(ronCalled)
													break;
												
												//OK we made it through now kan it
												Tile tileToCall = mTable.undiscardLastTile(curPlayer);
												mPlayers[curPlayer].autoPromotedKan(tileToCall);
												
												//mPlayers[curPlayer].autoSelfKan();
												if(!mTable.addDora()){
													//abortive hand, do...something
												}
												mPlayers[curPlayer].rinshan = true;
												mPlayers[curPlayer].myAI.handlePowersAtKan();
												needDraw = true;
											}
											catch(Exception e){
												String WTFAmI = e.toString();
												Log.e("MainGemThread.run.promotedKan", WTFAmI);
											}
											continue;
										}
									}
									else if(callCmd == Globals.CMD.RIICHI){
										mUI.showCall(curPlayer, callCmd, callCmd == Globals.CMD.TSUMO);
										mUI.triggerRedraw();
										sleep(1000);
										mPlayers[curPlayer].riichi = true;
										mPlayers[curPlayer].ippatsu = true;
										mTable.addPointsToTable(1000);
										mPlayers[curPlayer].offsetScore(-1000);
										RiichiCalled = true;
									}	
								}
								
								mUI.triggerRedraw();
								if(mPlayers[curPlayer].riichi && !RiichiCalled)
									mPlayers[curPlayer].ippatsu = false;
								
								if(!mPlayers[curPlayer].riichi || RiichiCalled){
									while(discardIdx == -1 && mRunning){
										mUI.waitingForDiscardInput();
										//I don't know a good way to do this without polling
										sleep(500);
									}
									/*if(discardIdx == Globals.CMD_TSUMO){
										//mPlayers[curPlayer].scoreHand();
										selfDrawWin = true;
										//figure out situational yakus
										result = curPlayer;
										break;
									}
									else if(discardIdx == Globals.CMD_SELFKAN){
										mPlayers[curPlayer].myHand.selfKan();
										if(!mTable.addDora()){
											//abortive hand, do...something
										}
										needDraw = true;
										continue;
										
									}
									else{*/
										tileToDiscard = mPlayers[curPlayer].myHand.discard(discardIdx, false);
										discardIdx = -1;
									//}
								}
								else{
									tileToDiscard = mPlayers[curPlayer].myHand.discard(mPlayers[curPlayer].myHand.Tile14, true);
								}
								
							}
							
							Globals.myAssert(tileToDiscard != null);
							mPlayers[curPlayer].rinshan = false;
							
							//Reset tilesAway for the AI =/
							if(mPlayers[curPlayer].AIControlled)
								tilesAway = mPlayers[curPlayer].myAI.ShantanCount;
							
							//Ugly, but has to be done to set up tenpaiTiles
							if(((tilesAway == 1) && (!mPlayers[curPlayer].riichi)) || RiichiCalled)
								tilesAway = mPlayers[curPlayer].myHand.getShantenCount_TreeVersion(2, true);
							
							if(tilesAway == 1){
								mPlayers[curPlayer].myHand.inTenpai = true;
								boolean clearFuriten = true;
								for(int i = 0; i < mPlayers[curPlayer].myHand.tenpaiTiles.size(); i++){
									if(mTable.hasBeenDiscarded(mPlayers[curPlayer].myHand.tenpaiTiles.get(i), curPlayer, true)){
										mPlayers[curPlayer].myHand.inFuriten = true;
										clearFuriten = false;
										break;
									}
								}
								if(clearFuriten)
									mPlayers[curPlayer].myHand.inFuriten = false;
							}
							else{
								mPlayers[curPlayer].myHand.inTenpai = false;
								mPlayers[curPlayer].myHand.inFuriten = false;
							}
							
							if(mPlayers[curPlayer].AIControlled)
								mPlayers[curPlayer].myAI.onDiscard();
							else{
								mPlayers[curPlayer].myHand.setupTilesToCall();
								//Special Case for the user
								mPlayers[curPlayer].myAI.handlePowersAtDiscard();
							}
							
							//Add to discard Pile
							//Discards[curPlayer][discardCount[curPlayer]++] = tileToDiscard;
							mTable.discardTile(tileToDiscard, curPlayer);
							int highestCmd = 0;
							int playerCalling = -1;
							if(!mPlayers[curPlayer].powerActivated[Globals.Powers.invisibility]){
								for(int playerToCheck = (curPlayer+1)%4; playerToCheck != curPlayer; playerToCheck = (playerToCheck+1)%4){
									if(mPlayers[playerToCheck].AIControlled){
										mPlayers[playerToCheck].myAI.requestOutput(AI.CALL);
										int whatToDo = mPlayers[playerToCheck].myAI.getOutput();
										if(whatToDo != -1){
											//Display something to indicate the call
											if(whatToDo > highestCmd){
												highestCmd = whatToDo;
												playerCalling = playerToCheck;
											}
										}
									}
									else{
										//mPlayers[playerToCheck].myHand.setupTilesToCall();
										callCmd = -1;
										if(!mPlayers[playerToCheck].riichi){
											boolean canCallChi = (curPlayer == ((playerToCheck+3)%4)) && mPlayers[playerToCheck].myHand.canCallChi(tileToDiscard);
											mUI.waitingForCallInput(mPlayers[playerToCheck].myHand.canCallPon(tileToDiscard), canCallChi, mPlayers[playerToCheck].myHand.canCallKan(tileToDiscard), mPlayers[playerToCheck].myHand.canCallRon(tileToDiscard), false, false, false);
										}
										else
											mUI.waitingForCallInput(false, false, false, mPlayers[playerToCheck].myHand.canCallRon(tileToDiscard), false, false, false);
										mUI.triggerRedraw();
										while(callCmd == -1){
											//mUI.waitingForCallInput(mPlayers[playerToCheck].myAI.canCallPon(tileToDiscard), mPlayers[playerToCheck].myAI.canCallChi(tileToDiscard), mPlayers[playerToCheck].myAI.canCallKan(tileToDiscard), mPlayers[playerToCheck].myAI.canCallRon(tileToDiscard), false);
											//I don't know a good way to do this without polling
											sleep(500);
										}
										int whatToDo = callCmd;
										if(whatToDo != -1){
											//Display something to indicate the call
											if(whatToDo > highestCmd){
												highestCmd = whatToDo;
												playerCalling = playerToCheck;
											}
										}
									}
								}
							}
							
							//If someone called the last tile
							if(highestCmd > 0){
								mPlayers[curPlayer].currentState = Globals.Characters.Graphics.SAD;
								mPlayers[playerCalling].currentState = Globals.Characters.Graphics.HAPPY;
								mUI.showCall(playerCalling, highestCmd, highestCmd == Globals.CMD.RON);
								mUI.triggerRedraw();
								sleep(1000);
								
								Tile lastDiscard = mTable.undiscardLastTile(curPlayer);
								mPlayers[playerCalling].myAI.handlePowersAtCall();
								
								if(highestCmd == Globals.CMD.RON){
									mPlayers[playerCalling].myHand.draw(lastDiscard, true);
									//mPlayers[playerCalling].scoreHand();
									//figure out situational yakus
									result = playerCalling;
									break;
								}
								else{
									//Do stuff
									mPlayers[curPlayer].currentState = Globals.Characters.Graphics.NEUTRAL;
									mPlayers[playerCalling].currentState = Globals.Characters.Graphics.NEUTRAL;
									for(int thisPlayer = 0; thisPlayer < 4; thisPlayer++){
										//Clear out any ippatsu's
										mPlayers[thisPlayer].ippatsu = false;
									}
									needDraw = false;
									if(highestCmd == Globals.CMD.CHI){
										if(mPlayers[playerCalling].AIControlled){
											mPlayers[playerCalling].myAI.AIMeld(lastDiscard, curPlayer);
										}
										else{
											ArrayList<Set> possibleChis = mPlayers[playerCalling].getPossibleChiList(lastDiscard);
											Globals.myAssert(!possibleChis.isEmpty());
											if(possibleChis.size() == 1){
												mPlayers[playerCalling].myHand.meld(lastDiscard, possibleChis.get(0).tiles[0], possibleChis.get(0).tiles[1], -1, curPlayer);
											}
											else{
												callCmd = -1;
												mUI.waitingForChiInput(possibleChis);
												mUI.triggerRedraw();
												while(callCmd == -1){
													//mUI.waitingForCallInput(mPlayers[playerToCheck].myAI.canCallPon(tileToDiscard), mPlayers[playerToCheck].myAI.canCallChi(tileToDiscard), mPlayers[playerToCheck].myAI.canCallKan(tileToDiscard), mPlayers[playerToCheck].myAI.canCallRon(tileToDiscard), false);
													//I don't know a good way to do this without polling
													sleep(500);
												}
												int whatToDo = callCmd;
												if(whatToDo != -1){
													mPlayers[playerCalling].myHand.meld(lastDiscard, possibleChis.get(whatToDo).tiles[0], possibleChis.get(whatToDo).tiles[1], -1, curPlayer);
												}
												else
													Globals.myAssert(false);
											}
										}
									}
									else if(highestCmd == Globals.CMD.PON){
										mPlayers[playerCalling].autoPon(lastDiscard, curPlayer);
									}
									else if(highestCmd == Globals.CMD.KAN){
										mPlayers[playerCalling].autoKan(lastDiscard, curPlayer);
										mPlayers[playerCalling].rinshan = true;
										//if(mPlayers[playerCalling].AIControlled){ //Needed for SakiAI
											mPlayers[playerCalling].myAI.handlePowersAtKan();
										//}
										needDraw = true;
										mTable.addDora();
									}
								}
								mUI.triggerRedraw();
								curPlayer = playerCalling;
								continue;
							}
							
							mUI.triggerRedraw();
							curPlayer = (curPlayer+1)%4;
							needDraw = true;
						}
						
						playerStats.updateForEndOfHand();
						AIStats.updateForEndOfHand();
						AIStats.updateForEndOfHand();
						AIStats.updateForEndOfHand();
						
						if(result > -1){
							//then someone won the hand
							//Globals.myAssert(false);
							
							int[] losers = new int[] {-1, -1, -1};
							boolean isEast = mPlayers[result].currentWind == Globals.Winds.EAST;
							if(mPlayers[result].riichi)
								mTable.addUraDora();
							int points = mPlayers[result].myHand.scoreHand(selfDrawWin);
							//mPlayers[result].score += points;
							if(selfDrawWin){
								if(isEast){
									mPlayers[result].myAI.handlePowersAtWin();
									
									if((points%300) != 0)
										points += 100;
									
									int pointsPer = points/3;
									mPlayers[result].offsetScore(points);
									int idx = 0;
									for(int i = (result+1)%4; i != result; i = (i+1)%4){
										mPlayers[i].myAI.handlePowersAtLose();
										losers[idx++] = i;
										mPlayers[i].offsetScore(-pointsPer);
									}
								}
								else{
									mPlayers[result].myAI.handlePowersAtWin();
									int eastPointsPer = points/2;
									if((eastPointsPer%100) != 0)
										eastPointsPer = (points+100)/2;
									
									int pointsPer = eastPointsPer/2;
									if((pointsPer%100) != 0)
										pointsPer = (eastPointsPer+100)/2;
									
									mPlayers[result].offsetScore(points);
									int idx = 0;
									for(int i = (result+1)%4; i != result; i = (i+1)%4){
										mPlayers[i].myAI.handlePowersAtLose();
										losers[idx++] = i;
										if(mPlayers[i].currentWind == Globals.Winds.EAST)
											mPlayers[i].offsetScore(-(2*pointsPer));
										else
											mPlayers[i].offsetScore(-pointsPer);
									}
								}
								
								if(result == 0)
									playerStats.updateForWin(mPlayers[result].myHand.yaku, mPlayers[result].myHand.han/* + mPlayers[result].myHand.dora*/, true);
								else
									AIStats.updateForWin(mPlayers[result].myHand.yaku, mPlayers[result].myHand.han/* + mPlayers[result].myHand.dora*/, true);
							}
							else{
								losers[0] = curPlayer;
								mPlayers[curPlayer].offsetScore(-points);
								mPlayers[result].offsetScore(points);
								
								mPlayers[result].myAI.handlePowersAtWin();
								mPlayers[curPlayer].myAI.handlePowersAtLose();
								
								if(result == 0){
									playerStats.updateForWin(mPlayers[result].myHand.yaku, mPlayers[result].myHand.han/* + mPlayers[result].myHand.dora*/, false);
									AIStats.updateForLoss(false);
								}
								else{
									AIStats.updateForWin(mPlayers[result].myHand.yaku, mPlayers[result].myHand.han/* + mPlayers[result].myHand.dora*/, false);
									playerStats.updateForLoss(false);
								}
							}
							
							mPlayers[result].offsetScore(mTable.getPointsOnTable());
							mTable.clearPointsOnTable();
							
							if(mRunning){//This is counter intuitive, but it prevents it from getting stuck on quit
								mUI.showScoreScreen(result, losers[0], losers[1], losers[2], points, false);
								while(!mRunning){
									sleep(250);
								}
							}
							
						}
						else if(result == DRAW){
							//It's a draw
							
							//Check For nagashi mangan
							int NagashiManganFor = -1;
							for(int thisPlayer = 0; thisPlayer < 4; thisPlayer++){
								//check melds
								boolean bNagashi = true;
								int[] discardCounts = mTable.getDiscardCounts(thisPlayer);
								for(int thisTile = 1; thisTile <= Tile.LAST_TILE; thisTile++){
									if(discardCounts[thisTile] > 0){
										if(Tile.convertRawToSuit(thisTile) < Globals.Suits.SANGEN){
											int relNum = Tile.convertRawToRelative(thisTile);
											if(relNum != 1 && relNum != 9){
												bNagashi = false;
												break;
											}
										}
									}
								}
								
								if(bNagashi){
									for(int meldPlayer = 0; meldPlayer < 4; meldPlayer++){
										if(meldPlayer == thisPlayer)
											continue;
										for(int thisMeld = 0; thisMeld < mPlayers[meldPlayer].myHand.numberOfMelds; thisMeld++){
											if(mPlayers[meldPlayer].myHand.melds[thisMeld][Hand.MELD_TILE_FROM] == thisPlayer){
												bNagashi = false;
												break;
											}
										}
										
										if(!bNagashi)
											break;
									}
								}
								
								if(bNagashi)
									NagashiManganFor = thisPlayer;
							}
							
							if(NagashiManganFor >= 0){
								int[] losers = new int[] {(NagashiManganFor+1)%4, (NagashiManganFor+2)%4, (NagashiManganFor+3)%4};
								boolean isEast = mPlayers[NagashiManganFor].currentWind == Globals.Winds.EAST;
								int points = Globals.otherLimitTable[0];
									if(isEast){
										points = Globals.eastLimitTable[0];
										mPlayers[NagashiManganFor].myAI.handlePowersAtWin();
										
										if((points%300) != 0)
											points += 100;
										
										int pointsPer = points/3;
										mPlayers[NagashiManganFor].offsetScore(points);
										int idx = 0;
										for(int i = (NagashiManganFor+1)%4; i != NagashiManganFor; i = (i+1)%4){
											mPlayers[i].myAI.handlePowersAtLose();
											losers[idx++] = i;
											mPlayers[i].offsetScore(-pointsPer);
										}
									}
									else{
										mPlayers[NagashiManganFor].myAI.handlePowersAtWin();
										int eastPointsPer = points/2;
										if((eastPointsPer%100) != 0)
											eastPointsPer = (points+100)/2;
										
										int pointsPer = eastPointsPer/2;
										if((pointsPer%100) != 0)
											pointsPer = (eastPointsPer+100)/2;
										
										mPlayers[NagashiManganFor].offsetScore(points);
										int idx = 0;
										for(int i = (NagashiManganFor+1)%4; i != NagashiManganFor; i = (i+1)%4){
											mPlayers[i].myAI.handlePowersAtLose();
											losers[idx++] = i;
											if(mPlayers[i].currentWind == Globals.Winds.EAST)
												mPlayers[i].offsetScore(-(2*pointsPer));
											else
												mPlayers[i].offsetScore(-pointsPer);
										}
									}
									
									int[] tempYaku = new int[Globals.ALLYAKUCOUNT];
									tempYaku[Globals.NAGASHIMANGAN] = 5;
									if(NagashiManganFor == 0)
										playerStats.updateForWin(tempYaku, 5, true);
									else
										AIStats.updateForWin(tempYaku, 5, true);
									
									mPlayers[NagashiManganFor].offsetScore(mTable.getPointsOnTable());
									mTable.clearPointsOnTable();
									
									mUI.showCall(NagashiManganFor, Globals.CMD.TSUMO, false);
									mUI.triggerRedraw();
									sleep(2000);
									
									if(mRunning){//This is counter intuitive, but it prevents it from getting stuck on quit
										mUI.showScoreScreen(NagashiManganFor, losers[0], losers[1], losers[2], points, true);
										while(!mRunning){
											sleep(250);
										}
									}
							}
							else{
							
								ArrayList<Integer> inTenpai = new ArrayList<Integer>();
								ArrayList<Integer> notInTenpai = new ArrayList<Integer>();
								for(int i = 0; i < 4; i++){
									if(mPlayers[i].myHand.inTenpai){
										inTenpai.add(i);
										if(i == curEast)
											result = curEast;
									}
									else
										notInTenpai.add(i);
								}
								
								if(inTenpai.size() != 0 && inTenpai.size() != 4){
									int toWinners = 3000/inTenpai.size();
									int fromLosers = 3000/notInTenpai.size();
									for(int i = 0; i < inTenpai.size(); i++){
										mPlayers[inTenpai.get(i)].offsetScore(toWinners);
									}
									for(int i = 0; i < notInTenpai.size(); i++){
										mPlayers[notInTenpai.get(i)].offsetScore(-fromLosers);
									}
									
								}
								
								for(int thisPlayer = 0; thisPlayer < 4; thisPlayer++){
									if(inTenpai.contains(thisPlayer))
										mUI.showCall(thisPlayer, Globals.CMD.TENPAI, true);
									else
										mUI.showCall(thisPlayer, Globals.CMD.NOTEN, false);
									mUI.triggerRedraw();
									sleep(2000);
								}
							}
							mUI.triggerRedraw();
							sleep(2000);
						}
						else if(result == ABORTIVE){
							
						}
						else{
							Globals.myAssert(false);
						}
						
						endRound(!(result == curEast));
					}
					catch(Exception e){
						String WTFAmI = e.toString();
						Log.e("MainGameThread.run", WTFAmI);
					}
					
				}
			try{
				int[] places = new int[] {1,2,3,4};
				int[] scores = new int[] {mPlayers[0].score, mPlayers[1].score, mPlayers[2].score, mPlayers[3].score};
				int highest = -99999;;
				int curUsed = -1;
				for(int rank = 1; rank <= 4; rank++){
					for(int i = 0; i < 4; i++){
						if(scores[i] > highest ){
							curUsed = i;
							highest = scores[i];
						}
					}
					places[curUsed] = rank;
					scores[curUsed] = -99999;
					highest = -99998;
				}
				playerStats.updateForEnd(mPlayers[0].score, places[0]);
				AIStats.updateForEnd(mPlayers[1].score, places[1], mPlayers[2].score, places[2], mPlayers[3].score, places[3]);
				
				if(bKeepStats && mRunning){//Don't save an aborted game
					playerStats.saveToFile(false);
					AIStats.saveToFile(true);
				}
				
				//We've reached the end of the game, show the result screen and the AI threads down
				mUI.showResultScreen();
				stopAIThreads();
				bWaitingForCharSelect = true;
			}
			catch(Exception e){
				String WTFAmI = e.toString();
				Log.e("MainGameThread.run_post", WTFAmI);
			}
			//mRunning = false;
		}
		
		//Shut down our threads
		//mUI.finish(); Can't terminate thread while running
		stopAIThreads();
	}

	/**
	 * Getters and Setters
	 */
	
	public void setUI(SakiView useThis){
		Globals.myAssert(useThis != null);
		mUI = useThis;
	}
	
	public Tile getTileFromWall(int player){
		try{
			Tile ret;
			if(mPlayers[player].powerActivated[Globals.Powers.drawBased]){
				ret = new Tile(mTable.drawNonRandomTile(mPlayers[player].powerTiles));
			}
			else if(mPlayers[player].powerActivated[Globals.Powers.drawBased_TripleOdds]){
				ret = new Tile(mTable.drawNonRandomTile(mPlayers[player].powerTiles, 3));
			}
			else if(mPlayers[player].powerActivated[Globals.Powers.drawBased_DoubleOdds]){
				ret = new Tile(mTable.drawNonRandomTile(mPlayers[player].powerTiles, 2));
			}
			else if(mPlayers[player].powerActivated[Globals.Powers.dealBased]){
				ret = new Tile(mTable.drawNonRandomTile(mPlayers[player].powerTiles, 3));
			}
			else{
				ret = new Tile(mTable.drawRandomTile());
			}
			
			return ret;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("MainGameThread.getTileFromWall", WTFAmI);
			
			//Try this as a last resort
			return new Tile(mTable.drawRandomTile());
		}
	}
	
	public int[] getMeldCounts(){
		int[] meldCounts = new int[] {0,
					 0,0,0,0,0,0,0,0,0,
					 0,0,0,0,0,0,0,0,0,
					 0,0,0,0,0,0,0,0,0,
					 0,0,0,0,
					 0,0,0};
		for(int player = 0; player < 4; player++){
			for(int i = 0; i < mPlayers[player].myHand.numberOfMelds; i++){
				meldCounts[mPlayers[player].getRawTileAt(mPlayers[player].myHand.melds[i][1]).rawNumber]++;
				meldCounts[mPlayers[player].getRawTileAt(mPlayers[player].myHand.melds[i][2]).rawNumber]++;
				meldCounts[mPlayers[player].getRawTileAt(mPlayers[player].myHand.melds[i][3]).rawNumber]++;
				if(mPlayers[player].myHand.melds[i][0] == 4)
					meldCounts[mPlayers[player].getRawTileAt(mPlayers[player].myHand.melds[i][4]).rawNumber]++;
			}
		}
		return meldCounts;
	}
	
	
	
	public int getNumberInRiichi(){
		int count = 0;
		for(int i = 0; i < 4; i++){
			if(mPlayers[i].riichi)
				count++;
		}
		return count;	
	}
	
	public int getNumberInRiichi(int everyoneButMe){
		int count = 0;
		for(int i = 0; i < 4; i++){
			if(i == everyoneButMe)
				continue;
			if(mPlayers[i].riichi)
				count++;
		}
		return count;	
	}
	
	/**
	 * Communication with other threads
	 */
	
	public void sendDiscardMessage(int Idx){
		Globals.myAssert(Idx >= 0);
		discardIdx = Idx;
	}
	
	public void sendCallMessage(int cmd){
		callCmd = cmd;
	}
	
	public void eastOnlyGame(){
		endWind = Globals.Winds.SOUTH;
	}
	
	public void eastSouthGame(){
		endWind = Globals.Winds.WEST;
	}
	
	public boolean setCharacter(int playerNum, int CharID){
		try{
			if(playerNum < 0 || playerNum > 3)
				return false;
			if(CharID < 0 || CharID >= Globals.Characters.COUNT)
				return false;
			mPlayers[playerNum].characterID = CharID;
			//Set AI here too
			if(CharID == Globals.Characters.SAKI){
				mPlayers[playerNum].myAI = new SakiAI(playerNum);
				mPlayers[playerNum].myAI.setGameThread(this);
			}
			else if(CharID == Globals.Characters.MOMOKA){
				mPlayers[playerNum].myAI = new MomokaAI(playerNum);
				mPlayers[playerNum].myAI.setGameThread(this);
			}
			else if(CharID == Globals.Characters.KOROMO){
				mPlayers[playerNum].myAI = new KoromoAI(playerNum);
				mPlayers[playerNum].myAI.setGameThread(this);
			}
			else if(CharID == Globals.Characters.HISA){
				mPlayers[playerNum].myAI = new HisaAI(playerNum);
				mPlayers[playerNum].myAI.setGameThread(this);
			}
			else if(CharID == Globals.Characters.MIHOKO || CharID == Globals.Characters.MAKO){
				mPlayers[playerNum].myAI = new MihokoAI(playerNum);
				mPlayers[playerNum].myAI.setGameThread(this);
			}
			else if(CharID == Globals.Characters.KAORI){
				mPlayers[playerNum].myAI = new KaoriAI(playerNum);
				mPlayers[playerNum].myAI.setGameThread(this);
			}
			else if(CharID == Globals.Characters.KANA){
				mPlayers[playerNum].myAI = new KanaAI(playerNum);
				mPlayers[playerNum].myAI.setGameThread(this);
			}
			else if(CharID == Globals.Characters.YUUKI){
				mPlayers[playerNum].myAI = new YuukiAI(playerNum);
				mPlayers[playerNum].myAI.setGameThread(this);
			}
			else{
				mPlayers[playerNum].myAI = new AI(playerNum);
				mPlayers[playerNum].myAI.setGameThread(this);
			}
			return true;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("MainGameThread.setChar", WTFAmI);
			return false;
		}
	}
	
	/**
	 * Thread Control
	 */
	void suspendThread(){
		mRunning = false;
	}
	
	void resumeThread(){
		mRunning = true;
	}
	
	public void terminateThread(){
		mRunning = false;
	}
	
	private void startAIThreads(){
		try{
			//Start Up the AI's
			for(int i = 0; i < 4; i++){
				if(mPlayers[i].AIControlled){
					if(mPlayers[i].myAI == null){
						Log.e("MainGAmeThread.StartRound", "AI not Initialized");
						mPlayers[i].myAI = new AI(i);
						mPlayers[i].myAI.setGameThread(this);
					}
					if(!mPlayers[i].myAI.isAlive())
						mPlayers[i].myAI.start();
				}
			}
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("MainGameThread.startAIThreads", WTFAmI);
		}
	}
	
	private void stopAIThreads(){
		try{
			//Start Up the AI's
			for(int i = 0; i < 4; i++){
				if(mPlayers[i].AIControlled){
					if(mPlayers[i].myAI != null){
						mPlayers[i].myAI.terminateThread();
						mPlayers[i].myAI.join();
					}
				}
			}
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("MainGameThread.stopAIThreads", WTFAmI);
		}
	}
	
	public void charSelectDone(){
		bWaitingForCharSelect = false;
	}
}

