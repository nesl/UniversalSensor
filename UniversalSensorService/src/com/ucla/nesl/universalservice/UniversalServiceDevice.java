package com.ucla.nesl.universalservice;

import java.util.ArrayList;
import java.util.Map;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalDriverManager;
import com.ucla.nesl.lib.UniversalSensor;

public class UniversalServiceDevice extends Device {
	// Ibinder handler used to communicate with the driver
	public IUniversalDriverManager mDriverStub;
	
	// This arraylist object maintains the list of all the sensors registered
	// by this device. So, two things can be done on sensor count zero
	// a) remove the device from the listing
	// b) only remove the device when an explicit unregister is received
	private ArrayList<UniversalServiceSensor> sensorList = new ArrayList<UniversalServiceSensor>();
	
	public UniversalServiceDevice(Device device, IUniversalDriverManager mDriverStub) {
		super(device);
		this.mDriverStub = mDriverStub;
	}
	
	synchronized public void addSensor(UniversalServiceSensor msensor, int rate)
	{
		sensorList.add(msensor);
		super.addSensor(msensor.sType, rate);
	}
	
	synchronized public void removeSensor(UniversalServiceSensor msensor)
	{
		super.removeSensor(msensor.sType);
		if(sensorList.contains(msensor))
			sensorList.remove(sensorList.indexOf(msensor));
	}

	synchronized public void onDestroy()
	{
		for(UniversalServiceSensor msensor:sensorList)
		{
			msensor.unregister();
		}
	}
	
	synchronized public boolean isEmpty()
	{
		return sensorList.isEmpty();
	}
	
	synchronized public boolean unregisterSensor(int sType)
	{
		if (sType == UniversalSensor.TYPE_ALL) {
			for (UniversalServiceSensor mSensor : sensorList) {
				removeSensor(mSensor);
			}
		}
	}
}
