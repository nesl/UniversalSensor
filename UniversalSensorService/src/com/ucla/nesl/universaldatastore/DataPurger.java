package com.ucla.nesl.universaldatastore;

import java.util.Calendar;

import android.os.Handler;
import android.util.Log;

public class DataPurger extends Thread {
	private Handler mHandler;
	DataStoreManager mDataStoreManager;
	private static String tag = DataPurger.class.getCanonicalName();
	
	public DataPurger(DataStoreManager mDataStoreManager)
	{
		this.mDataStoreManager = mDataStoreManager;
	}

	/*
	 * we can make use of the mDataStoreManager class object to 
	 * write functions in that class to implement our logic of
	 * purging the data.
	 */
	private void purge()
	{
//		mDataStoreManager.purgeall();
	}
	public void run()
	{
//		Looper.prepare();
//		mHandler = new Handler();
		while (true) {
			// Place all the functions related to purging here
			// Currently, we are purging weekly data
			Log.d(tag, "Purging data");
			purge();
			Calendar c = Calendar.getInstance();
	        c.add(Calendar.DAY_OF_MONTH, 1);
	        c.set(Calendar.HOUR_OF_DAY, 0);
	        c.set(Calendar.MINUTE, 0);
	        c.set(Calendar.SECOND, 0);
	        c.set(Calendar.MILLISECOND, 0);
	        try {
				Thread.sleep(c.getTimeInMillis() - System.currentTimeMillis());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		Looper.loop();
	}
	
	public Handler getHandler()
	{
		return mHandler;
	}
}
