package com.ucla.nesl.universalsensormanager;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalSensorManager;
import com.ucla.nesl.aidl.SensorParcel;
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

	public static UniversalSensorManager create(Context context) {
		if (mManager != null) {
			Log.d(tag, "UniversalSensorManager object already exists. Returning the same.");
			return mManager;
		}
		Log.d(tag, "Creating a new UniversalSensorManager object");
		mManager = new UniversalSensorManager(context);
		return mManager;
	}

	private UniversalSensorManager(Context context) {
		this.context = context;
		Log.d(tag, "Instantiating binder connection with UniversalService");
		remoteConnection = new UniversalManagerRemoteConnection(this);
		mstub = new UniversalSensorManagerStub(this);
		connectRemote();
	}

	public ArrayList<Device> listDevices()
	{
		try {
			return remoteConnection.listDevices();
		} catch (RemoteException e) {
			Log.e(tag, "listDevices returned with error " + e);
			return null;
		}
	}

	public boolean registerListener(UniversalEventListener mlistener, 
			String devID, int sType, boolean periodic, int rateUs, int bundleSize) {
		if (mstub == null) {
			Log.i(tag, "mstub is null " + devID);
			return false;
		}
		mstub.registerListener(mlistener);
		remoteConnection.registerListener(mstub, devID, sType, periodic, rateUs, bundleSize);
		return true;
	}

	public boolean unregisterListener(UniversalEventListener mlistener, String devID, int sType)
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

	public void registerNotification(UniversalEventListener mlistener)
	{
		remoteConnection.registerNotification(mstub);
	}

	class UniversalSensorManagerStub extends IUniversalSensorManager.Stub {
		UniversalEventListener mlistener = null;
		UniversalSensorManager mManager = null;
		int i = 0;
		UniversalSensorManagerStub(UniversalSensorManager mManager)
		{
			this.mManager = mManager;
		}

		public void registerListener(UniversalEventListener mlistener)
		{
			this.mlistener = mlistener;
		}

		@Override
		public void onSensorChanged(SensorParcel[] sp) throws RemoteException {
			UniversalSensorEvent[] event = new UniversalSensorEvent[sp.length];
			for (i = 0; i < sp.length; i++)
				event[i] = new UniversalSensorEvent(sp[i]);
			mlistener.onSensorChanged(event);
		}

		@Override
		public void notifyNewDevice(Device mdevice) {
			mlistener.notifyNewDevice(mdevice);
		}

		@Override
		public void notifySensorChanged(String devID, int sType, int action) {
			mlistener.notifySensorChanged(devID, sType, action);
		}
	}
}