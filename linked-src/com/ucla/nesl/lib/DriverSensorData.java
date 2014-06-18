package com.ucla.nesl.lib;

/**
 * Helper class to be used by the Driver writers to
 * perform efficient memory management.
 */
public class DriverSensorData {
	UniversalSensorEvent[] event;
	int index = 0;

	public DriverSensorData(String devID, int sType, int arraySize) {
		event  = new UniversalSensorEvent[arraySize];

		for (int i = 0; i < arraySize; i++) {
			event[i] = new UniversalSensorEvent(devID, sType);
		}
	}

	public void setDataValues(float[] values, long timestamp)
	{
		event[index++].setDataValues(values, timestamp);
	}

	public void initializeIndex()
	{
		index = 0;
	}

	public UniversalSensorEvent[] getEventArray()
	{
		return event;
	}

	public int getIndex()
	{
		return index;
	}
}
