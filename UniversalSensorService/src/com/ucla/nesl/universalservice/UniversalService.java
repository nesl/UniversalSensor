package com.ucla.nesl.universalservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UniversalService extends Service {
	private String tag = UniversalService.class.getCanonicalName();
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(tag, "onBind called ");

		return new UniversalManagerService(this);
	}
}
