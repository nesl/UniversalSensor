package com.ucla.nesl.lib;

import com.ucla.nesl.aidl.SensorParcel;

public class UniversalSensorEvent extends SensorParcel {
	//	int sType;
	//
	//	public final float[] values;
	//
	//    /**
	//     * The sensor that generated this event. See
	//     * {@link android.hardware.SensorManager SensorManager} for details.
	//     */
	//    public AndroidSensor sensor;
	//
	//    /**
	//     * The accuracy of this event. See {@link android.hardware.SensorManager
	//     * SensorManager} for details.
	//     */
	//    public int accuracy;
	//
	//    /**
	//     * The time in nanosecond at which the event happened
	//     */
	//    public long timestamp;
	//
	//	UniversalSensorEvent(int valueSize) {
	//		values = new float[valueSize];
	//	}

	public UniversalSensorEvent() {
		// TODO Auto-generated constructor stub
	}

	public UniversalSensorEvent(String devID, int sType, float[] values, float timestamp)
	{
		super(devID, sType, values, values.length, 0,timestamp);
	}

	public UniversalSensorEvent(int sType, float[] values, float timestamp)
	{
		super(sType, values, timestamp);
	}

	public UniversalSensorEvent(SensorParcel sp)
	{
		super(sp);
	}

	public boolean setDevID(String devID)
	{
		this.devID = devID;
		return true;
	}
}
