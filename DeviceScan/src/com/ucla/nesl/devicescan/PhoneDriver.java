package com.ucla.nesl.devicescan;

import android.content.Context;
import android.content.Intent;

public class PhoneDriver {
	DeviceScan parent = null;
	Context    context = null;
	private static String UNIVERSALDriverPackage = "com.ucla.nesl.universalphonedriver";
	private static String UNIVERSALDriverClass = "com.ucla.nesl.universalphonedriver.UniversalPhoneDriver";


	public static void init(DeviceScan parent, Context context)
	{
		Intent intent = new Intent("bindUniversalPhoneDriver");
		intent.setClassName(UNIVERSALDriverPackage, UNIVERSALDriverClass);
		parent.startService(intent);
	}

	public static void destroy()
	{
	}
}
