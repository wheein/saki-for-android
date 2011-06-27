package mahjong.riichi;

import java.util.Random;

public class YuukiAI extends AI {
	private Random randGenerator;
	
	/**
	 * Yuuki has 2 non-exclusive possibilities
	 * 
	 * 33% - Lucky draw
	 * 33% - Guaranteed tsumo
	 *
	 */
	private boolean bDrawMode;
	private boolean bTsumoMode;
	
	public YuukiAI() {
		super();
		
		randGenerator = new Random();
		bDrawMode = false;
		bTsumoMode = false;
	}

	public YuukiAI(Integer ID) {
		super(ID);
		
		randGenerator = new Random();
		bDrawMode = false;
		bTsumoMode = false;
	}
	
	/**
	 * Overrides for the special powers
	 */
	protected void handlePowersAtStart(){
		bDrawMode = false;
		bTsumoMode = false;
		pMyPlayer.powerTiles.clear();
		
		if(pGameThread.curWind == Globals.Winds.EAST){
			if(randGenerator.nextInt(3)==0)
				bDrawMode = true;
			if(randGenerator.nextInt(3)==0)
				bTsumoMode = true;
		}
		
		if(bDrawMode){
			pMyPlayer.powerActivated[Globals.Powers.dealBased] = true;
			//Set up the tiles we want to recieve
			//I'm not sure the best way to do this, for the time being we will ask for 
			//Honors, winds, and 1 random suit
			
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
		super.handlePowersAtStart();
	}
	
	protected void handlePowersAtDraw(){
		super.handlePowersAtDraw();
	}
	
	protected void handlePowersAtDiscard(){
		pMyPlayer.powerTiles.clear(); //Do this here to make sure draw based gets turned off after the initial deal
		if(bTsumoMode && ShantanCount == 1){
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
		pMyPlayer.powerActivated[Globals.Powers.drawBased] = false;
		pMyPlayer.powerActivated[Globals.Powers.dealBased] = false;
		pMyPlayer.powerTiles.clear();
	}

}

