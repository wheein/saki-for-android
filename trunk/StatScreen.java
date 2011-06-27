package mahjong.riichi;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class StatScreen extends Activity {
	
	boolean bJapanese;
	boolean bRomanji;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try{
        	//requestWindowFeature(Window.FEATURE_NO_TITLE);
        	setContentView(R.layout.playerstats);
        	//TextView errorMsg = (TextView)findViewById(R.id.errorMsg);
        	bJapanese = getIntent().getBooleanExtra("bJapanese", false);
        	bRomanji = getIntent().getBooleanExtra("bRomanji", true);
        	//if(savedInstanceState != null)
        	//	bJapanese = savedInstanceState.getBoolean("bDebug");
        	//else
        	//	bJapanese = false;
        	
        	loadStats(false);
        	
        	final ToggleButton togglebutton = (ToggleButton) findViewById(R.id.statSwitcher);
        	togglebutton.setOnClickListener(new OnClickListener() {
        	    public void onClick(View v) {
        	        // Perform action on clicks
        	        if (togglebutton.isChecked()) {
        	        	loadStats(true);
        	        } else {
        	        	loadStats(false);
        	        }
        	    }
        	});
        }
        catch(Exception e){
        	String WhatAmI = e.toString();
        	Log.e("StatScreen", WhatAmI);
        }
        
    }
	
	private void loadStats(boolean AI){
		 try{
	        	Stats playerStats = new Stats();
	        	String errorMsg = "";
	        	if(playerStats.loadFromFile(AI, errorMsg))
	        		playerStats.calcStats();
	        	if(AI)
	        		((TextView)findViewById(R.id.Header)).setText("AI Stats");
	        	else
	        		((TextView)findViewById(R.id.Header)).setText("Player Stats");
	        	((TextView)findViewById(R.id.errorMsg)).setText(errorMsg);
	        	((TextView)findViewById(R.id.errorMsg)).setTextColor(Color.RED);
	        	((TextView)findViewById(R.id.totalGames)).setText(idToText(R.id.totalGames, bJapanese)+": " + playerStats.totalGames.toString());
	        	
	        	String doubleConverter =  "";
	        	doubleConverter= String.valueOf(playerStats.placePercentages[0]);
	        	if(doubleConverter.length() > 5)
	        		doubleConverter = doubleConverter.substring(0, 4);
	        	((TextView)findViewById(R.id.firstPlace)).setText(idToText(R.id.firstPlace, bJapanese) + ": "+ doubleConverter + "%");

	        	doubleConverter= String.valueOf(playerStats.placePercentages[1]);
	        	if(doubleConverter.length() > 5)
	        		doubleConverter = doubleConverter.substring(0, 4);
	        	((TextView)findViewById(R.id.secondPlace)).setText(idToText(R.id.secondPlace, bJapanese) + ": "+ doubleConverter + "%");
	        	
	        	doubleConverter= String.valueOf(playerStats.placePercentages[2]);
	        	if(doubleConverter.length() > 5)
	        		doubleConverter = doubleConverter.substring(0, 4);
	        	((TextView)findViewById(R.id.thirdPlace)).setText(idToText(R.id.thirdPlace, bJapanese) + ": "+ doubleConverter + "%");
	        	
	        	doubleConverter= String.valueOf(playerStats.placePercentages[3]);
	        	if(doubleConverter.length() > 5)
	        		doubleConverter = doubleConverter.substring(0, 4);
	        	((TextView)findViewById(R.id.fourthPlace)).setText(idToText(R.id.fourthPlace, bJapanese) + ": "+ doubleConverter + "%");
	        	
	        	((TextView)findViewById(R.id.avgScore)).setText(idToText(R.id.avgScore, bJapanese) + ": " + playerStats.avgScore.toString());
	        	((TextView)findViewById(R.id.spacer)).setText("");
	        	if(AI)
	        		((TextView)findViewById(R.id.totalHands)).setText(idToText(R.id.totalHands, bJapanese) + ": " + String.valueOf(playerStats.totalHands/3));
	        	else
	        		((TextView)findViewById(R.id.totalHands)).setText(idToText(R.id.totalHands, bJapanese) + ": " + playerStats.totalHands.toString());
	        	
	        	doubleConverter= String.valueOf(playerStats.winPercentage);
	        	if(doubleConverter.length() > 5)
	        		doubleConverter = doubleConverter.substring(0, 4);
	        	((TextView)findViewById(R.id.winPercentage)).setText(idToText(R.id.winPercentage, bJapanese) + ": "+ doubleConverter + "%");
	        	
	        	doubleConverter= String.valueOf(playerStats.ronPercentage);
	        	if(doubleConverter.length() > 5)
	        		doubleConverter = doubleConverter.substring(0, 4);
	        	((TextView)findViewById(R.id.ronPercentage)).setText(idToText(R.id.ronPercentage, bJapanese) + ": "+ doubleConverter + "%");
	        	
	        	doubleConverter= String.valueOf(playerStats.tsumoPercentage);
	        	if(doubleConverter.length() > 5)
	        		doubleConverter = doubleConverter.substring(0, 4);
	        	((TextView)findViewById(R.id.tsumoPercentage)).setText(idToText(R.id.tsumoPercentage, bJapanese) + ": "+ doubleConverter + "%");
	        	
	        	((TextView)findViewById(R.id.Spacer2)).setText("");
	        	((TextView)findViewById(R.id.Header2)).setText(idToText(R.id.Header2, bJapanese));
	        	
	        	int[] yakuTextViewIDs = new int[] {R.id.yaku0,R.id.yaku1,R.id.yaku2,R.id.yaku3,R.id.yaku4,R.id.yaku5,R.id.yaku6,R.id.yaku7,R.id.yaku8,R.id.yaku9,
	        									   R.id.yaku10,R.id.yaku11,R.id.yaku12,R.id.yaku13,R.id.yaku14,R.id.yaku15,R.id.yaku16,R.id.yaku17,R.id.yaku18,R.id.yaku19,
	        									   R.id.yaku20,R.id.yaku21,R.id.yaku22,R.id.yaku23,R.id.yaku24,R.id.yaku25,R.id.yaku26,R.id.yaku27,R.id.yaku28,R.id.yaku29,
	        									   R.id.yaku30,R.id.yaku31,R.id.yaku32,R.id.yaku33,R.id.yaku34,R.id.yaku35,R.id.yaku36,R.id.yaku37,R.id.yaku38};
	        	
	        	for(int thisYaku = 0; thisYaku < Globals.NONYAKUMAN; thisYaku++){
	        		doubleConverter= String.valueOf(playerStats.yakuPercentages[thisYaku]);
		        	if(doubleConverter.length() > 5)
		        		doubleConverter = doubleConverter.substring(0, 4);
		        	((TextView)findViewById(yakuTextViewIDs[thisYaku])).setText(Globals.yakuToString(thisYaku, bJapanese, bRomanji) + ": "+ doubleConverter + "%");
	        	}
	        	
	        	((TextView)findViewById(R.id.Spacer3)).setText("");
	        	((TextView)findViewById(R.id.Header3)).setText(idToText(R.id.Header3, bJapanese));
	        	
	        	for(int thisYaku = Globals.NONYAKUMAN; thisYaku < Globals.ALLYAKUCOUNT; thisYaku++){
		        	((TextView)findViewById(yakuTextViewIDs[thisYaku])).setText(Globals.yakuToString(thisYaku, bJapanese, bRomanji) + ": "+ playerStats.yakuCount[thisYaku].toString());
	        	}
	        	
	        	((TextView)findViewById(R.id.Spacer4)).setText("");
	        	((TextView)findViewById(R.id.Header4)).setText(idToText(R.id.Header4, bJapanese));
	        	
	        	int[] hanTextViewIDs = new int[] {R.id.han1,R.id.han2,R.id.han3,R.id.han4,R.id.mangan,R.id.haneman,R.id.baiman,R.id.sanbaiman,R.id.yakuman};
	        	for(int thisHan = 1; thisHan < 10; thisHan++){
	        		doubleConverter= String.valueOf(playerStats.hanPercentages[thisHan]);
		        	if(doubleConverter.length() > 5)
		        		doubleConverter = doubleConverter.substring(0, 4);
		        	if(thisHan <= 4)
		        		((TextView)findViewById(hanTextViewIDs[thisHan-1])).setText(String.valueOf(thisHan) + " " + idToText(hanTextViewIDs[thisHan-1], bJapanese) + ": "+ doubleConverter + "%");
		        	else if(thisHan == 5){
		        		((TextView)findViewById(hanTextViewIDs[thisHan-1])).setText(Globals.LimitHandToString(5, bJapanese) + " : "+ doubleConverter + "%");
		        	}
		        	else if(thisHan == 6){
		        		((TextView)findViewById(hanTextViewIDs[thisHan-1])).setText(Globals.LimitHandToString(7, bJapanese) + " : "+ doubleConverter + "%");
		        	}
		        	else if(thisHan == 7){
		        		((TextView)findViewById(hanTextViewIDs[thisHan-1])).setText(Globals.LimitHandToString(9, bJapanese) + " : "+ doubleConverter + "%");
		        	}
		        	else if(thisHan == 8){
		        		((TextView)findViewById(hanTextViewIDs[thisHan-1])).setText(Globals.LimitHandToString(11, bJapanese) + " : "+ doubleConverter + "%");
		        	}
		        	else if(thisHan == 9){
		        		((TextView)findViewById(hanTextViewIDs[thisHan-1])).setText(Globals.LimitHandToString(13, bJapanese) + " : "+ doubleConverter + "%");
		        	}
	        	}
	        	
	        	((TextView)findViewById(R.id.Spacer5)).setText("");
	        	((TextView)findViewById(R.id.Header5)).setText(idToText(R.id.Header5, bJapanese));
	        	
	        	int[] charTextViewIDs = new int[] {R.id.char0,R.id.char1,R.id.char2,R.id.char3,R.id.char4,R.id.char5,R.id.char6,R.id.char7,R.id.char8,R.id.char9,
						   						   R.id.char10,R.id.char11,R.id.char12,R.id.char13,R.id.char14,R.id.char15,R.id.char16,R.id.char17,R.id.char18,R.id.char19};
	        	for(int thisChar = 0; thisChar < Globals.Characters.COUNT; thisChar++){
	        		doubleConverter= String.valueOf(playerStats.characterPercentages[thisChar]);
		        	if(doubleConverter.length() > 5)
		        		doubleConverter = doubleConverter.substring(0, 4);
	        		((TextView)findViewById(charTextViewIDs[thisChar])).setText(Globals.Characters.getName(thisChar, bJapanese) + ": "+ doubleConverter + "%");
	        	}
	        }
	        catch(Exception e){
	        	String WhatAmI = e.toString();
	        	Log.e("StatScreen_LoadStats", WhatAmI);
	        }
	}
	
	private String idToText(int ID, boolean bJapanese){
		switch(ID){
			case R.id.totalGames:
			if(bJapanese)
				return "戦数";
			else
				return "Total Games";
			case R.id.totalHands:
			if(bJapanese)
				return "局数";
			else
				return "Total Hands";
			case R.id.firstPlace:
				if(bJapanese)
					return "1位";
				else
					return "First Place";
			case R.id.secondPlace:
				if(bJapanese)
					return "2位";
				else
					return "Second Place";
			case R.id.thirdPlace:
				if(bJapanese)
					return "3位";
				else
					return "Third Place";
			case R.id.fourthPlace:
				if(bJapanese)
					return "4位";
				else
					return "Fourth Place";
			case R.id.avgScore:
				if(bJapanese)
					return "平均点";
				else
					return "Average Score";
			case R.id.winPercentage:
				if(bJapanese)
					return "和了率";
				else
					return "Win Rate";
			case R.id.ronPercentage:
				if(bJapanese)
					return "ロン率";
				else
					return "Ron";
			case R.id.tsumoPercentage:
				if(bJapanese)
					return "ツモ率";
				else
					return "Tsumo";
			case R.id.Header2:
				if(bJapanese)
					return "和了役";
				else
					return "Yaku Used";
			case R.id.Header3:
				if(bJapanese)
					return "役満";
				else
					return "Yakuman";
			case R.id.Header4:
				if(bJapanese)
					return "和了翻数";
				else
					return "Hand Value";
			case R.id.han1:
			case R.id.han2:
			case R.id.han3:
			case R.id.han4:
				if(bJapanese)
					return "翻";
				else
					return "Han";
			case R.id.Header5:
				if(bJapanese)
					return "キャラクタ";
				else
					return "Character Used";
			default:
				return "";
		}
	}
}

