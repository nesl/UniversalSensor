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
//    	ArrayList<IUniversalSensorManager>  registeredListeners = new ArrayList<IUniversalSensorManager>();
    	HashMap<String, UniversalServiceListener> registeredListeners = new HashMap<String, UniversalServiceListener>();

    	public UniversalManagerService(UniversalService parent) {
    		this.parent = parent;
		}

		@Override
		public ArrayList<Device> listDevices() throws RemoteException {
			ArrayList<Device> deviceList = new ArrayList<Device>();
			Set<Map.Entry<String, UniversalServiceDevice>> entries = registeredDevices.entrySet();
			for(Map.Entry<String, UniversalServiceDevice> entry : entries)
			{
				deviceList.add(entry.getValue().getDevice());
			}
			return deviceList;
		}

		@Override
		public boolean registerListener(IUniversalSensorManager mManager,
				String devID, int sType, int rateUs) throws RemoteException {
			Log.i(tag, "registering listener " +  Binder.getCallingPid());
			if (!registeredListeners.containsKey("" + Binder.getCallingPid())) {
				registeredListeners.put("" + Binder.getCallingPid(), new UniversalServiceListener(mManager ,"" + Binder.getCallingPid()));
			}
			UniversalServiceListener mlistener = registeredListeners.get("" + Binder.getCallingPid());
			UniversalServiceSensor msensor = registeredSensors.get(""+ devID +"-" + sType);
			if (msensor == null) { 
				Log.i(tag, "msensor is null");
			}
			if (mlistener == null) { 
				Log.i(tag, "mlistener is null");
			}
			msensor.registerListener(mlistener);
			mlistener.registerSensor(""+ devID + "-" + sType, msensor);
			return true;
		}

		@Override
		public void onSensorChanged(SensorParcel event) throws RemoteException {
			UniversalServiceSensor msensor = registeredSensors.get(""+event.devID + "-" + event.sType);
			if(msensor == null) 
				Log.i(tag, "msensor is null " + event.devID + "-" + event.sType);
			for (UniversalServiceListener mlistener:registeredSensors.get(""+event.devID + "-" + event.sType).registeredlisteners)
			{
				Log.i(tag, "registered " + mlistener.callingPid + " application for " + event.devID + "-" + event.sType);
			}
		}

		@Override
		public String registerDriver(IUniversalDriverManager mDriver, Device device) throws RemoteException {
			UniversalServiceDevice mdevice = new UniversalServiceDevice(device);
			Log.d(tag, "registering driver " + device.vendorID + " mdevice sensorlist " + device.sensorList);
			String devID = new String(""+Math.random());  //compute devID, for now using a random number
			mdevice.setDevID(devID);
			mdevice.mDriver = mDriver;
			registeredDevices.put(devID, mdevice);
			for (int i : device.sensorList)
			{
				Log.i(tag, "registering sensor " + i);
				addDriverSensor(devID, i);
			}
			return devID;
		}

		@Override
		public void addDriverSensor(String devID, int sType)
				throws RemoteException {
			registeredDevices.get(devID).getDevice().addSensor(sType);
			registeredSensors.put(""+ devID + "-" + sType, new UniversalServiceSensor());
//			mdevice.sensorlist.put(""+ devID + "-" + sType, new UniversalServiceSensor());
		}

		@Override
		public void removeDriverSensor(String devID, int sType)
				throws RemoteException {
			UniversalServiceDevice mdevice = registeredDevices.get(devID);
			mdevice.getDevice().removeSensor(sType);
		}

		@Override
		public void setRate(int rate) throws RemoteException {
			Set<Map.Entry<String, UniversalServiceDevice>> entries = registeredDevices.entrySet();
			for(Map.Entry<String, UniversalServiceDevice> entry : entries)
			{
				entry.getValue().mDriver.setRate(rate);
			}
		}    	
    }
}
