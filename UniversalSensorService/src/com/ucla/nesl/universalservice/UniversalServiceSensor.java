package com.ucla.nesl.universalservice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.lib.SensorParcelWrapper;
import com.ucla.nesl.lib.UniversalConstants;
import com.ucla.nesl.universaldatastore.UniversalDataStore;

public class UniversalServiceSensor {
	private static String tag = UniversalServiceSensor.class.getCanonicalName();
	public  int sType;
	private String mSensorKey;
	public  int[] rateList;
	private int[] bundleSizeList;
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
		this.rateList = rate.clone();
		Arrays.sort(this.rateList);
		this.bundleSizeList = bundleSize.clone();
		Arrays.sort(this.bundleSizeList);
		Log.d(tag, "rateList: " + Arrays.toString(rateList) + ", bundleSize: " + Arrays.toString(bundleSizeList));
		this.sRate  = this.rateList[0];
		this.sBundleSize = this.bundleSizeList[0];
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
		} catch (DeadObjectException e) {
			Handler mHandler = mdevice.getServiceHandler();
			mHandler.sendMessage(mHandler.obtainMessage(UniversalConstants.MSG_UnregisterDriver, mdevice.getDevID()));
		} catch (RemoteException e) {
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
						flag = false;
					} else {
						if (tRate < mlistener.getRate())
							tRate = mlistener.getRate();
						if (tBundleSize > mlistener.getBundleSize())
							tBundleSize = mlistener.getBundleSize();
					}
				}
				Log.d(tag, "Calculated rate:bundleSize::" + tRate + ":" + tBundleSize);
				// Now figure out the next best possible rate at which the Sensor can send
				for (int i = 0; i < rateList.length; i++) {
					if (tRate <= rateList[i]) {
						sRate = rateList[i];
						break;
					}
				}

				for (int i = 0; i < bundleSizeList.length; i++) {
					if (tBundleSize <= bundleSizeList[i]) {
						sBundleSize = bundleSizeList[i];
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

	public void onSensorChanged(String devID, int sType, float[] values, long[] timestamp)
	{
		//Log.i(tag, "values: " + values[0] + "," + values[1] + "," + values[2]);
		SensorParcelWrapper mSensorParcelWrapper = new SensorParcelWrapper(devID, sType, mSensorKey, values, timestamp);
		synchronized (listenersList) {
			// Go through the list of listeners and send the data to them
			for (Map.Entry<String, _Listener> entry : listenersList.entrySet())
			{
				Handler mhandler = entry.getValue().getHandler();
				mhandler.sendMessage(mhandler.obtainMessage(UniversalConstants.MSG_OnSensorChanged, mSensorParcelWrapper));
			}
		}

		Handler mHandler = UniversalDataStore.getHandler();
		if (mHandler != null)
			mHandler.sendMessage(mHandler.obtainMessage(UniversalConstants.MSG_STORE_RECORD, mSensorParcelWrapper));
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

	/** 
	 * Check if the rate and bundlesize requested by the listener are valid
	 * @param mRate
	 * @param mBundleSize
	 * @return
	 */
	synchronized public boolean checkParams(int mRate, int mBundleSize)
	{
		boolean rflag = false, bflag = false;

		for (int i : rateList)
		{
			if ((i % mRate == 0) || (mRate % i == 0)) {
				rflag = true;
				break;
			}
		}

		for (int i : bundleSizeList) {
			if ((i % mBundleSize == 0) || (mBundleSize % i == 0)) {
				bflag = true;
				break;
			}
		}
		return rflag && bflag;
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