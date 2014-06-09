package com.ucla.nesl.universalservice;

import java.util.ArrayList;
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
import com.ucla.nesl.lib.SensorParcelWrapper;
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
		int 					bundleSize  = 0;
		String 					sensorID 	= null;
		UniversalServiceSensor 	universalSensor = null; 
		_Sensor 				mSensor		= null;
		
		sensorID 		= (String) mMap.get("key");
		universalSensor = (UniversalServiceSensor) mMap.get("value");
		rate	 		= (Integer) mMap.get("rate");
		bundleSize 		= (Integer) mMap.get("bundleSize");

		synchronized (sensorMap) {
			if (!sensorMap.containsKey(sensorID)) {
				mSensor = new _Sensor(this, mlistener, sensorID, universalSensor, rate, bundleSize);
				sensorMap.put(sensorID, mSensor);
			} else {
				mSensor = sensorMap.get(sensorID);
				mSensor.updateListenerParam(rate, bundleSize);
			}
		}

//		universalSensor.linkListener(""+callingPid, this, rate, bundleSize);
		mSensor.linkListener();
	}

	/**
	 *  This method is called by the sensor when it is no longer available
	 * @param sensorID
	 */
	public void unlinkSensor(String sensorID)
	{
		UniversalServiceSensor mUniversalServiceSensor;
		_Sensor mSensor;
		synchronized (sensorMap) {
			mSensor = sensorMap.remove(sensorID);
		}

		if (mSensor == null) {
			return;
		}
		mUniversalServiceSensor = mSensor.getRegisteredSensor();
		try {
			mlistener.notifySensorChanged(mUniversalServiceSensor.getDevID(),
			mUniversalServiceSensor.sType, UniversalConstants.ACTION_UNREGISTER);
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
		_Sensor mSensor;
		synchronized (sensorMap) {
			mSensor = sensorMap.remove(key);
		}

		if (mSensor != null)
			mSensor.getRegisteredSensor().unlinkListner("" + callingPid);
	}

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

	public void onSensorChanged(SensorParcel[] event, int length)
	{
		_Sensor mSensor = null;
		synchronized (sensorMap) {
			if (sensorMap.containsKey(event[0].mSensorKey)) {
				mSensor = sensorMap.get(event[0].mSensorKey);
				mSensor.onSensorChaged(event, length);
			}
		}
//		try {
//			mlistener.onSensorChanged(event);
//		} catch (RemoteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
		int    sBundleSize = bundle.getInt(UniversalConstants.bundleSize);
		
		synchronized (sensorMap) {
			if (sensorMap.containsKey(mSensorKey)) {
				mSensor = sensorMap.get(mSensorKey);
				mSensor.updateSamplingParam(sRate, sBundleSize);
			}
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
					onSensorChanged(((SensorParcelWrapper) msg.obj).sp, ((SensorParcelWrapper) msg.obj).length);
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
					unlinkSensor((String) msg.obj);
					break;
				case UniversalConstants.MSG_UnregisterListener:
					unregister((String) msg.obj);
				default:
					break;
				}
			}
		};
		Looper.loop();
	}
	
	private class _Sensor
	{
		int 					counter     = 0;
		int						lRate 	    = -1;
		int  					lbundleSize = 0;
		int						sRate 	    = -1;
		int					    sbundleSize = 0;
		String 					sensorID 	= null;
		ArrayList<SensorParcel> eventQueue  = null;
		UniversalServiceSensor 	mSensor  	= null;
		UniversalServiceListener parent     = null;
		IUniversalSensorManager mlistener    = null;

		public _Sensor(UniversalServiceListener parent, IUniversalSensorManager mlistener,
				String sensorID, UniversalServiceSensor mSensor, int lRate, int bundleSize)
		{
			this.parent    = parent;
			this.mlistener = mlistener;
			this.sensorID  = new String(sensorID);
			this.mSensor   = mSensor;
			this.lRate     = lRate;
			this.lbundleSize = bundleSize;
			eventQueue 		= new ArrayList<SensorParcel>();
		}
		
		private void updatecounter()
		{
			counter = (int) Math.ceil(1.0*sRate/lRate);
		}
		
		public void updateListenerParam(int lRate, int lBundleSize)
		{
			this.lRate     = lRate;
			this.lbundleSize = lBundleSize;
			updatecounter();
		}
		
		public void updateSamplingParam(int sRate, int sBundleSize)
		{
			this.sRate    = sRate;
			this.sbundleSize = sBundleSize;
			updatecounter();
		}
		
		public UniversalServiceSensor getRegisteredSensor()
		{
			return mSensor;
		}
		
		public boolean linkListener()
		{
			return mSensor.linkListener(""+parent.callingPid, parent, lRate, lbundleSize);
		}
		
		private void sendData()
		{
			SensorParcel[] eventBundle = null;
			
			while (eventQueue.size() >= lbundleSize) {
				eventBundle = new SensorParcel[lbundleSize];
				for (int i = 0; i < lbundleSize; i++) {
					eventBundle[i] = eventQueue.remove(0);
				}
				try {
					mlistener.onSensorChanged(eventBundle);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		public void onSensorChaged(SensorParcel[] sp, int length)
		{
			for (int i = 0; i < sp.length; i++) {
				counter--;
				if (counter <= 0) {
					eventQueue.add(sp[i]);
					updatecounter();
				}
			}
			sendData();
		}
	}
}
