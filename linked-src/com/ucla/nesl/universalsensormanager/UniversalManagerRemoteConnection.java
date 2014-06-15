package com.ucla.nesl.universalsensormanager;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalManagerService;
import com.ucla.nesl.universalsensormanager.UniversalSensorManager.UniversalSensorManagerStub;

public class UniversalManagerRemoteConnection implements ServiceConnection {

	private UniversalSensorManager mManager = null;
	private IUniversalManagerService service = null;
	private static String tag = UniversalManagerRemoteConnection.class.getCanonicalName();

	public UniversalManagerRemoteConnection(UniversalSensorManager mManager)
	{
		this.mManager = mManager;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(tag, "Successfully established connection with UniversalService, now ready to perform remote operations.");
		this.service = IUniversalManagerService.Stub.asInterface(service);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d(tag, "UniversalService disconnected");
		this.service = null;
	}

	public ArrayList<Device> listDevices() throws RemoteException
	{
		// A better approach will be that we store the request and on connect
		// we execute this later.
		if (service == null) {
			mManager.connectRemote();
			Log.d(tag, "Service is not connected, failing listDevices");
			return null;
		}
		Log.d(tag, "Quering UniversalService to listDevices");
		return (ArrayList<Device>)service.listDevices();
	}

	public boolean registerListener(UniversalSensorManagerStub cb,
			String devID, int sType, boolean periodic, int rateUs, int bundleSize)
	{
		if (service == null) {
			mManager.connectRemote();
			Log.d(tag, "Service is not connected, failing registerListener");
			return false;
		}
		try {
			service.registerListener(cb, devID, sType, periodic, rateUs, bundleSize);
		}catch(RemoteException e)
		{
			
		}
		return true;
	}

	public boolean unregisterListener(String devID, int sType)
	{
		if (service == null) {
			mManager.connectRemote();
			Log.d(tag, "Service is not connected, failing unregisterListener");
			return false;
		}
		
		try {
			service.unregisterListener(devID, sType);
		}catch(RemoteException e){}
		
		return true;
	}

	public boolean registerNotification(UniversalSensorManagerStub cb)
	{
		if (service == null) {
			mManager.connectRemote();
			Log.d(tag, "Service is not connected, failing registerNotification");
			return false;
		}
		
		try {
			service.registerNotification(cb);
		} catch (RemoteException e) {}
		return true;
	}
}
