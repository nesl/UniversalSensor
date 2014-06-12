package com.ucla.nesl.lib;

public class DriverSensorData {
	UniversalSensorEvent[] event;
	int index = 0;

	public DriverSensorData(String devID, int sType, int arraySize) {
		event  = new UniversalSensorEvent[arraySize];
		
		for (int i = 0; i < arraySize; i++) {
			event[i] = new UniversalSensorEvent(devID, sType);
		}
	}

	public void setDataValues(float[] values, float timestamp)
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
