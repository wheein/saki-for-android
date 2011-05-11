package mahjong.riichi;

public class MomokaAI extends AI {
	private boolean canTurnInvisible;
	private int roundCounter;
	
	MomokaAI(){
		super();
	}
	
	MomokaAI(Integer ID){
		super(ID);
	}
	
	public void init(){
		super.init();
		canTurnInvisible = false;
		roundCounter = 0;
	}
	
	protected void handlePowersAtStart(){
		super.handlePowersAtStart();
		if(canTurnInvisible){
			roundCounter++;
			if(roundCounter >= 2)
				pMyPlayer.powerActivated[Globals.Powers.invisibility] = true;
		}
		else
			roundCounter = 0;
		
		canTurnInvisible = true;
	}
	
	protected void handlePowersAtDraw(){
		
	}
	
	protected void handlePowersAtDiscard(){
		
	}
	
	protected void handlePowersAtCall(){
		super.handlePowersAtCall();
		//Oh Noes!!! We can't stay invisible
		pMyPlayer.powerActivated[Globals.Powers.invisibility] = false;
		canTurnInvisible = false;
	}
	
	protected void handlePowersAtEnd(){
		
	}
	
	protected void handlePowersAtWin(){
		super.handlePowersAtWin();
		canTurnInvisible = false;
	}

	protected void handlePowersAtLose(){
	
	}
	
	protected void turnOffPowers(){
		pMyPlayer.powerActivated[Globals.Powers.invisibility] = false;
	}
}
