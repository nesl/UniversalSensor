package com.ucla.nesl.universaldatastore;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.IUniversalSensorManager;
import com.ucla.nesl.aidl.SensorParcel;
import com.ucla.nesl.lib.SensorParcelWrapper;
import com.ucla.nesl.lib.UniversalConstants;

public class UniversalDataStore extends Thread {
	private static Handler mHandler = null;
	private static String tag = UniversalDataStore.class.getCanonicalName();
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

	private void listHistoricalDevices(IUniversalSensorManager mlistener)
	{
		try {
			mlistener.listHistoricalDevice(mDataStoreManager.retrieve_all_tables());
		} catch (RemoteException e) {
			Log.e(tag, "listHistoricalDevices");
			e.printStackTrace();
		}
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
				case UniversalConstants.MSG_ListHistoricalDevices:
					listHistoricalDevices((IUniversalSensorManager)msg.obj);
					break;
				}
			}
		};
		Looper.loop();
	}

}
