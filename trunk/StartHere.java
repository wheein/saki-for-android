package mahjong.riichi;

import java.util.ArrayList;

import mahjong.riichi.SakiView.SakiThread;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

public class StartHere extends Activity/* implements View.OnTouchListener*/  {
	
	 /** A handle to the thread that's actually running the animation. */
    private SakiThread mSakiThread;
    private MainGameThread mGameThread;

    /** A handle to the View in which the game is running. */
    //Made it a global so that I can call invalidate from the gameEngine
    private SakiView mSakiView;
    
    
    /**
     * The various views/activities/screens/whatever you want to call them
     * 
     * I'm not in love with how this is implemented but whatever
     */
    //private ScoreScreen mScoreScreen;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Globals.GameEngine.deal();
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        mSakiView = (SakiView)findViewById(R.id.SakiView);
        //mSakiThread = mSakiView.getThread();
        //mSakiThread.doStart();
        //Globals.GameEngine.setView(mSakiView);
        try{
	        mGameThread = new MainGameThread();
	        
	        //Give the threads references to each other
	        //Maybe I could get around this using ThreadGroups
	        mGameThread.setUI(mSakiView);
	        mSakiView.setGameThread(mGameThread);
	        mSakiView.setActivity(this);
	        mSakiView.setLangauge(true);
	        mSakiView.setDebug(true);
        }
        catch(Exception e){
        	String WhatAmI = e.toString();
        	WhatAmI.length();
        }
        //mGameThread.start();
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }
    
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(!item.isChecked());
        if(item.getItemId() == R.id.langauge)
        	mSakiView.setLangauge(item.isChecked());
        else if(item.getItemId() == R.id.debug)
        	mSakiView.setDebug(item.isChecked());
        return true;
    }  
    
    public boolean onPrepareOptionsMenu(Menu menu){
    	boolean result = super.onPrepareOptionsMenu(menu);
    	if(menu.getItem(0).isChecked())
    		menu.getItem(0).setTitleCondensed("English");
    	else
        	menu.getItem(0).setTitleCondensed("Japanese");
    	
    	if(menu.getItem(1).isChecked())
    		menu.getItem(1).setTitleCondensed("Debug Off");
    	else
        	menu.getItem(1).setTitleCondensed("Debug On");
    	return result;

    }
    
    
    /*public boolean launchScoreScreen(int winnerCharID, int loserCharID1, int loserCharID2, int loserCharID3, int points, int fu, int han, int dora, int[] yaku){
    	try{
    		mGameThread.suspendThread();
    		Intent i = new Intent(this, ScoreScreen.class);
    		Bundle scoreScreenInfo = new Bundle();
    		scoreScreenInfo.putInt("winnerCharID", winnerCharID);
    		scoreScreenInfo.putInt("loserCharID1", loserCharID1);
    		scoreScreenInfo.putInt("loserCharID2", loserCharID2);
    		scoreScreenInfo.putInt("loserCharID3", loserCharID3);
    		scoreScreenInfo.putInt("points", points);
    		scoreScreenInfo.putInt("fu", fu);
    		scoreScreenInfo.putInt("han", han);
    		scoreScreenInfo.putInt("dora", dora);
    		scoreScreenInfo.putIntArray("yaku", yaku);
    		i.putExtras(scoreScreenInfo);
    		startActivityForResult(i, Globals.SCORESCREEN);
    	}
    	catch(Exception e){
    		mGameThread.resumeThread();
    		String WTFAmI = e.toString();
    		WTFAmI.length();
    		return false;
    	}
    	return true;
    }*/
    /*
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	// Here We identify the subActivity we started 
    	if(requestCode == Globals.SCORESCREEN){
    		mGameThread.resumeThread();
    	}
    }
    */
    public void onDestroy(){
    	mSakiView.finish();
    }
    
    //public void onResume(){
    	//Globals.GameEngine.mainLoop();
   // }
	
}