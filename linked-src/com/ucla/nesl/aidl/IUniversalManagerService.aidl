package com.ucla.nesl.aidl;

import com.ucla.nesl.aidl.IUniversalSensorManager;
import com.ucla.nesl.aidl.IUniversalDriverManager;
import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.SensorParcel;


interface IUniversalManagerService {
	java.util.List<Device> listDevices();
	boolean registerListener(IUniversalSensorManager mManager, String devID, int sType, int rateUs);
	boolean unregisterListener(String devID, int sType);
	boolean registerDriver(IUniversalDriverManager mDriver, in Device device);
	boolean unregisterDriver(String devID);
	boolean addDriverSensor(String devID, int sType);
	boolean removeDriverSensor(String devID, int sType);
	boolean onSensorChanged(in SensorParcel event);
	boolean setRate(int rate);
	String getDevID();
}