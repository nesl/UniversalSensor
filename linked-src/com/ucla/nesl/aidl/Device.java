package com.ucla.nesl.aidl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.hardware.Sensor;
import android.os.Parcel;
import android.os.Parcelable;

public class Device implements Parcelable{
	public int test = 0;
	private String devID;
	/**
	 *  Packet information:
	 *  Using a stream of integers
	 *  first integer 1
	 *  Stream of integers containing the information about the range of Rates
	 *  Delimiter 0xCC
	 *  Stream of integers containing the different BundleSize values supported by the hardware
	 */
	private HashMap<Integer, ArrayList<Integer>> mSensor = new HashMap<Integer, ArrayList<Integer>>();

	public Device()
	{
		devID = "";
	}

	public Device(String devID)
	{
		this.devID = new String(devID);
	}

	public Device(Device device)
	{
		this.devID      = new String(device.devID);
		this.mSensor    = new HashMap<Integer, ArrayList<Integer>>();
	}

	public Device(String vendorID, String devID)
	{
		this.devID = new String(devID);
		mSensor    = new HashMap<Integer, ArrayList<Integer>>();
	}

	public String getDevID()
	{
		return devID;
	}

	public void setDevID(String devID)
	{
		this.devID = new String(devID);
	}

	synchronized public boolean addSensor(int sType, int rate[], int bundleSize[])
	{
		boolean flag = false;
		ArrayList<Integer> val = null;

		if (!mSensor.containsKey(sType)) {
			mSensor.put(sType, new ArrayList<Integer>());
			mSensor.get(sType).add(Integer.valueOf(1));
			for (int i = 0; i < rate.length; i++)
				mSensor.get(sType).add(Integer.valueOf(rate[i]));
			mSensor.get(sType).add(Integer.valueOf(0xcc));
			for (int i = 0; i < bundleSize.length; i++)
				mSensor.get(sType).add(Integer.valueOf(bundleSize[i]));
			flag = true;
		}
		//		else {
		//			val = mSensor.get(sType);
		//			if (val.get(1) != rate) {
		//				val.set(1, Integer.valueOf(rate));
		//				flag = true;
		//			}
		//			if (val.get(2) != bundleSize) {
		//				val.set(2, Integer.valueOf(bundleSize));
		//				flag = true;
		//			}
		//		}
		return flag;
	}

	synchronized public int getMaxRate(int sType)
	{
		int rate = -1;
		if (mSensor.containsKey(sType)) {
			rate = mSensor.get(sType).get(1);
		}
		return rate;
	}

	/**
	 * return index of 0Xcc
	 */
	private int rateEndIndex(ArrayList<Integer> stream)
	{
		return stream.indexOf(Integer.valueOf(0xcc));
	}

	private int bundleSizeEndIndex(ArrayList<Integer> stream)
	{
		return stream.size();
	}

	synchronized public List<Integer> getRateList(int sType)
	{
		ArrayList<Integer> pk = null;

		if (mSensor.containsKey(sType)) {
			pk = mSensor.get(sType);
			return pk.subList(1, rateEndIndex(pk));
		}
		return null;
	}

	synchronized public List<Integer> getBundleSizeList(int sType)
	{
		ArrayList<Integer> pk = null;

		if (mSensor.containsKey(sType)) {
			pk = mSensor.get(sType);
			return pk.subList(rateEndIndex(pk) + 1, bundleSizeEndIndex(pk));
		}
		return null;
	}

	private void _removeSensor(HashMap<String, Object> map)
	{
		return;
	}

	synchronized public boolean removeSensor(int sType)
	{
		if (sType == Sensor.TYPE_ALL) {
			mSensor.clear();
			return true;
		}

		Integer key = Integer.valueOf(sType);
		if (mSensor.containsKey(key)) {
			mSensor.remove(key);
			return true;
		} else
			return false;
	}

	synchronized public ArrayList<Integer> getSensorList()
	{
		ArrayList<Integer> sList = new ArrayList<Integer>();

		for (Map.Entry<Integer, ArrayList<Integer>> entry: mSensor.entrySet())
		{
			sList.add(entry.getKey());
		}
		return sList;
	}

	synchronized public boolean isEmpty()
	{
		return mSensor.isEmpty();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	private int[] getIntArray(ArrayList<Integer> obj)
	{
		int[] sensors = new int[obj.size()];
		for (int i = 0; i < obj.size(); i++)
		{
			sensors[i] = (Integer) obj.get(i);
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
			int mapSize = src.readInt();
			if (mapSize > 0) {
				for (int i = 0; i < mapSize; i++)
				{
					int key = src.readInt();
					int[] iarray = src.createIntArray();
					ArrayList<Integer> value = device.getArrayList(iarray);
					device.mSensor.put(key, value);
				}
			}
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
		parcel.writeInt(mSensor.size());
		if (mSensor.size() > 0) {
			for(Map.Entry<Integer, ArrayList<Integer>> entry : mSensor.entrySet())
			{
				parcel.writeInt(entry.getKey());
				parcel.writeIntArray(getIntArray(entry.getValue()));
			}
		}
	}
}