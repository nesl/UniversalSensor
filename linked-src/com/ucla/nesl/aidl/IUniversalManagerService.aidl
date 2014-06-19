package com.ucla.nesl.aidl;

import com.ucla.nesl.aidl.IUniversalSensorManager;
import com.ucla.nesl.aidl.IUniversalDriverManager;
import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.SensorParcel;


interface IUniversalManagerService {
	java.util.List<Device> listDevices();
	boolean registerListener(IUniversalSensorManager mManager, String devID, int sType, boolean periodic, int rateUs, int bundleSize);
	boolean unregisterListener(String devID, int sType);
	boolean fetchRecord(String devID, int sType);
	boolean registerDriver(in Device device, IUniversalDriverManager mDriver, int sType, in int[] rate, in int[] bundleSize);
	boolean unregisterDriver(String devID, int sType);
	void registerNotification(IUniversalSensorManager mManager);
	void onSensorChanged(String devID, int sType, in float[] data, in long[] timestamp);
	boolean listHistoricalDevices(IUniversalSensorManager mListenerStub);
	boolean fetchHistoricalData(IUniversalSensorManager mManager, int txnID, String devID, int sType, long start, long end, long interval, int cmd); 
	String getDevID();
}