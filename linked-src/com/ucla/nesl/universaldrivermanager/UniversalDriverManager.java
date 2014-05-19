package com.ucla.nesl.universaldrivermanager;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalDriverManager;
import com.ucla.nesl.aidl.SensorParcel;

public class UniversalDriverManager {
	private String UNIVERSALServicePackage = "com.ucla.nesl.universalsensorservice";
	private String UNIVERSALServiceClass = "com.ucla.nesl.universalservice.UniversalService";
	private Context context;
	private UniversalDriverRemoteConnection remoteConnection;
	private static UniversalDriverManager mManager = null;
	
	public static UniversalDriverManager create(Context context)
	{
		if (mManager != null)
			return mManager;
		mManager = new UniversalDriverManager(context);
		return mManager;
	}
	
	public UniversalDriverManager(Context context) {
		this.context = context;
		remoteConnection = new UniversalDriverRemoteConnection(this);
		connectRemote();
	}
	
	public Boolean push(int sType, float[] values, int valueSize, int accuracy, float timestamp)
	{
		SensorParcel sp = new SensorParcel(sType, values, valueSize, accuracy, timestamp);
		remoteConnection.push(sp);
		return true;
	}

	public void registerDriver(Device device)
	{
		remoteConnection.registerDriver(new UniversalDriverManagerStub(), device);
	}

	void connectRemote()
	{
		Intent intent = new Intent("bindUniversalSensorService");
		intent.setClassName(UNIVERSALServicePackage, UNIVERSALServiceClass);
		context.bindService(intent, remoteConnection, Context.BIND_AUTO_CREATE);
	}
	
	public class UniversalDriverManagerStub extends IUniversalDriverManager.Stub {

		@Override
		public void setRate(int rate) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
	}
}