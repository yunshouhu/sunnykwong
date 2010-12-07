package com.sunnykwong.omc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.widget.Toast;


public class OMCPrefActivity extends PreferenceActivity implements OnPreferenceChangeListener{ 
    /** Called when the activity is first created. */
    static int appWidgetID;
    static AlertDialog mAD;
    Preference prefloadThemeFile, prefclearCache, prefclearImports, prefdownloadStarterPack, prefbSkinner ;

    @Override
    public void onCreate(Bundle savedInstanceState) {

    	super.onCreate(savedInstanceState);
        
		if (getIntent().getData() == null)
			appWidgetID=-999;
		else
			appWidgetID = Integer.parseInt(getIntent().getData().getSchemeSpecificPart());

		if (appWidgetID >= 0) {
			if (OMC.DEBUG) Log.i("OMCPref","Called by Widget " + appWidgetID);
        	OMC.getPrefs(appWidgetID);
        	OMC.PREFS.edit().putBoolean("widgetPersistence", OMC.FG).commit();
    		if (OMC.FREEEDITION) {
    			OMC.PREFS.edit().putBoolean("bFourByOne", false)
    					.putBoolean("bThreeByOne", false)
    					.putBoolean("bTwoByOne", false)
    					.commit();
    		}
    		this.getPreferenceManager().setSharedPreferencesName(OMC.SHAREDPREFNAME);
        	addPreferencesFromResource(getResources().getIdentifier("omcprefs", "xml", OMC.PKGNAME));

        	prefloadThemeFile = findPreference("loadThemeFile");
        	prefclearCache = findPreference("clearCache");
        	prefclearImports = findPreference("clearImports");
        	prefdownloadStarterPack = findPreference("downloadStarterPack");
        	prefbSkinner = findPreference("bSkinner");
        	
        	if (OMC.SINGLETON) {
        		((PreferenceScreen)findPreference("rootPrefs")).removePreference(prefloadThemeFile);
        		((PreferenceScreen)findPreference("rootPrefs")).removePreference(prefclearCache);
        		((PreferenceScreen)findPreference("rootPrefs")).removePreference(prefclearImports);
        		((PreferenceScreen)findPreference("rootPrefs")).removePreference(prefdownloadStarterPack);
        		((PreferenceScreen)findPreference("rootPrefs")).removePreference(prefbSkinner);
        		((PreferenceScreen)findPreference("rootPrefs")).setTitle(OMC.SINGLETONNAME + " - Preferences");
        	}
        	
        	if (OMC.FREEEDITION) {
        		findPreference("sVersion").setTitle("Version " + OMC.THISVERSION + " Free");
        		findPreference("sVersion").setSummary("Tap me to get the full version!");
        		findPreference("sVersion").setSelectable(true);
        	} else {
        		findPreference("sVersion").setTitle("Version " + OMC.THISVERSION);
        		findPreference("sVersion").setSummary("Thanks for supporting Xaffron!");
        		findPreference("sVersion").setSelectable(false);
        	}
        	//findPreference("widgetTheme").setOnPreferenceChangeListener(this);
        	findPreference("clearImports").setOnPreferenceChangeListener(this);
    		findPreference("bFourByTwo").setEnabled(false);
    		if (OMC.FREEEDITION) {
        		findPreference("bFourByOne").setEnabled(false); 
        		findPreference("bThreeByOne").setEnabled(false);
        		findPreference("bTwoByOne").setEnabled(false);
        		findPreference("sVersion").setEnabled(true);
    		}
        } else {
            // If they gave us an intent without the widget id, just bail.
        	if (OMC.DEBUG) Log.i("OMCPref","Called by Launcher - do nothing");
        	OMCPrefActivity.mAD = new AlertDialog.Builder(this)
        		.setTitle("Thanks for downloading!")
        		.setMessage("To begin, hit the back button to go back to the home screen, then push the menu button, select 'Add', then 'Widgets' to see your newly-downloaded widget listed.  Have fun!")
        	    .setCancelable(true)
        	    .setIcon(getResources().getIdentifier("fredicon_mdpi", "drawable", OMC.PKGNAME))
        	    .setOnKeyListener(new OnKeyListener() {
        	    	public boolean onKey(DialogInterface arg0, int arg1, android.view.KeyEvent arg2) {
        	    		dialogCancelled();
        	    		return true;
        	    	};
        	    }).create();
        	OMCPrefActivity.mAD.show();

        	((OMC)getApplication()).widgetClicks();
        	
        }

    }

    // If user sets a seeded theme, set external to false
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

//    	if (preference == findPreference("widgetTheme")) {
//	    	if (OMC.DEBUG) Log.i("OMCPref","Setting External to false");
//			OMC.PREFS.edit().putBoolean("external", false).commit();
//	    	return true;
//    	}
    	
    	return false;
    }
    
    // If user clicks on a preference...
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
    		Preference preference) {
    	if (preference == getPreferenceScreen().findPreference("widgetPrefs") && OMC.FREEEDITION) {
        	OMCPrefActivity.mAD = new AlertDialog.Builder(this)
			.setCancelable(true)
			.setTitle("Why are the widgets so big?")
			.setMessage("Actually, the donate version offers widget sizes of 4x2, 4x1, 3x1 and 2x1.\nPlease consider upgrading to get these sizes!\nAlternatively, if you use an alternative launcher such as ADW or Launcher Pro, you should be able to approximate this ability by dynamically resizing the 4x2 widget.")
			.setPositiveButton("Take me to the donate version!", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						OMCPrefActivity.mAD.dismiss();
						OMCPrefActivity.this.startActivity(OMC.OMCMARKETINTENT);
						OMCPrefActivity.this.finish();
						
					}
				}).create();
        	OMCPrefActivity.mAD.show();
    	}
    	if (preference == getPreferenceScreen().findPreference("widgetCredits")) {
    		startActivityForResult(OMC.CREDITSINTENT,0);
    	}
    	if (preference == getPreferenceScreen().findPreference("sVersion")) {
			this.startActivity(OMC.OMCMARKETINTENT);
        	this.finish();
    	}
    	if (preference == getPreferenceScreen().findPreference("downloadStarterPack")) {
        	OMCPrefActivity.mAD = new AlertDialog.Builder(this)
			.setCancelable(true)
			.setTitle("Starter Clock Pack")
			.setMessage("Any theme customizations you have made in your sdcard's OMC folder will be overwriten.  Are you sure?\n(If not sure, tap Yes)")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
		        	Toast.makeText(OMCPrefActivity.this, "Downloading starter clock pack...", Toast.LENGTH_LONG).show();
//		        	OMCThemePickerActivity.THEMEROOT.mkdir();
					startActivity(OMC.GETSTARTERPACKINTENT);
					mAD.cancel();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mAD.cancel();
				}
			})
			.create();
        	mAD.show();
    	}
    	if (preference == getPreferenceScreen().findPreference("loadThemeFile")) {
    		startActivityForResult(OMC.IMPORTTHEMEINTENT,0);
    	}
    	if (preference == getPreferenceScreen().findPreference("oTTL")) {
    		OMCPrefActivity.this.getPreferenceScreen().setEnabled(false);
    		Intent mainIntent = new Intent(Intent.ACTION_MAIN,
        			null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

			Intent pickIntent = new
			Intent(Intent.ACTION_PICK_ACTIVITY);
			pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
			startActivityForResult(pickIntent, OMCPrefActivity.appWidgetID);
			mainIntent=null;
			pickIntent=null;
    	}
    	if (preference == getPreferenceScreen().findPreference("clearCache")) {
    		OMC.purgeTypefaceCache();
    		Toast.makeText(this, "Font Cache Cleared", Toast.LENGTH_SHORT).show();
    		OMC.purgeBitmapCache();
    		Toast.makeText(this, "Bitmap Cache Cleared", Toast.LENGTH_SHORT).show();
    	}
    	if (preference == findPreference("clearImports")) {
        	OMCPrefActivity.mAD = new AlertDialog.Builder(this)
    		.setTitle("Warning!")
    		.setMessage("Clearing the Import cache will revert all your custom clocks to stock look.  Are you sure?")
    	    .setCancelable(true)
    	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					OMC.clearImportCache();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					OMCPrefActivity.mAD.dismiss();
				}
			})
    	    .setIcon(getResources().getIdentifier("fredicon_mdpi", "drawable", OMC.PKGNAME))
    	    .setOnKeyListener(new OnKeyListener() {
    	    	public boolean onKey(DialogInterface arg0, int arg1, android.view.KeyEvent arg2) {
					OMCPrefActivity.mAD.dismiss();
    	    		return true;
    	    	};
    	    }).create();
        	OMCPrefActivity.mAD.show();
    	}
    	return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    // The result is obtained in onActivityResult:
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		getPreferenceScreen().setEnabled(true);
		// If it's an independent child activity, do nothing
		if (requestCode == 0) return;
		if (data != null) {
			String s = data.toUri(MODE_PRIVATE).toString();
			OMC.PREFS.edit().putString("URI", s).commit();
		}
	}

    
    public void dialogCancelled() {
   	if (OMCPrefActivity.mAD!=null) { // && mAD.isShowing()
   		OMCPrefActivity.mAD.dismiss();
   		OMCPrefActivity.mAD = null;
   	}
    	finish();
    }

    @Override
    public void onPause() {
    	super.onPause();
    }

    @Override
    public void onDestroy() {
		if (appWidgetID >= 0) {

			if (OMC.DEBUG) Log.i("OMCPref","Saving Prefs for Widget " + OMCPrefActivity.appWidgetID);
			OMC.FG = OMC.PREFS.getBoolean("widgetPersistence", true)? true : false;
			OMC.UPDATEFREQ = OMC.PREFS.getInt("iUpdateFreq", 30) * 1000;
	    	OMC.setPrefs(OMCPrefActivity.appWidgetID);

	    	OMC.toggleWidgets(getApplicationContext());

			OMC.setServiceAlarm(System.currentTimeMillis()+500);
			sendBroadcast(OMC.WIDGETREFRESHINTENT);
		}
        super.onDestroy();
    }
} 