package com.sunnykwong.omc;

import java.io.BufferedInputStream;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.text.format.Time;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class OMCWeatherForecastActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Hide the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(getResources().getIdentifier("weatherforecast", "layout", OMC.PKGNAME));
		if (OMC.PREFS.getString("weathersetting", "bylatlong").equals("disabled")) {
			Toast.makeText(this, "Weather Disabled, or No Weather Loaded!", Toast.LENGTH_LONG);
			finish();
		} else {
			try {
				final JSONObject weather = new JSONObject(OMC.PREFS.getString("weather", ""));
				String sWeatherDigits = "--";
				if (OMC.PREFS.getString("weatherdisplay", "f").equals("f")) {
					sWeatherDigits = weather.optString("temp_f", "--")+"�F";
				} else {
					sWeatherDigits = weather.optString("temp_c", "--")+"�C";
				}
				float fStretch = Math.min(1f,120f/(sWeatherDigits.length()*40f));
				setText(findViewById(getResources().getIdentifier("CurrTemp", "id", OMC.PKGNAME)),sWeatherDigits,fStretch);
	
				String sCity = weather.optString("city") + ",\n" + weather.optString("country2")+" ";
				fStretch = Math.min(1f,800f/(sCity.length()*40f));
				setText(findViewById(getResources().getIdentifier("City", "id", OMC.PKGNAME)),sCity,fStretch);
				setText(findViewById(getResources().getIdentifier("Conditions", "id", OMC.PKGNAME)),weather.optString("condition") + " | " + weather.optString("wind_condition") + " " + weather.optString("humidity"));
				Time t = new Time();
				t.set(System.currentTimeMillis());
				String sTimeOfDay="day";
				if (t.hour>=6 && t.hour<=18) {
					//Daytime
				} else {
					//Nighttime
					sTimeOfDay="night";
				}
				((ImageView)findViewById(getResources().getIdentifier("ConditionImage", "id", OMC.PKGNAME))).setImageBitmap(OMC.getBitmap(OMC.DEFAULTTHEME, "w-"+weather.optString("condition_lcase","--")+"-"+sTimeOfDay+".png"));
				JSONArray wary = weather.optJSONArray("zzforecast_conditions");
				for (int i=0; i<wary.length(); i++) {
					JSONObject day = wary.optJSONObject(i);
					setText(findViewById(getResources().getIdentifier("dayofweek"+i, "id", OMC.PKGNAME)),day.optString("day_of_week"));
					((ImageView)findViewById(getResources().getIdentifier("ConditionImage"+i, "id", OMC.PKGNAME))).setImageBitmap(OMC.getBitmap(OMC.DEFAULTTHEME, "w-"+day.optString("condition_lcase","--")+"-day.png"));
					setText(findViewById(getResources().getIdentifier("ForecastCond"+i, "id", OMC.PKGNAME)),day.optString("condition"));
					if (OMC.PREFS.getString("weatherdisplay", "f").equals("f")) {
						setText(findViewById(getResources().getIdentifier("HighTemp"+i, "id", OMC.PKGNAME)),day.optString("high")+"�F");
						setText(findViewById(getResources().getIdentifier("LowTemp"+i, "id", OMC.PKGNAME)),day.optString("low")+"�F");
					} else {
						setText(findViewById(getResources().getIdentifier("HighTemp"+i, "id", OMC.PKGNAME)),day.optString("high_c")+"�C");
						setText(findViewById(getResources().getIdentifier("LowTemp"+i, "id", OMC.PKGNAME)),day.optString("low_c")+"�C");
					}
//				    TextView gradient = (TextView)findViewById(getResources().getIdentifier("divider"+i, "id", OMC.PKGNAME));
//				    final int iDay = i;
//				    gradient.setOnClickListener(new View.OnClickListener() {
//						
//						@Override
//						public void onClick(View v) {
//							Intent accu = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.accuweather.com/m/Details"+iDay+".aspx?lat="+weather.optLong("latitude_e6",0l)/1000000d
//									+ "&lon=" + weather.optLong("longitude_e6",0l)/1000000d));
//							startActivity(accu);
//							finish();
//						}
//					});
				}
				Time tStation = new Time();
				tStation.parse(weather.optString("current_local_time","19700101T000000"));
				if (tStation.year < 1980) {
					tStation.set(OMC.LASTWEATHERREFRESH);
					setText(findViewById(getResources().getIdentifier("LastUpdate", "id", OMC.PKGNAME)),"Weather updated from Google API at " + tStation.format("%R")  );
				} else {
					setText(findViewById(getResources().getIdentifier("LastUpdate", "id", OMC.PKGNAME)),"Weather recorded by Google API at " + tStation.format("%R") );
					
				}
				TextView acculink = ((TextView)findViewById(getResources().getIdentifier("AccuLink", "id", OMC.PKGNAME)));
				acculink.setText("Tap for Alternate Forecasts from AccuWeather�");
				acculink.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent accu = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.accuweather.com/m/Forecast.aspx?lat="+weather.optLong("latitude_e6",0l)/1000000d
								+ "&lon=" + weather.optLong("longitude_e6",0l)/1000000d));
						startActivity(accu);
						finish();
					}
				});
			} catch (Exception e) {
				Toast.makeText(this, "Weather Disabled, or No Weather Loaded!", Toast.LENGTH_LONG);
				e.printStackTrace();
				finish();
			}
		}
	}			

	public void setText(View vw, String sText) {
		setText(vw,sText,1f);
	}	
	
	public void setText(View vw, String sText, float stretchX) {
		TextView tv = (TextView)vw;
		tv.setTextScaleX(stretchX);
		tv.setText(sText);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		finish();
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		finish();
		return super.onTouchEvent(event);
	}
}