package com.ucla.nesl.universalservice;

import java.util.HashMap;

import android.os.Binder;

import com.ucla.nesl.aidl.IUniversalSensorManager;

public class UniversalServiceListener {
	private IUniversalSensorManager mlistener;
	private HashMap<String, UniversalServiceSensor> registeredSensors = new HashMap<String, UniversalServiceSensor>();
	public int callingPid;
	
	public UniversalServiceListener()
	{
		mlistener = null;
		callingPid = -1;
	}
	
	public UniversalServiceListener(IUniversalSensorManager listener, int callingPid)
	{
		this.mlistener = listener;
		this.callingPid = callingPid;
	}
	
	public void registerSensor(String sensorID,  UniversalServiceSensor sensor)
	{
		registeredSensors.put(sensorID, sensor);
	}
	
	// This method is called by the sensor when it is no longer available
	public void unregisterSensor(String sensorID)
	{
		UniversalServiceSensor mSensor = registeredSensors.remove(sensorID);
//		mSensor.unregisterListner(this);
	}

	// This functioned is called by the service when the app wants
	// to unregister itself with one or more sensors. For now let us
	// assume that we only handle one sensor unregister only
	public void unregister(String key)
	{
		// remove the entry from its list
		UniversalServiceSensor mSensor = registeredSensors.remove(key);
		mSensor.unregisterListner(this);
	}
	public IUniversalSensorManager getListener()
	{
		return mlistener;
	}
	
	public boolean isEmpty()
	{
		return registeredSensors.isEmpty();
	}
}
