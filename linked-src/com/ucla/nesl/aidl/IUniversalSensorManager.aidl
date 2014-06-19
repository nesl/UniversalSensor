package com.ucla.nesl.aidl;
import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.SensorParcel;

interface IUniversalSensorManager {
	oneway void onSensorChanged(String devID, int sType, in float[] data, in long[] timestamp);
	void notifyNewDevice(in Device mdevice);
	void notifySensorChanged(String devID, int sType, int action);
	void listHistoricalDevice(in String[] devices);
	void historicalDataResponse(int txnID, String devID, int sType, int cmd, in String dataStream);
}