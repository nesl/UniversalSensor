package com.ucla.nesl.universaldrivermanager;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalManagerService;
import com.ucla.nesl.aidl.SensorParcel;
import com.ucla.nesl.universaldrivermanager.UniversalDriverManager.UniversalDriverManagerStub;

public class UniversalDriverRemoteConnection implements ServiceConnection
{
	private static String tag = UniversalDriverManager.class.getCanonicalName();
	private IUniversalManagerService service;
	private UniversalDriverManager parent;

	UniversalDriverRemoteConnection(UniversalDriverManager parent)
	{
		this.parent = parent;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		this.service = IUniversalManagerService.Stub.asInterface(service);
	}

	@Override
	public void onServiceDisconnected(ComponentName name)
	{
		this.service = null;
	}

	public void push(SensorParcel[] sp, int length)
	{
		try {
			// Service can be null here, so try to reconnect and fail this 
			service.onSensorChanged(sp, length);
		} catch (RemoteException e) {
			Log.e(tag, "push");
			e.printStackTrace();
		}
	}

	public boolean registerDriver(Device device, UniversalDriverManagerStub mDriverManagerStub,
			int sType, int rate[], int bundleSize[])
	{
		try {
			return service.registerDriver(device, mDriverManagerStub, sType, rate, bundleSize);
		} catch (RemoteException e) {
			Log.e(tag, "registerDriver");
			e.printStackTrace();
		}
		return false;
	}

	public boolean unregisterDriver(String devID, int sType)
	{
		try {
			return service.unregisterDriver(devID, sType);
		} catch (RemoteException e) {
			Log.e(tag, "unregisterDriver ");
			e.printStackTrace();
		}
		return false;
	}
}

