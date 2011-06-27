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
        		dumpTextHere.append("Is this a port of the PSP game?" + "\n");
        		dumpTextHere.append("Sort of, not really.  I started making a generic riichi game, but soon after decided it should be based around Saki.  As soon as I started looking for images for the game I found the PSP game and bought it.  I used it for inspiration in a lot of areas (some of the powers, the game/table layout, etc) but it was never meant to be a direct port." + "\n" + "\n");
        		dumpTextHere.append("Why doesn't X have a special power?! X's power is terrible! Etc" + "\n");
        		dumpTextHere.append("Some of them were rather straight forward to do (ex Saki's Rinshan Kaihou), but I was at a loss for some of them and I'll admit a lot of the percentages were decided at complete random." + "\n" + "\n");
        		dumpTextHere.append("Some specific examples:" + "\n");
        		dumpTextHere.append("Nodoka - Her only real power in the series is that she negates other players powers.  I wasn't sure how to do that in a satisfying way.  So if there's a Nodoka in the game no one can use powers?  Thats not fun.  And if you are playing against a bunch of non-super powered people then her power is useless." + "\n" + "\n");
        		dumpTextHere.append("Koromo - It's really hard to initially set up a Haitei!  The only time the power even has a chance to get used is with an exhaustive draw and even then she has to be in tenpai, in line for the last tile, and the tile has to be in the wall.  It works, it's just really hard to use.  In my experience the PSP game doesn't do a much better job." + "\n" + "\n");
        		dumpTextHere.append("Kaori/Kana/Yuuki - The 'good initial draw' power is still a crap shoot.  We give you increased odds of drawing honor tiles and one random suit, but it's still subject to the whims of the random number generator." + "\n" + "\n");
        		dumpTextHere.append("I'd be willing to listen to suggestions for adding/improving the powers." + "\n" + "\n");
        		dumpTextHere.append("Why do all the powers seem to require me to get into tenpai first?" + "\n");
        		dumpTextHere.append("Short answer: I have no way of knowing what tiles you are looking for before then.  Sure I could guess earlier in the hand but I ran the risk of guessing wrong and I think that would be even more annoying (I think you are going for a pinfu but you were looking for a honitsu so I keep feeding you the wrong suit).  I also didn't want to let the powers get out of hand so I wanted to keep some randomness/fairness in the game even when you are using your powers." + "\n" +"\n");
        		dumpTextHere.append("Can I help?" + "\n");
        		dumpTextHere.append("Sure!  I suck at/hate working on graphics.  If you think you could improve the look of the game anywhere (first and foremost would be how the mahjong table looks).  Also these help pages are obviously in need of some TLC.  If you wanted to come up with an html version that would be great.  Also the Bios on the character select screen could use a rewriting in both japanese and english." +"\n");
        		dumpTextHere.append("Please contact me before doing anything though so I can give you and guidelines/requirements and so that you don;t waste your time doing something that I can't use." +"\n");
        	}
        	else if(type.equalsIgnoreCase("Powers")){
        		dumpTextHere.append("Explanation of the Special Powers" + "\n" + "\n");
        		dumpTextHere.append("Rinshan Kaihou" + "\n");
        		dumpTextHere.append("Characters: Saki" + "\n");
        		dumpTextHere.append("Power will activate when you are in tenpai, have a pon in your hand, and one of the following is true:" + "\n");
        		dumpTextHere.append("1) Both your kan and tsumo tiles are still in the wall" + "\n");
        		dumpTextHere.append("or 2) Your tsumo tile is still in the wall and you call kan off of someone's discard" + "\n");
        		dumpTextHere.append("When activated you are guaranteed to draw your kan or tsumo tile on your next turn." + "\n" + "\n");
        		dumpTextHere.append("Taco Power" + "\n");
        		dumpTextHere.append("Characters: Yuuki" + "\n");
        		dumpTextHere.append("Increases your luck in the East round." + "\n");
        		dumpTextHere.append("33% chance of getting a lucky initial draw" + "\n");
        		dumpTextHere.append("33% chance of an instant tsumo." + "\n" + "\n");
        		dumpTextHere.append("Haitei Raoyue" + "\n");
        		dumpTextHere.append("Characters: Koromo" + "\n");
        		dumpTextHere.append("If you are in tenpai and have the last draw from the wall you are guaranteed to draw your winning tile as long as it's still in the wall." + "\n" + "\n");
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
        		dumpTextHere.append("When under 15,000 points you will receive favorable deals and 100% odds at your tsumo tile" + "\n" + "\n");
        		dumpTextHere.append("Beginner's Luck" + "\n");
        		dumpTextHere.append("Characters: Kaori" + "\n");
        		dumpTextHere.append("Character has 3 modes:" + "\n");
        		dumpTextHere.append("1) Normal Mode - 50% odds - Behaves like any other player" + "\n");
        		dumpTextHere.append("2) Beginner Mode - 40% odds - Discards and calls tiles at random" + "\n");
        		dumpTextHere.append("3) Lucky Mode - 10% - Increased odds of receiving a good initial deal and 100% chance of drawing tsumo tile as soon as you get to tenpai" + "\n" + "\n");
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

