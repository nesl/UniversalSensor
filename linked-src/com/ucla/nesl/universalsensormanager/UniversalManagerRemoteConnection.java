package com.ucla.nesl.universalsensormanager;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.ucla.nesl.aidl.Device;
import com.ucla.nesl.aidl.IUniversalManagerService;
import com.ucla.nesl.universalsensormanager.UniversalSensorManager.UniversalSensorManagerStub;

public class UniversalManagerRemoteConnection implements ServiceConnection {

	private UniversalSensorManager mManager = null;
	private IUniversalManagerService service = null;
	
	public UniversalManagerRemoteConnection(UniversalSensorManager mManager)
	{
		this.mManager = mManager;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.service = IUniversalManagerService.Stub.asInterface(service);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
	}
	
	public Device[] listDevices() throws RemoteException
	{
		if (service == null) {
			mManager.connectRemote();
			return null;
		}
		return null;
	}
	
	public void registerListener(UniversalSensorManagerStub cb,
			String devID, int sType, int rateUs)
	{
		try {
			service.registerListener(cb, devID, sType, rateUs);
		}catch(RemoteException e){}
	}
}
