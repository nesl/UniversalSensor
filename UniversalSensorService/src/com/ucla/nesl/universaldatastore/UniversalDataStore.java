package com.ucla.nesl.universaldatastore;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.ucla.nesl.aidl.IUniversalSensorManager;
import com.ucla.nesl.lib.HelperWrapper;
import com.ucla.nesl.lib.SensorParcelWrapper;
import com.ucla.nesl.lib.UniversalConstants;

public class UniversalDataStore extends Thread {
	private static Handler mHandler = null;
	private static String tag = UniversalDataStore.class.getCanonicalName();
	private DataStoreManager mDataStoreManager = null;

	public static Handler getHandler()
	{
		return mHandler;
	}

	public UniversalDataStore(DataStoreManager mDataStoreManager)
	{
		this.mDataStoreManager = mDataStoreManager;
	}

	private void fetchRecord()
	{
	}

	private void storeRecord(SensorParcelWrapper spw)
	{
		mDataStoreManager.insertSensorData(spw.mSensorKey, spw.sType, spw.values, spw.timestamp);
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

	private void fetchHistoricalData(HelperWrapper	helperWrapper)
	{

		IUniversalSensorManager mListener = helperWrapper.mListener;
		Bundle mbundle = helperWrapper.mBundle;
		String tableName = mbundle.getString("tableName");
		int txnID = mbundle.getInt("txnID");
		String devID = mbundle.getString("devID");
		int sType = mbundle.getInt("sType");
		long start = mbundle.getLong("start");
		long end = mbundle.getLong("end");
		long interval = mbundle.getLong("interval");
		int function = mbundle.getInt("function");

		JSONObject obj = new JSONObject();

		long newstart = start;
		while(newstart <= end) {
			try {
				if (mDataStoreManager == null) {
					Log.e(tag, "mDataStoreManager is null");
					return;
				}
				HashMap<String, Float> h = mDataStoreManager.compute(tableName, sType, function, newstart, newstart + interval);
				if (h != null)
					obj.put(""+newstart, h);
				else
					break;

			} catch (JSONException e) {
				e.printStackTrace();
			}
			newstart = newstart + interval;
		}

		Log.d(tag, "Successfully completed fetch historical data request, sending response");
		try {
			mListener.historicalDataResponse(txnID, devID, sType, function, obj.toString());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run()
	{
		Looper.prepare();
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UniversalConstants.MSG_FETCH_RECORD:
					fetchRecord();
					break;
				case UniversalConstants.MSG_STORE_RECORD:
					storeRecord((SensorParcelWrapper) msg.obj);
					break;
				case UniversalConstants.MSG_ListHistoricalDevices:
					listHistoricalDevices((IUniversalSensorManager)msg.obj);
					break;
				case UniversalConstants.MSG_FETCH_HISTORICAL_DATA:
					fetchHistoricalData(((HelperWrapper)msg.obj));
				}
			}
		};
		Looper.loop();
	}

}
