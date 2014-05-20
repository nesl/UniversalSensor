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
    private TextView messageTextView;
    private String tag = UniversalSensorApp.class.getCanonicalName();
    private UniversalSensorManager mManager;    
	private String UNIVERSALDriverPackage = "com.ucla.nesl.universaldriverservice";
	private String UNIVERSALDriverClass = "com.ucla.nesl.universaldriverservice.UniversalDriverService";
	private UniversalSensor msensor;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        disconnectButton = (Button)findViewById(R.id.startDriver);
        queryButton = (Button)findViewById(R.id.queryButton);
        messageTextView = (TextView)findViewById(R.id.messageTextView);
        mManager = UniversalSensorManager.create(getApplicationContext());
        
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
//                mManager.registerListener("ad", 1, 1);
            	ArrayList<Device> d = mManager.listDevices();
            	Log.i(tag, "listing devices " + d.size());
            	for (Device device:mManager.listDevices())
            	{
            		Log.i(tag, device.vendorID);
            		for (int i = 0; i < device.sensorList.size(); i++)
            			Log.i(tag, UniversalSensorNameMap.getName(device.sensorList.get(i)));
            	}
            }
        });
    }

	@Override
	protected void onPause() 
	{
		super.onPause();
	}

	@Override
	protected void onResume() {
	    super.onResume();
//	    mManager.registerListener(this, "asf" , SensorManager.SENSOR_DELAY_NORMAL, 2);
	}
	
	@Override
	public void onSensorChanged(UniversalSensorEvent event) {
		
	}

	@Override
	public void onAccuracyChanged(UniversalSensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}
