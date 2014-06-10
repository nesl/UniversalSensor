package com.ucla.nesl.universalservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalDriverManager;
import com.ucla.nesl.aidl.IUniversalManagerService;
import com.ucla.nesl.aidl.IUniversalSensorManager;
import com.ucla.nesl.aidl.SensorParcel;
import com.ucla.nesl.lib.UniversalConstants;
import com.ucla.nesl.lib.UniversalSensor;
import com.ucla.nesl.lib.UniversalSensorNameMap;

public class UniversalManagerService extends IUniversalManagerService.Stub {
	private static String tag = UniversalManagerService.class.getCanonicalName();
	private Handler mHandler = null; // Cleanup handler
	private Thread cleanupThread;
	UniversalService parent;
	Map<String, UniversalServiceDevice> registeredDevices = new HashMap<String, UniversalServiceDevice>();
	HashMap<String, UniversalServiceListener> registeredListeners = new HashMap<String, UniversalServiceListener>();

	
	private Runnable cleanupRunnable = new Runnable() {
		
		@Override
		public void run() {
			Looper.prepare();
			mHandler = new Handler() {
				@SuppressWarnings("unchecked")
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case UniversalConstants.MSG_UnregisterListener:
						unregisterListenerAll((String)msg.obj);
						break;
					case UniversalConstants.MSG_UnregisterDriver:
						unregisterDriverAll((String) msg.obj);
						break;
					}
				}
			};
			Looper.loop();
		}
	};
	
	public Handler getHandler()
	{
		return mHandler;
	}
	
	public UniversalManagerService(UniversalService parent) {
		this.parent = parent;
		cleanupThread = new Thread(cleanupRunnable);
		cleanupThread.start();
	}

	public String generateSensorKey(String devID, int sType)
	{
		return ""+ devID + "-" + sType;
	}

	public String generateListenerKey(int pid)
	{
		return "" + pid;
	}

	public boolean  notifyListeners(Device mdevice)
	{
		Handler mhandler;
		UniversalServiceListener mlistener;

		Log.i(tag, "notify for new device");
		synchronized (registeredListeners) {
			for (Map.Entry<String, UniversalServiceListener> entry : registeredListeners.entrySet()) {
				Log.d(tag, "sending notification to " + entry.getKey());
				Log.d(tag, "sending notification to " + entry.getValue());
				mlistener = entry.getValue();

				if (mlistener.isNotifySet() == false)
					continue;

				mhandler = mlistener.getHandler();
				if (mhandler == null) {
					Log.e(tag, "mhandler of " + mlistener.callingPid + " is null");
					continue;
				}
				mhandler.sendMessage(mhandler.obtainMessage(UniversalConstants.MSG_NotifyDeviceChanged, mdevice));
			}
		}
		return true;
	}

	public boolean addRegisteredDevice(String devID, UniversalServiceDevice mdevice)
	{
		synchronized (registeredDevices) {
			registeredDevices.put(devID, mdevice);
		}
		return true;
	}

	public UniversalServiceDevice getRegisteredDevice(String devID)
	{
		UniversalServiceDevice mdevice = null;
		synchronized (registeredDevices) {
			mdevice = registeredDevices.get(devID);
		}
		return mdevice;
	}

	public UniversalServiceDevice removeRegisteredDevice(String devID)
	{
		UniversalServiceDevice mdevice = null;
		synchronized (registeredDevices) {
			mdevice = registeredDevices.remove(devID);
		}
		return mdevice;
	}

	public boolean addRegisteredListener(String listenerKey, UniversalServiceListener mlistener)
	{
		synchronized (registeredListeners) {
			registeredListeners.put(listenerKey, mlistener);
		}
		return true;
	}

	public UniversalServiceListener getRegisteredListener(String listenerKey)
	{
		UniversalServiceListener mlistener = null;
		synchronized (registeredListeners) {
			mlistener = registeredListeners.get(listenerKey);
		}
		return mlistener;
	}

	public boolean hasRegisteredListener(String listnerKey)
	{
		synchronized (registeredListeners) {
			return registeredListeners.containsKey(listnerKey);
		}
	}

	public UniversalServiceListener removeRegisteredListener(String listenerKey)
	{
		UniversalServiceListener mlistener = null;
		synchronized (registeredListeners) {
			mlistener = registeredListeners.remove(listenerKey);
		}
		return mlistener;
	}

	@Override
	public ArrayList<Device> listDevices() throws RemoteException {
		ArrayList<Device> deviceList = new ArrayList<Device>();

		Log.i(tag, "listDevices requested by " + Binder.getCallingPid());
		synchronized (registeredDevices) {
			for(Map.Entry<String, UniversalServiceDevice> entry : registeredDevices.entrySet())
			{
				deviceList.add(entry.getValue());
			}
		}
		return deviceList;
	}


	@Override
	public boolean registerDriver(Device mdevice, IUniversalDriverManager mDriverListener, 
			int sType, int[] maxRate, int[] bundleSize) throws RemoteException {
		String mSensorKey = null;
		UniversalServiceDevice mUniversalDevice = null;

		mUniversalDevice = getRegisteredDevice(mdevice.getDevID());
		if (mUniversalDevice == null) {
			// Create an instance of the Device
			Log.d(tag, "registering a new device");
			mUniversalDevice = new UniversalServiceDevice(this, mdevice, mDriverListener);
			addRegisteredDevice(mUniversalDevice.getDevID(), mUniversalDevice);
		}

		Log.d(tag, "registering driver " + mdevice.getVendorID() + " mdevice sensorlist " + mdevice.getSensorList());

		mSensorKey = generateSensorKey(mdevice.getDevID(), sType);
		mUniversalDevice.registerSensor(mSensorKey, sType, maxRate, bundleSize);
		//			if (addDriverSensor(mUniversalDevice.getDevID(), sType, maxRate) == false) {
		//				return false;
		//			}
		//
		//			Log.i(tag, "list of registered sensors");
		//
		//			for(Map.Entry<String, UniversalServiceSensor> entry : registeredSensors.entrySet())
		//				Log.i("tag", "as " + entry.getKey());

		notifyListeners(mdevice);

		return true;
	}

	//		public boolean addDriverSensor(String devID, int sType, int maxRate)
	//				throws RemoteException {
	//			UniversalServiceSensor mSensor = null;
	//			UniversalServiceDevice mdevice = null;
	//
	//			mdevice = getRegisteredDevice(devID);
	//			if (mdevice == null)
	//				return false;
	//			
	//			mSensor = new UniversalServiceSensor(devID, generateSensorKey(devID, sType), mdevice, sType, maxRate);
	//			mdevice.addSensor(mSensor, maxRate);
	//
	//			// add the sensor the universal list of sensors
	//			addRegisteredSensor(generateSensorKey(devID, sType), mSensor);
	//			return true;
	//		}
	//
	//		public boolean removeDriverSensor(UniversalServiceDevice mdevice, String devID, int sType)
	//				throws RemoteException {
	//			String 				   mSensorKey = null;
	//			UniversalServiceSensor mSensor 	  = null;
	//			
	//			mSensorKey = generateSensorKey(devID, sType);
	//			mSensor = removeRegisteredSensor(mSensorKey);
	//			if (mSensor == null) 
	//				return false;
	//
	//			// Notify all the listeners
	//			mSensor.unregister();
	//
	//			// Unregister the sensor from its device
	//			mdevice.removeSensor(mSensor);
	//
	//			return true;
	//		}

	@SuppressWarnings("unchecked")
	@Override
	public boolean unregisterDriver(String devID, int sType) {
		// remove the entry from the registeredDevices,
		// we should send notifications to all the applications that have asked for the notification
		// also free all the sensor objects

		String mSensorKey = null;

		Log.i(tag, "unregisterDriver: " + devID + ", sensorType " + sType);

		UniversalServiceDevice mdevice = getRegisteredDevice(devID);
		if (mdevice == null) {
			return false;
		}

		mSensorKey = generateSensorKey(devID, sType);
		mdevice.unregisterSensor(sType, mSensorKey);

		if (mdevice.isEmpty()) {
			Log.d(tag, "no more sensors, removing the device entry");
			removeRegisteredDevice(devID);
		}

		return true;
	}

	private boolean unregisterDriverAll(String devID)
	{
		return unregisterDriver(devID, UniversalSensor.TYPE_ALL);
	}

	private void _registerListener(int listenerPid, String mlistenerKey, IUniversalSensorManager cbk)
	{
		Log.d(tag, "adding a new listener with pid " + mlistenerKey);
		UniversalServiceListener mlistener = new UniversalServiceListener(this, cbk, listenerPid);
		mlistener.start();
		Handler mhandler = mlistener.getHandler();
		while (mhandler == null) {
			Log.d(tag, "_registerListener::handler is null");
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mhandler = mlistener.getHandler();
		}
		addRegisteredListener(mlistenerKey, mlistener);			
	}

	// TODO: Check if the listener has already registered
	@Override
	public boolean registerListener(IUniversalSensorManager mManager,
			String devID, int sType, int rate, int bundleSize) throws RemoteException {
		String mSensorKey   = generateSensorKey(devID, sType);
		int listenerPid     = Binder.getCallingPid();
		String mListenerKey = generateListenerKey(listenerPid);
		UniversalServiceDevice mDevice = null;
		UniversalServiceSensor mSensor = null;

		mDevice = getRegisteredDevice(devID);
		if (mDevice == null) {
			Log.e(tag, "Invalid device mentioned, registeration failed");
			return false;
		}

		mSensor = mDevice.getSensor(mSensorKey);
		if (mSensor == null) {
			// a null here means that the app wants to register to a sensor that
			// doesn't exist.
			Log.e(tag, "Incorrect sensor type, registering failed");
			return false;
		}

		// Check if this is a supported Rate and bundlesize
		if (!mSensor.checkParams(rate, bundleSize)) {
			Log.e(tag, "Incorrect rate or bundleSize, registerListener failed");
			return false;
		}

		// Add the listener to the map if it already doesn't exists
		if (!hasRegisteredListener(mListenerKey)) {
			_registerListener(listenerPid, mListenerKey, mManager);
		}
		UniversalServiceListener mlistener = getRegisteredListener(mListenerKey);

		Log.i(tag, "Registering to the sensor " + mSensorKey);

		Map<String, Object> mMap = new HashMap<String, Object>();
		mMap.put("key", mSensorKey);
		mMap.put("value", mSensor);
		mMap.put("rate", Integer.valueOf(rate));
		mMap.put("bundleSize", Integer.valueOf(bundleSize));

		Handler mhandler = mlistener.getHandler();
		if (mhandler == null) {
			Log.i(tag, "registerListener::handler is null");
			return false;
		}
		mhandler.sendMessage(mhandler.obtainMessage(UniversalConstants.MSG_Link_Sensor, mMap));

		Log.i(tag, "registering listener " +  Binder.getCallingPid() + ", " + sType);

		return true;
	}

	@Override
	public boolean unregisterListener(String devID, int sType) {
		String mListenerKey = generateListenerKey(Binder.getCallingPid());
		String mSensorKey = generateSensorKey(devID, sType);
		UniversalServiceListener mlistener;
		//			Handler mHandler;

		Log.d(tag, "unregisterListener on " + devID + ":" + sType);
		// remove the entry from registeredListener
		if (!hasRegisteredListener(mListenerKey)) {
			Log.i(tag, "Failed to unregister listener to sensor " + devID + ":" + UniversalSensorNameMap.getName(sType));
			return false;
		}
		mlistener = getRegisteredListener(mListenerKey);

		// Calling unregister directly for now because the
		// next call to check empty will never succeed if
		// we send a message via handler. One fix is we can
		// sleep for a while or make mlistener class trigger
		// cleanup when it is no more registered with a sensor.
		//			mlistener.getHandler()unregister(mSensorKey);
		mlistener.unregister(mSensorKey);

		// remove the entry only when this is the last sensor
		// that this app is registered to
		if (mlistener.isEmpty()) {
			// this is the last entry, so remove the listener from
			// registeredListeners
			removeRegisteredListener(mListenerKey);
		}

		// We should disable the sensor if removal of the
		// listener has made its listener list empty
		//				setDriverSensorRate(devID, sType, msensor.isEmpty()? 0 : msensor.getNextRate());
		return true;
	}

	public boolean unregisterListenerAll(String listenerKey)
	{
		UniversalServiceListener mlistener;
		
		mlistener = getRegisteredListener(listenerKey);
		
		mlistener.unregister();
		return true;
	}
	
	//		@Override
	//		public void onSensorChanged(SensorParcel event) throws RemoteException {
	//			String key = generateSensorKey(event.devID, event.sType);
	//
	//			UniversalServiceSensor msensor = getRegisteredDevice(event.devID).getRegisteredSensor(key);
	//			if(msensor == null) {
	//				Log.i(tag, "msensor is null " + key);
	//				return;
	//			}
	//
	//			msensor.onSensorChanged(event);
	//			return;
	//		}

	@Override
	public String getDevID() throws RemoteException {
		return new String(""+Math.random());  //compute devID, for now using a random number
	}

	@Override
	public void registerNotification(IUniversalSensorManager mManager)
			throws RemoteException {
		String mlistenerKey = generateListenerKey(Binder.getCallingPid());

		Log.i(tag, "adding listener to notification list " + Binder.getCallingPid());

		// Add the listener to the map if it already doesn't exists
		if (!hasRegisteredListener(mlistenerKey)) {
			_registerListener(Binder.getCallingPid(), mlistenerKey, mManager);
		}
		UniversalServiceListener mlistener = getRegisteredListener(mlistenerKey);

		mlistener.setNotify();
	}

	@Override
	public void onSensorChanged(SensorParcel[] event, int length)
			throws RemoteException {
		String key = generateSensorKey(event[0].devID, event[0].sType);
		event[0].mSensorKey = key;

		UniversalServiceSensor msensor = getRegisteredDevice(event[0].devID).getRegisteredSensor(key);
		if(msensor == null) {
			Log.i(tag, "msensor is null " + key);
			return;
		}

		msensor.onSensorChanged(event, length);

	}
}