package com.ucla.nesl.lib;

import com.ucla.nesl.aidl.SensorParcel;

public class SensorParcelWrapper {
	public float[] values;
	public int length;
	public long[] timestamp;
	public String devID;
	public int sType;
	public SensorParcelWrapper(String devID, int sType, int length, float[] values, long[] timestamp)
	{
		this.devID  = devID;
		this.sType  = sType;
		this.length = length;
		this.values = values;
		this.timestamp = timestamp;
	}
}
