package com.sunnykwong.omc;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ClockWidget2x2 extends AppWidgetProvider {
	
	public ClockWidget2x2() {
		super();
	}

	//	When one or more widgets are removed...
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		for (int i=0; i<N; i++) {
			//Remove Prefs
			if (OMC.DEBUG)Log.i(OMC.OMCSHORT + "Widget","Removed Widget #" + appWidgetIds[i]);
			OMC.removePrefs(appWidgetIds[i]);
	    	if (OMC.WIDGETBMPMAP.containsKey(appWidgetIds[i])) {
	    		if (!OMC.WIDGETBMPMAP.get(appWidgetIds[i]).isRecycled()) OMC.WIDGETBMPMAP.get(appWidgetIds[i]).recycle();
	    		OMC.WIDGETBMPMAP.remove(appWidgetIds[i]);
	    	}
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			Bundle extras = intent.getExtras();
			int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		}
		else {
			// Set time for output
	        OMCWidgetDrawEngine.updateAppWidget(context, OMC.WIDGET2x2CNAME);

			super.onReceive(context, intent);
		}
	}

	//	This should never fire since I implemented onReceive.
	@Override
	public void onUpdate(Context context, AppWidgetManager aWM, int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		for (int i=0; i<N; i++) {
		  	OMC.initPrefs(appWidgetIds[i]);
		  	OMCWidgetDrawEngine.updateAppWidget(context, aWM, appWidgetIds[i],OMC.WIDGET2x2CNAME);
		}

	}
	
}