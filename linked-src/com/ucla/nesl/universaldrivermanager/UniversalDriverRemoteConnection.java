package com.ucla.nesl.universaldrivermanager;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalManagerService;
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
		parent.disconnected();
	}

	public void push(String devID, int sType, float[] data, long[] timestamp)
	{
		if (service == null) {
			parent.connectRemote();
			return;
		}

		try {
			// Service can be null here, so try to reconnect and fail this 
			service.onSensorChanged(devID, sType, data, timestamp);
		} catch (RemoteException e) {
			Log.e(tag, "push");
			e.printStackTrace();
			parent.disconnected();
		}
	}

	public boolean registerDriver(Device device, UniversalDriverManagerStub mDriverManagerStub,
			int sType, int rate[], int bundleSize[])
	{
		if (service == null) {
			parent.connectRemote();
			return false;
		}

		try {
			return service.registerDriver(device, mDriverManagerStub, sType, rate, bundleSize);
		} catch (RemoteException e) {
			Log.e(tag, "registerDriver");
			e.printStackTrace();
			parent.disconnected();
		}
		return false;
	}

	public boolean unregisterDriver(String devID, int sType)
	{
		if (service == null) {
			parent.connectRemote();
			return false;
		}

		try {
			return service.unregisterDriver(devID, sType);
		} catch (RemoteException e) {
			Log.e(tag, "unregisterDriver ");
			e.printStackTrace();
			parent.disconnected();
		}
		return false;
	}

	public boolean isConnected()
	{
		return service == null ? false : true;
	}
}

