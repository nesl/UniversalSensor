package com.ucla.nesl.aidl;

import com.ucla.nesl.aidl.IUniversalSensorManager;
import com.ucla.nesl.aidl.IUniversalDriverManager;
import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.SensorParcel;


interface IUniversalManagerService {
	java.util.List<Device> listDevices();
	boolean registerListener(IUniversalSensorManager mManager, String devID, int sType, int rateUs, int bundleSize);
	boolean unregisterListener(String devID, int sType);
	boolean registerDriver(in Device device, IUniversalDriverManager mDriver, int sType, int rate, int bundleSize);
	boolean unregisterDriver(String devID, int sType);
	void registerNotification(IUniversalSensorManager mManager);
	oneway void onSensorChanged(in SensorParcel[] event, int length);
	String getDevID();
}