package com.sunnykwong.ompc;

import android.app.Activity;
import android.os.Bundle;
import android.net.wifi.WifiManager;
import android.widget.ImageButton;
import android.view.View;
import android.bluetooth.*;
import android.content.Context;
import android.graphics.Color;

public class OMPCActivity extends Activity implements View.OnClickListener {

	ImageButton mBTButton, mWIFIButton, m3GButton;
	BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();
	WifiManager mWFA; 
//	Intent mBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        
        mWFA = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        
        setContentView(R.layout.main);
        mBTButton = (ImageButton)this.findViewById(R.id.Button01);    
        if (mBTA!=null) {
        	mBTButton.setOnClickListener(this);
        	mBTButton.setBackgroundColor(mBTA.isEnabled()? Color.GREEN:Color.RED); 
        } else mBTButton.setEnabled(false);

        mWIFIButton = (ImageButton)this.findViewById(R.id.Button02);    
        mWIFIButton.setOnClickListener(this);
        mWIFIButton.setBackgroundColor(mWFA.isWifiEnabled()? Color.GREEN:Color.RED);

        m3GButton = (ImageButton)this.findViewById(R.id.Button03);    
        m3GButton.setOnClickListener(this);
        (this.findViewById(R.id.toplevel)).setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
    	// TODO Auto-generated method stub
    	if (v == mBTButton) {
    		System.out.println("BT clicked");
    		if (mBTA.isEnabled()) mBTA.disable();
    		else mBTA.enable();
    	}
    	if (v == mWIFIButton) {
    		System.out.println("WIFI clicked");
    		if (mWFA.isWifiEnabled()) mWFA.setWifiEnabled(false);
    		else mWFA.setWifiEnabled(true);
    	}
    	if (v == m3GButton) {
    		System.out.println("3G clicked");
    	}
    	if (v == this.findViewById(R.id.toplevel)) {
    		System.out.println("Background clicked");
    	}
    	finish();

    }

    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	if (mWFA!=null) {
    		mWFA = null;
    	}
    	super.onDestroy();
    }
}