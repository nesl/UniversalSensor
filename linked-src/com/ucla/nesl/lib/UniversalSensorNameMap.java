package com.ucla.nesl.lib;

import java.util.HashMap;

/*	public static final String CHEST_ACCELEROMETER_NAME = "ChestAccelerometer";
	public static final String ECG_NAME = "ECG";
	public static final String RIP_NAME = "RIP";
	public static final String SKIN_TEMPERATURE_NAME = "SkinTemperature";
	public static final String ZEPHYR_BATTERY_NAME = "ZephyrBattery";
	public static final String ZEPHYR_BUTTON_WORN_NAME = "ZephyrButtonWorn";
 */

public class UniversalSensorNameMap {
	private static HashMap<Integer, String> nameMap = new HashMap<Integer, String>(){
		{
			put(new Integer(UniversalSensor.TYPE_ACCELEROMETER),"accelerometer");
			put(new Integer(UniversalSensor.TYPE_MAGNETIC_FIELD), "magnetic_field");
			put(new Integer(UniversalSensor.TYPE_GYROSCOPE), "gyroscope");
			put(new Integer(UniversalSensor.TYPE_LIGHT), "light");
			put(new Integer(UniversalSensor.TYPE_CHEST_ACCELEROMETER),"ChestAccelerometer");
			put(new Integer(UniversalSensor.TYPE_ECG), "ECG");
			put(new Integer(UniversalSensor.TYPE_RIP), "RIP");
			put(new Integer(UniversalSensor.TYPE_SKIN_TEMPERATURE), "SkinTemperature");
			put(new Integer(UniversalSensor.TYPE_ZEPHYR_BATTERY), "ZephyrBattery");
			put(new Integer(UniversalSensor.TYPE_ZEPHYR_BUTTON_WORN), "ZephyrButtonWorn");
		}
	};

	public static String getName(int sType){
		return nameMap.get(sType);
	}
}
