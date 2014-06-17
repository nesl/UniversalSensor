package com.ucla.nesl.universalservice;

import java.util.HashMap;
import java.util.Map;

import android.os.Handler;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalDriverManager;
import com.ucla.nesl.lib.UniversalSensor;

public class UniversalServiceDevice extends Device {
	// Ibinder handler used to communicate with the driver
	public IUniversalDriverManager mDriverStub;

	private UniversalManagerService mService;

	// This arraylist object maintains the list of all the sensors registered
	// by this device. So, two things can be done on sensor count zero
	// a) remove the device from the listing
	// b) only remove the device when an explicit unregister is received
	//	private ArrayList<UniversalServiceSensor> sensorList = new ArrayList<UniversalServiceSensor>();
	private HashMap<String, UniversalServiceSensor> registeredSensors = new HashMap<String, UniversalServiceSensor>();

	public UniversalServiceDevice(UniversalManagerService mService, Device device, IUniversalDriverManager mDriverStub)
	{
		super(device);
		this.mService    = mService;
		this.mDriverStub = mDriverStub;
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

	//	synchronized public void removeSensor(UniversalServiceSensor msensor)
	//	{
	//		super.removeSensor(msensor.sType);
	//		if(sensorList.contains(msensor))
	//			sensorList.remove(sensorList.indexOf(msensor));
	//	}
	//
	//	synchronized public void onDestroy()
	//	{
	//		for(UniversalServiceSensor msensor:sensorList)
	//		{
	//			msensor.unregister();
	//		}
	//	}

	synchronized public boolean isEmpty()
	{
		synchronized (registeredSensors) {
			return registeredSensors.isEmpty();
		}
	}

	public Handler getServiceHandler()
	{
		return mService.getHandler();
	}

	public boolean unregisterSensor(int sType, String mSensorKey)
	{
		boolean flag = true;
		UniversalServiceSensor mSensor;

		if (sType == UniversalSensor.TYPE_ALL) {
			synchronized (registeredSensors) {
				for (Map.Entry<String, UniversalServiceSensor> entry : registeredSensors.entrySet()) {
					entry.getValue().unregister();
				}
				registeredSensors.clear();
			}
		} else {
			mSensor = getRegisteredSensor(mSensorKey);
			if (mSensor == null)
				flag = false;
			mSensor.unregister();
		}
		super.removeSensor(sType);

		return flag;
	}

	public boolean registerSensor(String mSensorKey, int sType, int[] maxRate, int[] bundleSize)
	{
		UniversalServiceSensor mSensor = null;

		mSensor = getRegisteredSensor(mSensorKey);
		if (mSensor == null) {
			mSensor = new UniversalServiceSensor(this, mSensorKey, sType, maxRate, bundleSize);
			addRegisteredSensor(mSensorKey, mSensor);
		} 
		//		else {
		//			 mSensor.update(maxRate, bundleSize);
		//		}
		// Here we are expecting the driver to send the array in descending order
		super.addSensor(sType, maxRate, bundleSize);

		return true;
	}

	public UniversalServiceSensor getSensor(String mSensorKey)
	{
		synchronized (registeredSensors) {
			return getRegisteredSensor(mSensorKey);
		}
	}
}
