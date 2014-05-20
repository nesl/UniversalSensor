package com.ucla.nesl.lib;

import java.util.ArrayList;
import java.util.HashMap;

import android.R.integer;

public class UniversalSensorNameMap {
	private static HashMap<Integer, String> nameMap = new HashMap<Integer, String>(){
		{
			put(new Integer(UniversalSensor.TYPE_ACCELEROMETER),"accelerometer");
			put(new Integer(UniversalSensor.TYPE_MAGNETIC_FIELD), "magnetic_field");
			put(new Integer(UniversalSensor.TYPE_GYROSCOPE), "gyroscope");
			put(new Integer(UniversalSensor.TYPE_LIGHT), "light");
		}
	};
	
	public static String getName(int sType){
		return nameMap.get(sType);
	}
}
