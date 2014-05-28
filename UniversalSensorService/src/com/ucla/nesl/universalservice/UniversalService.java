package com.ucla.nesl.universalservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalDriverManager;
import com.ucla.nesl.aidl.IUniversalManagerService;
import com.ucla.nesl.aidl.IUniversalSensorManager;
import com.ucla.nesl.aidl.SensorParcel;
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
    	HashMap<String, UniversalServiceListener> registeredNotifiers = new HashMap<String, UniversalServiceListener>();

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

    	public boolean addListenerToNotificationList(String key, UniversalServiceListener mlistener)
    	{
    		synchronized (registeredNotifiers) {
    			if(!registeredNotifiers.containsKey(key)) {
    				Log.i(tag, "registering listener for notification");
    				registeredNotifiers.put(key, mlistener);
    			}
			}
    		return true;
    	}

    	public boolean  notifyListeners(Device mdevice)
    	{
    		Log.i(tag, "notify for new device");
    		synchronized (registeredNotifiers) {
				for (Map.Entry<String, UniversalServiceListener> entry : registeredNotifiers.entrySet()) {
					try {
						Log.i(tag, "sending notification to " + entry.getKey());
						entry.getValue().getListener().notifyDeviceChange(mdevice);
					} catch(RemoteException e) {return false;}
				}
    		}
    		return true;
    	}

//    	public boolean removeListenerFromNotificationList(UniversalServiceListener mlistener)
//    	{
//    		synchronized (notificationList) {
//    			if (notificationList.contains(mlistener)) {
//    				notificationList.remove(mlistener);
//    				return true;
//    			} else
//    				return false;
//			}
//    	}

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
    	
    	public boolean setDriverSensorRate(String devID, int sType, int rate)  throws RemoteException
    	{
    		UniversalServiceDevice mdevice;
			mdevice = getRegisteredDevice(devID);
			mdevice.mDriverStub.setRate(sType, rate);
			return true;
    	}

		@Override
		public ArrayList<Device> listDevices() throws RemoteException {
			ArrayList<Device> deviceList = new ArrayList<Device>();

			synchronized (registeredDevices) {
				for(Map.Entry<String, UniversalServiceDevice> entry : registeredDevices.entrySet())
				{
					deviceList.add(entry.getValue());
				}
			}
			return deviceList;
		}

		@Override
		public boolean registerDriver(Device mdevice, IUniversalDriverManager mDriverListener, int sType) throws RemoteException {
			UniversalServiceDevice mUniversalDevice = null;
			
			Log.i(tag, "registering the device " + mdevice.getDevID());
			Log.i(tag, "ven " + mdevice.getVendorID());
			Log.i(tag, "list " + mdevice.getSensorList());
			mUniversalDevice = getRegisteredDevice(mdevice.getDevID());
			if (mUniversalDevice == null) {
				// Create an instance of the Device
				Log.d(tag, "test");
				mUniversalDevice = new UniversalServiceDevice(mdevice, mDriverListener);
				Log.d(tag, "registering a new device");
				addRegisteredDevice(mUniversalDevice.getDevID(), mUniversalDevice);
			}

			Log.d(tag, "registering driver " + mdevice.getVendorID() + " mdevice sensorlist " + mdevice.getSensorList());

			if (addDriverSensor(mUniversalDevice.getDevID(), sType) == false) {
				return false;
			}
			Log.i(tag, "list of registered sensors");
			for(Map.Entry<String, UniversalServiceSensor> entry : registeredSensors.entrySet())
				Log.i("tag", "as " + entry.getKey());

			notifyListeners(mdevice);
			return true;
		}

		public boolean addDriverSensor(String devID, int sType)
				throws RemoteException {
			UniversalServiceSensor mSensor = new UniversalServiceSensor(devID, sType,
					generateSensorKey(devID, sType));
			UniversalServiceDevice mdevice = null;
			
			mdevice = getRegisteredDevice(devID);
			if (mdevice == null)
				return false;
			mdevice.addSensor(mSensor);

			// add the sensor the universal list of sensors
			addRegisteredSensor(generateSensorKey(devID, sType), mSensor);
			return true;
		}

		public boolean removeDriverSensor(UniversalServiceDevice mdevice, String devID, int sType)
				throws RemoteException {
			String mSensorKey = generateSensorKey(devID, sType);
			UniversalServiceSensor mSensor = removeRegisteredSensor(mSensorKey);
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
			
			if (sType == UniversalSensor.TYPE_ALL) {
				// It seems like a race here
				Log.i(tag, "removing sensor " + sType);				
				ArrayList<Integer> sensorList = (ArrayList<Integer>) mdevice.getSensorList().clone();
				for (int stype:sensorList) {
					Log.i(tag, "removing sensor " + stype);
					removeDriverSensor(mdevice, devID, stype);
				}
			} else {
				removeDriverSensor(mdevice, devID, sType);
			}

			if (mdevice.isEmpty()) {
				Log.d(tag, "no more sensors, removing the device entry");
				removeRegisteredDevice(devID);
			}
			
			return true;
		}

		// TODO: Check if the listener has already registered
		@Override
		public boolean registerListener(IUniversalSensorManager mManager,
				String devID, int sType, int rate) throws RemoteException {
			String msensorKey   = generateSensorKey(devID, sType);
			int listenerPid     = Binder.getCallingPid();
			String mlistenerKey = generateListenerKey(listenerPid);

			Log.i(tag, "registering listener " +  Binder.getCallingPid());

			if (getRegisteredSensor(msensorKey) == null) {
				// a null here means that the app wants to register to a sensor that
				// doesn't exist.
				Log.d(tag, "msensor is null");
				return false;
			}
			UniversalServiceSensor msensor = getRegisteredSensor(msensorKey);

			// Add the listener to the map if it already doesn't exists
			if (!hasRegisteredListener(mlistenerKey)) {
				Log.d(tag, "adding a new listener with pid " + mlistenerKey);
				addRegisteredListener(mlistenerKey, new UniversalServiceListener(mManager, listenerPid));
			}
			UniversalServiceListener mlistener = getRegisteredListener(mlistenerKey);

			msensor.linkListener(mlistener, rate);
			mlistener.linkSensor(msensorKey, msensor);
			// now enable that particular devices sensor
			try {
				setDriverSensorRate(devID, sType, msensor.getNextRate());
			} catch (RemoteException e){
				return false;
			}
			return true;
		}

		@Override
		public boolean unregisterListener(String devID, int sType)
				throws RemoteException {
			String mListenerKey = generateListenerKey(Binder.getCallingPid());
			String mSensorKey = generateSensorKey(devID, sType);
			UniversalServiceListener mlistener;
			UniversalServiceSensor   msensor;
			UniversalServiceDevice   mdevice;
			
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
				try {
					setDriverSensorRate(devID, sType, msensor.isEmpty()? 0 : msensor.getNextRate());
				} catch (RemoteException e){
					return false;
				}
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

			Log.i(tag, "event received");
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
			Log.i(tag, "adding listener to notification list");
			String mlistenerKey = generateListenerKey(Binder.getCallingPid());
			UniversalServiceListener mlistener = new UniversalServiceListener(mManager, Binder.getCallingPid());
			addRegisteredListener(mlistenerKey, mlistener);
			addListenerToNotificationList(mlistenerKey, mlistener);
		}
    }
}
