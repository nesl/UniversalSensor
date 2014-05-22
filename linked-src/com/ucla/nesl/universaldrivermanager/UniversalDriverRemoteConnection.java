package com.ucla.nesl.universaldrivermanager;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

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
	
	public void push(SensorParcel sp)
	{
		try {
			service.onSensorChanged(sp);
		} catch(RemoteException e) {}
	}

	public boolean registerDriver(UniversalDriverManagerStub mDriver, Device device) throws RemoteException
	{
		return service.registerDriver(mDriver, device);
	}

	public boolean unregisterDriver(String devID) throws RemoteException
	{
		return service.unregisterDriver(devID);
	}
	
	public boolean addSensor(String devID, int sType) throws RemoteException
	{
		return service.addDriverSensor(devID, sType);
	}
	
	void removeSensor(String devID, int sType) throws RemoteException
	{
		service.removeDriverSensor(devID, sType);
	}
}

