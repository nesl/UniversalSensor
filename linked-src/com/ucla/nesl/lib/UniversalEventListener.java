package com.ucla.nesl.lib;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONObject;

import com.ucla.nesl.aidl.Device;

public interface UniversalEventListener {
	/**                                                                                  
	 * Called when sensor values have changed.                                           
	 * <p>See {@link android.hardware.SensorManager SensorManager}                       
	 * for details on possible sensor types.                                             
	 * <p>See also {@link android.hardware.SensorEvent SensorEvent}.                     
	 * 
	 * <p><b>NOTE:</b> The application doesn't own the                                   
	 * {@link android.hardware.SensorEvent event}                                        
	 * object passed as a parameter and therefore cannot hold on to it.                  
	 * The object may be part of an internal pool and may be reused by                   
	 * the framework.
	 *  
	 * @param event the {@link android.hardware.SensorEvent SensorEvent}.                
	 */                                                                                  
	public void onSensorChanged(String devID, int sType, float[] values, long[] timestamp);                                      

	public void notifySensorChanged(String devID, int sType, int action);

	void notifyNewDevice(Device mdevice);

	void listHistoricalDevices(Map<String, ArrayList<Integer>> deviceList);

	void historicalDataResponse(int txnID, String devID, int sType, int cmd, JSONObject result);

	void disconnected();
	
	void onUniversalServiceConnected();
}
