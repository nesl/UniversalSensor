package com.ucla.nesl.app;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

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
    private Button startZephyr;
    private EditText edittext;
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
        edittext = (EditText)findViewById(R.id.edittext);
        startZephyr = (Button)findViewById(R.id.startZephyr);
        mManager = UniversalSensorManager.create(getApplicationContext());
        
        register.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	String value  = edittext.getText().toString();
            	if (value.isEmpty()) {
            		Log.i(tag, "Please enter the device number and sensor type");
            		return;
            	}

            	dlist = mManager.listDevices();
            	int devNum = Integer.valueOf(value.split(":")[0]);
            	int sType = Integer.valueOf(value.split(":")[1]);

            	device = dlist.get(devNum);
            	Log.i(tag, "registering Device " + device.getDevID() + "::" + devNum + "," +sType);

                registerListener(device.getDevID(), sType, 1);
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
            			Log.i(tag, "" + i + ":" + UniversalSensorNameMap.getName(i));
            	}
            	registerNotification();
            }
        });
        
        unregister.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
            	String value  = edittext.getText().toString();
            	if (value.isEmpty()) {
            		Log.i(tag, "Please enter the device number and sensor type");
            		return;
            	}
            	dlist = mManager.listDevices();
            	int devNum = Integer.valueOf(value.split(":")[0]);
            	int sType = Integer.valueOf(value.split(":")[1]);
            	device = dlist.get(devNum);
				unregisterListener(device.getDevID(), sType);
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
				// TODO Auto-generated method stub
				
			}
	    });
	    startZephyr.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String bluetoothAddr = edittext.getText().toString();
				Log.i(tag, "calling broadcast " + edittext.getText());
				if (bluetoothAddr.isEmpty()) {
					Log.i(tag, "bluetoothAddr is empty");
					return;
				}
				Intent intent = new Intent("ZephyrDriverBroadcastReceiver");
				intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				intent.putExtra("bluetoothAddr", bluetoothAddr);
//				intent.setAction("ZephyrDriverBroadcastReceiver");
				startService(intent);
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
		Log.i(tag, "Event received: " + event.devID + "SensorType:" + UniversalSensorNameMap.getName(event.sType) + ", " + event.values[0] + ", " + event.timestamp);
	}

	@Override
	public void onAccuracyChanged(UniversalSensor sensor, int accuracy) {
	}
}
