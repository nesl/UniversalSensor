package com.ucla.nesl.universalservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.SensorParcel;

public class UniversalServiceSensor {
	private static String tag = UniversalServiceSensor.class.getCanonicalName();
	private String devID;
	public  int sType;
	public  String key;
	private ArrayList<UniversalServiceListener> listenersList;
	private Map<UniversalServiceListener, Integer> listenerSensorRate = new HashMap<UniversalServiceListener, Integer>();
		
	public UniversalServiceSensor(String devID, int sType, String key)
	{
		this.devID = new String(devID);
		this.sType = sType;
		this.key   = new String(key);
		listenersList = new ArrayList<UniversalServiceListener>();
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<UniversalServiceListener> getSensorList()
	{
		synchronized (listenersList) {
			return (ArrayList<UniversalServiceListener>)listenersList.clone();
		}
	}
	
	// This function is called by the listener to add itself into the
	// list of registeredListener of this sensor type
	public void linkListener(UniversalServiceListener mlistener, int rate)
	{
		synchronized (listenersList) {
			if (!listenersList.contains(mlistener)) {
				listenersList.add(mlistener);
				(listenerSensorRate).put(mlistener, rate);
			}
		}
	}

	// This function is called by the listener to remove itself from the
	// list of registeredListener of this sensor type
	public void unlinkListner(UniversalServiceListener mlistener)
	{
		synchronized (listenersList) {
			if (listenersList.contains(mlistener)) {
				listenersList.remove(mlistener);
				listenerSensorRate.remove(mlistener);
			}
		}
	}
	
	public int getNextRate()
	{
		int max = 0;
		synchronized (listenersList) {
			for(Map.Entry<UniversalServiceListener, Integer> entry : listenerSensorRate.entrySet()) {
				if (max < entry.getValue())
					max = entry.getValue();
			}
		}
		return max;
	}
	
	@SuppressWarnings("unchecked")
	public void onSensorChanged(SensorParcel event)
	{
		ArrayList<UniversalServiceListener> lList;

		synchronized (listenersList) {
			lList = (ArrayList<UniversalServiceListener>)listenersList.clone();
		}

		// Go through the list of listeners and send the data to them
		for (UniversalServiceListener mlistener: lList)
		{
			try {
				mlistener.getListener().onSensorChanged(event);
			} catch(RemoteException e){}
		}
	}
	
	public boolean isEmpty()
	{
		synchronized (listenersList) {
			return listenersList.isEmpty();
		}
	}
	
	public String getDevID()
	{
		return devID;
	}

	// This is called when the driver notifies the service
	// that a particular type of sensor is no more available
	// This can happen when the mobile phone goes out of range
	@SuppressWarnings("unchecked")
	synchronized public boolean unregister()
	{
		ArrayList<UniversalServiceListener> lList;

		synchronized (listenersList) {
			lList = (ArrayList<UniversalServiceListener>)listenersList.clone();
			listenersList.clear();
		}

		for(UniversalServiceListener mlistener : lList)
		{
			// Notify the listener about the sensor being disabled
			mlistener.unlinkSensor(key);			
		}

		return true;
	}
}
