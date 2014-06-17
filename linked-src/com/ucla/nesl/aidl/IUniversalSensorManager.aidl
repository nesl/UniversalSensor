package com.ucla.nesl.aidl;
import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.SensorParcel;

interface IUniversalSensorManager {
	void onSensorChanged(in SensorParcel[] event);
	void notifyNewDevice(in Device mdevice);
	void notifySensorChanged(String devID, int sType, int action);
	void listHistoricalDevice(in String[] devices);
	void fetchHistoricalData(int txnID, String devID, int sType, int cmd, in String dataStream);
}