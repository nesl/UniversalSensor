package com.ucla.nesl.universalservice;

import java.util.HashMap;
import java.util.Map;

import com.ucla.nesl.aidl.Device;

public class UniversalServiceDevice {
	private Device device;
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
