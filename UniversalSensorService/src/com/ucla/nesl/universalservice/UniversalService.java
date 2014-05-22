package com.ucla.nesl.universalservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

public class UniversalService extends Service {
	private String tag = UniversalService.class.getCanonicalName();

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
    	
    	public boolean activateSensor(String devID, int sType) throws RemoteException
    	{
    		UniversalServiceDevice mdevice;
			mdevice = registeredDevices.get(devID);
			mdevice.mDriverStub.activateSensor(sType);
			return true;    		
    	}
    	
    	public boolean deactivateSensor(String devID, int sType)  throws RemoteException
    	{
    		UniversalServiceDevice mdevice;
			mdevice = registeredDevices.get(devID);
			mdevice.mDriverStub.deactivateSensor(sType);
			return true;
    	}
    	
		@Override
		public ArrayList<Device> listDevices() throws RemoteException {
			ArrayList<Device> deviceList = new ArrayList<Device>();
			
			for(Map.Entry<String, UniversalServiceDevice> entry : registeredDevices.entrySet())
			{
				deviceList.add(entry.getValue());
			}
			return deviceList;
		}

		// This function is called only once, at the beginning when the driver registers
		// For operations like adding a sensor, removing a sensor, etc the addDriverSensor
		// removeDriverSensor function will be called.
		@Override
		public boolean registerDriver(IUniversalDriverManager mDriverListener, Device device) throws RemoteException {
			// Create an instance of the Device
			UniversalServiceDevice mdevice = new UniversalServiceDevice(device, mDriverListener);

			Log.d(tag, "registering driver " + device.getVendorID() + " mdevice sensorlist " + device.getSensorList());

			if (registeredDevices == null) {
				Log.e(tag, "registerdDevices is null");
			}
			if (device == null)
				Log.e(tag, "device is null");

			// This should never be true
			if (registeredDevices.containsKey(device.getDevID())) {
				//raise exception
				return false;
			} else {
				// add the device to the registered set of devices	
				registeredDevices.put(mdevice.getDevID(), mdevice);
			}

			// This will be non empty when the device is preloaded with a set of sensors
			// (which is the case most of the time) and wants to expose all of them.
			for (int sType : device.getSensorList())
			{
				Log.i(tag, "registering sensor " + sType);
				addDriverSensor(device.getDevID(), sType);
			}
			
			return true;
		}

		@Override
		public boolean addDriverSensor(String devID, int sType)
				throws RemoteException {
			UniversalServiceSensor mSensor = new UniversalServiceSensor(devID, sType);

			// Register the sensor with its device
			registeredDevices.get(devID).addSensor(mSensor);
			
			// add the sensor the universal list of sensors
			registeredSensors.put(generateSensorKey(devID, sType), mSensor);
			return true;
		}

		@Override
		public boolean removeDriverSensor(String devID, int sType)
				throws RemoteException {
			String mSensorKey = generateSensorKey(devID, sType);
			UniversalServiceSensor mSensor = registeredSensors.get(mSensorKey);
			if (mSensor == null) 
				return false;
			
			// Notify all the listeners
			mSensor.unregister(mSensorKey);

			// Unregister the sensor from its device
			registeredDevices.get(devID).removeSensor(mSensor);
			
			// remove from the registeredsensors list
			registeredSensors.remove(mSensorKey);
			return true;
		}

		@Override
		public boolean unregisterDriver(String devID) throws RemoteException {
			// remove the entry from the registeredDevices,
			// we should send notifications to all the applications that have asked for the notification
			// also free all the sensor objects
			if (!registeredDevices.containsKey(devID)) {
				return false;
			} else {
				registeredDevices.remove(devID).onDestroy();
			}
			
			return true;
		}

		// TODO: Check if the listener has already registered
		@Override
		public boolean registerListener(IUniversalSensorManager mManager,
				String devID, int sType, int rateUs) throws RemoteException {
			String msensorKey   = generateSensorKey(devID, sType);
			int listenerPid     = Binder.getCallingPid();
			String mlistenerKey = generateListenerKey(listenerPid);

			Log.i(tag, "registering listener " +  Binder.getCallingPid());

			// Add the listener to the map if it already doesn't exists
			if (!registeredListeners.containsKey(mlistenerKey)) {
				Log.d(tag, "adding a new listener with pid " + mlistenerKey);
				registeredListeners.put(mlistenerKey, new UniversalServiceListener(mManager, listenerPid));
			}

			if (!registeredSensors.containsKey(msensorKey)) {
				// a null here means that the app wants to register to a sensor that
				// doesn't exist.
				Log.d(tag, "msensor is null");
				return false;
			}
			UniversalServiceSensor msensor = registeredSensors.get(msensorKey);

			if (!registeredListeners.containsKey(mlistenerKey)) { 
				Log.d(tag, "mlistener is null");
				return false;
			}
			UniversalServiceListener mlistener = registeredListeners.get(mlistenerKey);

			msensor.registerListener(mlistener);
			mlistener.registerSensor(msensorKey, msensor);
			// now enable that particular devices sensor
			try {
				activateSensor(msensor.getDevID(), sType);
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
			if (!registeredListeners.containsKey(mListenerKey)) {
				return false;
			} else {
				mlistener = registeredListeners.get(mListenerKey);
				
				mlistener.unregister(mSensorKey);

				// remove the entry only when this is the last sensor
				// that this app is registered to
				if (mlistener.isEmpty()) {
					// this is the last entry, so remove the listener from
					// registeredListeners
					registeredListeners.remove(mListenerKey);
				}
				
				// We should disable the sensor if removal of the
				// listener has made its listener list empty
				msensor = registeredSensors.get(mSensorKey);
				if (msensor.isEmpty()) {
					try {
						deactivateSensor(msensor.getDevID(), sType);
					} catch (RemoteException e){
						return false;
					}
				}
			}
			return true;
		}

		@Override
		public boolean onSensorChanged(SensorParcel event) throws RemoteException {
			String key = generateSensorKey(event.devID, event.sType);
			
			UniversalServiceSensor msensor = registeredSensors.get(key);
			if(msensor == null) {
				Log.i(tag, "msensor is null " + key);
				// may be through exception, for now just returning
				return false;
			}

			msensor.onSensorChanged(event);
			return true;
		}

		@Override
		public boolean setRate(int rate) throws RemoteException {
			Set<Map.Entry<String, UniversalServiceDevice>> entries = registeredDevices.entrySet();
			for(Map.Entry<String, UniversalServiceDevice> entry : entries)
			{
//				entry.getValue().mDriver.setRate(rate);
			}
			return true;
		}

		@Override
		public String getDevID() throws RemoteException {
			return new String(""+Math.random());  //compute devID, for now using a random number
		}
    }
}
