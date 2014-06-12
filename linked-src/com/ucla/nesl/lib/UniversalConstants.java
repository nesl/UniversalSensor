package com.ucla.nesl.lib;

public class UniversalConstants {
	public static int ACTION_UNREGISTER = 0;

	public static final int MSG_OnSensorChanged = 0;
	public static final int MSG_Quit = 1;
	public static final int MSG_Link_Sensor = 2;
	public static final int MSG_NotifyDeviceChanged = 3;
	public static final int MSG_UpdateSamplingParam = 4;
	public static final int MSG_Unlink_Sensor = 5;
	public static final int MSG_UnregisterListener = 6;
	public static final int MSG_UnregisterDriver = 7;

	public static final String rate = "rate";
	public static final String bundleSize = "bundleSize";
	public static final String sType = "sType";
	
	public static int getValuesLength(int sType)
	{
		switch(sType) {
		case UniversalSensor.TYPE_ACCELEROMETER:
		case UniversalSensor.TYPE_CHEST_ACCELEROMETER:
			return 3;
		case UniversalSensor.TYPE_LIGHT:
			return 1;
		default:
			return 1;
		}
	}
}
