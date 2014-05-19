package com.ucla.nesl.universaldriverservice;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.universaldrivermanager.UniversalDriverManager;

public class UniversalDriverService extends Service implements SensorEventListener {
	private static String tag = UniversalDriverService.class.getCanonicalName();
	private static Boolean flag = false;
	Handler handler;
	SensorManager mSensorManager;
	Sensor mSensor;
	UniversalDriverManager mdriverManager = null;
	Device device = new Device("123");
	@Override
    public void onCreate() {
        super.onCreate();
        Log.i(tag, "onCreate called driver");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
        		SensorManager.SENSOR_DELAY_NORMAL);
        mdriverManager = UniversalDriverManager.create(getApplicationContext());
        if (mdriverManager == null) {
        	Log.e(tag, "mdrivermanager is null, this is not possible");
        } else {
        	Log.i(tag, "drivermanager is not null");
        }
        handler = new Handler();
        handler.postDelayed(r, 4000);
    }

    private Runnable r = new Runnable() {
		@Override
		public void run() {
	        mdriverManager.registerDriver(device);
			Log.i(tag, "device value: " + device.devID);
			handler.postDelayed(r, 4000);
		}
	};

    public void startSend()
    {
    	Log.i(tag, "startSend");
    	handler = new Handler();
    	handler.postDelayed(r, 4000);
    }

	@Override
	public IBinder onBind(Intent intent) {
		if (flag == false) {
			Log.i(tag, "onBind ss");
			flag = true;
		}
		return null;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
//		mdriverManager.push(event.sensor.getType(), event.values, event.values.length, event.accuracy, event.timestamp);
	}
}