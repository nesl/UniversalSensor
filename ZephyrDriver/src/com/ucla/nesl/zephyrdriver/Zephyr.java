package com.ucla.nesl.zephyrdriver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class Zephyr extends Service {
	private static String tag = Zephyr.class.getCanonicalName();
	Thread thread;
	int val = 0;
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		Log.i(tag, "onReceive called");
//		thread = new Thread(new ZephyrDriver(context, "C8:3E:99:0D:D4:90"));
//		thread.start();
//	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		val = intent.getIntExtra("bluetoothAddr", 0);
//		Log.i(tag, "onStartCommand called " + val);
		val++;
		if (val == 1)
			thread = new Thread(new ZephyrDriver(getApplicationContext(), "C8:3E:99:0D:D4:90"));
		if (val == 2)
			thread = new Thread(new ZephyrDriver(getApplicationContext(), "C8:3E:99:0D:CE:E3"));
		thread.start();
		return super.onStartCommand(intent,flags,startId);
	}
}