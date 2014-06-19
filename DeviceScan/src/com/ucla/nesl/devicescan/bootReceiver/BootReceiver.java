package com.ucla.nesl.devicescan.bootReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ucla.nesl.devicescan.DeviceScan;

public class BootReceiver extends BroadcastReceiver {
	private static String tag = BootReceiver.class.getCanonicalName();
	private static String ScanDevicePackage = "com.ucla.nesl.devicescan";
	private static String ScanDeviceClass = "com.ucla.nesl.devicescan.DeviceScan";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(tag, "received broadcast");
		Intent i = new Intent("ScanDevice");
		i.setClassName(ScanDevicePackage, ScanDeviceClass);
		context.startService(i);
	}
}
