package com.ucla.nesl.universalservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.app.Service;
import android.content.Intent;
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

    // TODO: Use read-write lock here
    private class UniversalManagerService extends IUniversalManagerService.Stub {
    	UniversalService parent;
    	Map<String, UniversalServiceDevice> registeredDevices = new HashMap<String, UniversalServiceDevice>();
    	Map<String, UniversalServiceSensor> registeredSensors = new HashMap<String, UniversalServiceSensor>();
    	
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
			Log.i(tag, "registering listener");
			return true;
		}

		@Override
		public void onSensorChanged(SensorParcel event) throws RemoteException {
		}

		@Override
		public String registerDriver(IUniversalDriverManager mDriver,
				Device device) throws RemoteException {
			UniversalServiceDevice mdevice = new UniversalServiceDevice(device);
			String devID = ""+Math.random();  //compute devID, for now using a random number
			mdevice.setDevID(devID);
			registeredDevices.put(devID, mdevice);
			return devID;
		}

		@Override
		public void registerDriverSensor(String devID, int sType)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}    	
    }
}
