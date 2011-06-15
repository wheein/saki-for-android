package mahjong.riichi;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

/**
 * 
 * ChangeLogScreen is now going to be "generic text" screen
 * We will reuse it for the FAQ
 *
 */

public class ChangeLogScreen extends Activity {
	public ChangeLogScreen() {
		// TODO Auto-generated constructor stub
	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try{
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        	setContentView(R.layout.change_log);
        	
        	String type = getIntent().getStringExtra("Type");
        	
        	TextView dumpTextHere = (TextView) findViewById(R.id.changeLogTextView);
        	char[] blank = new char[] {0};
        	dumpTextHere.setText(blank, 0, 0);
        	
        	/**
        	 * I think for the FAQs and Power descriptions I will just hard code it here
        	 * I don't expect it to change very often (unlike the change log) and it requires a little bit
        	 * more formatting
        	 */
        	if(type.equalsIgnoreCase("FAQ")){
        		
        	}
        	else if(type.equalsIgnoreCase("Powers")){
        		dumpTextHere.append("Explanation of the Special Powers" + "\n" + "\n");
        		dumpTextHere.append("Rinshan Kaihou" + "\n");
        		dumpTextHere.append("Characters: Saki" + "\n");
        		dumpTextHere.append("Power will activate when you are in tenpai, have a pon in your hand, and one of the following is true:" + "\n");
        		dumpTextHere.append("1) Both your kan and tsumo tiles are still in the wall" + "\n");
        		dumpTextHere.append("or 2) Your tsumo tile is still in the wall and you call kan off of someon's discard" + "\n");
        		dumpTextHere.append("When activated you are gaurenteed to draw your kan or tsumo tile on your next turn." + "\n" + "\n");
        		dumpTextHere.append("Taco Power" + "\n");
        		dumpTextHere.append("Characters: Yuuki" + "\n");
        		dumpTextHere.append("Increases your luck in the East round." + "\n");
        		dumpTextHere.append("33% chance of getting a lucky initial draw" + "\n");
        		dumpTextHere.append("33% chance of an instant tsumo." + "\n" + "\n");
        		dumpTextHere.append("Haitei Raoyue" + "\n");
        		dumpTextHere.append("Characters: Koromo" + "\n");
        		dumpTextHere.append("If you are in tenpai and have the last draw from the wall you are gaurenteed to draw your winning tile as long as it's still in the wall." + "\n" + "\n");
        		dumpTextHere.append("See All" + "\n");
        		dumpTextHere.append("Characters: Mako, Mihoko" + "\n");
        		dumpTextHere.append("Will reveal all of your opponents' hands.  25% chance of activating." + "\n" + "\n");
        		dumpTextHere.append("Hell Wait" + "\n");
        		dumpTextHere.append("Characters: Hisa" + "\n");
        		dumpTextHere.append("Increases your odds of drawing your winning tile depending on your wait" + "\n");
        		dumpTextHere.append("Any one-sided wait: 2X odds" + "\n");
        		dumpTextHere.append("One-sided wait where there is only 1 of your winning tile left in the wall: 3X odds" + "\n");
        		dumpTextHere.append("Hell Wait (1 tile left in wall, 3 already discarded): 100%" + "\n" + "\n");
        		dumpTextHere.append("Kitty Roar" + "\n");
        		dumpTextHere.append("Characters: Kana" + "\n");
        		dumpTextHere.append("When under 15,000 points you will recieve favorable deals and 100% odds at your tsumo tile" + "\n" + "\n");
        		dumpTextHere.append("Beginner's Luck" + "\n");
        		dumpTextHere.append("Characters: Kaori" + "\n");
        		dumpTextHere.append("Character has 3 modes:" + "\n");
        		dumpTextHere.append("1) Normal Mode - 50% odds - Behaves like any other player" + "\n");
        		dumpTextHere.append("2) Beginner Mode - 40% odds - Discards and calls tiles at random" + "\n");
        		dumpTextHere.append("3) Lucky Mode - 10% - Increased odds of recieving a good initial deal and 100% chance of drawing tsumo tile as soon as you get to tenpai" + "\n" + "\n");
        		dumpTextHere.append("Invisibility" + "\n");
        		dumpTextHere.append("Characters: Momoka" + "\n");
        		dumpTextHere.append("Activates after 2 straight hands where you don't declare pon, kan, chi, ron, or tsumo" + "\n");
        		dumpTextHere.append("Lasts until one of those calls is made." + "\n");
        		dumpTextHere.append("While invisible no one will be able to see or call any of your discards." + "\n" + "\n");
        	}
        	else{
	        	for(int i = 0; i < Globals.ChangeLog.COUNT; i++){
	        		dumpTextHere.append(Globals.ChangeLog.FullLog[i] + "\n");
	        	}
        	}
        }
        catch(Exception e){
        	String WhatAmI = e.toString();
        	Log.e("ChangeLogScreen", WhatAmI);
        }
        
    }
}
