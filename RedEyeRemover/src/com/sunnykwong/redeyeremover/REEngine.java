package com.sunnykwong.redeyeremover;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Set;

public class REEngine extends Activity {

	static Bitmap mBMP;
	static Set<int[]> reds;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    static public void autodetectionstage() {
    	mBMP = BitmapFactory.decodeFile("/sdcard/Test.jpg");
    	for (int i = 0; i < mBMP.getWidth(); i++) {
    		for (int j = 0; j < mBMP.getHeight(); j++) {
    			if (Color.red(mBMP.getPixel(i, j))>50) {
    		    	reds.add({i,j});
    			}
    		}
    	}
    }
    
    
}