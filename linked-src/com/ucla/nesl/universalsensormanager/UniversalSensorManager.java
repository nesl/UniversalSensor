package com.ucla.nesl.universalsensormanager;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalSensorManager;
import com.ucla.nesl.aidl.SensorParcel;
import com.ucla.nesl.lib.UniversalConstants;
import com.ucla.nesl.lib.UniversalEventListener;
import com.ucla.nesl.lib.UniversalSensorEvent;

public class UniversalSensorManager {
	private String UNIVERSALServicePackage = "com.ucla.nesl.universalsensorservice";
	private String UNIVERSALServiceClass = "com.ucla.nesl.universalservice.UniversalService";
	private static UniversalSensorManager mManager = null;
	private UniversalSensorManagerStub mstub = null;
	private UniversalManagerRemoteConnection remoteConnection;
	private Context context;
	private static String tag = UniversalSensorManager.class.getCanonicalName();

	/*
	 * This method must be used to create an object of the UniversalSensorManager. 
	 * UniversalSensorManager is a singleton class and the only way to create the 
	 * object of it is via the create method. When called, it creates a new 
	 * UniversalSensorManager object or returns an already created object.
	 */
	public static UniversalSensorManager create(Context context, UniversalEventListener mlistener) {
		if (mManager != null) {
			Log.d(tag, "UniversalSensorManager object already exists. Returning the same.");
			return mManager;
		}
		Log.d(tag, "Creating a new UniversalSensorManager object");
		mManager = new UniversalSensorManager(context, mlistener);
		return mManager;
	}

	private UniversalSensorManager(Context context, UniversalEventListener mlistener) {
		this.context = context;
		Log.d(tag, "Instantiating binder connection with UniversalService");
		remoteConnection = new UniversalManagerRemoteConnection(this);
		mstub = new UniversalSensorManagerStub(this, mlistener);
		connectRemote();
	}

	/*
	 * This method returns the list of device instances registered with the 
	 * UniversalService along with their sensors to application
	 */
	public ArrayList<Device> listDevices()
	{
		try {
			return remoteConnection.listDevices();
		} catch (RemoteException e) {
			Log.e(tag, "listDevices returned with error " + e);
			return null;
		}
	}

	/*
	 * Applications must use this method to register with the UniversalService 
	 * to receive sensor data of sensor(sType) of the device (devID).
	 * @params
	 * devID: The devID as seen from the listDevices
	 * sType: The sensorType, refer to UniversalSensor class for more information
	 * periodic: If true, the UniversalService will send data periodically to the 
	 * 	application or else its the responsibility of the application to pull the 
	 * 	data from the UniversalService.
	 * rate: The rate at which the application wants to receive sensordata
	 * bundleSize: Number of samples per update
	 */
	public boolean registerListener(String devID, int sType, boolean periodic, int rateUs, int bundleSize)
	{
		if (mstub == null) {
			Log.i(tag, "mstub is null " + devID);
			return false;
		}

		remoteConnection.registerListener(mstub, devID, sType, periodic, rateUs, bundleSize);
		return true;
	}

	/*
	 * This method unregisters an application from receiving data of 
	 * sType from the device with devID.
	 */
	public boolean unregisterListener(String devID, int sType)
	{
		remoteConnection.unregisterListener(devID, sType);
		return true;
	}

	void connectRemote()
	{
		Intent intent = new Intent("bindUniversalSensorService");
		intent.setClassName(UNIVERSALServicePackage, UNIVERSALServiceClass);
		context.bindService(intent, remoteConnection, Context.BIND_AUTO_CREATE);
	}

	/*
	 * This method must be called when an application wants to register 
	 * notifications when a new device is in the vicinity of the phone
	 */
	public void registerNotification(UniversalEventListener mlistener)
	{
		mstub.registerListener(mlistener);
		remoteConnection.registerNotification(mstub);
	}

	/*
	 * This method is used to query the list of all the devices and their 
	 * corresponding sensor data stored in the phone�s database. This is 
	 * an async method. When called it sends the request to the 
	 * UniversalService and returns. Later, the applications 
	 * listHistoricalDevices method will be called when the data is 
	 * received from the UniversalService.
	 */
	public boolean listHistoricalDevice()
	{
		return remoteConnection.listHistoricalDevices(mstub);
	}

	/*
	 * This function issues a request to the UniversalService to perform �function� 
	 * on the sensor of type sType of device devID beginner from timestamp �start� 
	 * until �end� with a window period of interval time. Everything is in nanoseconds. 
	 * This is an async call.
	 * @params
	 * txnID: a number generated by application. The response will contain the same number
	 * devID: device whose data has to be fetched
	 * sType: sensor type 
	 * start: Start time of the query
	 * end: end time of the query
	 * interval: the window size
	 * function: The type of function that must be prformed. Refer to UniversalConstants 
	 *   for more information on this. 
	 */
	public void fetchHistoricalData(int txnID, String devID, int sType,
			long start, long end, long interval, int function)
	{
		remoteConnection.fetchHistoricalData(mstub, txnID, devID, sType, start, end, interval, function);
	}

	/*
	 * This method is called by the RemoteConnection object when it sees that
	 * the UniversalService has is dead.
	 */
	public void disconnected()
	{
		mstub.mlistener.disconnected();
	}

	/* 
	 * Applications call this method to check if it is connected with the Universalservice
	 */
	public boolean isConnected()
	{
		return remoteConnection.isConnected();
	}

	class UniversalSensorManagerStub extends IUniversalSensorManager.Stub {
		UniversalEventListener mlistener = null;
		UniversalSensorManager mManager = null;
		int i = 0;
		UniversalSensorManagerStub(UniversalSensorManager mManager, UniversalEventListener mlistener)
		{
			this.mManager = mManager;
			this.mlistener = mlistener;
		}

		public void registerListener(UniversalEventListener mlistener)
		{
			this.mlistener = mlistener;
		}

		@Override
		public void onSensorChanged(String devID, int sType, float[] values, long[] timestamp) throws RemoteException 
		{
//			float[] mValues = new float[UniversalConstants.getValuesLength(sType)];
//			UniversalSensorEvent[] event = new UniversalSensorEvent[values.length];
//
//			for (i = 0; i < timestamp.length; i++) {
//				
//				event[i] = new UniversalSensorEvent();
//			}
			mlistener.onSensorChanged(devID, sType, values, timestamp);
		}

		@Override
		public void notifyNewDevice(Device mdevice) {
			mlistener.notifyNewDevice(mdevice);
		}

		@Override
		public void notifySensorChanged(String devID, int sType, int action) {
			mlistener.notifySensorChanged(devID, sType, action);
		}

		@Override
		public void listHistoricalDevice(String[] devices)
				throws RemoteException {
			if (devices == null) {
				Log.d(tag, "listHistoricalDevice: remote service returned null");
				mlistener.listHistoricalDevices(null);
			}
			Log.d(tag, "listHistoricalDevice: returned " + devices.length + " number of sensors");

			HashMap<String, ArrayList<Integer>> deviceList = new HashMap<String, ArrayList<Integer>>();
			for (String mSensor : devices) {
				String[] mdev;
				try {
					mdev = mSensor.split("_");

					ArrayList<Integer> sensorList = deviceList.get(mdev[0]);
					if (sensorList == null) {
						sensorList = new ArrayList<Integer>();
						deviceList.put(mdev[0], sensorList);
					}
					sensorList.add(Integer.valueOf(mdev[1]));
				} catch (Exception e) {
					continue;
				}
			}
			mlistener.listHistoricalDevices(deviceList);
		}

		@Override
		public void historicalDataResponse(int txnID, String devID, int sType,
				int function, String dataStream) {
			try {
				mlistener.historicalDataResponse(txnID, devID, sType, function, new JSONObject(dataStream));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}