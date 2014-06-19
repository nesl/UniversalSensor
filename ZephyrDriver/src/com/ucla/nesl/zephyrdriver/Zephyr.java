package com.ucla.nesl.zephyrdriver;

import java.util.HashMap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.IBinder;
import android.util.Log;

public class Zephyr extends Service {
	private static String tag = Zephyr.class.getCanonicalName();
	private HashMap<String, Thread> instanceMap = new HashMap<String, Thread>();
	Thread thread;
	int val = 0;
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		Log.i(tag, "onReceive called");
//		thread = new Thread(new ZephyrDriver(context, "C8:3E:99:0D:D4:90"));
//		thread.start();
//	}
	
	@Override
	public IBinder onBind(Intent arg0) 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		String bluetoothAddr = intent.getStringExtra("bluetoothAddr");
		Log.d(tag, "onStartCommand called for device addr" + bluetoothAddr);

		if (bluetoothAddr == null)
			return 0;

		thread = instanceMap.get(bluetoothAddr);
		if ((thread == null) || (thread.isAlive() == false)) {
			Log.i(tag, "starting a new instance of zephyr driver " + bluetoothAddr);
			thread = new Thread(new ZephyrDriver(getApplicationContext(), bluetoothAddr));
			thread.start();
			instanceMap.put(bluetoothAddr, thread);
		}
		return super.onStartCommand(intent,flags,startId);
	}
}