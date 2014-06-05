package com.ucla.nesl.universalservice;

import java.util.HashMap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalSensorManager;
import com.ucla.nesl.aidl.SensorParcel;
import com.ucla.nesl.lib.UniversalConstants;

public class UniversalServiceListener extends Thread {
	private static String tag = UniversalServiceListener.class.getCanonicalName();
	private IUniversalSensorManager mlistener;
	private HashMap<String, _Sensor> sensorMap = new HashMap<String, _Sensor>();
	public int callingPid;
	private boolean isNotify = false;
	private Looper threadLooper;
	private Handler mhandler;
	
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

	public Handler getHandler()
	{
		return mhandler;
	}
	
	public boolean setNotify()
	{
		isNotify = true;
		return true;
	}
	
	public boolean isNotifySet()
	{
		return isNotify;
	}
	
	private void linkSensor(HashMap<String, Object> mMap)
	{
		int						rate 	 	= -1;
		float					updateInterval = 0;
		String 					sensorID 	= null;
		UniversalServiceSensor 	universalSensor = null; 
		_Sensor 				mSensor		= null;
		
		sensorID 		= (String) mMap.get("key");
		universalSensor = (UniversalServiceSensor) mMap.get("value");
		rate	 		= (Integer) mMap.get("rate");
		updateInterval 	= (Float) mMap.get("updateInterval");

		mSensor = new _Sensor(sensorID, universalSensor, rate, updateInterval);

		synchronized (sensorMap) {
			sensorMap.put(sensorID, mSensor);
		}
		universalSensor.linkListener(""+callingPid, this, rate, updateInterval);
	}

	// This method is called by the sensor when it is no longer available
	public void unlinkSensor(String sensorID)
	{
/*		UniversalServiceSensor mSensor;
		synchronized (sensorsList) {
//			mSensor = sensorsList.remove(sensorID);
		}

		try {
			mlistener.notifySensorChanged(mSensor.getDevID(), mSensor.sType, UniversalConstants.ACTION_UNREGISTER);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
*/	}

	// This functioned is called by the service when the app wants
	// to unregister itself with one or more sensors. For now let us
	// assume that we only handle one sensor unregister only
	// TODO: extend it to unregister all
	public void unregister(String key)//, int sType)
	{
/*		// remove the entry from its list
		UniversalServiceSensor mSensor;
		synchronized (sensorsList) {
			mSensor = sensorsList.remove(key);
		}
		mSensor.unlinkListner(this);
*/	}

//	public IUniversalSensorManager getListener()
//	{
//		return mlistener;
//	}
//	
	public boolean isEmpty()
	{
		synchronized (sensorMap) {
			return sensorMap.isEmpty();
		}
	}

	public void onSensorChanged(SensorParcel event)
	{
		try {
			mlistener.onSensorChanged(event);
			Log.i(tag, "sending event to listener");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void quit()
	{
		threadLooper.quit();
	}
	
	private void notifyDeviceChange(Device mdevice)
	{
		try {
			Log.i(tag, "sending notification");
			mlistener.notifyDeviceChange(mdevice);
			Log.i(tag, "notification sent");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void updateSamplingParam(Bundle bundle)
	{
		_Sensor mSensor = null;
		String mSensorKey = bundle.getString(UniversalConstants.sType);
		int    sRate = bundle.getInt(UniversalConstants.rate);
		float  sUpdateInterval = bundle.getFloat(UniversalConstants.updateInterval);
		if (sensorMap.containsKey(mSensorKey)) {
			mSensor = sensorMap.get(mSensorKey);
			mSensor.updateSamplingParam(sRate, sUpdateInterval);
		}
	}

	@Override
	public void run()
	{
		Looper.prepare();
		threadLooper = Looper.myLooper();
		Log.i(tag, "starting thread");
		mhandler = new Handler() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UniversalConstants.MSG_OnSensorChanged:
					onSensorChanged((SensorParcel) msg.obj);
					break;
				case UniversalConstants.MSG_Quit:
					quit();
					break;
				case UniversalConstants.MSG_Link_Sensor:
					linkSensor((HashMap<String, Object>) msg.obj);
					break;
				case UniversalConstants.MSG_NotifyDeviceChanged:
					notifyDeviceChange((Device) msg.obj);
					break;
				case UniversalConstants.MSG_UpdateSamplingParam:
					updateSamplingParam((Bundle) msg.obj);
					break;
				case UniversalConstants.MSG_Unlink_Sensor:
					break;
				default:
					break;
				}
			}
		};
		Looper.loop();
	}
	
	private class _Sensor
	{
		int						lRate 	 	    = -1;
		float					lUpdateInterval = 0;
		int						sRate 	 	    = -1;
		float					sUpdateInterval = 0;
		String 					sensorID 	= null;
		UniversalServiceSensor 	msensor  	= null; 

		public _Sensor(String sensorID, UniversalServiceSensor msensor,
				int lRate, float lUpdateInterval)
		{
			this.sensorID = new String(sensorID);
			this.msensor  = msensor;
			this.lRate    = lRate;
			this.lUpdateInterval = lUpdateInterval;
		}
		
		public void updateSamplingParam(int sRate, float sUpdateInterval)
		{
			this.sRate    = sRate;
			this.sUpdateInterval = sUpdateInterval;
		}
	}
}
