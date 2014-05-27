package com.ucla.nesl.universaldriverservice;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.ucla.nesl.lib.UniversalDriverListener;
import com.ucla.nesl.lib.UniversalSensor;
import com.ucla.nesl.lib.UniversalSensorEvent;
import com.ucla.nesl.universaldrivermanager.UniversalDriverManager;

public class UniversalDriverService extends Service implements SensorEventListener, UniversalDriverListener {
	private static String tag = UniversalDriverService.class.getCanonicalName();
	private static Boolean flag = false;
	Handler handler, h2;
	SensorManager mSensorManager;
	Sensor mSensor;
	UniversalDriverManager mdriverManager1 = null, mdriverManager2 = null;
	boolean registered = false;
	int rate = 0;
	
	@Override
    public void onCreate() {
        super.onCreate();
        Log.i(tag, "onCreate called driver");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // This is the driver.
        mdriverManager1 = UniversalDriverManager.create(getApplicationContext(), "phoneSensor1");
        if (mdriverManager1 == null) {
        	Log.e(tag, "mdrivermanager is null, this is not possible");
        } else {
        	Log.i(tag, "drivermanager is not null");
        }

        handler = new Handler();
        handler.postDelayed(r, 1000);
        
        h2 = new Handler();
    }

	void register()
	{
		// Exposing accelerometer for now. You can expose all the sensors.
		// Only the exposed sensors will be visible to the applications
		ArrayList<Integer> sensorList = new ArrayList<Integer>();
		sensorList.add(Integer.valueOf(UniversalSensor.TYPE_ACCELEROMETER));
		sensorList.add(Integer.valueOf(UniversalSensor.TYPE_LIGHT));
        mdriverManager1.registerDriver(this, UniversalSensor.TYPE_ACCELEROMETER);
        mdriverManager1.registerDriver(this, UniversalSensor.TYPE_LIGHT);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        h2.postDelayed(r1, 20000);
        registered = true;
	}

	void unregister()
	{
		mdriverManager1.unregisterDriver(this, UniversalSensor.TYPE_ALL);
		handler.postDelayed(r, 10000);
		registered = false;
		rate = 0;
	}

    private Runnable r = new Runnable() {
		@Override
		public void run() {
			Log.i(tag, "registering");
			register();
		}
	};

    private Runnable r1 = new Runnable() {
		@Override
		public void run() {

				Log.i(tag, "unregistering");
				unregister();
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

	// this is androids's SensorEventListener function
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	// this is android's SensorEventListener function
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (rate > 0 && registered)
			mdriverManager1.push(new UniversalSensorEvent(event.sensor.getType(), event.values, event.timestamp));
	}

	@Override
	public void setRate(int sType, int rate) {
		this.rate = rate;
	}


}