package com.ucla.nesl.lib;


public class SensorParcelWrapper {
	public float[] values;
	public long[] timestamp;
	public String mSensorKey;
	public String devID;
	public int sType;
	public SensorParcelWrapper(String devID, int sType, String mSensorKey, float[] values, long[] timestamp)
	{
		this.devID  = devID;
		this.sType  = sType;
		this.mSensorKey	= mSensorKey;
		this.values = values;
		this.timestamp = timestamp;
	}
}
