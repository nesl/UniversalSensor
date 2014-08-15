package com.ucla.nesl.universalphonedriver;

import java.util.HashMap;

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
import com.ucla.nesl.universaldrivermanager.UniversalDriverManager;

public class UniversalPhoneDriver extends Service implements SensorEventListener, UniversalDriverListener {
	private static String tag = UniversalPhoneDriver.class.getCanonicalName();
	private static Boolean flag = false;
	private String devID = "phoneSensor1";
	Handler handler, h2;
	SensorManager mSensorManager;
	Sensor mSensor;
	UniversalDriverManager mdriverManager1 = null, mdriverManager2 = null;
	HashMap<Integer, int[]> sensorRate = new HashMap<Integer, int[]>();
	int[] delayType = new int[] {SensorManager.SENSOR_DELAY_FASTEST, SensorManager.SENSOR_DELAY_GAME, 
			SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI
	};
	boolean registered = false;
	long[] timeStamp = new long[1];
	int rate = 0;
	int bundleSize[];
	int count = 0;

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.i(tag, "onCreate called driver");

		sensorRate.put(UniversalSensor.TYPE_ACCELEROMETER, new int[]{200, 50,15, 50});
		sensorRate.put(UniversalSensor.TYPE_GYROSCOPE, new int[]{50, 5, 5, 15});
		sensorRate.put(UniversalSensor.TYPE_MAGNETIC_FIELD, new int[]{200, 50, 5, 15});
		bundleSize = new int[]{1};

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// This is the driver.
		mdriverManager1 = UniversalDriverManager.create(getApplicationContext(), this, devID);
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

		mdriverManager1.registerDriver(UniversalSensor.TYPE_ACCELEROMETER, sensorRate.get(UniversalSensor.TYPE_ACCELEROMETER), bundleSize);
		mdriverManager1.registerDriver(UniversalSensor.TYPE_GYROSCOPE, sensorRate.get(UniversalSensor.TYPE_GYROSCOPE), bundleSize);
		mdriverManager1.registerDriver(UniversalSensor.TYPE_MAGNETIC_FIELD, sensorRate.get(UniversalSensor.TYPE_MAGNETIC_FIELD), bundleSize);
	}

	void unregister()
	{
		mdriverManager1.unregisterDriver(UniversalSensor.TYPE_ALL);
		handler.postDelayed(r, 10000);
		registered = false;
		rate = 0;
	}

	private Runnable r = new Runnable()
	{
		@Override
		public void run() {
			Log.i(tag, "registering");
			register();
		}
	};

	private Runnable r1 = new Runnable()
	{
		@Override
		public void run() {

			Log.i(tag, "unregistering");
			unregister();
		}
	};

	@Override
	public IBinder onBind(Intent intent)
	{
		if (flag == false) {
			Log.i(tag, "onBind ss");
			flag = true;
		}
		return null;
	}

	// this is androids's SensorEventListener function
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}

	// this is android's SensorEventListener function
	@Override
	public void onSensorChanged(SensorEvent event)
	{	
		//Log.i(tag, "event.values: " + event.values[0] + "," + event.values[1] + "," + event.values[2]);
		timeStamp[0] = event.timestamp;
		mdriverManager1.push(devID, event.sensor.getType(), event.values, timeStamp);
	}

	int getDelayType(int sType, int rate)
	{
		int[] rates = sensorRate.get(sType);
		for (int i = 0; i < rates.length; i++) {
			if (rate == rates[i])
				return delayType[i];
		}
		return -1;
	}

	@Override
	public void setRate(int sType, int rate, int bundleSize)
	{
		Log.i(tag, "setRate:: sType: " + sType + ", rate: " + rate + ", bundleSize: " + bundleSize);
		if (rate == 0) {
			this.rate = 0;
			mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(sType));
			Log.i(tag, "setRate unregister  " + sType + " rate " + rate);
		} else {
			int delay_type = getDelayType(sType, rate);
			mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(sType));
			mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(sType), delay_type);
			Log.i(tag, "setRate " + sType + " rate " + rate + " delaytype " + delay_type);
		}
	}

	@Override
	public void disconnected()
	{
		setRate(Sensor.TYPE_ALL, 0, 0);
	}
}