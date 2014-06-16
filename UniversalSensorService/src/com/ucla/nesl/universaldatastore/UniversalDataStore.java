package com.ucla.nesl.universaldatastore;

import com.ucla.nesl.aidl.SensorParcel;
import com.ucla.nesl.lib.SensorParcelWrapper;
import com.ucla.nesl.lib.UniversalConstants;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class UniversalDataStore extends Thread {
	private static Handler mHandler = null;
	private static UniversalDataStore mUniversalDataStore = null;
	private DataStoreManager mDataStoreManager = null;

	public static Handler getHandler()
	{
		return mHandler;
	}

	public UniversalDataStore(Context context)
	{
		mDataStoreManager = new DataStoreManager(context, null, null, 1);
	}

	private void fetchRecord()
	{
	}

	private void storeRecord(SensorParcel[] sp, int length)
	{
		mDataStoreManager.insertSensorData(sp);//, length);
	}

	@Override
	public void run() {
		Looper.prepare();
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UniversalConstants.MSG_FETCH_RECORD:
					fetchRecord();
					break;
				case UniversalConstants.MSG_STORE_RECORD:
					storeRecord(((SensorParcelWrapper) msg.obj).sp, ((SensorParcelWrapper) msg.obj).length);
					break;
				}
			}
		};
		Looper.loop();
	}

}
