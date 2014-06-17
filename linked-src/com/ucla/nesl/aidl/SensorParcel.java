package com.ucla.nesl.aidl;

import com.ucla.nesl.lib.UniversalConstants;

import android.os.Parcel;
import android.os.Parcelable;

public class SensorParcel implements Parcelable {
	public String devID;
	public int sType;
	public int valueSize;
	public float values[];
	public long timestamp;
	public int accuracy;
	public String mSensorKey;

	public SensorParcel()
	{
	}

	public SensorParcel(int sType)
	{
		this.sType = sType;
		valueSize  = UniversalConstants.getValuesLength(sType);
		values     = new float[valueSize];
	}

	public SensorParcel(String devID, int sType)
	{
		this.devID = devID;
		this.sType = sType;
		valueSize  = UniversalConstants.getValuesLength(sType);
		values     = new float[valueSize];
	}

	public void setDataValues(float[] values, long timestamp)
	{
		this.timestamp = timestamp;
		for (int i = 0; i < valueSize; i++)
			this.values[i] = values[i];
	}

	private void makeCopy(String devID, int sType, float[] values,  int valueSize, long timestamp) {
		this.devID = devID;
		this.sType = sType;
		this.timestamp = timestamp;
		this.valueSize = valueSize;
		this.values = new float[valueSize];

		// try System.arraycopy();
		for (int i = 0; i < valueSize; i++) {
			this.values[i]	= values[i];
		}
	}
	public SensorParcel(SensorParcel sp)
	{
		makeCopy(sp.devID, sp.sType, sp.values, sp.valueSize,sp.timestamp);
	}

	public SensorParcel(String devID, int sType, float[] values,  int valueSize, int accuracy,
			long timestamp)
	{
		makeCopy(devID, sType, values, valueSize, timestamp);
	}

	public SensorParcel(int sType, float[] values, long timestamp)
	{
		this.sType 	   = sType;
		this.timestamp = timestamp;
		this.valueSize = values.length;
		this.values    = new float[valueSize];

		// try System.arraycopy();
		for (int i = 0; i < valueSize; i++) {
			this.values[i]	= values[i];
		}
	}

	@Override
	public int describeContents() 
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(devID);
		dest.writeInt(sType);
		dest.writeFloatArray(values);
		dest.writeInt(valueSize);
		dest.writeInt(accuracy);
		dest.writeLong(timestamp);
	}

	public static final Parcelable.Creator<SensorParcel> CREATOR = new Creator<SensorParcel>()
			{
		public SensorParcel createFromParcel(Parcel src)
		{
			SensorParcel sp = new SensorParcel();
			sp.devID = src.readString();
			sp.sType = src.readInt();
			sp.values = src.createFloatArray();
			sp.valueSize = src.readInt();
			sp.accuracy = src.readInt();
			sp.timestamp = src.readLong();
			return sp;
		}

		@Override
		public SensorParcel[] newArray(int size) 
		{
			return new SensorParcel[size];
		}
			};

}
