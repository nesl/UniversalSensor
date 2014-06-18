package com.ucla.nesl.lib;

public class UniversalConstants {
	public static int ACTION_UNREGISTER = 0;

	public static final int MSG_OnSensorChanged = 0;
	public static final int MSG_Quit = 1;
	public static final int MSG_Link_Sensor = 2;
	public static final int MSG_NotifyNewDevice = 3;
	public static final int MSG_UpdateSamplingParam = 4;
	public static final int MSG_Unlink_Sensor = 5;
	public static final int MSG_UnregisterListener = 6;
	public static final int MSG_UnregisterDriver = 7;
	public static final int MSG_PUSH_DATA = 8;
	public static final int MSG_FETCH_RECORD = 9;
	public static final int MSG_STORE_RECORD = 10;
	public static final int MSG_ListHistoricalDevices = 11;
	public static final int MSG_FETCH_HISTORICAL_DATA = 12;

	public static final String rate = "rate";
	public static final String bundleSize = "bundleSize";
	public static final String sType = "sType";

	public static final String DBName = "datastore.db";

	public static final int COMPUTE_AVG = 1;
	public static final int COMPUTE_MIN_MAX = 2;

	public static int getValuesLength(int sType)
	{
		switch(sType) {
		case UniversalSensor.TYPE_ACCELEROMETER:
		case UniversalSensor.TYPE_CHEST_ACCELEROMETER:
			return 3;
		case UniversalSensor.TYPE_ECG:
		case UniversalSensor.TYPE_LIGHT:
			return 1;
		default:
			return 1;
		}
	}
}
