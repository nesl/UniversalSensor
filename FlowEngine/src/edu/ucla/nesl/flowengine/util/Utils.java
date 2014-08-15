package edu.ucla.nesl.flowengine.util;

import java.util.ArrayList;

import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.lib.UniversalSensorNameMap;

public class Utils {
	public static void printDeviceList(String tag, ArrayList<Device> devList) {
		if (devList == null || devList.isEmpty()) {
			Log.i(tag, "No devices");
			return;
		}
		
		for (Device device : devList)
		{
			Log.i(tag, device.getDevID());
			for (Integer i : device.getSensorList())
				Log.i(tag, "SenorID: " + i + ", Name:" + UniversalSensorNameMap.getName(i) + 
						", Rates" + device.getRateList(i) + ", BundleSize: " + device.getBundleSizeList(i));
		}
	}
}
