package com.ucla.nesl.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class SensorParcel implements Parcelable {
	public String devID;
	public int sType;
	public int valueSize;
	public float values[];
	public float timestamp;
	public int accuracy;
	public String mSensorKey;

	public SensorParcel()
	{
	}
	
	public SensorParcel(SensorParcel sp)
	{
		this.devID = new String(sp.devID);
		this.sType = sp.sType;
		this.timestamp = sp.timestamp;
		this.accuracy = sp.accuracy;
		this.valueSize = sp.valueSize;
		this.values = new float[sp.valueSize];

		// try System.arraycopy();
		for (int i = 0; i < valueSize; i++) {
			this.values[i]	= sp.values[i];
		}
	}

	public SensorParcel(String devID, int sType, float[] values,  int valueSize, int accuracy,
			float timestamp) {
		this.devID = devID;
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
	
	public SensorParcel(int sType, float[] values, float timestamp) {
		this.sType = sType;
		this.timestamp = timestamp;
		this.valueSize = values.length;
		this.values = new float[valueSize];

		// try System.arraycopy();
		for (int i = 0; i < valueSize; i++) {
			this.values[i]	= values[i];
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(devID);
		dest.writeInt(sType);
		dest.writeFloatArray(values);
		dest.writeInt(valueSize);
		dest.writeInt(accuracy);
		dest.writeFloat(timestamp);
	}

	public static final Parcelable.Creator<SensorParcel> CREATOR = new Creator<SensorParcel>() {
		public SensorParcel createFromParcel(Parcel src)
		{
			SensorParcel sp = new SensorParcel();
			sp.devID = src.readString();
			sp.sType = src.readInt();
			sp.values = src.createFloatArray();
			sp.valueSize = src.readInt();
			sp.accuracy = src.readInt();
			sp.timestamp = src.readFloat();
			return sp;
		}

		@Override
		public SensorParcel[] newArray(int size) {
			return new SensorParcel[size];
		}
	};

}
