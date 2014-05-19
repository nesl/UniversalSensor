package com.ucla.nesl.lib;

public class UniversalSensorHelper {
	int sType;
	int valueSize;
	float values[];
	float timestamp;
	int accuracy;

	public UniversalSensorHelper(int sType, int valueSize, float values[], int accuracy, float timestamp)
	{
		this.sType = sType;
		this.timestamp = timestamp;
		this.accuracy = accuracy;
		this.valueSize = valueSize;
		this.values = new float[valueSize];

		// try System.arraycopy();
		for (int i = 0; i < valueSize; i++) {
			this.values[i]	= values[i];
		}
	}
}
