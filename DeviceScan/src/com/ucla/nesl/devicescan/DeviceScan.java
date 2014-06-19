package com.ucla.nesl.devicescan;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class DeviceScan extends Service {
	private String tag = DeviceScan.class.getCanonicalName();
	private boolean started = false;
	Handler mhandler = null;
	Long startTime = 60000l;

	void initProtocol()
	{
		ScanBluetooth.init(this, getApplicationContext());
		PhoneDriver.init(this, getApplicationContext());
	}

	void deinitProtocol()
	{
		ScanBluetooth.destroy();
		PhoneDriver.destroy();
	}
	
	Runnable rStart = new Runnable() {
		public void run() {
			initProtocol();
			mhandler.postDelayed(rStart, startTime);
		}
	};
		
	void init()
	{
		if (started == true)
			return;
		Log.i(tag, "DeviceScan starting");
		started = true;
		mhandler = new Handler();
		mhandler.postDelayed(rStart, 100);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		init();
		return 0;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onDestroy()
	{
		deinitProtocol();
	}
}
