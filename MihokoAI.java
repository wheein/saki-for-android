package mahjong.riichi;

import java.util.Random;


public class MihokoAI extends AI {
	private Random randGenerator;
	
	/**
	 * Constructors
	 */
	MihokoAI(){
		super();
		randGenerator = new Random();
	}
	
	MihokoAI(Integer ID){
		super(ID);
		randGenerator = new Random();
	}
	
	/**
	 * Overrides for the special powers
	 */
	protected void handlePowersAtStart(){
		super.handlePowersAtStart();
		if(randGenerator.nextInt(4) == 1){
			pMyPlayer.powerActivated[Globals.Powers.pureVision] = true;
		}
	}
	
	protected void handlePowersAtDraw(){
		super.handlePowersAtDraw();
	}
	
	protected void handlePowersAtDiscard(){
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
		pMyPlayer.powerActivated[Globals.Powers.pureVision] = false;
	}
}
