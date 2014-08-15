package com.ucla.nesl.zephyrdriver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.ucla.nesl.lib.DriverSensorData;
import com.ucla.nesl.lib.UniversalDriverListener;
import com.ucla.nesl.lib.UniversalSensor;
import com.ucla.nesl.universaldrivermanager.UniversalDriverManager;

public class ZephyrDriver implements Runnable, UniversalDriverListener {
	private static String TAG = ZephyrDriver.class.getCanonicalName();
	private String devID;
	String bluetoothAddr = null;
	private PowerManager.WakeLock mWakeLock;
	private UniversalDriverManager mDriverManager;
	private Context context;

	private int[] chRate = {50}, ecRate = {250}, rpRate = {18}, stRate = {1}, zbRate = {1}, zbtRate = {1};
	private int[] chBundleSize = {20}, ecBundleSize = {63}, rpBundleSize = {18},
			stBundleSize = {1}, zbBundleSize = {1}, zbtBundleSize = {1};

	private static final int RETRY_INTERVAL = 5000; // ms
	private static final int LIFE_SIGN_SEND_INTERVAL = 8000; //ms

	private static final String BLUETOOTH_SERVICE_UUID = "00001101-0000-1000-8000-00805f9b34fb";

	private static final byte START_ECG_PACKET[] = { 0x02, 0x16, 0x01, 0x01, 0x5e, 0x03 };
	private static final byte START_RIP_PACKET[] = { 0x02, 0x15, 0x01, 0x01, 0x5e, 0x03};
	private static final byte START_ACCELEROMETER_PACKET[] = { 0x02, 0x1e, 0x01, 0x01, 0x5e, 0x03 };
	private static final byte START_GENERAL_PACKET[] = { 0x02, 0x14, 0x01, 0x01, 0x5e, 0x03 };

	private static final byte STOP_ECG_PACKET[] = { 0x02, 0x16, 0x01, 0x00, 0x00, 0x03 };
	private static final byte STOP_RIP_PACKET[] = { 0x02, 0x15, 0x01, 0x00, 0x00, 0x03};
	private static final byte STOP_ACCELEROMETER_PACKET[] = { 0x02, 0x1e, 0x01, 0x00, 0x00, 0x03 };
	private static final byte STOP_GENERAL_PACKET[] = { 0x02, 0x14, 0x01, 0x00, 0x00, 0x03 };

	private static final String BUNDLE_SENSOR_ID = "sensor_id";

	//private byte SET_RTC_PACKET[];

//	private FlowEngineAPI mAPI;
//	private int	mDeviceID;
//	private ZephyrDeviceService mThisService = this;

	private BluetoothSocket mSocket;
	private boolean mIsStopRequest = false;
	private OutputStream mOutputStream;
	private InputStream mInputStream;

	private boolean mIsSkinTemp = false;
	private boolean mIsBattery = false;
	private boolean mIsButtonWorn = false;
	private boolean mIsECG = false;
	private boolean mIsRIP = false;
	private boolean mIsAccelerometer = false;

	private long lastWornTime = -1;
	private long lastUnwornTime = -1;
	private long lastECGRecvTime = -1;
	private long lastRIPRecvTime = -1;

	private HashMap<Integer, DriverSensorData> mSensorDataMap = new HashMap<Integer, DriverSensorData>();
	
	float[] breathingData = new float[18];
	float[] ecgData = new float[63];
	float[] accData = new float[60];
	float[] stData  = new float[1];
	float[] zbData  = new float[1];
	float[] zbtData = new float[1];
	long[]	cbtime  = new long[20];
	long[]  rptime  = new long[18];
	long[]	ecgtime = new long[63];
	long[]	sttime  = new long[1];
	long[]  zbtime	= new long[1];
	long[]	zbttime	= new long[1];
	
	public ZephyrDriver(Context context, String bluetoothAddr)
	{
		this.context = context;
		this.bluetoothAddr = new String(bluetoothAddr);
//		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
//		mWakeLock.setReferenceCounted(false);
	}
	
	private boolean connect(String deviceAddress) {
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {        	
			// the UUID of the bridge's service
			UUID uuid = UUID.fromString(BLUETOOTH_SERVICE_UUID);
			mSocket = device.createRfcommSocketToServiceRecord(uuid);
		} 
		catch (IOException e) { 
			Log.e(TAG, "Exception from createRfcommSocketToServiceRecord()..");
			e.printStackTrace();
			return false;
		}

		// just in case, always cancel discovery before trying to connect to a socket.  
		// discovery will slow down or prevent connections from being made
		btAdapter.cancelDiscovery();

		try {
			// Connect the device through the socket. This will block
			// until it succeeds or throws an exception
			mSocket.connect();
		} catch (IOException e) {
			Log.d(TAG, "Failed to connect to " + deviceAddress);
			e.printStackTrace();
			try {
				mSocket.close();
			} catch (IOException e1) {
				Log.e(TAG, "IOException from mSocket.close()..");
				e1.printStackTrace();
			}
			mSocket = null;
			return false;
		}

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			Log.d(TAG, "Thread sleep interrupted.");
		}

		try {
			mOutputStream = mSocket.getOutputStream();
			mInputStream = mSocket.getInputStream();
		} 
		catch (IOException e)
		{
			Log.d(TAG, "IOException from getting input and output stream..");
			e.printStackTrace();
			mSocket = null;
			return false;
		}

		sendStopAllSensorsPacket();
		
		sendSetRTCPacket();
		//sendGetRTCPacket();

		if (mIsAccelerometer) {
			sendStartAccPacket();
		}
		if (mIsECG || mIsRIP || mIsSkinTemp || mIsBattery || mIsButtonWorn) {
			sendStartGeneralPacket();
		}

		return true;
	}
	
	private boolean tryToConnect() {

		int tryCount = 0;
		if (bluetoothAddr == null) {
			Log.d(TAG, "Please setup Bluetooth Address.");
			return false;
		}

		if (mSocket == null) {
			Log.d(TAG, "Connecting..");
			while (!connect(bluetoothAddr)) {
				Log.i(TAG, "Retrying to connect to Zephyr...");
				try {
					Thread.sleep(RETRY_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
			Log.i(TAG, "Connected to Zephyr!");
		}
		return true;
	}

	private long parseTimestamp(byte[] receivedBytes) {
		
		int year = (receivedBytes[4] & 0xFF) | ((receivedBytes[5] & 0xFF) << 8);
		int month = receivedBytes[6];
		int day = receivedBytes[7];
		long millisDay = 0;
		for (int i=8, j=0; i<12; i++, j+=8) {
			millisDay |= (receivedBytes[i] & 0xFF) << j;
		}
		long timestamp = new Date(year - 1900, month - 1, day).getTime() + millisDay;
		
		return timestamp;
	}

	void attachZephyr()
	{
		final byte STX = 2;
		long lastTime = System.currentTimeMillis();
		byte[] receivedBytes = new byte[128];

		Log.i(TAG, "attachZephyr");
		while (!mIsStopRequest) {
			try {					
				// Receiving STX
				do {
					mInputStream.read(receivedBytes, 0, 1);
				} while (receivedBytes[0] != STX);

				// Receiving Msg ID and DLC
				mInputStream.read(receivedBytes, 1, 2);
				int msgID = receivedBytes[1] & 0xFF;

				try {
					// Receiving payload, CRC, and ACK
					mInputStream.read(receivedBytes, 3, receivedBytes[2]+2);
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					continue;
				}
				
				int year = (receivedBytes[4] & 0xFF) | ((receivedBytes[5] & 0xFF) << 8);
				int month = receivedBytes[6];
				int day = receivedBytes[7];

				if (msgID == 0x20) {
					//sample interval: 1s
					//Log.d(TAG, "Received General Data Packet");
					long timestamp = parseTimestamp(receivedBytes);
					//int heartRate = (receivedBytes[12]&0xFF) | ((receivedBytes[13]&0xFF)<<8);
					//int respirationRate = (receivedBytes[14]&0xFF) | ((receivedBytes[15]&0xFF)<<8);
					int skinTemp = (receivedBytes[16]&0xFF) | ((receivedBytes[17]&0xFF)<<8);
					int battery = receivedBytes[54] & 0x7F;
					int buttonWorn = receivedBytes[55] & 0xF0; 

					boolean isWorn = (buttonWorn & 0x80) > 0;
					//boolean isHRSignalLow =(buttonWorn & 0x20) > 0; 
					//handleChestbandStatus(isWorn);
//					handleChestbandStatus(true);

					if (mIsSkinTemp) {
						stData[0] = skinTemp;
						sttime[1] = timestamp;
						mDriverManager.push(devID, UniversalSensor.TYPE_SKIN_TEMPERATURE, stData, sttime);
					}
					if (mIsBattery) {
						zbData[0] = battery;
						zbtime[0] = timestamp;
						mDriverManager.push(devID, UniversalSensor.TYPE_ZEPHYR_BATTERY, zbData, zbtime);
					}
					if (mIsButtonWorn) {
						zbtData[0] = buttonWorn;
						zbttime[0] = timestamp;
						mDriverManager.push(devID, UniversalSensor.TYPE_ZEPHYR_BUTTON_WORN, zbtData, zbttime);
					}
				} else if (msgID == 0x21) {
					//Log.d(TAG, "Received Breathing Waveform Packet");
					long timestamp = parseTimestamp(receivedBytes);
					lastRIPRecvTime = timestamp;
					for (int i=12, j=0; i<35; i+=5)	{
						breathingData[j++] = (receivedBytes[i]&0xFF) | (((receivedBytes[i+1]&0xFF) & 0x03) << 8);
						if (i+2 < 35)
							breathingData[j++] = ((receivedBytes[i+1]&0xFF)>>2) | (((receivedBytes[i+2]&0xFF)&0x0F) << 6);
						if (i+3 < 35)
							breathingData[j++] = ((receivedBytes[i+2]&0xFF)>>4) | (((receivedBytes[i+3]&0xFF)&0x3F) << 4);
						if (i+4 < 35)
							breathingData[j++] = ((receivedBytes[i+3]&0xFF)>>6) | ((receivedBytes[i+4]&0xFF) << 2);
					}

					// sample interval: 56ms
					if (mIsRIP) {
						long mtimeStamp = timestamp;
						for (int i = 0; i < 18; i++) {
							rptime[i] = mtimeStamp;
							mtimeStamp += 56;
						}
						mDriverManager.push(devID, UniversalSensor.TYPE_RIP, breathingData, rptime);
					}
				} else if (msgID == 0x22) {
					//Log.d(TAG, "Received ECG Waveform Packet");
					long timestamp = parseTimestamp(receivedBytes);
					lastECGRecvTime = timestamp;
					for (int i=12, j=0; i<91; i+=5) {
						ecgData[j++] = (receivedBytes[i]&0xFF) | (((receivedBytes[i+1]&0xFF) & 0x03) << 8);
						if (i+2 < 91)
							ecgData[j++] = ((receivedBytes[i+1]&0xFF)>>2) | (((receivedBytes[i+2]&0xFF)&0x0F) << 6);
						if (i+3 < 91)
							ecgData[j++] = ((receivedBytes[i+2]&0xFF)>>4) | (((receivedBytes[i+3]&0xFF)&0x3F) << 4);
						if (i+4 < 91)
							ecgData[j++] = ((receivedBytes[i+3]&0xFF)>>6) | ((receivedBytes[i+4]&0xFF) << 2);
					}

					// sample iterval: 4ms
					if (mIsECG) {
						long mtimeStamp = timestamp;
						for (int i = 0; i < 63; i++) {
							ecgtime[i] = mtimeStamp;
							mtimeStamp += 4;
						}
						mDriverManager.push(devID, UniversalSensor.TYPE_ECG, ecgData, ecgtime);
					}
				} else if (msgID == 0x25) {
					//Log.d(TAG, "Received Accelerometer Packet");
					long timestamp = parseTimestamp(receivedBytes);
					for (int i=12, j=0; i<87; i+=5) {
						accData[j++] = (receivedBytes[i]&0xFF) | (((receivedBytes[i+1]&0xFF) & 0x03) << 8);
						if (i+2 < 87)
							accData[j++] = ((receivedBytes[i+1]&0xFF)>>2) | (((receivedBytes[i+2]&0xFF)&0x0F) << 6);
						if (i+3 < 87)
							accData[j++] = ((receivedBytes[i+2]&0xFF)>>4) | (((receivedBytes[i+3]&0xFF)&0x3F) << 4);
						if (i+4 < 87)
							accData[j++] = ((receivedBytes[i+3]&0xFF)>>6) | ((receivedBytes[i+4]&0xFF) << 2);
					}

					// sample interval: 20ms
					if (mIsAccelerometer) {
						int  j = 0;
						for (int i = 0; i < accData.length; i += 3, j++) {
							accData[0] = (float) convertADCtoG(accData[i]);
							accData[1] = (float) convertADCtoG(accData[i+1]);
							accData[2] = (float) convertADCtoG(accData[i+2]);
						}
						mDriverManager.push(devID, UniversalSensor.TYPE_CHEST_ACCELEROMETER, accData, cbtime);
					}
				} else if (msgID == 0x23 ) {
					//Log.d(TAG, "Recevied lifesign from Zephyr.");
				} else {
					//Log.d(TAG, "Received something else.. msgID: 0x" + Integer.toHexString(msgID));
				}

				long currTime = System.currentTimeMillis();
				if (currTime - lastTime > LIFE_SIGN_SEND_INTERVAL)
				{
					// Sending lifesign. (Zephyr requires this at least every 10 seconds)
					if (mOutputStream != null)
					{
						byte lifesign[] = { 0x02, 0x23, 0x00, 0x00, 0x03 };
						mOutputStream.write(lifesign);
						//Log.d(TAG, "Sent Lifesign");
					} else {
						throw new NullPointerException("mOutputStream is null");
					}
					lastTime = System.currentTimeMillis();
				}
			} catch (IOException e) {
				e.printStackTrace();
				try {
					if (mSocket != null)
						mSocket.close();
				} catch (IOException e1) {}
				mSocket = null;
				Log.d(TAG, "Trying to reconnect(1)..");
				int numRetries = 5;
				while (!mIsStopRequest && !connect(bluetoothAddr) && numRetries > 0) {
					Log.d(TAG, "Trying to reconnect(" + numRetries + ")..");
					numRetries -= 1;
					try {
						Thread.sleep(RETRY_INTERVAL);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				if (numRetries == 0)
					break;
			}
		} // end while

		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mSocket = null;
		mInputStream = null;
		mOutputStream = null;
		mIsStopRequest = false;
	}

	private void init(int sType, int bundleSize)
	{
		DriverSensorData mData = new DriverSensorData(devID, sType, bundleSize);
		mSensorDataMap.put(sType, mData);
	}
	
	private int max(int[] arr)
	{
		int maxValue = arr[0];
		for (int i = 1; i < arr.length; i++)
			if (maxValue < arr[i])
				maxValue = arr[i];
		return maxValue;
	}

	@Override
	public void run() {
		Log.i(TAG, "Zephyr Driver thread started " + bluetoothAddr);
		devID = new String("Zephyr" + bluetoothAddr.replaceAll(":", ""));
		tryToConnect();
		mDriverManager = UniversalDriverManager.create(context, this, devID);
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		init(UniversalSensor.TYPE_CHEST_ACCELEROMETER, max(chBundleSize));
		mDriverManager.registerDriver(UniversalSensor.TYPE_CHEST_ACCELEROMETER, chRate, chBundleSize);
		init(UniversalSensor.TYPE_ECG, max(ecBundleSize));
		mDriverManager.registerDriver(UniversalSensor.TYPE_ECG, ecRate, ecBundleSize);
		init(UniversalSensor.TYPE_RIP, max(rpBundleSize));
		mDriverManager.registerDriver(UniversalSensor.TYPE_RIP, rpRate, rpBundleSize);
		init(UniversalSensor.TYPE_SKIN_TEMPERATURE, max(stBundleSize));
		mDriverManager.registerDriver(UniversalSensor.TYPE_SKIN_TEMPERATURE, stRate, stBundleSize);
		init(UniversalSensor.TYPE_ZEPHYR_BATTERY, max(zbBundleSize));
		mDriverManager.registerDriver(UniversalSensor.TYPE_ZEPHYR_BATTERY, zbRate, zbBundleSize);
		init(UniversalSensor.TYPE_ZEPHYR_BUTTON_WORN, max(zbtBundleSize));
		mDriverManager.registerDriver(UniversalSensor.TYPE_ZEPHYR_BUTTON_WORN, zbtRate, zbtBundleSize);
		attachZephyr();
		mDriverManager.unregisterDriver(UniversalSensor.TYPE_ALL);
	}
	
	private void sendSetRTCPacket() {
		if (mOutputStream != null) {
			byte[] msg = new byte[] { 0x02, 0x07, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03 };

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
			String curDateTime = sdf.format(new Date(Calendar.getInstance().getTimeInMillis()));
			
//			Log.d(TAG, "curDateTime: " + curDateTime);
			
			String split[] = curDateTime.split("-");
			int year = Integer.parseInt(split[0]);
			int month = Integer.parseInt(split[1]);
			int day = Integer.parseInt(split[2]);
			int hour= Integer.parseInt(split[3]);
			int minute = Integer.parseInt(split[4]);
			int sec = Integer.parseInt(split[5]);

			msg[3] = (byte)day;
			msg[4] = (byte)month;
			msg[5] = (byte)(year & 0xFF);
			msg[6] = (byte)((year>>8) & 0xFF);
			msg[7] = (byte)hour;
			msg[8] = (byte)minute;
			msg[9] = (byte)sec;
			
			putCRC(msg);
			
			Log.d(TAG, "here sent: " + getHex(msg, msg.length));
			
			try {
				mOutputStream.write(msg);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	void sendGetRTCPacket() {
		byte[] msg = new byte[] { 0x02, 0x08, 0x00, 0x00, 0x03 };
		putCRC(msg);
		
		try {
			mOutputStream.write(msg);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	private void sendStartGeneralPacket() {
		if (mOutputStream != null) {
			try {
				mOutputStream.write(START_GENERAL_PACKET);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void sendStopECGPacket() {
		//Log.d(TAG, "sendStopECGPacket()");
		if (mOutputStream != null) {
			try {
				mOutputStream.write(STOP_ECG_PACKET);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void sendStopRIPPacket() {
		//Log.d(TAG, "sendStopRIPPacket()");
		if (mOutputStream != null) {
			try {
				mOutputStream.write(STOP_RIP_PACKET);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void sendStartAccPacket() {
		if (mOutputStream != null) {
			try {
				mOutputStream.write(START_ACCELEROMETER_PACKET);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void sendStopAllSensorsPacket() {
		if (mOutputStream != null) {
			try {
				mOutputStream.write(STOP_ECG_PACKET);
				mOutputStream.write(STOP_RIP_PACKET);
				mOutputStream.write(STOP_ACCELEROMETER_PACKET);
				mOutputStream.write(STOP_GENERAL_PACKET);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void sendStopAccPacket() {
		if (mOutputStream != null) {
			try {
				mOutputStream.write(STOP_ACCELEROMETER_PACKET);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private boolean isAllSensorExceptAccUnflagged() {
		return !mIsECG && !mIsRIP && !mIsSkinTemp && !mIsBattery && !mIsButtonWorn;
	}
	
	private void sendStopGeneralPacket() {
		if (mOutputStream != null) {
			try {
				mOutputStream.write(STOP_GENERAL_PACKET);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void putCRC(byte[] msg) {
		int payloadLength = msg.length - 5;
		byte[] payload = new byte[payloadLength];
		System.arraycopy(msg, 3, payload, 0, payloadLength);
		msg[msg.length - 2] = crc8PushBlock(payload);
	}

	private byte crc8PushBlock(byte[] payload) {
		byte crc = 0;
		for (int i = 0; i < payload.length; i++) {
			crc = crc8PushByte(crc, payload[i]);
		}
		return crc;
	}

	private byte crc8PushByte(byte crc, byte b) {
		crc = (byte) (crc ^ b);
		for (int i = 0; i < 8; i++) {
			if ((crc & 1) == 1) {
				crc = (byte) ((crc >> 1) ^ 0x8C);
			} else {
				crc = (byte) (crc >> 1);
			}
		}
		return crc;
	}
	
	// source: http://rgagnon.com/javadetails/java-0596.html
	static final String HEXES = "0123456789ABCDEF";
	public String getHex(byte[] raw, int num) {
		if (raw == null) 
			return null;
		final StringBuilder hex = new StringBuilder(2 * num);
		for (int i=0; i<num; i++) {
			hex.append(HEXES.charAt((raw[i] & 0xF0) >> 4)).append(HEXES.charAt((raw[i] & 0x0F)));
		}
		return hex.toString();
	}
	
	private double convertADCtoG(float sample) {
		// 10bits ADC 0 ~ 1023 = -16g ~ 16g
		return (sample / 1023.0) * 32.0 - 16.0;
	}

	private void unflagAllSensor() {
		mIsSkinTemp = false;
		mIsBattery = false;
		mIsButtonWorn = false;
		mIsECG = false;
		mIsRIP = false;
		mIsAccelerometer = false;
	}

	private void flagAllSensor() {
		mIsSkinTemp = true;
		mIsBattery = true;
		mIsButtonWorn = true;
		mIsECG = true;
		mIsRIP = true;
		mIsAccelerometer = true;
	}

	private void handleStopSensor(int sensorId) {
		switch (sensorId) {
		case UniversalSensor.TYPE_ECG:
			mIsECG = false;
			sendStopECGPacket();
			break;
		case UniversalSensor.TYPE_RIP:
			mIsRIP = false;
			sendStopRIPPacket();
			break;
		case UniversalSensor.TYPE_CHEST_ACCELEROMETER:
			mIsAccelerometer = false;
			sendStopAccPacket();
			break;
		case UniversalSensor.TYPE_SKIN_TEMPERATURE:
			mIsSkinTemp = false;
			break;
		case UniversalSensor.TYPE_ZEPHYR_BATTERY:
			mIsBattery = false;
			break;
		case UniversalSensor.TYPE_ZEPHYR_BUTTON_WORN:
			mIsButtonWorn = false;
			break;
		case UniversalSensor.TYPE_ALL:
			unflagAllSensor();
			sendStopAllSensorsPacket();
			break;
		}
		if (isAllSensorExceptAccUnflagged()) {
			sendStopGeneralPacket();
		}
	}
	
	private void handleStartSensor(int sensorId) {
		if (true) { //tryToConnect()) {
			switch (sensorId) {
			case UniversalSensor.TYPE_ECG:
				mIsECG = true;
				sendStartGeneralPacket();
				//sendStartECGPacket();
				break;
			case UniversalSensor.TYPE_RIP:
				mIsRIP = true;
				sendStartGeneralPacket();
				//sendStartRIPPacket();
				break;
			case UniversalSensor.TYPE_CHEST_ACCELEROMETER:
				mIsAccelerometer = true;
				Log.i(TAG, "setting TYPE_CHEST_ACCELEROMETER");
				sendStartAccPacket();
				break;
			case UniversalSensor.TYPE_SKIN_TEMPERATURE:
				mIsSkinTemp = true;
				break;
			case UniversalSensor.TYPE_ZEPHYR_BATTERY:
				mIsBattery = true;
				break;
			case UniversalSensor.TYPE_ZEPHYR_BUTTON_WORN:
				mIsButtonWorn = true;
				break;
			}
			if (mIsSkinTemp || mIsBattery || mIsButtonWorn) {
				sendStartGeneralPacket();
			}
		}
	}
	
	@Override
	public void setRate(int sType, int rate, int bundleSize) {
		Log.i(TAG, "setting rate " + sType + ": " + rate);
		if (rate > 0) {
			handleStartSensor(sType);
		} else {
			handleStopSensor(sType);
		}
	}

	@Override
	public void disconnected() {
		handleStopSensor(UniversalSensor.TYPE_ALL);
	}
}
