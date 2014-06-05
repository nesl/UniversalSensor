package com.ucla.nesl.universalservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalDriverManager;
import com.ucla.nesl.aidl.IUniversalManagerService;
import com.ucla.nesl.aidl.IUniversalSensorManager;
import com.ucla.nesl.aidl.SensorParcel;
import com.ucla.nesl.lib.UniversalConstants;
import com.ucla.nesl.lib.UniversalSensor;

public class UniversalService extends Service {
	private String tag = UniversalService.class.getCanonicalName();
	Random rn = new Random();
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(tag, "onBind called ");

		return new UniversalManagerService(this);
	}

	private class UniversalManagerService extends IUniversalManagerService.Stub {
		UniversalService parent;
		Map<String, UniversalServiceDevice> registeredDevices = new HashMap<String, UniversalServiceDevice>();
		Map<String, UniversalServiceSensor> registeredSensors = new HashMap<String, UniversalServiceSensor>();
		HashMap<String, UniversalServiceListener> registeredListeners = new HashMap<String, UniversalServiceListener>();

		public UniversalManagerService(UniversalService parent) {
			this.parent = parent;
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
//					Log.i(tag, "Sending message to handler");
					if (mhandler == null) {
						Log.e(tag, "mhandler of " + mlistener.callingPid + " is null");
						continue;
					}
					mhandler.sendMessage(mhandler.obtainMessage(UniversalConstants.MSG_NotifyDeviceChanged, mdevice));
//					Log.i(tag, "message sent to handler");
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

		public boolean addRegisteredSensor(String sensorKey, UniversalServiceSensor msensor)
		{
			synchronized (registeredSensors) {
				registeredSensors.put(sensorKey, msensor);
			}
			return true;
		}

		public UniversalServiceSensor getRegisteredSensor(String sensorKey)
		{
			UniversalServiceSensor msensor = null;
			synchronized (registeredSensors) {
				msensor = registeredSensors.get(sensorKey);
			}
			return msensor;
		}

		public UniversalServiceSensor removeRegisteredSensor(String sensorKey)
		{
			UniversalServiceSensor msensor = null;
			synchronized (registeredSensors) {
				msensor = registeredSensors.remove(sensorKey);
			}
			return msensor;
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

//		public boolean setDriverSensorRate(String devID, int sType, int rate)  throws RemoteException
//		{
//			UniversalServiceDevice mdevice;
//			mdevice = getRegisteredDevice(devID);
//			mdevice.mDriverStub.setRate(sType, rate);
//			return true;
//		}

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
				int sType, int maxRate) throws RemoteException {
			UniversalServiceDevice mUniversalDevice = null;

			mUniversalDevice = getRegisteredDevice(mdevice.getDevID());
			if (mUniversalDevice == null) {
				// Create an instance of the Device
				Log.d(tag, "registering a new device");
				mUniversalDevice = new UniversalServiceDevice(mdevice, mDriverListener);
				addRegisteredDevice(mUniversalDevice.getDevID(), mUniversalDevice);
			}

			Log.d(tag, "registering driver " + mdevice.getVendorID() + " mdevice sensorlist " + mdevice.getSensorList());

			if (addDriverSensor(mUniversalDevice.getDevID(), sType, maxRate) == false) {
				return false;
			}
//
//			Log.i(tag, "list of registered sensors");
//
//			for(Map.Entry<String, UniversalServiceSensor> entry : registeredSensors.entrySet())
//				Log.i("tag", "as " + entry.getKey());

			notifyListeners(mdevice);

			return true;
		}

		public boolean addDriverSensor(String devID, int sType, int maxRate)
				throws RemoteException {
			UniversalServiceSensor mSensor = null;
			UniversalServiceDevice mdevice = null;

			mdevice = getRegisteredDevice(devID);
			if (mdevice == null)
				return false;
			
			mSensor = new UniversalServiceSensor(devID, generateSensorKey(devID, sType), mdevice, sType, maxRate);
			mdevice.addSensor(mSensor, maxRate);

			// add the sensor the universal list of sensors
			addRegisteredSensor(generateSensorKey(devID, sType), mSensor);
			return true;
		}

		public boolean removeDriverSensor(UniversalServiceDevice mdevice, String devID, int sType)
				throws RemoteException {
			String 				   mSensorKey = null;
			UniversalServiceSensor mSensor 	  = null;
			
			mSensorKey = generateSensorKey(devID, sType);
			mSensor = removeRegisteredSensor(mSensorKey);
			if (mSensor == null) 
				return false;

			// Notify all the listeners
			mSensor.unregister();

			// Unregister the sensor from its device
			mdevice.removeSensor(mSensor);

			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean unregisterDriver(String devID, int sType) throws RemoteException {
			// remove the entry from the registeredDevices,
			// we should send notifications to all the applications that have asked for the notification
			// also free all the sensor objects
			Log.i(tag, "unregisterDriver: " + devID + ", sensorType " + sType);

			UniversalServiceDevice mdevice = getRegisteredDevice(devID);
			if (mdevice == null) {
				return false;
			}

			mdevice.unregisterSensor(sType);
//			if (sType == UniversalSensor.TYPE_ALL) {
//				// It seems like a race here
//				Log.i(tag, "removing all the registered sensors from the device " + mdevice.getVendorID());				
//				ArrayList<Integer> sensorList = (ArrayList<Integer>) mdevice.getSensorList().clone();
//				for (int stype:sensorList) {
//					Log.i(tag, "removing sensor " + stype);
//					removeDriverSensor(mdevice, devID, stype);
//				}
//			} else {
//				String mSensorKey = gener
//				removeDriverSensor(mdevice, devID, sType);
//			}

			if (mdevice.isEmpty()) {
				Log.d(tag, "no more sensors, removing the device entry");
				removeRegisteredDevice(devID);
			}

			return true;
		}

		private void _registerListener(int listenerPid, String mlistenerKey, IUniversalSensorManager mManager)
		{
			Log.d(tag, "adding a new listener with pid " + mlistenerKey);
			UniversalServiceListener mlistener = new UniversalServiceListener(mManager, listenerPid);
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
				String devID, int sType, int rate, float updateInterval) throws RemoteException {
			String msensorKey   = generateSensorKey(devID, sType);
			int listenerPid     = Binder.getCallingPid();
			String mlistenerKey = generateListenerKey(listenerPid);

			Log.i(tag, "registering listener " +  Binder.getCallingPid() + ", " + sType);

			if (getRegisteredSensor(msensorKey) == null) {
				// a null here means that the app wants to register to a sensor that
				// doesn't exist.
				Log.e(tag, "Incorrect sensor type, registering failed");
				return false;
			}

			UniversalServiceSensor msensor = getRegisteredSensor(msensorKey);

			// Add the listener to the map if it already doesn't exists
			if (!hasRegisteredListener(mlistenerKey)) {
				_registerListener(listenerPid, mlistenerKey, mManager);
			}
			UniversalServiceListener mlistener = getRegisteredListener(mlistenerKey);

			Log.i(tag, "Registering to the sensor " + msensorKey);
//			msensor.linkListener(mlistener, rate);

			Map<String, Object> mMap = new HashMap<String, Object>();
			mMap.put("key", msensorKey);
			mMap.put("value", msensor);
			mMap.put("rate", Integer.valueOf(rate));
			mMap.put("updateInterval", Float.valueOf(updateInterval));

			Handler mhandler = mlistener.getHandler();
			if (mhandler == null) {
				Log.i(tag, "registerListener::handler is null");
				return false;
			}
			mhandler.sendMessage(mhandler.obtainMessage(UniversalConstants.MSG_Link_Sensor, mMap));

			// now enable that particular devices sensor
			// This functionality must be moved to universalservicesensor class
			// inside the function linkListener
//			try {
//				setDriverSensorRate(devID, sType, msensor.getNextRate());
//			} catch (RemoteException e) {
//				return false;
//			}
			return true;
		}

		@Override
		public boolean unregisterListener(String devID, int sType)
				throws RemoteException {
			String mListenerKey = generateListenerKey(Binder.getCallingPid());
			String mSensorKey = generateSensorKey(devID, sType);
			UniversalServiceListener mlistener;
			UniversalServiceSensor   msensor;

			// remove the entry from registeredListener
			if (!hasRegisteredListener(mListenerKey)) {
				return false;
			} else {
				mlistener = getRegisteredListener(mListenerKey);

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
				msensor = registeredSensors.get(mSensorKey);
//				setDriverSensorRate(devID, sType, msensor.isEmpty()? 0 : msensor.getNextRate());
			}
			return true;
		}

		@Override
		public void onSensorChanged(SensorParcel event) throws RemoteException {
			String key = generateSensorKey(event.devID, event.sType);

			UniversalServiceSensor msensor = getRegisteredSensor(key);
			if(msensor == null) {
				Log.i(tag, "msensor is null " + key);
				for(Map.Entry<String, UniversalServiceSensor> entry : registeredSensors.entrySet())
					Log.i("tag", "as " + entry.getKey());
				// may be through exception, for now just returning
				return;
			}

			msensor.onSensorChanged(event);
			return;
		}

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
		public void onSensorChangedArray(SensorParcel[] event, int length)
				throws RemoteException {
			Log.i(tag, "onSensorChangedArray");
			for (int i = 0; i < length; i++)
			{
				Log.i(tag, "Type: " + event[i].sType);
			}
		}
	}
}
