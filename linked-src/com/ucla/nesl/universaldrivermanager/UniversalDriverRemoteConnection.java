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

public class UniversalDriverRemoteConnection implements ServiceConnection {
	private IUniversalManagerService service;
	private UniversalDriverManager parent;
	
	UniversalDriverRemoteConnection(UniversalDriverManager parent) {
		this.parent = parent;
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.service = IUniversalManagerService.Stub.asInterface(service);
		try {
			parent.setDevID(this.service.getDevID());
		} catch(RemoteException e){}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
	}		
	
	public void push(SensorParcel[] sp)
	{
		try {
			service.onSensorChanged(sp, sp.length);
		} catch(RemoteException e) {}
	}

	public boolean registerDriver(Device device, UniversalDriverManagerStub mDriverManagerStub,
			int sType, int rate, int bundleSize) throws RemoteException
	{
		return service.registerDriver(device, mDriverManagerStub, sType, rate, bundleSize);
	}

	public boolean unregisterDriver(String devID, int sType) throws RemoteException
	{
		return service.unregisterDriver(devID, sType);
	}
	
//	public void pushArray(SensorParcel[] sp)
//	{
//		try {
//			service.onSensorChangedArray(sp, sp.length);
//		} catch (RemoteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}

