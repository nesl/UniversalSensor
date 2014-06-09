package com.ucla.nesl.aidl;
import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.SensorParcel;

interface IUniversalSensorManager {
	void onSensorChanged(in SensorParcel[] event);
	void notifyDeviceChange(in Device mdevice);
	void notifySensorChanged(String devID, int sType, int action);
}