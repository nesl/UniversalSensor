package com.ucla.nesl.lib;

import com.ucla.nesl.aidl.SensorParcel;

public class SensorParcelWrapper {
	public SensorParcel[] sp;
	public int length;
	public SensorParcelWrapper(SensorParcel[] sp, int length)
	{
		this.sp = sp;
		this.length = length;
	}
}
