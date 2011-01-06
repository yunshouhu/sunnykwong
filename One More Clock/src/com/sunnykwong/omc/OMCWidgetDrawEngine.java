package com.sunnykwong.omc;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Html;
import android.text.SpannedString;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class OMCWidgetDrawEngine {

	// Needs to be synchronized now that we have four different widget types 
	// calling this same method creating a potential race condition
	static synchronized void updateAppWidget(Context context, ComponentName cName) {
		if (!OMCService.RUNNING) {
			OMC.setServiceAlarm(System.currentTimeMillis() + 10000);
		}
		AppWidgetManager aWM = AppWidgetManager.getInstance(context);

		final int N = aWM.getAppWidgetIds(cName)==null? 0: aWM.getAppWidgetIds(cName).length;

		for (int i=0; i<N; i++) {
			OMCWidgetDrawEngine.updateAppWidget(context, aWM, aWM.getAppWidgetIds(cName)[i], cName);
		}
	}
	
	static synchronized void updateAppWidget(final Context context,
			final AppWidgetManager appWidgetManager,
			final int appWidgetId, ComponentName cName) { 

		if (OMC.DEBUG)Log.i(OMC.OMCSHORT + "Widget", "Redrawing widget" + appWidgetId + " @ " + OMC.TIME.format("%T"));

		String sTheme = OMC.PREFS.getString("widgetTheme"+appWidgetId,OMC.DEFAULTTHEME);

		// OK, now actually render the widget on a bitmap.
		OMCWidgetDrawEngine.drawBitmapForWidget(context,appWidgetId);

		// Blit the buffer over
		final RemoteViews rv = new RemoteViews(context.getPackageName(),context.getResources().getIdentifier("omcwidget", "layout", OMC.PKGNAME));

		OMC.STRETCHINFO = OMC.getTheme(context,sTheme).optJSONObject("customscaling");
		String sWidgetSize = cName.toShortString().substring(cName.toShortString().length()-4,cName.toShortString().length()-1);

		//look for this size's custom scaling info
//		OMC.STRETCHINFO = OMC.STRETCHINFO.optJSONObject(sWidgetSize);
//		if (OMC.STRETCHINFO != null){
//			//Custom scaling
//			OMC.TEMPMATRIX.reset();
//			OMC.TEMPMATRIX.postScale((float)OMC.STRETCHINFO.optDouble("horizontal_stretch"), 
//					(float)OMC.STRETCHINFO.optDouble("vertical_stretch"));
//			OMC.TEMPMATRIX.postRotate((float)OMC.STRETCHINFO.optDouble("cw_rotate"));
//			rv.setImageViewBitmap(context.getResources().getIdentifier("omcIV", "id", OMC.PKGNAME),
//					Bitmap.createBitmap(OMC.BUFFER, 
//							OMC.STRETCHINFO.optInt("left_crop"), 
//							OMC.STRETCHINFO.optInt("top_crop"), 
//							OMC.BUFFER.getWidth()- OMC.STRETCHINFO.optInt("left_crop") - OMC.STRETCHINFO.optInt("right_crop"), 
//							OMC.BUFFER.getHeight() - OMC.STRETCHINFO.optInt("top_crop") - OMC.STRETCHINFO.optInt("bottom_crop"), 
//							OMC.TEMPMATRIX, true));
//		} else {
			//Default scaling
			OMC.TEMPMATRIX.reset();
	//		rv.setImageViewBitmap(context.getResources().getIdentifier("omcIV", "id", OMC.PKGNAME),Bitmap.createBitmap(OMC.BUFFER, 0, 0, OMC.BUFFER.getWidth(), OMC.BUFFER.getHeight(), OMC.TEMPMATRIX, true));
			rv.setImageViewBitmap(context.getResources().getIdentifier("omcIV", "id", OMC.PKGNAME),OMC.BUFFER);
//		}
		
		OMC.OVERLAYURIS = new String[9];
		JSONObject temp = OMC.getTheme(context, sTheme).optJSONObject("customURIs");
		if (temp == null) {
			//No custom URIs - always go to options screen
        	Intent intent = new Intent(context, OMCPrefActivity.class);
        	intent.setData(Uri.parse("omc:"+appWidgetId));

			for (int i=0; i<9; i++){
				OMC.OVERLAYURIS[i] = intent.toUri(0);
			}
			
		} else {
			for (int i=0; i<9; i++){
				OMC.OVERLAYURIS[i] = temp.optString(OMC.COMPASSPOINTS[i]);
			}
		}

        if (OMC.PREFS.getString("URI"+appWidgetId, "").equals("")) { 
////	Using a broadcast is more flexible, but less crash-proof. So we're not using it for now.
////        	Intent intent = new Intent("com.sunnykwong.omc.WIDGET_CONFIG");
////        	intent.setData(Uri.parse("omc:"+appWidgetId));
////        	PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        	Intent intent = new Intent(context, OMCPrefActivity.class);
//        	intent.setData(Uri.parse("omc:"+appWidgetId));
//        	PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
//        	
//        	rv.setOnClickPendingIntent(context.getResources().getIdentifier("omcIV", "id", OMC.PKGNAME), pi);
//            for (int i = 0; i < 9; i++) {
//            	if (OMC.OVERLAYURI[i].equals("default")) {
//	            	rv.setOnClickPendingIntent(OMC.OVERLAYRESOURCES[i], pi);
//            	} else {
//    				Intent s = (new Intent(Intent.ACTION_VIEW,OMC.OVERLAYURI[i]));
//    				s.addCategory(Intent.CATEGORY_DEFAULT);
//
//            		rv.setOnClickPendingIntent(OMC.OVERLAYRESOURCES[i],            	
//            			PendingIntent.getActivity(context, 0, s,0));
//            	}
//            }
//        } else {
//
        	try {
//	        	Intent intent = Intent.parseUri(OMC.PREFS.getString("URI"+appWidgetId, ""), 0);
	        	Intent intent = new Intent(context, OMCPrefActivity.class);
	        	intent.setData(Uri.parse("omc:"+appWidgetId));
	        	PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
//	            rv.setOnClickPendingIntent(context.getResources().getIdentifier("omcIV", "id", OMC.PKGNAME), pi);
	            for (int i = 0; i < 9; i++) {
//	            	if (OMC.OVERLAYURI[i].equals("default")) {
		            	rv.setOnClickPendingIntent(OMC.OVERLAYRESOURCES[i], pi);
//	            	} else rv.setOnClickPendingIntent(OMC.OVERLAYRESOURCES[i], 
//	            			PendingIntent.getBroadcast(context, 0, new Intent(
//	            					Intent.ACTION_VIEW,OMC.OVERLAYURI[i]), 0));
	            }
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
//        }
        	
        	if (OMC.PREFS.getBoolean("newbie" + appWidgetId, true)) {
        		System.out.println("Adding newbie ribbon to widget "+ appWidgetId + ".");
        		rv.setInt(OMC.OVERLAYRESOURCES[0], "setBackgroundResource", context.getResources().getIdentifier("tapme", "drawable", OMC.PKGNAME));
        	} else {
        		rv.setInt(OMC.OVERLAYRESOURCES[0], "setBackgroundResource", context.getResources().getIdentifier("transparent", "drawable", OMC.PKGNAME));
        	}
        
            // Kudos to Eric for solution to dummy out "unsetonlickpendingintent":
            // http://groups.google.com/group/android-developers/browse_thread/thread/f9e80e5ce55bb1e0/78153eb730326488
        	// I'm not using it right now, but it's a useful hint nonetheless. Thanks!
//        	rv.setOnClickPendingIntent(R.id.omcLink, PendingIntent.getBroadcast(context, 0, OMC.DUMMYINTENT,
//        		    PendingIntent.FLAG_UPDATE_CURRENT));
        
        appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
	}
	
	static synchronized Bitmap drawBitmapForWidget(final Context context, final int aWI) {
		OMC.BUFFER.eraseColor(Color.TRANSPARENT);

		final String sTheme = OMC.PREFS.getString("widgetTheme"+aWI,OMC.DEFAULTTHEME);

		JSONObject oTheme = OMC.getTheme(context, sTheme);
		if (oTheme==null) {
			Toast.makeText(context, "Error loading theme.\nRestoring default look...", Toast.LENGTH_SHORT).show();
			OMC.PREFS.edit()
					.putString("widgetTheme"+aWI,OMC.DEFAULTTHEME)
					.commit();
			return null;
		}
		try {
			oTheme = OMCTypedArray.renderThemeObject(oTheme, aWI);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		OMC.LAYERLIST = oTheme.optJSONArray("layers_bottomtotop");

		for (int i = 0; i < OMC.LAYERLIST.length(); i++) {
			JSONObject layer = OMC.LAYERLIST.optJSONObject(i);
			if (layer==null) {
				Toast.makeText(context, "Error loading theme.\nRestoring default look...", Toast.LENGTH_SHORT).show();
				OMC.PREFS.edit()
						.putString("widgetTheme"+aWI,OMC.DEFAULTTHEME)
						.commit();
				return null;
			}
			// Clear the text buffer first.
			OMC.TXTBUF="";
			
			//Skip any disabled layers
			if (layer.optBoolean("enabled")==false) continue;
			
			String sType = layer.optString("type");
			
			if (sType.equals("text")) {
				OMC.TXTBUF = layer.optString("text");
				OMCWidgetDrawEngine.drawTextLayer(context, layer, sTheme, aWI);
			}
			else if (sType.equals("panel"))OMCWidgetDrawEngine.drawPanelLayer(context, layer, sTheme, aWI);
			else if (sType.equals("flare"))OMCWidgetDrawEngine.drawFlareLayer(context, layer, sTheme, aWI);
			else if (sType.equals("quote"))OMCWidgetDrawEngine.drawQuoteLayer(context, layer, sTheme, aWI);
			else if (sType.equals("image"))OMCWidgetDrawEngine.drawBitmapLayer(context, layer, sTheme, aWI);

		}
		
		return Bitmap.createBitmap(OMC.BUFFER);
		
	}

	// This is where the theme-specific tweaks (regardless of layer) are processed.
	// Tweaks = hacks, but at least all the hacks are in one block of code.
	static void layerThemeTweaks(final Context context, final JSONObject layer, final String sTheme, final int aWI) {
		// do nothing
	}

	// This layer is pretty much dedicated to lens flare (bokeh beauty), 
	// but will need more tweaking for realism 
	static void drawFlareLayer(final Context context, final JSONObject layer, final String sTheme, final int aWI) {
		OMC.PT1.reset();
		OMC.PT1.setAntiAlias(true);
		OMC.PT1.setStyle(Paint.Style.FILL_AND_STROKE);
		OMC.PT2.reset();
		OMC.PT2.setAntiAlias(true);
		OMC.PT2.setARGB(32, 255, 255, 255);
		OMC.PT2.setStyle(Paint.Style.STROKE);

		//Depending on time of day, endpoints.
		final float ratio = (((OMC.TIME.hour+6)*60 + OMC.TIME.minute) % (12*60)) / (12f*60);
		final float x1 = OMC.WIDGETWIDTH * ratio;  
		final float y1 = OMC.WIDGETHEIGHT;
		final float x2 = OMC.WIDGETWIDTH - x1;
		final float y2 = 0;
		
		float dist = -0.45f;
		for (int i = 0; i < layer.optInt("number_circles"); i++) {
			OMC.PT1.setColor(OMC.FLARECOLORS[i]);
			dist += (float)(1.f/layer.optInt("number_circles")); 
			float x = (x2-x1) * dist + OMC.WIDGETWIDTH/2f;
			float y = (y2-y1) * dist + OMC.WIDGETHEIGHT/2f;
			OMC.CANVAS.drawCircle(x, y, OMC.FLARERADII[i], OMC.PT1);
			OMC.CANVAS.drawCircle(x, y, OMC.FLARERADII[i]+1, OMC.PT2);
		}
		
    	// theme-specific tweaks.
		OMCWidgetDrawEngine.layerThemeTweaks(context, layer, sTheme, aWI);
	}

	// Static rectangular panel.
	static void drawPanelLayer(final Context context, final JSONObject layer, final String sTheme, final int aWI) {
		try {
			OMC.FGRECT.left = (float)layer.getDouble("left");
			OMC.FGRECT.top = (float)layer.getDouble("top");
			OMC.FGRECT.right = (float)layer.getDouble("right");
			OMC.FGRECT.bottom = (float)layer.getDouble("bottom");
		} catch (JSONException e) {
			Log.w(OMC.OMCSHORT + "Engine", " (panel) is missing left/top/right/bottom values!  Giving up.");
			if (OMC.DEBUG) e.printStackTrace();
			return;
		}
		OMC.PT1.reset();
		OMC.PT1.setAntiAlias(true);
		OMC.PT1.setColor(Color.parseColor(layer.optString("fgcolor")));
		OMC.PT2.reset();
		OMC.PT2.setAntiAlias(true);
		OMC.PT2.setColor(Color.parseColor(layer.optString("bgcolor")));

    	// theme-specific tweaks.
		OMCWidgetDrawEngine.layerThemeTweaks(context, layer, sTheme, aWI);
    	
		//Draw the SFX
		if (layer.optString("render_style").equals("emboss")) {
			OMC.BGRECT.left = OMC.FGRECT.left-1;
			OMC.BGRECT.top = OMC.FGRECT.top-1;
			OMC.BGRECT.right = OMC.FGRECT.right-1;
			OMC.BGRECT.bottom = OMC.FGRECT.bottom-1;
			OMC.CANVAS.drawRoundRect(OMC.BGRECT, layer.optInt("xcorner"), layer.optInt("ycorner"), OMC.PT2);
			OMC.BGRECT.left+=2;
			OMC.BGRECT.top+=2;
			OMC.BGRECT.right+=2;
			OMC.BGRECT.bottom+=2;
			OMC.CANVAS.drawRoundRect(OMC.BGRECT, layer.optInt("xcorner"), layer.optInt("ycorner"), OMC.PT2);
		} else if (layer.optString("render_style").equals("shadow")) {
			OMC.BGRECT.left = OMC.FGRECT.left+3;
			OMC.BGRECT.top = OMC.FGRECT.top+3;
			OMC.BGRECT.right = OMC.FGRECT.right+3;
			OMC.BGRECT.bottom = OMC.FGRECT.bottom+3;
			OMC.CANVAS.drawRoundRect(OMC.BGRECT, layer.optInt("xcorner"), layer.optInt("ycorner"), OMC.PT2);
		}
		//Either way, draw the proper panel
		OMC.CANVAS.drawRoundRect(OMC.FGRECT, layer.optInt("xcorner"), layer.optInt("ycorner"), OMC.PT1);

	}

	//Bitmap layer.  This is really only for skinning.
	static void drawBitmapLayer(final Context context, final JSONObject layer, final String sTheme, final int aWI) {
		OMC.PT1.reset();
		OMC.PT1.setAntiAlias(true);

    	// theme-specific tweaks.
		OMCWidgetDrawEngine.layerThemeTweaks(context, layer, sTheme, aWI);
		
		// Blit the buffer over
		Bitmap tempBitmap = OMC.getBitmap(sTheme, layer.optString("filename"));
		if (tempBitmap==null) {
			Toast.makeText(context, "Theme not found in memory or in cache directory.\nPlease go to settings screen and re-select your theme.", Toast.LENGTH_SHORT).show();
			OMC.PREFS.edit()
					.putString("widgetTheme"+aWI,OMC.DEFAULTTHEME)
					.commit();
			return;
		}

		// Prepare the transformation matrix.
		
		OMC.TEMPMATRIX.reset();
		OMC.TEMPMATRIX.postTranslate(-tempBitmap.getWidth()/2f, -tempBitmap.getHeight()/2f);
		OMC.TEMPMATRIX.postScale((float)layer.optDouble("horizontal_stretch"),(float)layer.optDouble("vertical_stretch"));
		OMC.TEMPMATRIX.postRotate((float)layer.optDouble("cw_rotate"));

		OMC.TEMPMATRIX.postTranslate((tempBitmap.getWidth()*(float)layer.optDouble("horizontal_stretch"))/2f
				+ layer.optInt("x"), 
				(tempBitmap.getHeight()*(float)layer.optDouble("vertical_stretch"))/2f
				+ layer.optInt("y"));

		tempBitmap.setDensity(DisplayMetrics.DENSITY_HIGH);
		OMC.CANVAS.drawBitmap(tempBitmap,OMC.TEMPMATRIX,OMC.PT1);
	}

	// Quote layer.  Set the Text to be shown before passing to drawTextLayer.
	static void drawQuoteLayer(final Context context, final JSONObject layer, final String sTheme, final int aWI) {
		OMC.TALKBACKS = OMC.loadStringArray(sTheme, aWI, layer.optString("array"));

		if (OMC.TALKBACKS == null) OMC.TXTBUF="Tap me to add quotes!";
		else OMC.TXTBUF = OMC.TALKBACKS.optString(OMC.RND.nextInt(OMC.TALKBACKS.length()));
		OMCWidgetDrawEngine.drawTextLayer(context, layer, sTheme, aWI);
		OMC.TALKBACKS=null;
	}

	// Text layer.  Written this way so we can have as many as we want with minimal effort.
	static void drawTextLayer(final Context context, final JSONObject layer, final String sTheme, final int aWI) {
		OMC.PT1.reset();
		OMC.PT1.setAntiAlias(true);
		Typeface tempTypeface = OMC.getTypeface(sTheme, layer.optString("filename"));
		if (tempTypeface==null) {
			Toast.makeText(context, "Error loading theme typeface.\nRestoring default look...", Toast.LENGTH_SHORT).show();
			OMC.PREFS.edit()
					.putString("widgetTheme"+aWI,OMC.DEFAULTTHEME)
					.commit();
			return;
		}
		OMC.PT1.setTypeface(tempTypeface);
		OMC.PT1.setTextSize(layer.optInt("text_size"));
		OMC.PT1.setTextSkewX((float)layer.optDouble("text_skew"));
		String sTemp = layer.optString("text_stretch");
		if (sTemp==null) {
			OMC.PT1.setTextScaleX(1f);
		} else {
			int iTemp;
			if (sTemp.startsWith("f")) {
				OMC.PT1.setTextScaleX(1f);
				float fFactor = Float.parseFloat(sTemp.substring(1))/OMCWidgetDrawEngine.getSpannedStringWidth(new SpannedString(Html.fromHtml(OMC.TXTBUF)),OMC.PT1);
				OMC.PT1.setTextScaleX(fFactor);
			} else if ((iTemp = sTemp.indexOf("m"))!= -1) {
				OMC.PT1.setTextScaleX(Float.parseFloat(sTemp.substring(0,iTemp)));
				int iMax = Integer.parseInt(sTemp.substring(iTemp+1));
				int iLength = OMCWidgetDrawEngine.getSpannedStringWidth(new SpannedString(Html.fromHtml(OMC.TXTBUF)),OMC.PT1); 
				if (iLength <= iMax){
					//do nothing, PT1 properly set
				} else {
					OMC.PT1.setTextScaleX(((float)iMax)/iLength);
				}
			} else {
				OMC.PT1.setTextScaleX((float)layer.optDouble("text_stretch"));
			}
		}
		
		OMC.PT1.setColor(Color.parseColor(layer.optString("fgcolor")));
		
		float fRot = (float)layer.optDouble("cw_rotate");

		if (layer.optString("text_align").equals("center")) {
			OMC.PT1.setTextAlign(Paint.Align.CENTER);
		} else if (layer.optString("text_align").equals("left")) {
			OMC.PT1.setTextAlign(Paint.Align.LEFT);
		} else if (layer.optString("text_align").equals("right")) {
			OMC.PT1.setTextAlign(Paint.Align.RIGHT);
		};

		OMC.PT2.reset();
		OMC.PT2.setAntiAlias(true);
		OMC.PT2.setTypeface(tempTypeface);
		OMC.PT2.setTextSize(OMC.PT1.getTextSize());
		OMC.PT2.setTextSkewX(OMC.PT1.getTextSkewX());
		OMC.PT2.setTextScaleX(OMC.PT1.getTextScaleX());
		OMC.PT2.setColor(Color.parseColor(layer.optString("bgcolor")));
		OMC.PT2.setTextAlign(OMC.PT1.getTextAlign());

    	// theme-specific tweaks.
		OMCWidgetDrawEngine.layerThemeTweaks(context, layer, sTheme, aWI);
    	
    	// Draw the layer.
		OMCWidgetDrawEngine.fancyDrawText(
    			layer.optString("render_style"),
    			OMC.CANVAS,
    			OMC.TXTBUF,
    			layer.optInt("x"),
    			layer.optInt("y"),
    			OMC.PT1,
    			OMC.PT2,
    			fRot);

	}
	
	static int getSpannedStringWidth(SpannedString ss, final Paint pt) {
		int result = 0;
		int iStart=0;
		float fDefaultskew;
		Paint ptTemp = new Paint(pt);
		ptTemp.setTextAlign(Paint.Align.LEFT);
		fDefaultskew = pt.getTextSkewX();
		while (iStart < ss.length()){
			int iEnd = ss.nextSpanTransition(iStart, ss.length(), StyleSpan.class);
			if (iEnd == -1) return 0;
			if (!pt.isFakeBoldText()) ptTemp.setFakeBoldText(false);
			ptTemp.setTextSkewX(fDefaultskew);
			for (Object o:ss.getSpans(iStart, iEnd, StyleSpan.class)) {
				StyleSpan style = (StyleSpan)o;
				switch (style.getStyle()) {
					case Typeface.BOLD:
						ptTemp.setFakeBoldText(true);
						break;
					case Typeface.BOLD_ITALIC:
						ptTemp.setFakeBoldText(true);
						ptTemp.setTextSkewX(fDefaultskew-0.25f);
						break;
					case Typeface.ITALIC:
						ptTemp.setTextSkewX(fDefaultskew-0.25f);
						break;
					default:
						ptTemp.setFakeBoldText(false);
						ptTemp.setTextSkewX(fDefaultskew);
				}
			}
			result+=ptTemp.measureText(ss.toString().substring(iStart, iEnd));
			iStart = iEnd;
		}
		return result;
	}
	
	static void fancyDrawSpanned(final Canvas cvas, final String str, final int x, final int y, final Paint pt, final float fRot) {
		final SpannedString ss = new SpannedString(Html.fromHtml(str));
		Paint ptTemp = new Paint(pt);
		ptTemp.setTextAlign(Paint.Align.LEFT);
		int bufferWidth = Math.max(OMCWidgetDrawEngine.getSpannedStringWidth(ss, pt),1);
		int bufferHeight = Math.max(pt.getFontMetricsInt().bottom - pt.getFontMetricsInt().top,1);
		int iCursor = 0;
		OMC.rotBUFFER = Bitmap.createBitmap((int)(bufferWidth*1.2f), bufferHeight, Bitmap.Config.ARGB_4444);
		OMC.rotBUFFER.setDensity(DisplayMetrics.DENSITY_HIGH);
		OMC.rotBUFFER.eraseColor(Color.TRANSPARENT);
		OMC.rotCANVAS = new Canvas(OMC.rotBUFFER);

		int iStart=0;
		while (iStart < ss.length()){
			int iEnd = ss.nextSpanTransition(iStart, ss.length(), StyleSpan.class);
			if (iEnd == -1) return;
			ptTemp.set(pt);
			ptTemp.setTextAlign(Paint.Align.LEFT);
			for (Object o:ss.getSpans(iStart, iEnd, StyleSpan.class)) {
				StyleSpan style = (StyleSpan)o;
				switch (style.getStyle()) {
					case Typeface.BOLD:
						ptTemp.setFakeBoldText(true);
						break;
					case Typeface.BOLD_ITALIC:
						ptTemp.setFakeBoldText(true);
						ptTemp.setTextSkewX(-0.25f);
						break;
					case Typeface.ITALIC:
						ptTemp.setTextSkewX(-0.25f);
						break;
					default:
				}

			}
			OMC.rotCANVAS.drawText(ss.subSequence(iStart, iEnd).toString(), iCursor, 0-pt.getFontMetricsInt().top, ptTemp);
			iCursor+=ptTemp.measureText(ss.toString().substring(iStart, iEnd));
			iStart = iEnd;
		}
		OMC.TEMPMATRIX.reset();
		OMC.TEMPMATRIX.postTranslate(-bufferWidth/2f, pt.getFontMetricsInt().top);
		OMC.TEMPMATRIX.postRotate(fRot);
		if (pt.getTextAlign() == Paint.Align.LEFT) {
			// Do nothing
			OMC.TEMPMATRIX.postTranslate(bufferWidth/2f+x, y);
		} else if (pt.getTextAlign() == Paint.Align.CENTER) {
			OMC.TEMPMATRIX.postTranslate(x, y);
		} else if (pt.getTextAlign() == Paint.Align.RIGHT) {
			OMC.TEMPMATRIX.postTranslate(-bufferWidth/2f+x, y);
		} else {
			// Huh? do nothing
			OMC.TEMPMATRIX.postTranslate(bufferWidth/2f+x, y);
		}
		cvas.drawBitmap(OMC.rotBUFFER, OMC.TEMPMATRIX, pt);
		OMC.rotCANVAS = null;
		OMC.rotBUFFER.recycle();
		

		
	}
	
	static void fancyDrawText(final String style, final Canvas cvas, final String text, final int x, final int y, final Paint pt1, final Paint pt2, final float fRot)  {
		//Draw the SFX
		if (style.equals("emboss")) {
			
			//SpannableStringBuilder ssb = new SpannableStringBuilder(Html.fromHtml(text));
			OMCWidgetDrawEngine.fancyDrawSpanned(cvas, text, x-1, y-1, pt2, fRot);
			OMCWidgetDrawEngine.fancyDrawSpanned(cvas, text, x+1, y+1, pt2, fRot);
		} else if (style.equals("shadow")) {
			OMCWidgetDrawEngine.fancyDrawSpanned(cvas, text, x+3, y+3, pt2, fRot);
		}
		//Either way, draw the proper text
		OMCWidgetDrawEngine.fancyDrawSpanned(cvas, text, x, y, pt1, fRot);
	}



}