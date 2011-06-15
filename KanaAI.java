package mahjong.riichi;

public class KanaAI extends AI {

	public KanaAI() {
		// TODO Auto-generated constructor stub
	}

	public KanaAI(Integer ID) {
		super(ID);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Overrides for the special powers
	 */
	protected void handlePowersAtStart(){
		
		if(pMyPlayer.score <= 15000){
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
		super.handlePowersAtStart();
	}
	
	protected void handlePowersAtDraw(){
		super.handlePowersAtDraw();
	}
	
	protected void handlePowersAtDiscard(){
		if(pMyPlayer.score <= 15000 && ShantanCount == 1){
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
		pMyPlayer.powerTiles.clear();
		pMyPlayer.powerActivated[Globals.Powers.drawBased] = false;
		pMyPlayer.powerActivated[Globals.Powers.dealBased] = false;
	}
}
