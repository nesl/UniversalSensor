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
	Handler handler;
	SensorManager mSensorManager;
	Sensor mSensor;
	UniversalDriverManager mdriverManager1 = null, mdriverManager2 = null;
	boolean once = true;
	int rate = SensorManager.SENSOR_DELAY_NORMAL;
	
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
    }

	void register()
	{
		// Exposing accelerometer for now. You can expose all the sensors.
		// Only the exposed sensors will be visible to the applications
		ArrayList<Integer> sensorList = new ArrayList<Integer>();
		sensorList.add(new Integer(UniversalSensor.TYPE_ACCELEROMETER));
		sensorList.add(new Integer(UniversalSensor.TYPE_LIGHT));
        mdriverManager1.registerDriver(this, sensorList);
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

	// this is androids's SensorEventListener function
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	// this is android's SensorEventListener function
	@Override
	public void onSensorChanged(SensorEvent event) {
		mdriverManager1.push(new UniversalSensorEvent(event.sensor.getType(), event.values, event.timestamp));
	}

	@Override
	public void setRate(int rate) {
		// Use the rate that the UniversalService tells us to use
		// To begin with we will operate at the standard rate for this sensor
		this.rate = rate;
	}

	@Override
	public void activateSensor(int sType) {
		// register for the sensor data only when some application
		// needs the data. Else disable it.
		Log.i(tag, "received enable from the service");
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(sType), rate);
	}

	@Override
	public void deactivateSensor(int sType) {
		// No application has registered for this sensor, so 
		// its better to disable it to save battery
		mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(sType));
		Log.i(tag, "received disable from the service");
	}
}