package com.ucla.nesl.universalservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalSensorManager;
import com.ucla.nesl.lib.SensorParcelWrapper;
import com.ucla.nesl.lib.UniversalConstants;

public class UniversalServiceListener extends Thread {
	private static String tag = UniversalServiceListener.class.getCanonicalName();
	private UniversalManagerService mService;
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

	public UniversalServiceListener(UniversalManagerService mService, IUniversalSensorManager listener, int callingPid)
	{
		this.mService   = mService;
		this.mlistener  = listener;
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
		boolean					periodic	= false;
		UniversalServiceSensor 	universalSensor = null; 
		_Sensor 				mSensor		= null;

		sensorID 		= (String) mMap.get("key");
		universalSensor = (UniversalServiceSensor) mMap.get("value");
		rate	 		= (Integer) mMap.get("rate");
		bundleSize 		= (Integer) mMap.get("bundleSize");

		synchronized (sensorMap) {
			if (!sensorMap.containsKey(sensorID)) {
				mSensor = new _Sensor(this, mlistener, sensorID, universalSensor, periodic, rate, bundleSize);
				sensorMap.put(sensorID, mSensor);
			} else {
				mSensor = sensorMap.get(sensorID);
				mSensor.updateListenerParam(periodic, rate, bundleSize);
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

	public IUniversalSensorManager getListener()
	{
		return mlistener;
	}


	public void unregister()
	{
		HashMap<String, _Sensor> _mSensorMap = null;
		synchronized (sensorMap) {
			_mSensorMap = sensorMap;
			sensorMap   = new HashMap<String, _Sensor>();
		}

		for (Map.Entry<String, _Sensor> entry : _mSensorMap.entrySet()) {
			entry.getValue().getRegisteredSensor().unlinkListner(getID());
		}
	}
	public boolean isEmpty()
	{
		synchronized (sensorMap) {
			return sensorMap.isEmpty();
		}
	}

	public void onSensorChanged(String devID, int sType, float[] values, long[] timestamp)
	{
		String	mSensorKey = mService.generateSensorKey(devID, sType);
		_Sensor mSensor = null;
		synchronized (sensorMap) {
			if (sensorMap.containsKey(mSensorKey)) {
				mSensor = sensorMap.get(mSensorKey);
				mSensor.onSensorChanged(devID, sType, values, timestamp);
			}
		}
	}

	private void quit()
	{
		threadLooper.quit();
	}

	public String getID()
	{
		return mService.generateListenerKey(callingPid);
	}

	private void notifyNewDevice(Device mdevice)
	{
		try {
			mlistener.notifyNewDevice(mdevice);
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

	public void pushData(String mSensorKey)
	{
		_Sensor mSensor = null;
		synchronized (sensorMap) {
			if (sensorMap.containsKey(mSensorKey)) {
				mSensor = sensorMap.get(mSensorKey);
			}
		}
		mSensor.sendData();
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
					SensorParcelWrapper spw = (SensorParcelWrapper) msg.obj;
					onSensorChanged(spw.devID, spw.sType, spw.values, spw.timestamp);
					break;
				case UniversalConstants.MSG_Quit:
					quit();
					break;
				case UniversalConstants.MSG_Link_Sensor:
					linkSensor((HashMap<String, Object>) msg.obj);
					break;
				case UniversalConstants.MSG_NotifyNewDevice:
					notifyNewDevice((Device) msg.obj);
					break;
				case UniversalConstants.MSG_UpdateSamplingParam:
					updateSamplingParam((Bundle) msg.obj);
					break;
				case UniversalConstants.MSG_Unlink_Sensor:
					unlinkSensor((String) msg.obj);
					break;
				case UniversalConstants.MSG_UnregisterListener:
					unregister((String) msg.obj);
					break;
				case UniversalConstants.MSG_FETCH_RECORD:
					pushData((String) msg.obj);
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
		int 					counter     = 0;
		int						lRate 	    = -1;
		int  					lbundleSize = 0;
		int						sRate 	    = -1;
		int					    sbundleSize = 0;
		String 					sensorID 	= null;
		boolean					periodic	= false;
		int						valuesLength = -1;
		int						valueIndex	= 0;
		int 					tsIndex		= 0;
		float[]					values		= null;
		long[]					timestamp	= null;
		ArrayList<Float>		valueQueue	= new ArrayList<Float>();
		ArrayList<Long> 		tsQueue		= new ArrayList<Long>();
		UniversalServiceSensor 	mSensor  	= null;
		UniversalServiceListener parent     = null;
		IUniversalSensorManager mlistener   = null;

		public _Sensor(UniversalServiceListener parent, IUniversalSensorManager mlistener,
				String sensorID, UniversalServiceSensor mSensor, boolean periodic, int lRate, int lBundleSize)
		{
			this.parent    = parent;
			this.mlistener = mlistener;
			this.sensorID  = new String(sensorID);
			this.mSensor   = mSensor;
			this.valuesLength = UniversalConstants.getValuesLength(mSensor.sType);
			updateListenerParam(periodic, lRate, lBundleSize);
//			eventQueue 		= new ArrayList<SensorParcel>();
		}

	    private void updatecounter()
		{
			counter = (int) Math.ceil(1.0*sRate/lRate);
		}

		public void updateListenerParam(boolean periodic, int lRate, int lBundleSize)
		{
			this.periodic  = periodic;
			this.lRate     = lRate;
			this.lbundleSize = lBundleSize;
			initIndex();
			values 		   = new float[valuesLength * lBundleSize];
			timestamp	   = new long[lBundleSize];
			updatecounter();
			Log.i(tag, "updateListenerParam: rate:" + lRate + " bundleSize: " + lbundleSize + " counter: " + counter);
		}

		private void initIndex()
		{
			valueIndex = tsIndex = 0;
		}

		public void updateSamplingParam(int sRate, int sBundleSize)
		{
			this.sRate    = sRate;
			this.sbundleSize = sBundleSize;
			updatecounter();
			Log.i(tag, "updateSamplingParam: rate:" + sRate + " bundleSize: " + sbundleSize + " counter: " + counter);

		}

		public UniversalServiceSensor getRegisteredSensor()
		{
			return mSensor;
		}

		public boolean linkListener()
		{
			return mSensor.linkListener(""+parent.callingPid, parent, lRate, lbundleSize);
		}

		private void queueData()
		{
			while (tsQueue.size() > lbundleSize) {
				for (int i = 0; i < valuesLength; i++)
					valueQueue.remove(0);
				tsQueue.remove(0);
			}
		}

		private void sendData()
		{			
			while (tsQueue.size() >= lbundleSize) {
				initIndex();
				for (int i = 0; i < lbundleSize; i++) {
					for (int j = 0; j < valuesLength; j++)
						values[valueIndex++] = valueQueue.remove(0);
					timestamp[tsIndex++] = tsQueue.remove(0);
				}
				try {
					//Log.i(tag, "values: " + values[0] + "," + values[1] + "," + values[2]);
					mlistener.onSensorChanged(mSensor.getDevID(), mSensor.sType, values, timestamp);
				} catch (DeadObjectException e) {
					Log.e(tag, "Listener " + parent.getId() + " is dead, cleaning it up");
					Handler mhandler = parent.mService.getHandler();
					mhandler.sendMessage(mhandler.obtainMessage(UniversalConstants.MSG_UnregisterListener, parent.getID()));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

		synchronized public void onSensorChanged(String devID, int sType, float[] values, long[] timestamp)
		{
			int valuesLength = UniversalConstants.getValuesLength(sType);
			for (int i = 0, k = 0; i < timestamp.length; i++, k += valuesLength) {
				if (--counter > 0)
					continue;
				for (int j = 0; j < valuesLength; j++) {
					valueQueue.add(values[k + j]);
				}
				tsQueue.add(timestamp[i]);
				updatecounter();
			}
			
			/*StringBuilder sb = new StringBuilder();
			sb.append("after values in q: ");
			for (float v : valueQueue) {
				sb.append(v); sb.append(",");
			}
			Log.i(tag, sb.toString());*/
			
			if (periodic)
				queueData();
			else
				sendData();
		}
	}
}
