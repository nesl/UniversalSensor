package com.ucla.nesl.devicescan.startup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class StartupApp extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String ScanDevicePackage = "com.ucla.nesl.devicescan";
		String ScanDeviceClass = "com.ucla.nesl.devicescan.DeviceScan";
		Intent i = new Intent("ScanDevice");
		i.setClassName(ScanDevicePackage, ScanDeviceClass);
		startService(i);
		try {
			finish();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("startup", "oncreate done");
	}
	
//	@Override
//	protected void onPause() 
//	{
//		super.onPause();
//	}
//
//	@Override
//	protected void onResume()
//	{
//		super.onResume();
//	}
}
