package com.ucla.nesl.aidl;

import java.util.ArrayList;

import android.R.integer;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Device implements Parcelable{
	public String devID;
	public String vendorID;
	public ArrayList<Integer> sensorList;

	public Device()
	{
		devID = "";
		vendorID = null;
	}

	public Device(String vendorID)
	{
		this.devID = "";
		this.vendorID = new String(vendorID);
		sensorList = new ArrayList<Integer>();
	}

	public boolean addSensor(int sType)
	{
		Integer tmp = new Integer(sType);
		if (sensorList.indexOf(tmp) >= 0)
			return false;
		sensorList.add(tmp);
		return true;
	}

	public boolean removeSensor(int sType)
	{
		Integer tmp = new Integer(sType);
		int index = sensorList.indexOf(tmp);
		if (index >= 0) {
			sensorList.remove(index);
			return true;
		} else
			return false;
	}
	public String getVendorID()
	{
		return vendorID;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public String getdevID()
	{
		return devID;
	}
	
	public void setdevID(String devID)
	{
		this.devID = devID;
	}

	private int[] getIntArray()
	{
		int[] sensors = new int[sensorList.size()];
		for (int i = 0; i < sensorList.size(); i++)
		{
			sensors[i] = sensorList.get(i);
		}
		return sensors;
	}
	
	public ArrayList<Integer> getArrayList(int[] sensors)
	{
		ArrayList<Integer> alist = new ArrayList<Integer>();
		
		for(int i = 0; i < sensors.length; i++)
		{
			alist.add(new Integer(sensors[i]));
		}
		return alist;
	}

	public static final Parcelable.Creator<Device> CREATOR = new Creator<Device>() {
		public Device createFromParcel(Parcel src)
		{
			Device device = new Device();
			device.devID = src.readString();
			device.vendorID = src.readString();
			int[] iarray = src.createIntArray();
			device.sensorList = device.getArrayList(iarray);
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
		parcel.writeString(vendorID);
		parcel.writeIntArray(getIntArray());
	}
}