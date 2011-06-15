package mahjong.riichi;

import mahjong.riichi.SakiView.SakiThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
        try{
	        mGameThread = new MainGameThread();
	        mGameThread.setUI(mSakiView);
	        mSakiView.setGameThread(mGameThread);
	        mSakiView.setActivity(this);
        }
        catch(Exception e){
        	String WhatAmI = e.toString();
        	Log.e("StartHere", WhatAmI);
        }
        
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
        
        if(item.getItemId() == R.id.settings){
        	startActivityForResult(new Intent(this, Settings.class), 123);
            return true;
        }
        else if(item.getItemId() == R.id.changeLog){
        	if(item.isChecked())
        		item.setChecked(false);
        	Intent intentForTextScreen = new Intent(this, ChangeLogScreen.class);
        	intentForTextScreen.putExtra("Type", "ChangeLog");
        	startActivity(intentForTextScreen);
            return true;
        }
        else if(item.getItemId() == R.id.faq){
        	if(item.isChecked())
        		item.setChecked(false);
        	Intent intentForTextScreen = new Intent(this, ChangeLogScreen.class);
        	intentForTextScreen.putExtra("Type", "FAQ");
        	startActivity(intentForTextScreen);
            return true;
        }
        else if(item.getItemId() == R.id.powerInfo){
        	if(item.isChecked()){
        		item.setChecked(false);
        		item.setCheckable(false);
        	}
        	Intent intentForTextScreen = new Intent(this, ChangeLogScreen.class);
        	intentForTextScreen.putExtra("Type", "Powers");
        	startActivity(intentForTextScreen);
            return true;
        }
        else if(item.getItemId() == R.id.riichiInfo){
        	Intent intentForWebScreen = new Intent(this, BrowserScreen.class);
        	intentForWebScreen.putExtra("Type", "Rules");
        	startActivity(intentForWebScreen);
            return true;
        }
        else if(item.getItemId() == R.id.yakuList){
        	Intent intentForWebScreen = new Intent(this, BrowserScreen.class);
        	intentForWebScreen.putExtra("Type", "Yaku");
        	startActivity(intentForWebScreen);
            return true;
        }
        return true;
    }  
    
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == 123) {
        	SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            mSakiView.updatePreferences(myPrefs);
        }
    }
    
    @Override
    public void onBackPressed() {
    	if(!mSakiView.onBackButton()){
    		new AlertDialog.Builder(this)
            .setTitle("Quit?")
            .setMessage("Do you want to exit the application?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //Stop the activity
                	StartHere.this.finish();    
                }

            })
            .setNegativeButton("No", null)
            .show();
    	}
    	return;
    }
    
    public void onDestroy(){
    	mSakiView.finish();
    	super.onDestroy();
    	this.finish();
    	//This shouldn't technically be necessary but I'd rather be sure we are done
    	android.os.Process.killProcess(android.os.Process.myPid());
    }
	
}
