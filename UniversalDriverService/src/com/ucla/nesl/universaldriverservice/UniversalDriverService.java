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
import com.ucla.nesl.lib.UniversalDriverListener;
import com.ucla.nesl.lib.UniversalSensor;
import com.ucla.nesl.universaldrivermanager.UniversalDriverManager;

public class UniversalDriverService extends Service implements SensorEventListener, UniversalDriverListener {
	private static String tag = UniversalDriverService.class.getCanonicalName();
	private static Boolean flag = false;
	Handler handler;
	SensorManager mSensorManager;
	Sensor mSensor;
	UniversalDriverManager mdriverManager1 = null, mdriverManager2 = null;
	boolean once = true;
	
	@Override
    public void onCreate() {
        super.onCreate();
        Log.i(tag, "onCreate called driver");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
        		SensorManager.SENSOR_DELAY_NORMAL);

        // Create two drivers
        mdriverManager1 = UniversalDriverManager.create(getApplicationContext(), "phoneSensor1");
        if (mdriverManager1 == null) {
        	Log.e(tag, "mdrivermanager is null, this is not possible");
        } else {
        	Log.i(tag, "drivermanager is not null");
        }

        mdriverManager2 = UniversalDriverManager.create(getApplicationContext(), "phoneSensor2");
        if (mdriverManager2 == null) {
        	Log.e(tag, "mdrivermanager is null, this is not possible");
        } else {
        	Log.i(tag, "drivermanager is not null");
        }

        handler = new Handler();
        handler.postDelayed(r, 4000);
    }

	void register()
	{
        mdriverManager1.registerDriver(this, UniversalSensor.TYPE_ACCELEROMETER);
        mdriverManager2.registerDriver(this, UniversalSensor.TYPE_MAGNETIC_FIELD);		
	}

    private Runnable r = new Runnable() {
		@Override
		public void run() {
			if (once) {
				Log.i(tag, "registering");
				register();
			}
			once = false;
		}
	};

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

	@Override
	public void setRate(int rate) {
	}
}