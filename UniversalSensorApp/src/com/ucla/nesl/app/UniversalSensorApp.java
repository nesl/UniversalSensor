package com.ucla.nesl.app;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.app.universalsensorapp.R;
import com.ucla.nesl.lib.UniversalEventListener;
import com.ucla.nesl.lib.UniversalSensor;
import com.ucla.nesl.lib.UniversalSensorEvent;
import com.ucla.nesl.lib.UniversalSensorNameMap;
import com.ucla.nesl.universalsensormanager.UniversalSensorManager;

public class UniversalSensorApp extends Activity implements UniversalEventListener {
    private Button disconnectButton;
    private Button queryButton;
    private Button unregisterSensorButton;
    private EditText sTypeText;
    private TextView messageTextView;
    private String tag = UniversalSensorApp.class.getCanonicalName();
    private UniversalSensorManager mManager, mManager2;    
	private String UNIVERSALDriverPackage = "com.ucla.nesl.universaldriverservice";
	private String UNIVERSALDriverClass = "com.ucla.nesl.universaldriverservice.UniversalDriverService";
	private UniversalSensor msensor;
	private Device di;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        disconnectButton = (Button)findViewById(R.id.startDriver);
        queryButton = (Button)findViewById(R.id.queryButton);
        unregisterSensorButton = (Button)findViewById(R.id.setsensor);
        sTypeText = (EditText)findViewById(R.id.sType);
        messageTextView = (TextView)findViewById(R.id.messageTextView);
        mManager = UniversalSensorManager.create(getApplicationContext());
        mManager2 = UniversalSensorManager.create(getApplicationContext());
        
        disconnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Log.i(tag, "starting driver service");
        		Intent intent = new Intent("bindUniversalDriverService");
        		intent.setClassName(UNIVERSALDriverPackage, UNIVERSALDriverClass);
        		startService(intent);
            }
        });
        
        queryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	ArrayList<Device> d = mManager.listDevices();
            	Log.i(tag, "listing devices " + d.size());

            	for (Device device:mManager.listDevices())
            	{
            		Log.i(tag, device.getVendorID() +":" + device.getDevID());
            		for (int i : device.getSensorList())
            			Log.i(tag, UniversalSensorNameMap.getName(i));
            	}
            	di = mManager.listDevices().get(0);

                registerListener(di.getDevID(), di.getSensorList().get(0), 1);
            }
        });
        
        unregisterSensorButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				unregisterListener(di.getDevID(), di.getSensorList().get(0));
			}
		});
    }

    private void registerListener(String devID, int sType, int rate)
    {
    	mManager.registerListener(this, devID, sType, rate);
    }
    
    private void unregisterListener(String devID, int sType)
    {
    	mManager.unregisterListener(this, devID, sType);
    }
    
	@Override
	protected void onPause() 
	{
		super.onPause();
	}

	@Override
	protected void onResume() {
	    super.onResume();
	}
	
	@Override
	public void onSensorChanged(UniversalSensorEvent event) {
		Log.i(tag, "Event received: SensorType:" + UniversalSensorNameMap.getName(event.sType) + ", " + event.values[0] + ", " + event.timestamp);
	}

	@Override
	public void onAccuracyChanged(UniversalSensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}
