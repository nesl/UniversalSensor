package com.ucla.nesl.zephyrdriver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class Zephyr extends BroadcastReceiver  {
	private static String tag = Zephyr.class.getCanonicalName();
	Thread thread;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(tag, "onReceive called");
		thread = new Thread(new ZephyrDriver("C8:3E:99:0D:D4:90"));
		thread.start();
	}
}