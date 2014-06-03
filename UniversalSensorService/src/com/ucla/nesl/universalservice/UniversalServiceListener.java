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
	private HashMap<String, UniversalServiceSensor> sensorsList = new HashMap<String, UniversalServiceSensor>();
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
	
	private void linkSensor(String sensorID,  UniversalServiceSensor sensor)
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
			mlistener.notifySensorChanged(mSensor.getDevID(), mSensor.sType, UniversalConstants.ACTION_UNREGISTER);
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

//	public IUniversalSensorManager getListener()
//	{
//		return mlistener;
//	}
//	
	public boolean isEmpty()
	{
		synchronized (sensorsList) {
			return sensorsList.isEmpty();
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
	
	@Override
	public void run()
	{
		Looper.prepare();
		threadLooper = Looper.myLooper();
		Log.i(tag, "starting thread");
		mhandler = new Handler() {
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
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map =  (HashMap<String, Object>) msg.obj;
					linkSensor((String)map.get("key"), (UniversalServiceSensor) map.get("value"));
					break;
				case UniversalConstants.MSG_NotifyDeviceChanged:
					notifyDeviceChange((Device) msg.obj);
				default:
					break;
				}
			}
		};
		Looper.loop();
	}
}
