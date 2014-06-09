package com.ucla.nesl.universalservice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
	public  int[] rateRange;
	private int[] bundleSize;
	private int sRate;
	private int sBundleSize;
	UniversalServiceDevice mdevice;
	private Map<String, _Listener> listenersList = new HashMap<String, UniversalServiceSensor._Listener>();
	private Map<UniversalServiceListener, Integer> listenerSensorRate = new HashMap<UniversalServiceListener, Integer>();

	public UniversalServiceSensor(UniversalServiceDevice mdevice, String mSensorKey, int sType, int[] rate, int[] bundleSize)
	{
		this.mdevice = mdevice;
		this.sType = sType;
		this.mSensorKey = new String(mSensorKey);
		this.rateRange = rate.clone();
		Arrays.sort(this.rateRange);
		this.bundleSize = bundleSize.clone();
		Arrays.sort(this.bundleSize);
		Log.d(tag, "rateRange: " + Arrays.toString(rateRange) + ", bundleSize: " + Arrays.toString(bundleSize));
		this.sRate  = this.rateRange[0];
		this.sBundleSize = this.bundleSize[0];
//		listenersList = new ArrayList<UniversalServiceListener>();
	}

//	public boolean update(int rateRange, int bundleSize)
//	{
//		this.rateRange = rateRange;
//		this.bundleSize = bundleSize;
//		return true;
//	}

/*
	@SuppressWarnings("unchecked")
	private ArrayList<UniversalServiceListener> getSensorList()
	{
		synchronized (listenersList) {
//			return (ArrayList<UniversalServiceListener>)listenersList.clone();
		}
	}
*/
	
	private void updateDriverRate()
	{
		try {
			mdevice.mDriverStub.setRate(sType, sRate, sBundleSize);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void updateSamplingParams()
	{
		int     tRate = sRate,
				tBundleSize = sBundleSize;
		boolean   flag = true;
		_Listener mlistener;
		Handler   mhandler;
		synchronized (listenersList) {
			if (listenersList.isEmpty()) {
				sRate = 0;
				sBundleSize = 0;
			} else {
				for (Map.Entry<String, _Listener> entry : listenersList.entrySet()) {
					mlistener = entry.getValue();
					if (flag) {
						tRate = mlistener.getRate();
						tBundleSize = mlistener.getBundleSize();
					} else {
						if (tRate < mlistener.getRate())
							tRate = mlistener.getRate();
						if (tBundleSize > mlistener.getBundleSize())
							tBundleSize = mlistener.getBundleSize();
					}
				}
				Log.d(tag, "Calculated rate:bundleSize::" + tRate + ":" + tBundleSize);
				// Now figure out the next best possible rate at which the Sensor can send
				for (int i = 0; i < rateRange.length; i++) {
					if (tRate <= rateRange[i]) {
						sRate = rateRange[i];
						break;
					}
				}
				
				for (int i = 0; i < bundleSize.length; i++) {
					if (tBundleSize <= bundleSize[i]) {
						sBundleSize = bundleSize[i];
						break;
					}
				}
				
				for (Map.Entry<String, _Listener> entry : listenersList.entrySet()) {
					Bundle bundle = new Bundle();
					bundle.putString(UniversalConstants.sType, mSensorKey);
					bundle.putInt(UniversalConstants.rate, sRate);
					bundle.putInt(UniversalConstants.bundleSize, sBundleSize);
					
					mhandler = entry.getValue().getHandler();
					mhandler.sendMessage(mhandler.obtainMessage(UniversalConstants.MSG_UpdateSamplingParam, bundle));
				}
			}
		}
		//updateDriver
		updateDriverRate();
	}

	// This function is called by the listener to add itself into the
	// list of registeredListener of this sensor type
	public boolean linkListener(String listenerID, UniversalServiceListener mUniversalListener, int rate, int bundleSize)
	{
		_Listener mListener = null;
		synchronized (listenersList) {
			if (!listenersList.containsKey(listenerID)) {
				mListener = new _Listener(listenerID, mUniversalListener, rate, bundleSize);
				listenersList.put(listenerID, mListener);
			} else {
				mListener = listenersList.get(listenerID);
				mListener.update(rate, bundleSize);
			}
		}
		updateSamplingParams();
		return true;
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
		int				         lbundleSize = 0;
		UniversalServiceListener mListener = null;
		
		public _Listener(String listenerID, UniversalServiceListener mListener, int lRate, int lbundleSize)
		{
			this.listenerID = new String(listenerID);
			this.mListener  = mListener;
			this.lRate      = lRate;
			this.lbundleSize = lbundleSize;
		}
		
		public void update(int lRate, int lbundleSize)
		{
			this.lRate = lRate;
			this.lbundleSize = lbundleSize;
		}
		
		public int getRate()
		{
			return lRate;
		}
		
		public int getBundleSize()
		{
			return lbundleSize;
		}
		
		public Handler getHandler()
		{
			return mListener.getHandler();
		}
	}
}