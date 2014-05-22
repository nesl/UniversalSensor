package com.ucla.nesl.universalservice;

import java.util.ArrayList;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalDriverManager;

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
	
	public void addSensor(UniversalServiceSensor msensor)
	{
		sensorList.add(msensor);
	}
	
	public void removeSensor(UniversalServiceSensor msensor)
	{
		if(sensorList.contains(msensor))
			sensorList.remove(sensorList.indexOf(msensor));
	}
	
	public void onDestroy()
	{
		// sensorlist always should be empty
	}
}
