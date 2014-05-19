package com.ucla.nesl.lib;



public class UniversalSensorEvent {
	int sType;

	public final float[] values;

    /**
     * The sensor that generated this event. See
     * {@link android.hardware.SensorManager SensorManager} for details.
     */
    public AndroidSensor sensor;

    /**
     * The accuracy of this event. See {@link android.hardware.SensorManager
     * SensorManager} for details.
     */
    public int accuracy;

    /**
     * The time in nanosecond at which the event happened
     */
    public long timestamp;

	UniversalSensorEvent(int valueSize) {
		values = new float[valueSize];
	}
}
