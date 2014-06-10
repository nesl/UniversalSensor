package com.ucla.nesl.universaldrivermanager;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalDriverManager;
import com.ucla.nesl.lib.UniversalDriverListener;
import com.ucla.nesl.lib.UniversalSensorEvent;

public class UniversalDriverManager {
	private static String tag = UniversalDriverManager.class.getCanonicalName();
	private String UNIVERSALServicePackage = "com.ucla.nesl.universalsensorservice";
	private String UNIVERSALServiceClass = "com.ucla.nesl.universalservice.UniversalService";
	private Context context;
	private UniversalDriverRemoteConnection remoteConnection;
	//	private UniversalDriverManager mManager = null;
	private Device device = null;
	private String devID  = null;
	private String vendorID = null;
	UniversalDriverManagerStub mDriverManagerStub;
	boolean once = true;

	public static UniversalDriverManager create(Context context, String vendorID)
	{
		//		if (mManager != null)
		//			return mManager;
		//		mManager = new UniversalDriverManager(context);
		//		return mManager;
		return new UniversalDriverManager(context, vendorID);
	}

	public UniversalDriverManager(Context context, String vendorID) {
		this.context = context;
		device = new Device(vendorID);
		this.vendorID = new String(vendorID);
		remoteConnection = new UniversalDriverRemoteConnection(this);
		mDriverManagerStub = null;
		connectRemote();
	}

	public String getVendorID()
	{
		return vendorID;
	}

	private void connectRemote()
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

	public Boolean push(UniversalSensorEvent[] mSensor)
	{
		for (int i = 0; i < mSensor.length; i++)
			mSensor[i].setDevID(devID);
		remoteConnection.push(mSensor);
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
	public boolean registerDriver(UniversalDriverListener mlistener, int sType, int rate[], int bundleSize[])
	{
		// devID null means that the connection establishment is not yet complete
		// TODO: wait and retry after sometime
		if (devID == null) {
			return false;
		}

		// Check if sensor is already registered
		if (device.addSensor(sType, rate, bundleSize) == false) {
			Log.i(tag, "sType " + sType + " already registered");
			return false;
		}

		if (mDriverManagerStub == null)
			mDriverManagerStub = new UniversalDriverManagerStub(this, mlistener);

		Log.d(tag, "Registering new sensor, vendor id: " + device.getVendorID() + ", sensor type:" + sType);
		try {
			remoteConnection.registerDriver(device, mDriverManagerStub, sType, rate, bundleSize);
		} catch (RemoteException e) {
			return false;
			// Also remove the sensor from registered list
		}
		return true;
	}

	public boolean unregisterDriver(UniversalDriverListener listener, int sType)
	{
		Log.i(tag, "unregistering the device " + devID);

		try {
			device.removeSensor(sType);
			remoteConnection.unregisterDriver(device.getDevID(), sType);
		} catch (RemoteException e) {return false;}
		return true;
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