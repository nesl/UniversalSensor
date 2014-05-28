package com.ucla.nesl.app;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.app.universalsensorapp.R;
import com.ucla.nesl.lib.UniversalEventListener;
import com.ucla.nesl.lib.UniversalSensor;
import com.ucla.nesl.lib.UniversalSensorEvent;
import com.ucla.nesl.lib.UniversalSensorNameMap;
import com.ucla.nesl.universalsensormanager.UniversalSensorManager;

public class UniversalSensorApp extends Activity implements UniversalEventListener {
    private Button listDevices;
    private Button register;
    private Button unregister;
    private Button registerDriver;
    private Button unregisterDriver;
    private String tag = UniversalSensorApp.class.getCanonicalName();
    private UniversalSensorManager mManager;    
	private String UNIVERSALDriverPackage = "com.ucla.nesl.universaldriverservice";
	private String UNIVERSALDriverClass = "com.ucla.nesl.universaldriverservice.UniversalDriverService";
	private ArrayList<Device> dlist;
	Device device;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        listDevices = (Button)findViewById(R.id.listDevices);
        register = (Button)findViewById(R.id.register);
        unregister = (Button)findViewById(R.id.unregister);
        registerDriver = (Button)findViewById(R.id.registerDriver);
        unregisterDriver = (Button)findViewById(R.id.unregisterDriver);
        mManager = UniversalSensorManager.create(getApplicationContext());
        
        register.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Log.i(tag, "registering Devices");
            	device = mManager.listDevices().get(0);
                registerListener(device.getDevID(), device.getSensorList().get(0), 1);
            }
        });

        listDevices.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	dlist = mManager.listDevices();
            	Log.i(tag, "listing devices " + dlist.size());

            	for (Device device:mManager.listDevices())
            	{
            		Log.i(tag, device.getVendorID() +":" + device.getDevID());
            		for (int i : device.getSensorList())
            			Log.i(tag, UniversalSensorNameMap.getName(i));
            	}
            	registerNotification();
            }
        });
        
        unregister.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				unregisterListener(device.getDevID(), device.getSensorList().get(0));
			}
		});

        registerDriver.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
        		Intent intent = new Intent("bindUniversalDriverService");
        		intent.setClassName(UNIVERSALDriverPackage, UNIVERSALDriverClass);
        		startService(intent);
			}
		});

	    unregisterDriver.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
			}
		});
	}

    void registerNotification()
    {
    	mManager.registerNotification(this);
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
	}
}
