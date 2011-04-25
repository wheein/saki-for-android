package mahjong.riichi;
import java.util.ArrayList;

import mahjong.riichi.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * 
 * Please refer to UI Prototype.png to see what we are trying to do here
 * 
 * We are going under the assumption that our screen size is at least 480x294
 * (because that was the default size of the emulator).  If it's bigger than that
 * we will letterbox.  If it's smaller....well we have a problem.  We will do our 
 * best to try and scale things down, but we will have to have some lower limit (TBD)
 * 
 * From now on absolutely EVERYTHING must be done will relative measurements (ie
 * no absolute cordinates/sizes)
 * 
 * Also the multi-activity thing doesn't seems like a great idea.  So all drawing will be 
 * moved here from now on (score screen will be on its own for now since it works)
 *
 */
public class SakiView extends SurfaceView implements SurfaceHolder.Callback {
	
	/**
	 * SakiThread is actually fairly useless.  It is a remnant of the JetBoy 
	 * sample project that I used as a jumping off point.  In theory this thread
	 * should always be running and redrawing the screen at a given framerate; but
	 * that is waste of cpu cycles for this game, so the thread doesn't run and we
	 * trigger redraws manually.  It's left in here just in case I need this feature for
	 * a future project.
	 *
	 */
	class SakiThread extends Thread{

		/**
         * Screen size information
         */

        private int mCanvasHeight = 1;
		private int mCanvasWidth = 1;
		private int centerX = 0;
        private int centerY = 0;
        
        /**
         * Other graphic size info
         */
        private int tileWidth = 0;
        private int tileHeight = 0;
        private int miniTileWidth = 0;
        private int miniTileHeight = 0;
        private int portraitWidth = 0;
        private int portraitHeight = 0;
        
        
		/** Handle to the surface manager object we interact with */
        private SurfaceHolder mSurfaceHolder;
        
        /** Message handler used by thread to interact with TextView */
        private Handler mHandler;
        
        /** Handle to the application context, used to e.g. fetch Drawables. */
        private Context mContext;
        
        /**
         * Bitmap Stuff.  The idea is to load every bitmap we need once at the start.
         * Consider moving this to Globals.
         */
        Bitmap[] TileBMPs;
        Bitmap[] TileBMPs50PercentUser;
        Bitmap[] TileBMPsUser;
        Bitmap[] TileBMPsUserHorizontal;
        Bitmap[] TileBMPsSouthRotated;
        Bitmap[] TileBMPsWestRotated;
        Bitmap[] TileBMPsNorthRotated;
        Bitmap[] characterPortraits;
        Bitmap[] miscBitmaps;
        private Bitmap mBackgroundImage;
        
        /**
         * Button info
         */
        Bitmap[] Buttons;
        public int PON_BTN = 0;
        public int CHI_BTN = 1;
        public int KAN_BTN = 2;
        public int RON_BTN = 3;
        public int PASS_BTN = 4;
        public int TSUMO_BTN = 5;
        public int RIICHI_BTN = 6;
        public int SELFKAN_BTN = 7;
        public boolean[] needButtons;
        
        /**
         * Mock dialogs
         */
        boolean bNeedChiList;
        ArrayList<Set> chiList;
        
        /**
         * Various flags 
         */
        private int callToShow;
        private int playerCalling;
        private boolean[] showHand;
        private boolean bTitleScreen;
        public boolean bJapanese;
        public boolean bDebug;
        private boolean bScoreScreen;
        
        /**
         * Selection stuff
         */
        int cursorX;
        int cursorY;
        int tileSelected;
        
        /**
         * Score Screen Info
         */
        public int winner;
        public int loser1;
        public int loser2;
        public int loser3;
        public int points;
        
        /**
         * These would be useful if we needed this thread to actually run
         */
        //private boolean mRunning;
        //private boolean needRedraw;
        
        /**
         * Pointers to other classes/threads
         */
        private MainGameThread pGameThread;
	
        public SakiThread(SurfaceHolder surfaceHolder, Context context,
	                Handler handler) {
        		super("UI");
	            // get handles to some important objects
	            mSurfaceHolder = surfaceHolder;
	            mHandler = handler;
	            mContext = context;
	            
	            //mRunning = true;
	            //needRedraw = true;

	            //Init stuff
	            callToShow = Globals.CMD.PASS;
	            playerCalling = 0;
	            showHand = new boolean[] {false, false, false, false};
	            bTitleScreen = true;

	            Resources res = context.getResources();
	            TileBMPs = new Bitmap[39];
	            TileBMPs50PercentUser = new Bitmap[39];
	            TileBMPsUser = new Bitmap[39];
	            TileBMPsUserHorizontal = new Bitmap[39];
	            TileBMPsSouthRotated = new Bitmap[39];
	            TileBMPsWestRotated = new Bitmap[39];
	            TileBMPsNorthRotated = new Bitmap[39];
	            characterPortraits = new Bitmap[Globals.Characters.COUNT];
	            miscBitmaps = new Bitmap[Globals.Graphics.MISC_COUNT];
	            
	            //Initialize Bitmaps
	            TileBMPs[0] = BitmapFactory.decodeResource(res, R.drawable.blank);
	            TileBMPs[1] = BitmapFactory.decodeResource(res, R.drawable.bam1);
	            TileBMPs[2] = BitmapFactory.decodeResource(res, R.drawable.bam2);
	            TileBMPs[3] = BitmapFactory.decodeResource(res, R.drawable.bam3);
	            TileBMPs[4] = BitmapFactory.decodeResource(res, R.drawable.bam4);
	            TileBMPs[5] = BitmapFactory.decodeResource(res, R.drawable.bam5);
	            TileBMPs[6] = BitmapFactory.decodeResource(res, R.drawable.bam6);
	            TileBMPs[7] = BitmapFactory.decodeResource(res, R.drawable.bam7);
	            TileBMPs[8] = BitmapFactory.decodeResource(res, R.drawable.bam8);
	            TileBMPs[9] = BitmapFactory.decodeResource(res, R.drawable.bam9);
	            
	            TileBMPs[10] = BitmapFactory.decodeResource(res, R.drawable.pin1);
	            TileBMPs[11] = BitmapFactory.decodeResource(res, R.drawable.pin2);
	            TileBMPs[12] = BitmapFactory.decodeResource(res, R.drawable.pin3);
	            TileBMPs[13] = BitmapFactory.decodeResource(res, R.drawable.pin4);
	            TileBMPs[14] = BitmapFactory.decodeResource(res, R.drawable.pin5);
	            TileBMPs[15] = BitmapFactory.decodeResource(res, R.drawable.pin6);
	            TileBMPs[16] = BitmapFactory.decodeResource(res, R.drawable.pin7);
	            TileBMPs[17] = BitmapFactory.decodeResource(res, R.drawable.pin8);
	            TileBMPs[18] = BitmapFactory.decodeResource(res, R.drawable.pin9);
	            
	            TileBMPs[19] = BitmapFactory.decodeResource(res, R.drawable.man1);
	            TileBMPs[20] = BitmapFactory.decodeResource(res, R.drawable.man2);
	            TileBMPs[21] = BitmapFactory.decodeResource(res, R.drawable.man3);
	            TileBMPs[22] = BitmapFactory.decodeResource(res, R.drawable.man4);
	            TileBMPs[23] = BitmapFactory.decodeResource(res, R.drawable.man5);
	            TileBMPs[24] = BitmapFactory.decodeResource(res, R.drawable.man6);
	            TileBMPs[25] = BitmapFactory.decodeResource(res, R.drawable.man7);
	            TileBMPs[26] = BitmapFactory.decodeResource(res, R.drawable.man8);
	            TileBMPs[27] = BitmapFactory.decodeResource(res, R.drawable.man9);
	            
	            TileBMPs[28] = BitmapFactory.decodeResource(res, R.drawable.red);
	            TileBMPs[29] = BitmapFactory.decodeResource(res, R.drawable.white);
	            TileBMPs[30] = BitmapFactory.decodeResource(res, R.drawable.green);
	            
	            TileBMPs[31] = BitmapFactory.decodeResource(res, R.drawable.east);
	            TileBMPs[32] = BitmapFactory.decodeResource(res, R.drawable.south);
	            TileBMPs[33] = BitmapFactory.decodeResource(res, R.drawable.west);
	            TileBMPs[34] = BitmapFactory.decodeResource(res, R.drawable.north);
	            
	            TileBMPs[35] = BitmapFactory.decodeResource(res, R.drawable.bam5red);
	            TileBMPs[36] = BitmapFactory.decodeResource(res, R.drawable.pin5red);
	            TileBMPs[37] = BitmapFactory.decodeResource(res, R.drawable.man5red);
	            
	            Matrix RotateAndScale = new Matrix();
	            tileWidth = TileBMPs[1].getWidth();
	            tileHeight = TileBMPs[1].getHeight();
	            
	        	for(int i = 0; i < 38; i++){
	        		RotateAndScale.reset();
	        		RotateAndScale.setScale(0.5f, 0.5f);
		        	//RotateAndScale.preRotate(90.0f);
	        		TileBMPs50PercentUser[i] = Bitmap.createBitmap(TileBMPs[i], 0, 0, tileWidth, tileHeight, RotateAndScale, false);
	        	
	        		RotateAndScale.reset();
		        	//RotateAndScale.setRotate(90.0f);
	        		TileBMPsUser[i] = Bitmap.createBitmap(TileBMPs[i], 0, 0, tileWidth, tileHeight, RotateAndScale, false);
	        		
	        		RotateAndScale.reset();
		        	RotateAndScale.preRotate(90.0f);
		        	TileBMPsUserHorizontal[i] = Bitmap.createBitmap(TileBMPs[i], 0, 0, tileWidth, tileHeight, RotateAndScale, false);
		        	
	        		RotateAndScale.reset();
	        		RotateAndScale.setScale(0.5f, 0.5f);
		        	RotateAndScale.preRotate(270.0f);
		        	TileBMPsSouthRotated[i] = Bitmap.createBitmap(TileBMPs[i], 0, 0, tileWidth, tileHeight, RotateAndScale, false);
	        		
	        		RotateAndScale.reset();
	        		RotateAndScale.setScale(0.5f, 0.5f);
		        	RotateAndScale.preRotate(180.0f);
		        	TileBMPsWestRotated[i] = Bitmap.createBitmap(TileBMPs[i], 0, 0, tileWidth, tileHeight, RotateAndScale, false);
	        		
	        		RotateAndScale.reset();
	        		RotateAndScale.setScale(0.5f, 0.5f);
		        	RotateAndScale.preRotate(90.0f);
		        	TileBMPsNorthRotated[i] = Bitmap.createBitmap(TileBMPs[i], 0, 0, tileWidth, tileHeight, RotateAndScale, false);
	        		
	        	}
	            
	            mBackgroundImage = BitmapFactory.decodeResource(res, R.drawable.background);
        
	            Buttons = new Bitmap[8];
	            Buttons[PON_BTN] =  BitmapFactory.decodeResource(res, R.drawable.pon);
	            Buttons[CHI_BTN] =  BitmapFactory.decodeResource(res, R.drawable.chi);
	            Buttons[KAN_BTN] =  BitmapFactory.decodeResource(res, R.drawable.kan);
	            Buttons[RON_BTN] =  BitmapFactory.decodeResource(res, R.drawable.ron);
	            Buttons[PASS_BTN] =  BitmapFactory.decodeResource(res, R.drawable.pass);
	            Buttons[TSUMO_BTN] =  BitmapFactory.decodeResource(res, R.drawable.tsumo);
	            Buttons[RIICHI_BTN] =  BitmapFactory.decodeResource(res, R.drawable.riichi);
	            Buttons[SELFKAN_BTN] =  BitmapFactory.decodeResource(res, R.drawable.kan);
	            needButtons = new boolean[] {false, false, false, false, false, false, false, false};
	            
	            //Initialize Bitmaps
	            characterPortraits[Globals.Characters.GENERIC] = BitmapFactory.decodeResource(res, R.drawable.portraitplaceholder);
	            characterPortraits[Globals.Characters.KANA] = BitmapFactory.decodeResource(res, R.drawable.kana);
	            characterPortraits[Globals.Characters.KOROMO] = BitmapFactory.decodeResource(res, R.drawable.koromo);
	            characterPortraits[Globals.Characters.SAKI] = BitmapFactory.decodeResource(res, R.drawable.saki);
	            
	          //Default Values
	            miniTileWidth = TileBMPsWestRotated[1].getWidth();
	            miniTileHeight = TileBMPsWestRotated[1].getHeight();
	            portraitWidth = characterPortraits[Globals.Characters.GENERIC].getWidth();
	            portraitHeight = characterPortraits[Globals.Characters.GENERIC].getHeight();
	            
	            //Misc
	            miscBitmaps[Globals.Graphics.MISC_BUBBLE_LEFT] = BitmapFactory.decodeResource(res, R.drawable.bubble);
	            miscBitmaps[Globals.Graphics.MISC_BUBBLE_RIGHT] = BitmapFactory.decodeResource(res, R.drawable.bubbleright);
	            miscBitmaps[Globals.Graphics.MISC_LOGO] = BitmapFactory.decodeResource(res, R.drawable.logo);
	            miscBitmaps[Globals.Graphics.MISC_START_BTN] = BitmapFactory.decodeResource(res, R.drawable.start);
	            miscBitmaps[Globals.Graphics.MISC_OK_BTN] = BitmapFactory.decodeResource(res, R.drawable.ok);
	            miscBitmaps[Globals.Graphics.MISC_CENTER] = BitmapFactory.decodeResource(res, R.drawable.center);
	            miscBitmaps[Globals.Graphics.MISC_CENTER] = Bitmap.createScaledBitmap(miscBitmaps[Globals.Graphics.MISC_CENTER], (miniTileWidth*6), (miniTileWidth*6), true);
	            
	            cursorX = 0;
	            cursorY = 0;
	            tileSelected = -1;
	            
	            bDebug = true;
	            bJapanese = true;
	            bScoreScreen = false;
	            
	            

        }
        
        public void doStart() {
            synchronized (mSurfaceHolder) {
            	//start();
            	//mRunning = true;
            }
        }
        
        public void triggerRedraw() {
	                Canvas c = null;
	                try {
	                    c = mSurfaceHolder.lockCanvas(null);
	                    synchronized (mSurfaceHolder) {
	                        if(c != null){
	                        	doDraw(c);
	                        }
	                    }
	                }
	                catch(Exception e){
	                	String WTFAmI = e.toString();
	        			Log.e("SakiView.triggerRedraw", WTFAmI);
	                }
	                finally {
	                    // do this in a finally so that if an exception is thrown
	                    // during the above, we don't leave the Surface in an
	                    // inconsistent state
	                    if (c != null) {
	                        mSurfaceHolder.unlockCanvasAndPost(c);
	                    }
	                }
	                //needRedraw = false;
        }
        
        private void doDraw(Canvas canvas) {
        	try{
        		if(bTitleScreen){
        			canvas.drawColor(Color.WHITE);
        			//canvas.drawBitmap(mBackgroundImage, 0, 0, null);
        			int logoWidth = miscBitmaps[Globals.Graphics.MISC_LOGO].getWidth();
        			int logoHeight = miscBitmaps[Globals.Graphics.MISC_LOGO].getHeight();
        			canvas.drawBitmap(miscBitmaps[Globals.Graphics.MISC_LOGO], centerX - (logoWidth/2), centerY/2, null);
        			
        			Paint textBrush = new Paint();
		        	textBrush.setColor(Color.BLACK);
		        	textBrush.setTextSize(16.0f);
		        	canvas.drawText(Globals.VERSION, centerX/2, (centerY/2)+logoHeight+5, textBrush);
		        	
		        	int btnWidth = miscBitmaps[Globals.Graphics.MISC_START_BTN].getWidth();
		        	canvas.drawBitmap(miscBitmaps[Globals.Graphics.MISC_START_BTN], centerX - (btnWidth/2), (centerY/2)+logoHeight+25, null);
        		}
        		else{
        			if(bScoreScreen)
        				canvas.drawColor(Color.WHITE);
        			else
        				canvas.drawBitmap(mBackgroundImage, 0, 0, null);
		        	
		        	//Portraits First
		        	int X = 0;
		        	int Y = ((mCanvasHeight - tileHeight)/2)-portraitHeight;
		        	canvas.drawBitmap(characterPortraits[pGameThread.mPlayers[2].characterID], X, Y,  null);
		        	canvas.drawBitmap(characterPortraits[pGameThread.mPlayers[3].characterID], X, Y+portraitHeight,  null);
		        	
		        	X = mCanvasWidth - portraitWidth;
		        	Y = ((mCanvasHeight - tileHeight)/2)-portraitHeight;
		        	canvas.drawBitmap(characterPortraits[pGameThread.mPlayers[1].characterID], X, Y,  null);
		        	canvas.drawBitmap(characterPortraits[pGameThread.mPlayers[0].characterID], X, Y+portraitHeight,  null);
		        	
		        	//Scores
		        	Paint textBrush = new Paint();
		        	textBrush.setColor(Color.BLACK);
		        	textBrush.setTextSize(16.0f);
		        	
		        	Typeface myBoldText = Typeface.defaultFromStyle(Typeface.BOLD);
		        	Paint boldTextBrush = new Paint();
		        	boldTextBrush.setColor(Color.BLACK);
		        	boldTextBrush.setTextSize(16.0f);
		        	boldTextBrush.setTypeface(myBoldText);
		        	
		        	X = portraitWidth/2;
		        	Y = ((mCanvasHeight - tileHeight)/2)-portraitHeight+16;
		        	Integer temp = pGameThread.mPlayers[2].score;
		        	canvas.drawText(temp.toString(), X, Y, boldTextBrush);
		        	temp = pGameThread.mPlayers[3].score;
		        	canvas.drawText(temp.toString(), X, Y+portraitHeight, boldTextBrush);
		        	
		        	X = (mCanvasWidth - portraitWidth) + (portraitWidth/2);
		        	Y = ((mCanvasHeight - tileHeight)/2)-portraitHeight+16;
		        	temp = pGameThread.mPlayers[1].score;
		        	canvas.drawText(temp.toString(), X, Y, boldTextBrush);
		        	temp = pGameThread.mPlayers[0].score;
		        	canvas.drawText(temp.toString(), X, Y+portraitHeight, boldTextBrush);
		        	
		        	if(bScoreScreen){
		        		X = 1;
			        	Y = ((mCanvasHeight - tileHeight)/2)-portraitHeight+13;
			        	if(winner == 2)
			        		canvas.drawText("Winner", X, Y, textBrush);
			        	else if(loser1 == 2 || loser2 == 2 || loser3 == 2)
			        		canvas.drawText("Paying", X, Y, textBrush);
			        	
			        	if(winner == 3)
			        		canvas.drawText("Winner", X, Y+portraitHeight, textBrush);
			        	else if(loser1 == 3 || loser2 == 3 || loser3 == 3)
			        		canvas.drawText("Paying", X, Y+portraitHeight, textBrush);
			        	
			        	X = mCanvasWidth - portraitWidth+1;
			        	Y = ((mCanvasHeight - tileHeight)/2)-portraitHeight+13;
			        	if(winner == 1)
			        		canvas.drawText("Winner", X, Y, textBrush);
			        	else if(loser1 == 1 || loser2 == 1 || loser3 == 1)
			        		canvas.drawText("Paying", X, Y, textBrush);
			        	if(winner == 0)
			        		canvas.drawText("Winner", X, Y+portraitHeight, textBrush);
			        	else if(loser1 == 0 || loser2 == 0 || loser3 == 0)
			        		canvas.drawText("Paying", X, Y+portraitHeight, textBrush);
			        	
			        	float xOffset = textBrush.measureText("Points: " + String.valueOf(points));
			        	canvas.drawText("Points: " + String.valueOf(points), centerX - xOffset, centerY/4, textBrush);
			        	canvas.drawText("Fu: " + String.valueOf(pGameThread.mPlayers[winner].myHand.fu), centerX - xOffset, (centerY/4)+14, textBrush);
			        	canvas.drawText("Han: " + String.valueOf(pGameThread.mPlayers[winner].myHand.han), centerX - xOffset, (centerY/4)+28, textBrush);
			        	canvas.drawText("Yaku: ", centerX - xOffset, (centerY/4)+42, textBrush);
			        	
			        	ArrayList<Tile> Dora = pGameThread.mTable.getDora();
			        	if(pGameThread.mPlayers[winner].riichi){
			        		//We have ura Dora
			        		int regDora = Dora.size()/2;
			        		for(int i = 0; i < regDora; i++){
			        			Tile tempTile = Dora.get(i);
			        			if(tempTile != null)
			        				canvas.drawBitmap(TileBMPs50PercentUser[tempTile.rawNumber], centerX+((i+1)*miniTileWidth), (centerY/4),  null);
			        			tempTile = Dora.get(i+regDora);
			        			if(tempTile != null)
			        				canvas.drawBitmap(TileBMPs50PercentUser[tempTile.rawNumber], centerX+((i+1)*miniTileWidth), (centerY/4)+1+miniTileHeight,  null);
			        		}
			        	}
			        	else{
			        		for(int i = 0; i < Dora.size(); i++){
			        			Tile tempTile = Dora.get(i);
			        			if(tempTile != null)
			        				canvas.drawBitmap(TileBMPs50PercentUser[tempTile.rawNumber], centerX+((i+1)*miniTileWidth), (centerY/4),  null);
			        		}
			        	}
			        	
			        	
			        	Y = (centerY/4)+66;
			        	xOffset = xOffset/2;
			        	
			        	if(pGameThread.mPlayers[winner].myHand.dora > 0){
			        		if(bJapanese)
			        			canvas.drawText(String.valueOf(pGameThread.mPlayers[winner].myHand.dora) + " - ドラ", centerX - xOffset, Y, textBrush);
			        		else
			        			canvas.drawText(String.valueOf(pGameThread.mPlayers[winner].myHand.dora) + " - Dora", centerX - xOffset, Y, textBrush);
			        		Y += 16;
			        	}
			        	
			        	boolean yakumanFound = false;
			        	for(int thisYaku = Globals.NONYAKUMAN; thisYaku < Globals.ALLYAKUCOUNT; thisYaku++){
			        		if(pGameThread.mPlayers[winner].myHand.yaku[thisYaku] > 0){
			        			canvas.drawText(String.valueOf(pGameThread.mPlayers[winner].myHand.yaku[thisYaku]) + " - " + Globals.yakuToString(thisYaku, bJapanese), centerX - xOffset, Y, textBrush);
			        			Y += 16;
			        			yakumanFound = true;
			        		}
			        	}
			        	
			        	if(!yakumanFound){
				        	for(int thisYaku = 0; thisYaku <= Globals.NONYAKUMAN; thisYaku++){
				        		if(pGameThread.mPlayers[winner].myHand.yaku[thisYaku] > 0){
				        			canvas.drawText(String.valueOf(pGameThread.mPlayers[winner].myHand.yaku[thisYaku]) + " - " + Globals.yakuToString(thisYaku, bJapanese), centerX - xOffset, Y, textBrush);
				        			Y += 16;
				        		}
				        	}
			        	}
			        	
			        	int HandSize = pGameThread.mPlayers[winner].myHand.activeHandSize;
			        	X = centerX - ((HandSize/2)*miniTileWidth);
		
			        	for(int i = 0; i < pGameThread.mPlayers[winner].myHand.activeHandSize; i++){
			        		int bmpIdx = pGameThread.mPlayers[winner].myHand.rawHand[pGameThread.mPlayers[winner].myHand.activeHand[i]].rawNumber;
			        		//if(!bDebug)
			        		//	bmpIdx = 0;
			        		canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+(i*miniTileWidth)+i, Y,  null);
			        	}
			        	
			        	int numberOfMelds = pGameThread.mPlayers[winner].myHand.numberOfMelds;
			        	X = centerX - (((numberOfMelds*4)/2)*miniTileWidth);
			        	Y += (miniTileHeight*2);
			        	
			        	if(numberOfMelds > 0){
			        		//X = centerX - (2*miniTileWidth)-miniTileHeight-3;
			            	//Y = mCanvasHeight - miniTileHeight;
				        	//Globals.myAssert(startPos > 0);
				        	for(int meldIdx = 0; meldIdx < numberOfMelds; meldIdx++){
				        		//Closed Kan
				        		if(pGameThread.mPlayers[winner].myHand.rawHand[pGameThread.mPlayers[winner].myHand.melds[meldIdx][1]].selfKan){
				        			int bmpIdx = pGameThread.mPlayers[winner].myHand.rawHand[pGameThread.mPlayers[winner].myHand.melds[meldIdx][1]].rawNumber;
		    						canvas.drawBitmap(TileBMPs50PercentUser[0], X, Y,  null);
		    						canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileWidth, Y,  null);
		    						canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+(2*miniTileWidth), Y,  null);
		    						canvas.drawBitmap(TileBMPs50PercentUser[0], X+(3*miniTileWidth), Y,  null);
				        		}
				        		//Open Kan
				        		else if(pGameThread.mPlayers[winner].myHand.melds[meldIdx][winner] == 4){
				        			int playerFrom = pGameThread.mPlayers[winner].myHand.melds[meldIdx][5];
				        			int bmpIdx = pGameThread.mPlayers[winner].myHand.rawHand[pGameThread.mPlayers[winner].myHand.melds[meldIdx][1]].rawNumber;
				        			if(playerFrom == 3){
				        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y+(miniTileHeight-miniTileWidth),  null);
				        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y+(miniTileHeight-miniTileWidth)-miniTileWidth,  null);
				        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileHeight, Y,  null);
				        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileHeight+miniTileWidth, Y,  null);
				        			}
				        			if(playerFrom == 2){
				        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X, Y,  null);
				        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X+miniTileWidth, Y+(miniTileHeight-miniTileWidth),  null);
				        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X+miniTileWidth, Y+(miniTileHeight-miniTileWidth)-miniTileWidth,  null);
				        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileWidth+miniTileHeight, Y,  null);
				        			}
				        			if(playerFrom == 1){
				        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X, Y,  null);
				        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileWidth, Y,  null);
				        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X+(2*miniTileWidth), Y+(miniTileHeight-miniTileWidth),  null);
				        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X+(2*miniTileWidth), Y+(miniTileHeight-miniTileWidth)-miniTileWidth,  null);
				        			}
				        		}
				        		else{
					        		//All Others
					        		int playerFrom = pGameThread.mPlayers[winner].myHand.melds[meldIdx][5];
				        			int bmpIdx = pGameThread.mPlayers[winner].myHand.rawHand[pGameThread.mPlayers[winner].myHand.melds[meldIdx][1]].rawNumber;
				        			if(playerFrom == 3){
				        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y+(miniTileHeight-miniTileWidth),  null);
				        				bmpIdx = pGameThread.mPlayers[winner].myHand.rawHand[pGameThread.mPlayers[winner].myHand.melds[meldIdx][2]].rawNumber;
				        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileHeight, Y,  null);
				        				bmpIdx = pGameThread.mPlayers[winner].myHand.rawHand[pGameThread.mPlayers[winner].myHand.melds[meldIdx][3]].rawNumber;
				        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileHeight+miniTileWidth, Y,  null);
				        			}
				        			if(playerFrom == 2){
				        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X, Y,  null);
				        				bmpIdx = pGameThread.mPlayers[winner].myHand.rawHand[pGameThread.mPlayers[winner].myHand.melds[meldIdx][2]].rawNumber;
				        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X+miniTileWidth, Y+(miniTileHeight-miniTileWidth),  null);
				        				bmpIdx = pGameThread.mPlayers[winner].myHand.rawHand[pGameThread.mPlayers[winner].myHand.melds[meldIdx][3]].rawNumber;
				        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileWidth+miniTileHeight, Y,  null);
				        			}
				        			if(playerFrom == 1){
				        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X, Y,  null);
				        				bmpIdx = pGameThread.mPlayers[winner].myHand.rawHand[pGameThread.mPlayers[winner].myHand.melds[meldIdx][2]].rawNumber;
				        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileWidth, Y,  null);
				        				bmpIdx = pGameThread.mPlayers[winner].myHand.rawHand[pGameThread.mPlayers[winner].myHand.melds[meldIdx][3]].rawNumber;
				        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X+(2*miniTileWidth), Y+(miniTileHeight-miniTileWidth),  null);
				        			}
				        		}
			        			X += (miniTileWidth*4);
				        	}
			        	}
			        	
			        	canvas.drawBitmap(miscBitmaps[Globals.Graphics.MISC_OK_BTN], centerX - (miscBitmaps[Globals.Graphics.MISC_OK_BTN].getWidth()/2), mCanvasHeight - miscBitmaps[Globals.Graphics.MISC_OK_BTN].getHeight()-1, null);
			        	
			        	return;
	        		}
		        	
		        	//Winds
		        	X = 5;
		        	Y = ((mCanvasHeight - tileHeight)/2)-portraitHeight+16;
		        	canvas.drawText(Globals.windToString(pGameThread.mPlayers[2].currentWind), X, Y, boldTextBrush);
		        	canvas.drawText(Globals.windToString(pGameThread.mPlayers[3].currentWind), X, Y+portraitHeight, boldTextBrush);
		        	
		        	X = mCanvasWidth - portraitWidth+5;
		        	Y = ((mCanvasHeight - tileHeight)/2)-portraitHeight+16;
		        	canvas.drawText(Globals.windToString(pGameThread.mPlayers[1].currentWind), X, Y, boldTextBrush);
		        	canvas.drawText(Globals.windToString(pGameThread.mPlayers[0].currentWind), X, Y+portraitHeight, boldTextBrush);
		        	
		        	//Center Info
		        	X = centerX - (miniTileWidth*3);
		        	Y = centerY - (miniTileWidth*3);
		        	canvas.drawBitmap(miscBitmaps[Globals.Graphics.MISC_CENTER], X, Y, null);
		        	
		        	X = (int) (centerX - Math.round(miniTileWidth * 1.0));
		        	Y = centerY + (miniTileWidth);
		        	temp = pGameThread.mTable.wallCount();
		        	canvas.drawText(temp.toString(), X, Y, textBrush);
		        	
		        	X = (int) (centerX + Math.round(miniTileWidth * 1.0));
		        	Y = centerY + (miniTileWidth);
		        	canvas.drawText(Globals.windToString(pGameThread.curWind), X, Y, textBrush);
		        	
		        	X = (int) (centerX - (miniTileWidth * 2));
		        	Y = (int) (centerY - (miniTileWidth * 1.5));
		        	ArrayList<Tile> dora = pGameThread.mTable.getDora();
		        	for(int i = 0; i < 4; i++){
		        		int bmpIdx = 0;
		        		if(i < dora.size()){
		        			bmpIdx = dora.get(i).rawNumber;
		        		}
		        		canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+(i*miniTileWidth), Y,  null);
		        	}
		        	
		        	//Sticks, furiten, and other misc
		        	Paint redTextBrush = new Paint();
		        	redTextBrush.setColor(Color.RED);
		        	redTextBrush.setTextSize(16.0f);
		        	
		        	X = 0;
		        	Y = mCanvasHeight-16;
		        	if(pGameThread.mPlayers[0].myHand.inFuriten)
		        		canvas.drawText("FURITEN", X, Y, redTextBrush);
		        	
		        	//Players Hand
		        	int HandSize = pGameThread.mPlayers[0].myHand.activeHandSize;
		        	X = centerX - ((HandSize * tileWidth)/2);
		        	Y = (mCanvasHeight - tileHeight) - 1;
		        	//float startPos = (mCanvasWidth-(29*HandSize))/2;
		        	//Globals.myAssert(startPos > 0);
		        	for(int i = 0; i < pGameThread.mPlayers[0].myHand.activeHandSize; i++){
		        		int bmpIdx = pGameThread.mPlayers[0].myHand.rawHand[pGameThread.mPlayers[0].myHand.activeHand[i]].rawNumber;
		        		if(i == tileSelected)
		        			canvas.drawBitmap(TileBMPsUser[bmpIdx], X+(tileWidth*i), Y - 5,  null);
		        		else
		        			canvas.drawBitmap(TileBMPsUser[bmpIdx], X+(tileWidth*i), Y,  null);
		        	}
		        	
		        	//AI Hands
		        	HandSize = pGameThread.mPlayers[1].myHand.activeHandSize;
		        	X = mCanvasWidth - portraitWidth - miniTileHeight - 1;
		        	Y = ((mCanvasHeight - tileHeight)/2)+portraitHeight -  miniTileWidth;
	
		        	for(int i = 0; i < pGameThread.mPlayers[1].myHand.activeHandSize; i++){
		        		int bmpIdx = pGameThread.mPlayers[1].myHand.rawHand[pGameThread.mPlayers[1].myHand.activeHand[i]].rawNumber;
		        		if(!bDebug)
		        			bmpIdx = 0;
		        		canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y-(i*miniTileWidth)-i,  null);
		        	}
		        	
		        	HandSize = pGameThread.mPlayers[2].myHand.activeHandSize;
		        	X = centerX + ((HandSize*(miniTileWidth+1))/2);
		        	Y = 1;
		        	
		        	for(int i = 0; i < pGameThread.mPlayers[2].myHand.activeHandSize; i++){
		        		int bmpIdx = pGameThread.mPlayers[2].myHand.rawHand[pGameThread.mPlayers[2].myHand.activeHand[i]].rawNumber;
		        		if(!bDebug)
		        			bmpIdx = 0;
		        		canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X-(i*miniTileWidth)-i, Y, null);
		        	}
		        	
		        	HandSize = pGameThread.mPlayers[3].myHand.activeHandSize;
		        	X = portraitWidth + 1;
		        	Y = ((mCanvasHeight - tileHeight)/2)- portraitHeight;
		        	
		        	
		        	for(int i = 0; i < pGameThread.mPlayers[3].myHand.activeHandSize; i++){
		        		int bmpIdx = pGameThread.mPlayers[3].myHand.rawHand[pGameThread.mPlayers[3].myHand.activeHand[i]].rawNumber;
		        		if(!bDebug)
		        			bmpIdx = 0;
		        		canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx],  X, Y+(i*miniTileWidth)+i, null);
		        	}
		        	
		        	//Discards
		        	ArrayList<Tile> discards = pGameThread.mTable.getDiscards(0);
		        	int riichiTile = pGameThread.mTable.getRiichiTile(0);
		        	
		        	int row = 0;
		        	int column = 0;
		        	boolean riichiTileUsed = false;
		        	for(int i = 0; i < discards.size(); i++){
		        		float PosX = (centerX - ((float)(miniTileWidth*3))) + (column * miniTileWidth);
		        		float PosY = (centerY + ((float)(miniTileWidth*3))) + (row * miniTileHeight) + row;
		        		if(riichiTileUsed)
		        			PosX += miniTileHeight-miniTileWidth;
		        		if(i == riichiTile){
		        			riichiTileUsed = true;
		        			canvas.drawBitmap(TileBMPsSouthRotated[discards.get(i).rawNumber], PosX, PosY, null);
		        		}
		        		else
		        			canvas.drawBitmap(TileBMPs50PercentUser[discards.get(i).rawNumber], PosX, PosY, null);
		        		
		        		column++;
		        		if(column == 6){
		        			column = 0;
		        			riichiTileUsed = false;
		        			row++;
		        		}
		        	}
		        	
		        	discards = pGameThread.mTable.getDiscards(1);
		        	riichiTile = pGameThread.mTable.getRiichiTile(1);
		        	
		        	row = 0;
		        	column = 0;
		        	riichiTileUsed = false;
		        	for(int i = 0; i < discards.size(); i++){
		        		float PosX = (centerX + ((float)(miniTileWidth*3))) + (row * miniTileHeight);
		        		float PosY = (centerY + ((float)(miniTileWidth*3))) + (column * -miniTileWidth) - miniTileWidth;
		        		if(riichiTileUsed)
		        			PosY -= (miniTileHeight-miniTileWidth);
		        		if(i == riichiTile){
		        			riichiTileUsed = true;
		        			canvas.drawBitmap(TileBMPsWestRotated[discards.get(i).rawNumber], PosX, PosY-(miniTileHeight-miniTileWidth), null);
		        		}
		        		else
		        			canvas.drawBitmap(TileBMPsSouthRotated[discards.get(i).rawNumber], PosX, PosY, null);
		        		
		        		column++;
		        		if(column == 6){
		        			column = 0;
		        			riichiTileUsed = false;
		        			row++;
		        		}
		        	}
		        	
		        	discards = pGameThread.mTable.getDiscards(2);
		        	riichiTile = pGameThread.mTable.getRiichiTile(2);
		        	
		        	row = 0;
		        	column = 0;
		        	riichiTileUsed = false;
		        	for(int i = 0; i < discards.size(); i++){
		        		float PosX = (centerX + ((float)(miniTileWidth*3))) + (column * -miniTileWidth) - miniTileWidth;
		        		float PosY = (centerY - ((float)(miniTileWidth*3))) + (row * -miniTileHeight) - miniTileHeight - 1;
		        		/*float PosX = (centerX + 45.0f) + (row * 19.0f);
		        		float PosY = (centerY + 45.0f) + (column * -15.0f) - 14.0f;*/
		        		if(riichiTileUsed)
		        			PosX -= (miniTileHeight-miniTileWidth);
		        		if(i == riichiTile){
		        			riichiTileUsed = true;
		        			canvas.drawBitmap(TileBMPsSouthRotated[discards.get(i).rawNumber], PosX, PosY, null);
		        		}
		        		else
		        			canvas.drawBitmap(TileBMPsWestRotated[discards.get(i).rawNumber], PosX, PosY, null);
		        		
		        		column++;
		        		if(column == 6){
		        			column = 0;
		        			riichiTileUsed = false;
		        			row++;
		        		}
		        	}
		        	
		        	discards = pGameThread.mTable.getDiscards(3);
		        	riichiTile = pGameThread.mTable.getRiichiTile(3);
		        	
		        	row = 0;
		        	column = 0;
		        	riichiTileUsed = false;
		        	for(int i = 0; i < discards.size(); i++){
		        		float PosX = (centerX - ((float)(miniTileWidth*3))) + (row * -miniTileHeight) - miniTileHeight;
		        		float PosY = (centerY - ((float)(miniTileWidth*3))) + (column * miniTileWidth) /*- 18.0f*/;
		        		if(riichiTileUsed)
		        			PosY += miniTileHeight-miniTileWidth;
		        		if(i == riichiTile){
		        			riichiTileUsed = true;
		        			canvas.drawBitmap(TileBMPsWestRotated[discards.get(i).rawNumber], PosX, PosY, null);
		        		}
		        		else
		        			canvas.drawBitmap(TileBMPsNorthRotated[discards.get(i).rawNumber], PosX, PosY, null);
		        		
		        		column++;
		        		if(column == 6){
		        			column = 0;
		        			riichiTileUsed = false;
		        			row++;
		        		}
		        	}
		        	
		        	//Player Melds
		        	int numberOfMelds = pGameThread.mPlayers[0].myHand.numberOfMelds;
		        	if(numberOfMelds > 0){
		        		X = mCanvasWidth - (4*miniTileWidth);
		            	Y = mCanvasHeight - miniTileHeight;
			        	//Globals.myAssert(startPos > 0);
			        	for(int meldIdx = 0; meldIdx < numberOfMelds; meldIdx++){
			        		
			        		if(meldIdx == 1 || meldIdx == 3)
			        			Y -= ((miniTileWidth*2) + 1);
			        		if(meldIdx == 2){
			        			X = mCanvasWidth - (8*miniTileWidth);
				            	Y = mCanvasHeight - miniTileHeight;
			        		}
			        		
			        		//Closed Kan
			        		if(pGameThread.mPlayers[0].myHand.rawHand[pGameThread.mPlayers[0].myHand.melds[meldIdx][1]].selfKan){
			        			int bmpIdx = pGameThread.mPlayers[0].myHand.rawHand[pGameThread.mPlayers[0].myHand.melds[meldIdx][1]].rawNumber;
	    						canvas.drawBitmap(TileBMPs50PercentUser[0], X, Y,  null);
	    						canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileWidth, Y,  null);
	    						canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+(2*miniTileWidth), Y,  null);
	    						canvas.drawBitmap(TileBMPs50PercentUser[0], X+(3*miniTileWidth), Y,  null);
			        			continue;
			        		}
			        		
			        		//Open Kan
			        		if(pGameThread.mPlayers[0].myHand.melds[meldIdx][0] == 4){
			        			int playerFrom = pGameThread.mPlayers[0].myHand.melds[meldIdx][5];
			        			int bmpIdx = pGameThread.mPlayers[0].myHand.rawHand[pGameThread.mPlayers[0].myHand.melds[meldIdx][1]].rawNumber;
			        			if(playerFrom == 3){
			        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y+(miniTileHeight-miniTileWidth),  null);
			        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y+(miniTileHeight-miniTileWidth)-miniTileWidth,  null);
			        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileHeight, Y,  null);
			        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileHeight+miniTileWidth, Y,  null);
			        			}
			        			if(playerFrom == 2){
			        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X, Y,  null);
			        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X+miniTileWidth, Y+(miniTileHeight-miniTileWidth),  null);
			        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X+miniTileWidth, Y+(miniTileHeight-miniTileWidth)-miniTileWidth,  null);
			        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileWidth+miniTileHeight, Y,  null);
			        			}
			        			if(playerFrom == 1){
			        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X, Y,  null);
			        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileWidth, Y,  null);
			        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X+(2*miniTileWidth), Y+(miniTileHeight-miniTileWidth),  null);
			        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X+(2*miniTileWidth), Y+(miniTileHeight-miniTileWidth)-miniTileWidth,  null);
			        			}
			        			continue;
			        		}
			        		
			        		//All Others
			        		int playerFrom = pGameThread.mPlayers[0].myHand.melds[meldIdx][5];
		        			int bmpIdx = pGameThread.mPlayers[0].myHand.rawHand[pGameThread.mPlayers[0].myHand.melds[meldIdx][1]].rawNumber;
		        			if(playerFrom == 3){
		        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y+(miniTileHeight-miniTileWidth),  null);
		        				bmpIdx = pGameThread.mPlayers[0].myHand.rawHand[pGameThread.mPlayers[0].myHand.melds[meldIdx][2]].rawNumber;
		        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileHeight, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[0].myHand.rawHand[pGameThread.mPlayers[0].myHand.melds[meldIdx][3]].rawNumber;
		        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileHeight+miniTileWidth, Y,  null);
		        			}
		        			if(playerFrom == 2){
		        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[0].myHand.rawHand[pGameThread.mPlayers[0].myHand.melds[meldIdx][2]].rawNumber;
		        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X+miniTileWidth, Y+(miniTileHeight-miniTileWidth),  null);
		        				bmpIdx = pGameThread.mPlayers[0].myHand.rawHand[pGameThread.mPlayers[0].myHand.melds[meldIdx][3]].rawNumber;
		        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileWidth+miniTileHeight, Y,  null);
		        			}
		        			if(playerFrom == 1){
		        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[0].myHand.rawHand[pGameThread.mPlayers[0].myHand.melds[meldIdx][2]].rawNumber;
		        				canvas.drawBitmap(TileBMPs50PercentUser[bmpIdx], X+miniTileWidth, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[0].myHand.rawHand[pGameThread.mPlayers[0].myHand.melds[meldIdx][3]].rawNumber;
		        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X+(2*miniTileWidth), Y+(miniTileHeight-miniTileWidth),  null);
		        			}
			        	}
		        	}
		        	
		        	//AI Melds
		        	numberOfMelds = pGameThread.mPlayers[1].myHand.numberOfMelds;
		        	if(numberOfMelds > 0){
			        	//Globals.myAssert(startPos > 0);
			        	for(int meldIdx = 0; meldIdx < numberOfMelds; meldIdx++){
			        		X = mCanvasWidth - portraitWidth - miniTileHeight;
			            	Y = ((mCanvasHeight - tileHeight)/2)-portraitHeight+(4*miniTileWidth)/*offset*/-(miniTileWidth*2);
			            	
			        		if(meldIdx == 1)
			        			X -= ((miniTileWidth*2) + 1);
			        		if(meldIdx == 2){
				            	Y += (4*miniTileWidth);
			        		}
			        		if(meldIdx == 3){
			        			X -= ((miniTileWidth*2) + 1);
					            Y += (4*miniTileWidth);
			        		}
			        		
			        		//Closed Kan
			        		if(pGameThread.mPlayers[1].myHand.rawHand[pGameThread.mPlayers[1].myHand.melds[meldIdx][1]].selfKan){
			        			int bmpIdx = pGameThread.mPlayers[1].myHand.rawHand[pGameThread.mPlayers[1].myHand.melds[meldIdx][1]].rawNumber;
	    						canvas.drawBitmap(TileBMPsSouthRotated[0], X, Y,  null);
	    						canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y-miniTileWidth,  null);
	    						canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y-(miniTileWidth*2),  null);
	    						canvas.drawBitmap(TileBMPsSouthRotated[0], X, Y-(miniTileWidth*3),  null);
			        			continue;
			        		}
			        		
			        		//Open Kan
			        		if(pGameThread.mPlayers[1].myHand.melds[meldIdx][0] == 4){
			        			int playerFrom = pGameThread.mPlayers[1].myHand.melds[meldIdx][5];
			        			int bmpIdx = pGameThread.mPlayers[1].myHand.rawHand[pGameThread.mPlayers[1].myHand.melds[meldIdx][1]].rawNumber;
			        			if(playerFrom == 2){
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+(miniTileHeight-miniTileWidth), Y,  null);
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+(miniTileHeight-miniTileWidth)-miniTileWidth, Y,  null);
			        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y-miniTileHeight,  null);
			        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y-miniTileHeight-miniTileWidth,  null);
			        			}
			        			if(playerFrom == 3){
			        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y,  null);
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+(miniTileHeight-miniTileWidth), Y-miniTileHeight,  null);
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+(miniTileHeight-miniTileWidth)-miniTileWidth, Y-miniTileHeight,  null);
			        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y-miniTileHeight-miniTileWidth,  null);
			        			}
			        			if(playerFrom == 0){
			        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y,  null);
			        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y-miniTileWidth,  null);
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+(miniTileHeight-miniTileWidth), Y-(2*miniTileWidth),  null);
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+(miniTileHeight-miniTileWidth)-miniTileWidth, Y-(2*miniTileWidth),  null);
			        			}
			        			continue;
			        		}
			        		
			        		//All Others
			        		int playerFrom = pGameThread.mPlayers[1].myHand.melds[meldIdx][5];
		        			int bmpIdx = pGameThread.mPlayers[1].myHand.rawHand[pGameThread.mPlayers[1].myHand.melds[meldIdx][1]].rawNumber;
		        			if(playerFrom == 0){
		        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+(miniTileHeight-miniTileWidth), Y,  null);
		        				bmpIdx = pGameThread.mPlayers[1].myHand.rawHand[pGameThread.mPlayers[1].myHand.melds[meldIdx][2]].rawNumber;
		        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y-miniTileWidth,  null);
		        				bmpIdx = pGameThread.mPlayers[1].myHand.rawHand[pGameThread.mPlayers[1].myHand.melds[meldIdx][3]].rawNumber;
		        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y/*-miniTileHeight*/-(2*miniTileWidth),  null);
		        			}
		        			if(playerFrom == 2){
		        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[1].myHand.rawHand[pGameThread.mPlayers[1].myHand.melds[meldIdx][2]].rawNumber;
		        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+(miniTileHeight-miniTileWidth), Y-miniTileHeight,  null);
		        				bmpIdx = pGameThread.mPlayers[1].myHand.rawHand[pGameThread.mPlayers[1].myHand.melds[meldIdx][3]].rawNumber;
		        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y-miniTileHeight-miniTileWidth,  null);
		        			}
		        			if(playerFrom == 3){
		        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[1].myHand.rawHand[pGameThread.mPlayers[1].myHand.melds[meldIdx][2]].rawNumber;
		        				canvas.drawBitmap(TileBMPsSouthRotated[bmpIdx], X, Y-miniTileWidth,  null);
		        				bmpIdx = pGameThread.mPlayers[1].myHand.rawHand[pGameThread.mPlayers[1].myHand.melds[meldIdx][3]].rawNumber;
		        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+(miniTileHeight-miniTileWidth), Y-(miniTileWidth*2)-(miniTileHeight-miniTileWidth),  null);
		        			}
			        	}
		        	}
		        	
		        	numberOfMelds = pGameThread.mPlayers[2].myHand.numberOfMelds;
		        	if(numberOfMelds > 0){
			        	//Globals.myAssert(startPos > 0);
			        	for(int meldIdx = 0; meldIdx < numberOfMelds; meldIdx++){
			        		X = 1;
			            	Y = 1;
			            	
			        		if(meldIdx == 1)
			        			X += (4*miniTileWidth);
			        			//Y += ((miniTileWidth*2) + 1);
			        		if(meldIdx == 2){
				            	X += ((4*miniTileWidth)*2);
			        		}
			        		if(meldIdx == 3){
			        			//Y += ((miniTileWidth*2) + 1);
					            X += ((4*miniTileWidth)*3);
			        		}
			        		
			        		//Closed Kan
			        		if(pGameThread.mPlayers[2].myHand.rawHand[pGameThread.mPlayers[2].myHand.melds[meldIdx][1]].selfKan){
			        			int bmpIdx = pGameThread.mPlayers[2].myHand.rawHand[pGameThread.mPlayers[2].myHand.melds[meldIdx][1]].rawNumber;
	    						canvas.drawBitmap(TileBMPsWestRotated[0], X, Y,  null);
	    						canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+miniTileWidth, Y,  null);
	    						canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+(miniTileWidth*2), Y,  null);
	    						canvas.drawBitmap(TileBMPsWestRotated[0], X+(miniTileWidth*3), Y,  null);
			        			continue;
			        		}
			        		
			        		//Open Kan
			        		if(pGameThread.mPlayers[2].myHand.melds[meldIdx][0] == 4){
			        			int playerFrom = pGameThread.mPlayers[2].myHand.melds[meldIdx][5];
			        			int bmpIdx = pGameThread.mPlayers[2].myHand.rawHand[pGameThread.mPlayers[2].myHand.melds[meldIdx][1]].rawNumber;
			        			if(playerFrom == 3){
			        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y,  null);
			        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y+miniTileWidth,  null);
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+miniTileHeight, Y,  null);
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+miniTileHeight+miniTileWidth, Y,  null);
			        			}
			        			if(playerFrom == 0){
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X, Y,  null);
			        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X+miniTileWidth, Y,  null);
			        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X+miniTileWidth, Y+miniTileWidth,  null);
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+miniTileHeight+miniTileWidth, Y,  null);
			        			}
			        			if(playerFrom == 1){
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X, Y,  null);
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+miniTileWidth, Y,  null);
			        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X+(2*miniTileWidth), Y,  null);
			        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X+(2*miniTileWidth), Y+miniTileWidth,  null);
			        			}
			        			continue;
			        		}
			        		
			        		//All Others
			        		int playerFrom = pGameThread.mPlayers[2].myHand.melds[meldIdx][5];
		        			int bmpIdx = pGameThread.mPlayers[2].myHand.rawHand[pGameThread.mPlayers[2].myHand.melds[meldIdx][1]].rawNumber;
		        			if(playerFrom == 3){
		        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[2].myHand.rawHand[pGameThread.mPlayers[2].myHand.melds[meldIdx][2]].rawNumber;
		        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+miniTileHeight, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[2].myHand.rawHand[pGameThread.mPlayers[2].myHand.melds[meldIdx][3]].rawNumber;
		        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+miniTileHeight+miniTileWidth, Y,  null);
		        			}
		        			if(playerFrom == 0){
		        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[2].myHand.rawHand[pGameThread.mPlayers[2].myHand.melds[meldIdx][2]].rawNumber;
		        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X+miniTileWidth, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[2].myHand.rawHand[pGameThread.mPlayers[2].myHand.melds[meldIdx][3]].rawNumber;
		        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+miniTileHeight+miniTileWidth, Y,  null);
		        			}
		        			if(playerFrom == 1){
		        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[2].myHand.rawHand[pGameThread.mPlayers[2].myHand.melds[meldIdx][2]].rawNumber;
		        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+miniTileWidth, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[2].myHand.rawHand[pGameThread.mPlayers[2].myHand.melds[meldIdx][3]].rawNumber;
		        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X+(2*miniTileWidth), Y,  null);
		        			}
			        	}
		        	}
		        	
		        	numberOfMelds = pGameThread.mPlayers[3].myHand.numberOfMelds;
		        	if(numberOfMelds > 0){
			        	//Globals.myAssert(startPos > 0);
			        	for(int meldIdx = 0; meldIdx < numberOfMelds; meldIdx++){
			        		X = portraitWidth;
			            	Y = ((mCanvasHeight - tileHeight)/2)+portraitHeight-(4*miniTileWidth)+/*Offset*/(miniTileWidth*2);
			            	
			        		if(meldIdx == 1)
			        			X += ((miniTileWidth*2) + 1);
			        		if(meldIdx == 2){
				            	Y -= (4*miniTileWidth);
			        		}
			        		if(meldIdx == 3){
			        			X += ((miniTileWidth*2) + 1);
					            Y -= (4*miniTileWidth);
			        		}
			        		
			        		//Closed Kan
			        		if(pGameThread.mPlayers[3].myHand.rawHand[pGameThread.mPlayers[3].myHand.melds[meldIdx][1]].selfKan){
			        			int bmpIdx = pGameThread.mPlayers[3].myHand.rawHand[pGameThread.mPlayers[3].myHand.melds[meldIdx][1]].rawNumber;
	    						canvas.drawBitmap(TileBMPsNorthRotated[0], X, Y,  null);
	    						canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y+miniTileWidth,  null);
	    						canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y+(miniTileWidth*2),  null);
	    						canvas.drawBitmap(TileBMPsNorthRotated[0], X, Y+(miniTileWidth*3),  null);
			        			continue;
			        		}
			        		
			        		//Open Kan
			        		if(pGameThread.mPlayers[3].myHand.melds[meldIdx][0] == 4){
			        			int playerFrom = pGameThread.mPlayers[3].myHand.melds[meldIdx][5];
			        			int bmpIdx = pGameThread.mPlayers[3].myHand.rawHand[pGameThread.mPlayers[3].myHand.melds[meldIdx][1]].rawNumber;
			        			if(playerFrom == 2){
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X, Y,  null);
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+miniTileWidth, Y,  null);
			        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y+miniTileHeight,  null);
			        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y+miniTileHeight+miniTileWidth,  null);
			        			}
			        			if(playerFrom == 1){
			        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y,  null);
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X, Y+miniTileWidth,  null);
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+miniTileWidth, Y+miniTileWidth,  null);
			        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y+miniTileHeight+miniTileWidth,  null);
			        			}
			        			if(playerFrom == 0){
			        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y,  null);
			        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y+miniTileWidth,  null);
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X, Y+(2*miniTileWidth),  null);
			        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X+miniTileWidth, Y+(2*miniTileWidth),  null);
			        			}
			        			continue;
			        		}
			        		
			        		//All Others
			        		int playerFrom = pGameThread.mPlayers[3].myHand.melds[meldIdx][5];
		        			int bmpIdx = pGameThread.mPlayers[3].myHand.rawHand[pGameThread.mPlayers[3].myHand.melds[meldIdx][1]].rawNumber;
		        			if(playerFrom == 2){
		        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[3].myHand.rawHand[pGameThread.mPlayers[3].myHand.melds[meldIdx][2]].rawNumber;
		        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y+miniTileHeight,  null);
		        				bmpIdx = pGameThread.mPlayers[3].myHand.rawHand[pGameThread.mPlayers[3].myHand.melds[meldIdx][3]].rawNumber;
		        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y+miniTileHeight+miniTileWidth,  null);
		        			}
		        			if(playerFrom == 1){
		        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[3].myHand.rawHand[pGameThread.mPlayers[3].myHand.melds[meldIdx][2]].rawNumber;
		        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X, Y+miniTileWidth,  null);
		        				bmpIdx = pGameThread.mPlayers[3].myHand.rawHand[pGameThread.mPlayers[3].myHand.melds[meldIdx][3]].rawNumber;
		        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y+miniTileHeight+miniTileWidth,  null);
		        			}
		        			if(playerFrom == 0){
		        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y,  null);
		        				bmpIdx = pGameThread.mPlayers[3].myHand.rawHand[pGameThread.mPlayers[3].myHand.melds[meldIdx][2]].rawNumber;
		        				canvas.drawBitmap(TileBMPsNorthRotated[bmpIdx], X, Y+miniTileWidth,  null);
		        				bmpIdx = pGameThread.mPlayers[3].myHand.rawHand[pGameThread.mPlayers[3].myHand.melds[meldIdx][3]].rawNumber;
		        				canvas.drawBitmap(TileBMPsWestRotated[bmpIdx], X, Y+(2*miniTileWidth),  null);
		        			}
			        	}
		        	}
		        	
		        	//Buttons
		        	int buttonWidth = Buttons[PON_BTN].getWidth();
		        	int buttonHeight = Buttons[PON_BTN].getHeight();
		        	X = centerX - (buttonWidth/2);
		        	Y = mCanvasHeight - tileHeight - 1 - buttonHeight;
		        	
		        	if(needButtons[PASS_BTN]){
		        		canvas.drawBitmap(Buttons[PASS_BTN], X, Y, null);
		        	}
		        	if(needButtons[PON_BTN]){
		        		canvas.drawBitmap(Buttons[PON_BTN], X + buttonWidth + 1, Y, null);
		        	}
		        	if(needButtons[CHI_BTN]){
		        		canvas.drawBitmap(Buttons[CHI_BTN], X + (buttonWidth*2) + 2, Y, null);
		        	}
		        	if(needButtons[KAN_BTN]){
		        		canvas.drawBitmap(Buttons[KAN_BTN], X + (buttonWidth*3) + 3, Y, null);
		        	}
		        	if(needButtons[SELFKAN_BTN]){
		        		canvas.drawBitmap(Buttons[SELFKAN_BTN], X + (buttonWidth*3) + 3, Y, null);
		        	}
		        	if(needButtons[RIICHI_BTN]){
		        		canvas.drawBitmap(Buttons[RIICHI_BTN], X - buttonWidth - 1, Y, null);
		        	}
		        	if(needButtons[RON_BTN]){
		        		canvas.drawBitmap(Buttons[RON_BTN],X - (buttonWidth*2) - 2, Y, null);
		        	}
		        	if(needButtons[TSUMO_BTN]){
		        		canvas.drawBitmap(Buttons[TSUMO_BTN], X - (buttonWidth*3) - 3, Y, null);
		        	}
		        	
		        	
		        	if(bNeedChiList){
		        		//Box is 100x64
		        		int setSize = (2*tileWidth)+3;
		        		Paint blackBrush = new Paint();
		        		blackBrush.setColor(Color.BLACK);
		        		Paint whiteBrush = new Paint();
		        		whiteBrush.setColor(Color.WHITE);
		        		canvas.drawRect(centerX - 17 -  (int)(setSize * 1.5), centerY - 24, centerX + 17 + (int)(setSize * 1.5), centerY + 22, blackBrush);
		        		canvas.drawRect(centerX - 15 - (int)(setSize * 1.5), centerY - 22, centerX + 15 + (int)(setSize * 1.5), centerY + 20, whiteBrush);
		        		Globals.myAssert(chiList.size() >= 2 && chiList.size() <= 3);
		        		//canvas.drawBitmap(Buttons[PASS_BTN], centerX - 10, centerY + 10, null);
		        		float startX = 0;
		        		if(chiList.size() == 2){
		        			 startX = centerX - 5 - setSize;
		        		}
		        		else{
		        			startX = centerX - 10 - (setSize/2) - setSize;
		        		}
		        		for(int i = 0; i < chiList.size(); i++){
		        			int bmpIdx = pGameThread.mPlayers[0].myHand.rawHand[chiList.get(i).tiles[0]].rawNumber;
		        			canvas.drawBitmap(TileBMPsUser[bmpIdx], startX+(29*0), centerY - 20,  null);
		        			bmpIdx = pGameThread.mPlayers[0].myHand.rawHand[chiList.get(i).tiles[1]].rawNumber;
		        			canvas.drawBitmap(TileBMPsUser[bmpIdx], startX+(29*1), centerY - 20,  null);
		        			//canvas.drawBitmap(TileBMPsUser[chiList.get(i).tiles[2]], startX+(29*2), centerY - 27,  null);
		        			startX += setSize + 10;
		        		}
		        	}
		        	
		        	if(callToShow != Globals.CMD.PASS){
		        		if(playerCalling == 0){
		        			int bubbleWidth = miscBitmaps[Globals.Graphics.MISC_BUBBLE_RIGHT].getWidth();
		        			int bubbleHeight = miscBitmaps[Globals.Graphics.MISC_BUBBLE_RIGHT].getWidth();
		        			X = mCanvasWidth - portraitWidth - bubbleWidth;
				        	Y = ((mCanvasHeight - tileHeight)/2)/*-portraitHeight*/;
		    	        	canvas.drawBitmap(miscBitmaps[Globals.Graphics.MISC_BUBBLE_RIGHT], X, Y,  null);
		    	        	canvas.drawText(Globals.cmdToString(callToShow, bJapanese), X+(bubbleWidth/4), Y+(bubbleHeight/3), textBrush);
		        		}
		        		else if(playerCalling == 1){
		        			int bubbleWidth = miscBitmaps[Globals.Graphics.MISC_BUBBLE_RIGHT].getWidth();
		        			int bubbleHeight = miscBitmaps[Globals.Graphics.MISC_BUBBLE_RIGHT].getWidth();
		        			X = mCanvasWidth - portraitWidth - bubbleWidth;
		        			Y = ((mCanvasHeight - tileHeight)/2)-portraitHeight;
		    	        	canvas.drawBitmap(miscBitmaps[Globals.Graphics.MISC_BUBBLE_RIGHT], X, Y,  null);
		    	        	canvas.drawText(Globals.cmdToString(callToShow, bJapanese), X+(bubbleWidth/4), Y+(bubbleHeight/3), textBrush);
		        		}
		        		else if(playerCalling == 2){
		        			int bubbleWidth = miscBitmaps[Globals.Graphics.MISC_BUBBLE_RIGHT].getWidth();
		        			int bubbleHeight = miscBitmaps[Globals.Graphics.MISC_BUBBLE_RIGHT].getWidth();
		        			X = portraitWidth;
		        			Y = ((mCanvasHeight - tileHeight)/2)-portraitHeight;
		    	        	canvas.drawBitmap(miscBitmaps[Globals.Graphics.MISC_BUBBLE_LEFT], X, Y,  null);
		    	        	canvas.drawText(Globals.cmdToString(callToShow, bJapanese), X+(bubbleWidth/4), Y+(bubbleHeight/3), textBrush);
		        		}
		        		else if(playerCalling == 3){
		        			int bubbleWidth = miscBitmaps[Globals.Graphics.MISC_BUBBLE_RIGHT].getWidth();
		        			int bubbleHeight = miscBitmaps[Globals.Graphics.MISC_BUBBLE_RIGHT].getWidth();
		        			X = portraitWidth;
		        			Y = ((mCanvasHeight - tileHeight)/2)/*-portraitHeight*/;
		    	        	canvas.drawBitmap(miscBitmaps[Globals.Graphics.MISC_BUBBLE_LEFT], X, Y,  null);
		    	        	canvas.drawText(Globals.cmdToString(callToShow, bJapanese), X+(bubbleWidth/4), Y+(bubbleHeight/3), textBrush);
		        		}
		        		callToShow = Globals.CMD.PASS;
		        		showHand[playerCalling] = false;
		        	}
        		}
        	}
        	catch(Exception e){
        		String WTFAmI = e.toString();
    			Log.e("SakiThread.doDraw", WTFAmI);
        	}
        }
        
        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;
                centerX = mCanvasWidth / 2;
            	centerY = mCanvasHeight / 2;
            	
            	/*if(width < Globals.Graphics.DEFAULT_WIDTH || height < Globals.Graphics.DEFAULT_HEIGHT){
            		float widthScaler = ((float)width)/((float)Globals.Graphics.DEFAULT_WIDTH);
            		float heightScaler = ((float)height)/((float)Globals.Graphics.DEFAULT_HEIGHT);
            		scaleAll(widthScaler, heightScaler);
            	}*/
                // don't forget to resize the background image
                mBackgroundImage = /*mBackgroundImage*/Bitmap.createScaledBitmap(mBackgroundImage, width, height, true);
            }
        }
        
        public int getWidth(){
        	return mCanvasWidth;
        }
        
        public int getHeight(){
        	return mCanvasHeight;
        }
        
        public int handleButtonPress(float x, float y){
        	int buttonWidth = Buttons[PON_BTN].getWidth();
        	int buttonHeight = Buttons[PON_BTN].getHeight();
        	int buttonX = centerX - (buttonWidth/2);
        	int buttonY = mCanvasHeight - tileHeight - 1 - buttonHeight;
        	
        	if((y > buttonY)&&(y < (buttonY + buttonHeight))){
        		if((x > (buttonX - buttonWidth - 1))&&(x < (buttonX - 1))&&needButtons[RIICHI_BTN]){
        			return Globals.CMD.RIICHI;
        		}
        		if((x > (buttonX - (buttonWidth*2) - 2))&&(x < (buttonX - 2 - (buttonWidth)))&&needButtons[RON_BTN]){
        			return Globals.CMD.RON;
        		}
        		if((x > (buttonX + 1 + buttonWidth))&&(x < (buttonX + 1 + (buttonWidth*2)))&&needButtons[PON_BTN]){
        			return Globals.CMD.PON;
        		}
        		if((x > (buttonX + 2 + (buttonWidth*2)))&&(x < (buttonX + 2 + (buttonWidth*3)))&&needButtons[CHI_BTN]){
        			return Globals.CMD.CHI;
        		}
        		if((x > (buttonX + 3 + (buttonWidth*3)))&&(x < (buttonX + 3 + (buttonWidth*4)))&&needButtons[KAN_BTN]){
        			return Globals.CMD.KAN;
        		}
        		if((x > buttonX)&&(x < (buttonX + buttonWidth))&&needButtons[PASS_BTN]){
        			return Globals.CMD.PASS;
        		}
        		if((x > (buttonX - (buttonWidth*3) - 3))&&(x < (buttonX - 3 - (buttonWidth*2)))&&needButtons[TSUMO_BTN]){
        			return Globals.CMD.TSUMO;
        		}
        		if((x > (buttonX + 3 + (buttonWidth*3)))&&(x < (buttonX + 3 + (buttonWidth*4)))&&needButtons[SELFKAN_BTN]){
        			return Globals.CMD.SELFKAN;
        		}
        	}
        	return -1;
        }
        
        public int handleChiListTouch(float x, float y){
        	int setSize = (2*tileWidth)+3;
    		//canvas.drawBitmap(Buttons[PASS_BTN], centerX - 10, centerY + 10, null);
        	if(y < (centerY-16) || y > (centerY+16))
        		return -1;
        	
    		float startX = 0;
    		if(chiList.size() == 2){
    			 startX = centerX - 5 - setSize;
    			 if(x > startX && x < (startX + setSize))
    				 return 0;
    			 else if(x > (startX + setSize +10) && x < (startX + setSize +10 +  setSize))
    				 return 1;
    		}
    		else{
    			startX = centerX - 10 - (setSize/2) - setSize;
    			if(x > startX && x < (startX + setSize))
   				 	return 0;
   			 	else if(x > (startX + setSize +10) && x < (startX + setSize +10 +  setSize))
   			 		return 1;
   			 else if(x > (startX + setSize + 20 +  setSize) && x < (startX + setSize + 20 +  setSize + setSize))
			 		return 2;
    		}
        	return -1;
        }
        
        public boolean handleTouch(float x, float y){
        	if(bTitleScreen){
    			int logoHeight = miscBitmaps[Globals.Graphics.MISC_LOGO].getHeight();
	        	int btnWidth = miscBitmaps[Globals.Graphics.MISC_START_BTN].getWidth();
	        	int btnHeight = miscBitmaps[Globals.Graphics.MISC_START_BTN].getHeight();
        		if((x > (centerX - (btnWidth/2))) && (x < (centerX - (btnWidth/2) + btnWidth))){
        			if((y > ((centerY/2)+logoHeight+25)) && (y < ((centerY/2)+logoHeight+25+btnHeight))){
        				bTitleScreen = false;
        				return true;
        			}
        				
        		}
	        	return false;
        	}
        	else if(bScoreScreen){
        		if((x > (centerX - (miscBitmaps[Globals.Graphics.MISC_OK_BTN].getWidth()/2))) && (x < (centerX + miscBitmaps[Globals.Graphics.MISC_OK_BTN].getWidth()))){
        			if(y > (mCanvasHeight - miscBitmaps[Globals.Graphics.MISC_OK_BTN].getHeight()-1)){
        				bScoreScreen = false;
        				pGameThread.resumeThread();
        				return true;
        			}
        				
        		}
        		return false;
        	}
        	else{
	        	if(pGameThread.curPlayer != 0)
	        		return false;
	        	cursorX = Math.round(x);
	        	cursorY = Math.round(y);
	        	Log.i("XY=", String.valueOf(x)+"," + String.valueOf(y));
	        	int tileIdx = coordinatesToTile(x, y);
	        	if(tileSelected != tileIdx){
	        		tileSelected = tileIdx;
	        		return true;
	        	}
        	}
        	return false;
        }
        
        public int handleFling(float x, float y, float velocityX, float velocityY){
        	//int tileIdx = coordinatesToTile(x, y);
        	//if(tileIdx == -1)
        	//	return -1;
        	if(tileSelected == -1)
        		return -1;
        	if((velocityX < 25.0)&&(velocityX > -25.0)&&(velocityY < -30)){
        		tileSelected = -1;
        		return tileSelected;
        	}
        	return -1;
        }
        
        public int handleDiscard(float x, float y){
        	return coordinatesToTile(x, y);
        }
        
        public void setGameThread(MainGameThread useThis){
        	Globals.myAssert(useThis != null);
        	pGameThread = useThis;
        }
        
        void scaleAll(float widthScaler, float heightScaler){
        	
        }
        
       /* void transparify(Bitmap thisBitmap){
        	for(int x = 0; x < thisBitmap.getWidth(); x++){
        		for(int y = 0; y < thisBitmap.getHeight(); y++){
            		if(thisBitmap.getPixel(x, y) == Color.MAGENTA)
            			thisBitmap.setPixel(x, y, Color.TRANSPARENT);
            	}
        	}
        }*/
        private int coordinatesToTile(float x, float y){
        	int HandSize = pGameThread.mPlayers[0].myHand.activeHandSize;
        	int tileX = centerX - ((HandSize * tileWidth)/2);
        	int tileY = (mCanvasHeight - tileHeight) - 1;
        	if(y > tileY){
        		int tileIdx = (int)((x - tileX)/(tileWidth));
        		if(tileIdx < HandSize)
        			return tileIdx;
        	}
        	return -1;
        }
       
	}
	
	/** The thread that actually draws the animation */
    private SakiThread thread;
    private GestureDetector mGestureDetector;
    private OnTouchListener mTouchListener;
    
    /**
     * Pointers to other classes
     */
    private MainGameThread pGameThread;
    private StartHere pActivity;
    
    /**
     * Flags for user actions
     */
    private boolean bNeedDiscardInput;
    private boolean bNeedCallInput;
    private boolean bNeedChiInput;

	public SakiView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        setFocusable(true); // make sure we get key events
        
        //Handle all Touch Events
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
					if(bNeedDiscardInput){
						//These thresholds may need to be tweaked
						/*if((velocityX < 25.0)&&(velocityX > -25.0)&&(velocityY < -30)){
							int HandSize = pGameThread.mPlayers[0].myHand.activeHandSize;
							float startPos = (thread.getWidth()-(29*HandSize))/2;
							//Translate (x,y) to tile number
							float y = e1.getY();
							float startPosY = (thread.getHeight() - 33);
							if(y > startPosY){
								float x = e1.getX();
								int tileIdx = ((int)(x - startPos))/29;
								if(tileIdx <= HandSize){
									bNeedDiscardInput = false;
									pGameThread.sendDiscardMessage(tileIdx);
								}
							}
						}*/
						int tileIdx = thread.handleFling(e1.getX(), e1.getY(), velocityX, velocityY);
						if(tileIdx != -1){
							bNeedDiscardInput = false;
							pGameThread.sendDiscardMessage(tileIdx);
						}
					}
					return true;
			}
			public boolean onSingleTapConfirmed(MotionEvent e){
				if(thread.bTitleScreen || thread.bScoreScreen){
					if(thread.handleTouch(e.getX(), e.getY()))
						thread.triggerRedraw();
				}
				else if(bNeedCallInput || bNeedDiscardInput){
					int cmd = thread.handleButtonPress(e.getX(), e.getY());
					if(cmd != -1){
						
						pGameThread.sendCallMessage(cmd);
						bNeedCallInput = false;
						thread.needButtons[thread.PON_BTN] = false;
						thread.needButtons[thread.CHI_BTN] = false;
						thread.needButtons[thread.RON_BTN] = false;
						thread.needButtons[thread.PASS_BTN] = false;
						thread.needButtons[thread.TSUMO_BTN] = false;
						thread.needButtons[thread.KAN_BTN] = false;
						thread.needButtons[thread.RIICHI_BTN] = false;
						thread.needButtons[thread.SELFKAN_BTN] = false;
						//The details of this need to be worked out, but for now we are keeping it simple
					}
				}
				else if(bNeedChiInput){
					int cmd = thread.handleChiListTouch(e.getX(), e.getY());
					if(cmd != -1){
						pGameThread.sendCallMessage(cmd);
						bNeedChiInput = false;
						thread.bNeedChiList = false;
					}
				}
				return true;
			}
			
			public boolean onDoubleTap(MotionEvent e){
				if(bNeedDiscardInput){
					int tileIdx = thread.handleDiscard(e.getX(), e.getY());
					if(tileIdx != -1){
						bNeedDiscardInput = false;
						pGameThread.sendDiscardMessage(tileIdx);
					}
				}
				return true;
			}
			
			//public void onShowPress(MotionEvent e){
			//	if(thread.handleTouch(e.getX(), e.getY()))
			//		thread.triggerRedraw();
			//}
        });
        
        mTouchListener = new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(mGestureDetector.onTouchEvent(event)){
					return true;
				}
				if(event.getAction()==MotionEvent.ACTION_DOWN || event.getAction()==MotionEvent.ACTION_MOVE){
					if(thread.handleTouch(event.getX(), event.getY()))
						thread.triggerRedraw();
					return true;
				}
				else if(event.getAction() == MotionEvent.ACTION_UP){
					if(thread.handleTouch(0, 0))
						thread.triggerRedraw();
					return true;
				}
				else
					return false;
			}
        	
        };

        setOnTouchListener(mTouchListener);
        
    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     * 
     * @return the animation thread
     */
    public SakiThread getThread() {
        return thread;
    }


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		try{
			thread.setSurfaceSize(width, height);
			thread.triggerRedraw();
		}
		catch(Exception e){
			String WTFAmI = e.toString();
			Log.e("SakiView.surfaceChanged", WTFAmI);
		}
		
		//This seems really random but we need to wait for the surface to be created 
		//and drawn before we start the game logic
		if(!pGameThread.isAlive()){
			pGameThread.start();
		}
	}

	 /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        //thread.setRunning(true);
    	// create thread only; it's started in surfaceCreated()
    	if(thread == null){
    		thread = new SakiThread(holder, getContext(), new Handler() {
    			@Override
    			public void handleMessage(Message m) {
                
    			}
    		});
    		thread.setGameThread(pGameThread);
    		thread.doStart();
    	}
    	/**
    	 * This "thread" doesn't really run (long story) so don't bother starting it again
    	 */
    	//if(!thread.isAlive())
    	//	thread.start();
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        while (retry) {
            try {
                //thread.join();
                retry = false;
            } catch (Exception e) {
            	String WTFAmI = e.toString();
    			Log.e("SakiView.surfaceDestroyed", WTFAmI);
            }
        }
    }
    
    public void triggerRedraw(){
    	thread.triggerRedraw();
    }
    
    public void setGameThread(MainGameThread useThis){
    	pGameThread = useThis;
    }
    
    public void setActivity(StartHere thisActivity){
    	pActivity = thisActivity;
    }
    
    public void waitingForDiscardInput(){
    	bNeedDiscardInput = true;
    }
    
    public void waitingForCallInput(boolean pon, boolean chi, boolean kan, boolean ron, boolean tsumo, boolean riichi, boolean selfKan){
    	if(pon || chi || kan || ron || tsumo || riichi || selfKan){
	    	bNeedCallInput = true;
	    	thread.needButtons[thread.PASS_BTN] = true;
	    	if(pon)
	    		thread.needButtons[thread.PON_BTN] = true;
	    	if(chi)
	    		thread.needButtons[thread.CHI_BTN] = true;
	    	if(ron)
	    		thread.needButtons[thread.RON_BTN] = true;
	    	if(tsumo)
	    		thread.needButtons[thread.TSUMO_BTN] = true;
	    	if(kan)
	    		thread.needButtons[thread.KAN_BTN] = true;
	    	if(riichi)
	    		thread.needButtons[thread.RIICHI_BTN] = true;
	    	if(selfKan)
	    		thread.needButtons[thread.SELFKAN_BTN] = true;
    	}
    	else{
    		pGameThread.sendCallMessage(Globals.CMD.PASS);
    	}
    		
    }
    
    public void waitingForChiInput(ArrayList<Set> chiList){
    	bNeedChiInput = true;
    	thread.bNeedChiList = true;
    	thread.chiList = chiList;
    }
    

    public void showScoreScreen(int winnerCharID, int loserCharID1, int loserCharID2, int loserCharID3,int points){
    	thread.winner = winnerCharID;
    	thread.loser1 = loserCharID1;
    	thread.loser2 = loserCharID2;
    	thread.loser3 = loserCharID3;
    	thread.points = points;
    	thread.bScoreScreen = true;
    	thread.triggerRedraw();
    	pGameThread.suspendThread();
    }
    
    public void showCall(int playerCalling, int cmd, boolean showHand){
    	thread.playerCalling = playerCalling;
    	thread.callToShow = cmd;
    	thread.showHand[playerCalling] = showHand;
    }
    
    public void finish(){
    	try{
    		pGameThread.mPlayers[1].myAI.join();
    		pGameThread.mPlayers[2].myAI.join();
    		pGameThread.mPlayers[3].myAI.join();
    		pGameThread.join();
    		thread.join();
    	}
    	catch(Exception e){
    		String WTFAmI = e.toString();
			Log.e("SakiView.finish", WTFAmI);
    	}
    }
    
    public void setLangauge(boolean japanese){
    	thread.bJapanese = japanese;
    	thread.triggerRedraw();
    }
    
    public void setDebug(boolean debug){
    	thread.bDebug = debug;
    	thread.triggerRedraw();
    }

}
