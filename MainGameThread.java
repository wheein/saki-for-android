package mahjong.riichi;

import java.util.ArrayList;
import java.util.Random;

import android.os.Message;
import android.util.Log;

public class MainGameThread extends Thread implements Runnable{
	//TODO Prevent Chombo
	//TODO Prevent Bad Riichi Discard
	//Private variables
	private boolean mRunning;
	private boolean roundInProgress;
	private SakiView mUI;
	//Random randGenerator;
	//GameManager pGameEngine;
	
	//Variable exposed/Used by Other Threads (Read access only)
	//public int[] discardCount; 
	//public Tile[][] Discards;
	public int curWind;
	//public Tile[] Dora;
	//public int doraCount;
	public int curPlayer;
	public int curEast;
	public int curRound;
	public boolean selfDrawWin;
	
	//Other classes
	public Table mTable;
	public Player[] mPlayers;
	
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
				mPlayers[i].myAI.setGameThread(this);
				//Remove this
				//mPlayers[i].myAI.bCallEverythingMode = true;
			}
			mPlayers[0].setAIControl(false);
			mPlayers[1].characterID = Globals.Characters.KANA;
			mPlayers[2].characterID = Globals.Characters.SAKI;
			mPlayers[3].characterID = Globals.Characters.KOROMO;
			
			init();
			
			mRunning = true;
			discardIdx = -1;
			callCmd = -1;
			//randGenerator = new Random();
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("MainGameThread()", WTFAmI);
		}
	}
	
	public void init(){
		mTable.init();
		curPlayer = 0;
		curWind = Globals.Winds.EAST;
		curEast = 0;
		curRound = 1;
		//Dora = new Tile[8];
		//doraCount = 0;
		//Discards = new Tile[4][22];
		//discardCount = new int[] {0,0,0,0};
		roundInProgress = false;
	}
	
	public void setUI(SakiView useThis){
		Globals.myAssert(useThis != null);
		mUI = useThis;
	}
	
	public void startRound(){
		try{
			
			mTable.init();
			
			selfDrawWin = false;
			
			int windIter = curEast;
			for(int iter = 0; iter < 4; iter++){
				mPlayers[windIter].clearHand(Globals.Winds.EAST + iter);
				windIter = (windIter+1)%4;
			}
			//pGameEngine.deal();
			int wallCount = mTable.wallCount();
			
			//Deal the starting hands
			for(int tileNum = 0; tileNum < 13; tileNum++){
				for(int dealTo = 0; dealTo < 4; dealTo++){
					Tile temp = mTable.drawRandomTile();
					mPlayers[dealTo].myHand.deal(temp);
				}
			}
			
			wallCount = mTable.wallCount();
			
			mPlayers[0].myHand.sort();
			mPlayers[1].myHand.sort();
			mPlayers[2].myHand.sort();
			mPlayers[3].myHand.sort();
			
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
			else
				mTable.addBonus();
			
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
				if(mPlayers[i].AIControlled)
					mPlayers[i].myAI.onEnd();
			}
			roundInProgress = false;
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("MainGameThread.EndRound", WTFAmI);
		}
	}
	
	public void drawFromWall(int player){
		try{
			Tile temp = new Tile(mTable.drawRandomTile());
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
				//sleep(2000); //Give the render thread time to get started, maybe use a yeild?
				//TODO reduce the amount of direct varaible manipulation of GameEngine
				//pGameEngine = Globals.GameEngine;
				
				//The only time this won't be tru is if someone calls something
				boolean needDraw = true;
				while(curWind != Globals.Winds.WEST){
					if(!roundInProgress){
						startRound();
					}
					
					//pGameEngine.mSakiView.triggerRedraw();
					
					int result = -1;
					
					//Until the wall is empty
					while(mTable.wallCount() > 0){
						
						int wallCount = mTable.wallCount();
						if(needDraw)
							drawFromWall(curPlayer);
						
						//We are moving this to try and hide load times
						//mUI.triggerRedraw();
						
						
						if(mPlayers[curPlayer].AIControlled){
							mPlayers[curPlayer].myAI.onDraw();
						}
						//sleep(2000); //delete this, it's just so I can see what's happening
						
						Tile tileToDiscard = null;
						
						boolean RiichiCalled = false;
						int tilesAway = 6;
						
						//Delete Later
						if(mPlayers[curPlayer].riichi && mPlayers[curPlayer].myHand.tenpaiTiles.isEmpty())
							Globals.myAssert(false);
						
						//There needs to be a special Tsumo tile/signal...in the future
						if(mPlayers[curPlayer].AIControlled){
							mUI.triggerRedraw();
							
							tilesAway = mPlayers[curPlayer].myAI.ShantanCount;
							
							boolean canKan = mPlayers[curPlayer].myHand.canCallSelfKan();
							if(canKan){
								mPlayers[curPlayer].myAI.requestOutput(AI.SELFKAN);
								if(mPlayers[curPlayer].myAI.getOutput() == 1){
									mUI.showCall(curPlayer, Globals.CMD.SELFKAN, true);
									mUI.triggerRedraw();
									sleep(1000);
									
									mPlayers[curPlayer].autoSelfKan();
									if(!mTable.addDora()){
										//abortive hand, do...something
									}
									mPlayers[curPlayer].rinshan = true;
									needDraw = true;
									continue;
								}
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
							
							mPlayers[curPlayer].myAI.requestOutput(AI.TSUMO);
							if(mPlayers[curPlayer].myAI.getOutput() == 1){
								//we won!
								result = curPlayer;  //or something
								selfDrawWin = true;
								
								mUI.showCall(curPlayer, Globals.CMD.TSUMO, true);
								mUI.triggerRedraw();
								sleep(1000);
								//mPlayers[curPlayer].scoreHand();
								//Add in situational yaku
								break;
							}
							else{
									
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
							}
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
							
							//Other end of the hack
							mPlayers[curPlayer].ippatsu = ippatsuSaver;
							
							if(!mPlayers[curPlayer].myHand.openHand){
								if((tilesAway == 1)&&(mPlayers[curPlayer].score >= 1000)){
									canRiichi = true;
								}
							}
							if(tilesAway == 0){
								if(mPlayers[curPlayer].myHand.scoreHand(true) > 0)
									canTsumo = true;
							}
							
							canKan = mPlayers[curPlayer].myHand.canCallSelfKan();
							
							callCmd = -1;
							if(canTsumo || canKan || (canRiichi && !mPlayers[curPlayer].riichi)){
								
								mUI.waitingForCallInput(false, false, false, false, canTsumo, canRiichi, canKan);
								mUI.triggerRedraw();
								while(callCmd == -1){
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
									//mPlayers[curPlayer].scoreHand();
									selfDrawWin = true;
									result = curPlayer;
									break;
								}
								else if(callCmd == Globals.CMD.SELFKAN){
									mPlayers[curPlayer].autoSelfKan();
									if(!mTable.addDora()){
										//abortive hand, do...something
									}
									mPlayers[curPlayer].rinshan = true;
									needDraw = true;
									continue;
									
								}
								else if(callCmd == Globals.CMD.RIICHI){
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
								while(discardIdx == -1){
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
							tilesAway = mPlayers[curPlayer].myHand.getShantenCount_TreeVersion(1, true);
						
						if(tilesAway == 1){
							mPlayers[curPlayer].myHand.inTenpai = true;
							for(int i = 0; i < mPlayers[curPlayer].myHand.tenpaiTiles.size(); i++){
								if(mTable.hasBeenDiscarded(mPlayers[curPlayer].myHand.tenpaiTiles.get(i), curPlayer, true)){
									mPlayers[curPlayer].myHand.inFuriten = true;
									break;
								}
							}
						}
						else{
							mPlayers[curPlayer].myHand.inTenpai = false;
							mPlayers[curPlayer].myHand.inFuriten = false;
						}
						
						if(mPlayers[curPlayer].AIControlled)
							mPlayers[curPlayer].myAI.onDiscard();
						else
							mPlayers[curPlayer].myHand.setupTilesToCall();
						
						//Check for Pons/Chis/rons....in the future
						
						//Add to discard Pile
						//Discards[curPlayer][discardCount[curPlayer]++] = tileToDiscard;
						mTable.discardTile(tileToDiscard, curPlayer);
						int highestCmd = 0;
						int playerCalling = -1;
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
						
						//If someone called the last tile
						if(highestCmd > 0){
							mUI.showCall(playerCalling, highestCmd, highestCmd == Globals.CMD.RON);
							mUI.triggerRedraw();
							sleep(1000);
							Tile lastDiscard = mTable.undiscardLastTile(curPlayer);
							
							if(highestCmd == Globals.CMD.RON){
								mPlayers[playerCalling].myHand.draw(lastDiscard, true);
								//mPlayers[playerCalling].scoreHand();
								//figure out situational yakus
								result = playerCalling;
								break;
							}
							//else if(mPlayers[playerCalling].AIControlled){
							//	mPlayers[playerCalling].myAI.AIMeld(lastDiscard);
							//}
							else{
								//Do stuff
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
									needDraw = true;
									mTable.addDora();
									mPlayers[playerCalling].rinshan = true;
								}
							}
							//mTable.undiscardLastTile(curPlayer);
							mUI.triggerRedraw();
							curPlayer = playerCalling;
							//needDraw = false;
							continue;
						}
						
						mUI.triggerRedraw();
						curPlayer = (curPlayer+1)%4;
						needDraw = true;
						//sleep(2000); //delete this, it's just so I can see what's happening
					}
					
					if(result != -1){
						//then someone won the hand
						//Globals.myAssert(false);
						
						//mPlayers[result].scoreHand();
						//int[] pYaku = mPlayers[result].yaku;
						int[] losers = new int[] {-1, -1, -1};
						boolean isEast = mPlayers[result].currentWind == Globals.Winds.EAST;
						if(mPlayers[result].riichi)
							mTable.addUraDora();
						int points = mPlayers[result].myHand.scoreHand(selfDrawWin);
						//mPlayers[result].score += points;
						if(selfDrawWin){
							if(isEast){
								int pointsPer = points/3;
								mPlayers[result].offsetScore(points);
								int idx = 0;
								for(int i = (result+1)%4; i != result; i = (i+1)%4){
									losers[idx++] = i;
									mPlayers[i].offsetScore(-pointsPer);
								}
							}
							else{
								int pointsPer = points/4;
								mPlayers[result].offsetScore(points);
								int idx = 0;
								for(int i = (result+1)%4; i != result; i = (i+1)%4){
									losers[idx++] = i;
									if(mPlayers[i].currentWind == Globals.Winds.EAST)
										mPlayers[i].offsetScore(-(2*pointsPer));
									else
										mPlayers[i].offsetScore(-pointsPer);
								}
							}
						}
						else{
							losers[0] = curPlayer;
							mPlayers[curPlayer].offsetScore(-points);
							mPlayers[result].offsetScore(points);
						}
						
						mPlayers[result].offsetScore(mTable.getPointsOnTable());
						mTable.clearPointsOnTable();
						
						mUI.showScoreScreen(result, losers[0], losers[1], losers[2], points);
						while(!mRunning){
							sleep(250);
						}
						/*int[] pYaku = mPlayers[result].yaku;
						mUI.getThread().drawScoreScreen(result, curPlayer, points, mPlayers[result].yaku, mPlayers[result].dora, mPlayers[result].fu);
						mUI.waitingForClick();
						while(callCmd == -1){
							//mUI.waitingForCallInput(mPlayers[playerToCheck].myAI.canCallPon(tileToDiscard), mPlayers[playerToCheck].myAI.canCallChi(tileToDiscard), mPlayers[playerToCheck].myAI.canCallKan(tileToDiscard), mPlayers[playerToCheck].myAI.canCallRon(tileToDiscard), false);
							//I don't know a good way to do this without polling
							sleep(500);
						}*/
						
					}
					else{
						//It's a draw
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
					}
					
					endRound(!(result == curEast));
					
				}
			
			}
			catch(Exception e){
				String WTFAmI = e.toString();
				Log.e("MainGameThread.run", WTFAmI);
			}
			//mRunning = false;
		}
	}

	public void sendDiscardMessage(int Idx){
		discardIdx = Idx;
	}
	
	public void sendCallMessage(int cmd){
		callCmd = cmd;
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
				meldCounts[mPlayers[player].myHand.melds[i][1]]++;
				meldCounts[mPlayers[player].myHand.melds[i][2]]++;
				meldCounts[mPlayers[player].myHand.melds[i][3]]++;
				if(mPlayers[player].myHand.melds[i][0] == 4)
					meldCounts[mPlayers[player].myHand.melds[i][4]]++;
			}
		}
		return meldCounts;
	}
	
	void suspendThread(){
		mRunning = false;
	}
	
	void resumeThread(){
		mRunning = true;
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
}
