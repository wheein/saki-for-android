package mahjong.riichi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;

public class BrowserScreen extends Activity {
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try{
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        	setContentView(R.layout.web_browser);
        	
        	String type = getIntent().getStringExtra("Type");
        	
        	WebView mWebView = (WebView) findViewById(R.id.webViewID);
            mWebView.getSettings().setJavaScriptEnabled(true);
            
            if(type.equalsIgnoreCase("Rules"))
            	mWebView.loadUrl("http://mahjong.wikidot.com/rules:riichi-competition-rules-overview");
            else
            	mWebView.loadUrl("http://en.wikipedia.org/wiki/Japanese_Mahjong_yaku");
        }
        catch(Exception e){
        	String WhatAmI = e.toString();
        	Log.e("ChangeLogScreen", WhatAmI);
        }
	}
}
