package com.ucla.nesl.universaldrivermanager;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalDriverManager;
import com.ucla.nesl.aidl.SensorParcel;
import com.ucla.nesl.lib.UniversalDriverListener;

public class UniversalDriverManager {
	private static String tag = UniversalDriverManager.class.getCanonicalName();
	private String UNIVERSALServicePackage = "com.ucla.nesl.universalsensorservice";
	private String UNIVERSALServiceClass = "com.ucla.nesl.universalservice.UniversalService";
	private Context context;
	private UniversalDriverRemoteConnection remoteConnection;
//	private UniversalDriverManager mManager = null;
	private Device device = null;
	private UniversalDriverListener listener = null;
	private boolean registered = false;

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
		remoteConnection = new UniversalDriverRemoteConnection(this);
		connectRemote();
	}

	public Boolean push(int sType, float[] values, int accuracy, float timestamp)
	{
		SensorParcel sp = new SensorParcel(device.devID, sType, values, values.length, accuracy, timestamp);
		remoteConnection.push(sp);
		return true;
	}

	public void registerDriver(UniversalDriverListener listener, int sType)
	{
		boolean flag = false;
		if (device.addSensor(sType))
			flag = true;
		if (!registered) {
			device.devID = remoteConnection.registerDriver(new UniversalDriverManagerStub(this), device);
			registered = true;
			flag = false;
		}
		Log.i(tag, "devID: " + device.devID);
		if (flag)
			enableSensor(device.devID, sType);
	}

	public void enableSensor(String devID, int sType)
	{
		remoteConnection.addSensor(devID, sType);
	}

	public void unregisterDriver(UniversalDriverListener listener, int sType)
	{
		device.removeSensor(sType);
		remoteConnection.removeSensor(device.devID, sType);
	}
	private void connectRemote()
	{
		Intent intent = new Intent("bindUniversalSensorService");
		intent.setClassName(UNIVERSALServicePackage, UNIVERSALServiceClass);
		context.bindService(intent, remoteConnection, Context.BIND_AUTO_CREATE);
	}

	public class UniversalDriverManagerStub extends IUniversalDriverManager.Stub {
		private UniversalDriverManager dManager;

		public UniversalDriverManagerStub(UniversalDriverManager dManager) {
			this.dManager = dManager;
		}

		@Override
		public void setRate(int rate) throws RemoteException {
			if (rate > 0) {
				dManager.registerDriver(null, rate);
			} else {
				dManager.unregisterDriver(null, -rate);
			}
		}
	}
}