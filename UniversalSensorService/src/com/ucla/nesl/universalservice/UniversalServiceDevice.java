package com.ucla.nesl.universalservice;

import java.util.HashMap;
import java.util.Map;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalDriverManager;

public class UniversalServiceDevice {
	private Device device;
	public IUniversalDriverManager mDriver;
	Map<String, UniversalServiceSensor> sensorlist;
	
	public UniversalServiceDevice(Device device) {
		this.device = device;
		sensorlist = new HashMap<String, UniversalServiceSensor>();
	}
	
	public void setDevID(String devID)
	{
		this.device.devID = devID;
	}
	
	public Device getDevice()
	{
		return device;
	}
}
