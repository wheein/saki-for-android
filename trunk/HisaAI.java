package mahjong.riichi;

public class HisaAI extends AI {
	/**
	 * Pure Hell Wait is defined as one sided wait, with 3 tiles either in our hand or discarded
	 * Impure is one-sided wait with only 1 tile left in the wall but other tiles are in people's hands
	 * One Sided is a one sided wait (duh) where there are multiple in the wall
	 */
	boolean bPureHellWait;
	boolean bImpureHellWait;
	boolean bOneSidedWait;
	
	/**
	 * Constructors
	 */
	HisaAI(){
		super();
	}
	
	HisaAI(Integer ID){
		super(ID);
	}
	
	/**
	 * Overrides for the special powers
	 */
	protected void handlePowersAtStart(){
		super.handlePowersAtStart();
	}
	
	protected void handlePowersAtDraw(){
		super.handlePowersAtDraw();
	}
	
	protected void handlePowersAtDiscard(){
		bPureHellWait = false;
		bImpureHellWait = false;
		bOneSidedWait = true;
		
		//Chances are this isn't neccessary but we have to make sure
		turnOffPowers();
		
		if(ShantanCount == 1){
			//There's a chance that tenpaiTiles > 1 but they are the same tile
			int theFirstTile = 0;
			for(int thisTile = 0; thisTile < pMyPlayer.myHand.tenpaiTiles.size(); thisTile++){
				if(thisTile == 0){
					theFirstTile = pMyPlayer.myHand.tenpaiTiles.get(thisTile);
				}
				else if(pMyPlayer.myHand.tenpaiTiles.get(thisTile) != theFirstTile){
					bOneSidedWait = false;
				}
			}
			
			if(theFirstTile <= 0)
				return;
			
			if(bOneSidedWait && pGameThread.mTable.isLeftInWall(theFirstTile)){
				//OK we verified that it's one sided and that it's in the wall, so at least the minimum
				//strength of her power will be activated
				pMyPlayer.powerTiles.add(theFirstTile);
				//pMyPlayer.powerActivated[Globals.Powers.drawBased_DoubleOdds] = true;
				
				if(!pGameThread.mTable.isMultipleInWall(theFirstTile)){
					//Only 1 left in the wall
					bImpureHellWait = true;
					
					int[] tileCounts = pMyPlayer.myHand.getRawTileCounts();
					int[] allDiscardCounts = pGameThread.mTable.getAllDiscardCounts();
					if((tileCounts[theFirstTile] + allDiscardCounts[theFirstTile]) == 3)
						bPureHellWait = true;
				}
			}
			else
				bOneSidedWait = false;
		}
		else
			bOneSidedWait = false;
		
		if(bPureHellWait){
			//By definition there should be 1 and only 1 member of powerTiles
			pGameThread.mTable.reserveTile(pMyPlayer.powerTiles.get(0));
			pMyPlayer.powerActivated[Globals.Powers.drawBased] = true;
		}
		else if(bImpureHellWait)
			pMyPlayer.powerActivated[Globals.Powers.drawBased_TripleOdds] = true;
		else if(bOneSidedWait)
			pMyPlayer.powerActivated[Globals.Powers.drawBased_DoubleOdds] = true;

		
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
		pMyPlayer.powerActivated[Globals.Powers.drawBased_DoubleOdds] = false;
		pMyPlayer.powerActivated[Globals.Powers.drawBased_TripleOdds] = false;
		
		for(int i = 0;i < pMyPlayer.powerTiles.size(); i++){
			pGameThread.mTable.unreserveTile(pMyPlayer.powerTiles.get(i));
		}
		pMyPlayer.powerTiles.clear();
	}
}
