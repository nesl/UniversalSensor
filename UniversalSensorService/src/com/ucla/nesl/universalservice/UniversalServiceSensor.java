package com.ucla.nesl.universalservice;

import java.util.ArrayList;

import com.ucla.nesl.aidl.SensorParcel;

public class UniversalServiceSensor {
	private String devID;
	public  int sType;
	private ArrayList<UniversalServiceListener> registeredlisteners;
	
	public UniversalServiceSensor(String devID, int sType)
	{
		this.devID = new String(devID);
		this.sType = sType;
		registeredlisteners = new ArrayList<UniversalServiceListener>();
	}
	
	public ArrayList<UniversalServiceListener> getSensorList()
	{
		return registeredlisteners;
	}
	
	// This function is called by the listener to add itself into the
	// list of registeredListener of this sensor type
	public void registerListener(UniversalServiceListener mlistener)
	{
		if (!registeredlisteners.contains(mlistener))
			registeredlisteners.add(mlistener);
	}

	// This function is called by the listener to remove itself from the
	// list of registeredListener of this sensor type
	public void unregisterListner(UniversalServiceListener mlistener)
	{
		if (registeredlisteners.contains(mlistener))
			registeredlisteners.remove(mlistener);
	}
	
	public void onSensorChanged(SensorParcel event)
	{
		// Go through the list of listeners and send the data to them
		// for (UniversalServiceListener mlistener: registeredlisteners)
	}
	
	public boolean isEmpty()
	{
		return registeredlisteners.isEmpty();
	}
	
	public String getDevID()
	{
		return devID;
	}
	
	// This is called when the driver notifies the service
	// that a particular type of sensor is no more available
	// This can happen when the mobile phone goes out of range
	public boolean unregister(String key)
	{
		for(UniversalServiceListener mlistener : registeredlisteners)
		{
			// Notify the listener about the sensor being disabled
			mlistener.unregisterSensor(key);			
		}

		// remove the listener entry from its list
		registeredlisteners.clear();

		return true;
	}
}
