package com.ucla.nesl.aidl;

import com.ucla.nesl.aidl.IUniversalSensorManager;
import com.ucla.nesl.aidl.IUniversalDriverManager;
import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.SensorParcel;


interface IUniversalManagerService {
	java.util.List<Device> listDevices();
	boolean registerListener(IUniversalSensorManager mManager, String devID, int sType, int rateUs);
	String registerDriver(IUniversalDriverManager mDriver, in Device device);
	void addDriverSensor(String devID, int sType);
	void removeDriverSensor(String devID, int sType);
	void onSensorChanged(in SensorParcel event);
	void setRate(int rate);
}