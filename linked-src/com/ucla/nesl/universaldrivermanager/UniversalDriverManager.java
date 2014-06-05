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
	
	public Boolean push(UniversalSensorEvent mSensor)
	{
		mSensor.setDevID(devID);
		remoteConnection.push(mSensor);
		return true;
	}

	public boolean registerDriver(UniversalDriverListener mlistener, int sType, int rate)
	{
		// devID null means that the connection establishment is not yet complete
		if (devID == null) {
			return false;
		}

		// Check if sensor is already registered
		if (device.addSensor(sType, rate) == false) {
			Log.i(tag, "sType " + sType + " already registered");
			return false;
		}
		
		if (mDriverManagerStub == null)
			mDriverManagerStub = new UniversalDriverManagerStub(this, mlistener);
		Log.d(tag, "vendor id: " + device.getVendorID());
		try {
			remoteConnection.registerDriver(device, mDriverManagerStub, sType, rate);
			if (once) {
				float[] val = new float[3];
				val[0] = 0.01f;
				val[1] = 0.02f;
				val[2] = 0.03f;
				UniversalSensorEvent[] event = new UniversalSensorEvent[3];
				event[0] = new UniversalSensorEvent(1, val, 100234);
				event[1] = new UniversalSensorEvent(2, val, 100235);
				event[2] = new UniversalSensorEvent(3, val, 100236);
				remoteConnection.pushArray(event);
				once = false;
			}
		} catch (RemoteException e) {
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
		public void setRate(int sType, int rate, float updateInterval) throws RemoteException {
			this.mlistener.setRate(sType, rate);
		}

	}
}