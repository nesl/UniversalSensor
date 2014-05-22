package com.ucla.nesl.aidl;

import java.util.ArrayList;

import android.R.integer;
import android.os.Parcel;
import android.os.Parcelable;

public class Device implements Parcelable{
	private String devID;
	private String vendorID;
	private ArrayList<Integer> sensorList;

	public Device()
	{
		devID = "";
		vendorID = null;
	}

	public Device(Device device)
	{
		this.devID      = new String(device.devID);
		this.vendorID   = new String(device.vendorID);
		this.sensorList = new ArrayList<Integer>(device.sensorList);
	}

	public Device(String vendorID, String devID)
	{
		this.devID = new String(devID);
		this.vendorID = new String(vendorID);
		sensorList = new ArrayList<Integer>();
	}

	public String getDevID()
	{
		return devID;
	}
	
	public void setDevID(String devID)
	{
		this.devID = new String(devID);
	}
	
	public String getVendorID()
	{
		return vendorID;
	}
	
	public void setVendorID(String vendorID)
	{
		this.vendorID = vendorID;
	}
	
	public boolean addSensor(int sType)
	{
		Integer tmp = new Integer(sType);
		if (sensorList.indexOf(tmp) >= 0)
			return false;
		sensorList.add(tmp);
		return true;
	}

	public boolean addSensor(ArrayList<Integer> sensorList)
	{
		for (int sType : sensorList)
			addSensor(sType);
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

	public ArrayList<Integer> getSensorList()
	{
		return sensorList;
	}
	
	public boolean isEmpty()
	{
		return sensorList.isEmpty();
	}
	
	@Override
	public int describeContents() {
		return 0;
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