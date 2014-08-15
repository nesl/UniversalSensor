package com.ucla.nesl.universaldrivermanager;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalDriverManager;
import com.ucla.nesl.lib.UniversalDriverListener;

public class UniversalDriverManager {
	private static String tag = UniversalDriverManager.class.getCanonicalName();
	private String UNIVERSALServicePackage = "com.ucla.nesl.universalsensorservice";
	private String UNIVERSALServiceClass = "com.ucla.nesl.universalservice.UniversalService";
	private Context context;
	private UniversalDriverRemoteConnection remoteConnection;
	private Device device = null;
	private String devID  = null;
	private UniversalDriverManagerStub mDriverManagerStub;
	boolean once = true;

	public static UniversalDriverManager create(Context context, UniversalDriverListener mlistener, String devID)
	{
		return new UniversalDriverManager(context, mlistener, devID);
	}

	public UniversalDriverManager(Context context, UniversalDriverListener mlistener, String devID) {
		this.context = context;
		mDriverManagerStub = new UniversalDriverManagerStub(this, mlistener);
		device = new Device(devID);
		remoteConnection = new UniversalDriverRemoteConnection(this);
		connectRemote();
	}

	public void connectRemote()
	{
		Intent intent = new Intent("bindUniversalSensorService");
		intent.setClassName(UNIVERSALServicePackage, UNIVERSALServiceClass);
		context.bindService(intent, remoteConnection, Context.BIND_AUTO_CREATE);
	}

	public void setDevID(String devID)
	{
		this.devID = devID;
		device.setDevID(devID);
	}

	/*
	 * Sends the Sensor data to the UniversalService.
	 * @params event: Array containing the sensor sample
	 * params length: Bundle size
	 */
	public Boolean push(String devID, int sType, float[] data, long[] timestamp)
	{
		remoteConnection.push(devID, sType, data, timestamp);
		return true;
	}

	/**
	 * Function to register a particular sensor with the UniversalService
	 * @param mlistener 
	 * @param sType Type of sensor
	 * @param rate Maximum samples per second
	 * @param bundleSize Number of samples sent in one bundle
	 * @return Returns true on success and false on failure
	 */
	public boolean registerDriver(int sType, int rate[], int bundleSize[])
	{
		// Check if sensor is already registered
		if (device.addSensor(sType, rate, bundleSize) == false) {
			Log.i(tag, "sType " + sType + " already registered");
			return false;
		}

		Log.d(tag, "Registering new sensor, vendor id: " + device.getDevID() + ", sensor type:" + sType);

		remoteConnection.registerDriver(device, mDriverManagerStub, sType, rate, bundleSize);
		return true;
	}

	/*
	 * Use this when the driver wants to unregister itself with the UniversalService. 
	 * This can happen when the driver cannot connect to the device anymore.
	 */
	public boolean unregisterDriver(int sType)
	{
		Log.i(tag, "unregistering the device " + devID);

		device.removeSensor(sType);
		remoteConnection.unregisterDriver(device.getDevID(), sType);
		return true;
	}

	public void disconnected()
	{
		mDriverManagerStub.mlistener.disconnected();
	}

	/*
	 * Check to see if the driver is connected to the UniversalService.
	 */
	public boolean isConnected()
	{
		return remoteConnection.isConnected();
	}

	public class UniversalDriverManagerStub extends IUniversalDriverManager.Stub {
		private UniversalDriverManager dManager;
		UniversalDriverListener mlistener;

		public UniversalDriverManagerStub(UniversalDriverManager dManager, UniversalDriverListener mlistener) {
			this.dManager = dManager;
			this.mlistener = mlistener;
		}

		@Override
		public void setRate(int sType, int rate, int bundleSize) throws RemoteException {
			this.mlistener.setRate(sType, rate, bundleSize);
		}

	}
}