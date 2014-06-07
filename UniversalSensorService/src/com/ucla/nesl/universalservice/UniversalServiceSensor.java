package com.ucla.nesl.universalservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.R.string;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.SensorParcel;
import com.ucla.nesl.lib.SensorParcelWrapper;
import com.ucla.nesl.lib.UniversalConstants;

public class UniversalServiceSensor {
	private static String tag = UniversalServiceSensor.class.getCanonicalName();
	public  int sType;
	private String mSensorKey;
	public  int maxRate;
	private int bundleSize;
	private int sRate;
	private float sUpdateInterval;
	UniversalServiceDevice mdevice;
	private Map<String, _Listener> listenersList = new HashMap<String, UniversalServiceSensor._Listener>();
	private Map<UniversalServiceListener, Integer> listenerSensorRate = new HashMap<UniversalServiceListener, Integer>();
	
	public UniversalServiceSensor(UniversalServiceDevice mdevice, String mSensorKey, int sType, int rate, int bundleSize)
	{
		this.mdevice = mdevice;
		this.sType = sType;
		this.mSensorKey = new String(mSensorKey);
		this.maxRate = rate;
		this.bundleSize = bundleSize;
		this.sRate  = 0;
		this.sUpdateInterval = 0;
//		listenersList = new ArrayList<UniversalServiceListener>();
	}

	public boolean update(int maxRate, int bundleSize)
	{
		this.maxRate = maxRate;
		this.bundleSize = bundleSize;
		return true;
	}

/*
	@SuppressWarnings("unchecked")
	private ArrayList<UniversalServiceListener> getSensorList()
	{
		synchronized (listenersList) {
//			return (ArrayList<UniversalServiceListener>)listenersList.clone();
		}
	}
*/
	
	private void updateSamplingParams()
	{
		boolean flag = true;
		_Listener mlistener;
		Handler   mhandler;
		synchronized (listenersList) {
			for (Map.Entry<String, _Listener> entry : listenersList.entrySet()) {
				mlistener = entry.getValue();
				if (flag) {
					sRate = mlistener.getRate();
					sUpdateInterval = mlistener.getUpdateInterval();
				} else {
					if (sRate < mlistener.getRate())
						sRate = mlistener.getRate();
					if (sUpdateInterval > mlistener.getUpdateInterval())
						sUpdateInterval = mlistener.getUpdateInterval();
				}
			}
			for (Map.Entry<String, _Listener> entry : listenersList.entrySet()) {
				Bundle bundle = new Bundle();
				bundle.putString(UniversalConstants.sType, mSensorKey);
				bundle.putInt(UniversalConstants.rate, sRate);
				bundle.putFloat(UniversalConstants.updateInterval, sUpdateInterval);
				
				mhandler = entry.getValue().getHandler();
				mhandler.sendMessage(mhandler.obtainMessage(UniversalConstants.MSG_UpdateSamplingParam, bundle));
			}
		}
		//updateDriver
		try {
			mdevice.mDriverStub.setRate(sType, sRate, sUpdateInterval);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// This function is called by the listener to add itself into the
	// list of registeredListener of this sensor type
	public void linkListener(String listenerID, UniversalServiceListener mUniversalListener, int rate, float updateInterval)
	{
		_Listener mListener = null;
		synchronized (listenersList) {
			if (!listenersList.containsKey(listenerID)) {
				mListener = new _Listener(listenerID, mUniversalListener, rate, updateInterval);
				listenersList.put(listenerID, mListener);
			} else {
				mListener = listenersList.get(listenerID);
				mListener.update(rate, updateInterval);
			}
		}
		updateSamplingParams();
	}

	// This function is called by the listener to remove itself from the
	// list of registeredListener of this sensor type
	public void unlinkListner(String listenerID)
	{
		_Listener mListener = null;
		synchronized (listenersList) {
			if (listenersList.containsKey(listenerID)) {
				mListener = listenersList.remove(listenerID);
			}
		}
		// Nothing to do here
		updateSamplingParams();
	}
	
	@SuppressWarnings("unchecked")
	public void onSensorChanged(SensorParcel[] event, int length)
	{
		synchronized (listenersList) {
			// Go through the list of listeners and send the data to them
			for (Map.Entry<String, _Listener> entry : listenersList.entrySet())
			{
				Handler mhandler = entry.getValue().getHandler();
				mhandler.sendMessage(mhandler.obtainMessage(UniversalConstants.MSG_OnSensorChanged, new SensorParcelWrapper(event, length)));
			}
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
		return mdevice.getDevID();
	}

	/**
	 * This function is called when the driver sends unregister
	 * notification to the UniversalService. After this, the
	 * sensor will no longer be available for data sampling.
	 * @return True on success and False on failure
	 */
	synchronized public boolean unregister()
	{
		/**
		 * Notify all the Listeners to unlink themselves with this
		 * sensor.
		 */
		Log.d(tag, "Unregistering sensor " + mSensorKey);
		synchronized (listenersList) {
			for(Map.Entry<String, _Listener> entry : listenersList.entrySet()) {
				Handler mhandler = entry.getValue().getHandler();
				mhandler.sendMessage(mhandler.obtainMessage(UniversalConstants.MSG_Unlink_Sensor, mSensorKey));
			}
			listenersList.clear();
		}
		return true;
	}
	
	private class _Listener
	{
		String					 listenerID = null;
		int 					 lRate	   = 0;
		float				     lUpdateInterval = 0;
		UniversalServiceListener mListener = null;
		
		public _Listener(String listenerID, UniversalServiceListener mListener, int lRate, float lUpdateInterval)
		{
			this.listenerID = new String(listenerID);
			this.mListener  = mListener;
			this.lRate      = lRate;
			this.lUpdateInterval = lUpdateInterval;
		}
		
		public void update(int lrate, float lUpdateInterval)
		{
			this.lRate = lRate;
			this.lUpdateInterval = lUpdateInterval;
		}
		
		public int getRate()
		{
			return lRate;
		}
		
		public float getUpdateInterval()
		{
			return lUpdateInterval;
		}
		
		public Handler getHandler()
		{
			return mListener.getHandler();
		}
	}
}