package com.ucla.nesl.universalservice;

import java.util.HashMap;

import android.os.RemoteException;

import com.ucla.nesl.aidl.IUniversalSensorManager;
import com.ucla.nesl.lib.UniversalConstants;

public class UniversalServiceListener {
	private IUniversalSensorManager mlistener;
	private HashMap<String, UniversalServiceSensor> sensorsList = new HashMap<String, UniversalServiceSensor>();
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

	public void linkSensor(String sensorID,  UniversalServiceSensor sensor)
	{
		synchronized (sensorsList) {
			sensorsList.put(sensorID, sensor);
		}
	}

	// This method is called by the sensor when it is no longer available
	public void unlinkSensor(String sensorID)
	{
		UniversalServiceSensor mSensor;
		synchronized (sensorsList) {
			mSensor = sensorsList.remove(sensorID);
		}
		try {
			mlistener.notify(mSensor.getDevID(), mSensor.sType, UniversalConstants.ACTION_UNREGISTER);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	// This functioned is called by the service when the app wants
	// to unregister itself with one or more sensors. For now let us
	// assume that we only handle one sensor unregister only
	// TODO: extend it to unregister all
	public void unregister(String key)//, int sType)
	{
		// remove the entry from its list
		UniversalServiceSensor mSensor;
		synchronized (sensorsList) {
			mSensor = sensorsList.remove(key);
		}
		mSensor.unlinkListner(this);
	}

	public IUniversalSensorManager getListener()
	{
		return mlistener;
	}
	
	public boolean isEmpty()
	{
		synchronized (sensorsList) {
			return sensorsList.isEmpty();
		}
	}
}
