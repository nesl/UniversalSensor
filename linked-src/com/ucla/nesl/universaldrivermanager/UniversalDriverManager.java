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
	private String devID  = null;
	private String vendorID = null;
	private UniversalDriverListener listener = null;

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
//		device = new Device(vendorID);
		this.vendorID = new String(vendorID);
		remoteConnection = new UniversalDriverRemoteConnection(this);
		connectRemote();
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
	}
	
	public Boolean push(int sType, float[] values, int accuracy, float timestamp)
	{
		SensorParcel sp = new SensorParcel(devID, sType, values, values.length, accuracy, timestamp);
		remoteConnection.push(sp);
		return true;
	}

	public Boolean registerDriver(UniversalDriverListener mlistener, ArrayList<Integer> sTypeList)
	{
		// devID null means that the connection establishment is not yet complete
		if (devID == null) {
			return false;
		}

		if (device == null || device.isEmpty()) {
			// First time registeration
			if(device == null)
				device = new Device(vendorID, devID);
			device.addSensor(sTypeList);
			try {
				remoteConnection.registerDriver(new UniversalDriverManagerStub(this, mlistener), device);
			} catch (RemoteException e)
			{
				device = null;
				return false;
			}
		} else {
			for (int sType : sTypeList)
				try {
					addSensor(device.getDevID(), sType);
				} catch (RemoteException e){return false;}
		}
		return true;
	}

	public boolean addSensor(String devID, int sType) throws RemoteException
	{
		return remoteConnection.addSensor(devID, sType);
	}

	public boolean unregisterDriver(UniversalDriverListener listener, ArrayList<Integer> sTypeList)
	{
		try {
			for (int sType : sTypeList) {
				device.removeSensor(sType);
				remoteConnection.removeSensor(device.getDevID(), sType);
			}

			if (device.isEmpty())
				remoteConnection.unregisterDriver(device.getDevID());
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
		public void setRate(int rate) throws RemoteException {
			mlistener.setRate(rate);
		}

		@Override
		public void activateSensor(int sType) throws RemoteException {
			mlistener.activateSensor(sType);
		}

		@Override
		public void deactivateSensor(int sType) throws RemoteException {
			mlistener.deactivateSensor(sType);
		}
	}
}