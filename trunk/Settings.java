package mahjong.riichi;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

public class Settings extends PreferenceActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            PreferenceManager myPrefMan = getPreferenceManager();
            //Log.i("Activity File:", myPrefMan.getSharedPreferencesName().toString());
    }
	
}
