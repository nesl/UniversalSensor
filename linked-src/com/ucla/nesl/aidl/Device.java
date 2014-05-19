package com.ucla.nesl.aidl;

import java.util.ArrayList;

import android.R.integer;
import android.os.Parcel;
import android.os.Parcelable;

public class Device implements Parcelable{
	public String devID;
	public String vendorID;
	public ArrayList<integer> sensorList;

	public Device()
	{
		devID = null;
		vendorID = null;
	}
	
	public Device(String devID)
	{
		this.devID = devID;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	public String getdevID()
	{
		return devID;
	}
	
	public static final Parcelable.Creator<Device> CREATOR = new Creator<Device>() {
		public Device createFromParcel(Parcel src)
		{
			Device device = new Device();
			device.devID = src.readString();
			return device;
		}

		@Override
		public Device[] newArray(int size) {
			return new Device[size];
		}
	};

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(devID);
	}

}
