package com.ucla.nesl.devicescan;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ScanBluetooth {

	private static String tag = ScanBluetooth.class.getCanonicalName();
	DeviceScan parent = null;
	Context    context = null;
	String    ZephyrPattern = "^[B][H][ ][B][H][T].*";
	private static ScanBluetooth mScanBluetooth;
	private BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	public static void init(DeviceScan parent, Context context)
	{
		if (mScanBluetooth == null) {
			mScanBluetooth = new ScanBluetooth(parent, context);
		}
		mScanBluetooth.discover();
	}

	private ScanBluetooth(DeviceScan parent, Context context)
	{
		this.parent = parent;
		this.context = context;
	}

	public void startDiscovery()
	{
		myBluetoothAdapter.startDiscovery();
	}
	
	public void _unregisterReceiver(BroadcastReceiver receiver)
	{
		context.unregisterReceiver(receiver);
	}
	
	public void _registerReceiver(BroadcastReceiver receiver, IntentFilter filter)
	{
		context.registerReceiver(receiver, filter);
	}

	void discover()
	{
		startDiscovery();
		_registerReceiver(ActionFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

	}

	private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent receiverIntent) {
			String action = receiverIntent.getAction();
			if(BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = receiverIntent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Log.d(tag, "Name: " + device.getName() + ", address: " + device.getAddress() + " , class: " + device.getBluetoothClass() + " , ");
				if (device.getName() != null && device.getName().matches(ZephyrPattern)) {
					Log.d(tag, "Zephyr device: " + device.getName() + ", address: " + device.getAddress());
					Intent intent = new Intent("ZephyrDriverBroadcastReceiver");
					intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					intent.putExtra("bluetoothAddr", device.getAddress());
					context.startService(intent);
				}
			}
		}
	};

	private void _destroy()
	{
		_unregisterReceiver(ActionFoundReceiver);
	}

	public static void destroy()
	{
		mScanBluetooth._destroy();
	}
}
