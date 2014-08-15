package com.ucla.nesl.universaldatastore;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ucla.nesl.lib.UniversalConstants;

/**
 * Class to manage transaction with the SQLite database.
 * It creates a table for each Sensor of a external sensor
 * device instance.
 */
public class DataStoreManager extends SQLiteOpenHelper {
	private static String tag = DataStoreManager.class.getCanonicalName();
	private ComputeEngine mComputeEngine = null;
	private boolean once = true;

	public DataStoreManager(Context context, String name,
			CursorFactory factory, int version) {
		super(context, UniversalConstants.DBName, factory, version);
		mComputeEngine = new ComputeEngine();
	}

	private String constructTable(String tableName)
	{
		StringBuilder tableSchema = new StringBuilder();

		tableSchema.append("CREATE TABLE ");
		tableSchema.append(tableName);
		tableSchema.append("(_id INTEGER PRIMARY KEY, ");
		tableSchema.append("timestamp INTEGER, ");
		tableSchema.append("value0 REAL NOT NULL, ");
		tableSchema.append("value1 REAL, ");
		tableSchema.append("value2 REAL)");
		return tableSchema.toString();
	}

	private void createTable(String tableName)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(constructTable(tableName));
		db.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// Not creating any table on purpose. Tables will be created
		// as and when listeners register to the sensors.
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}

	private void _insert(SQLiteDatabase db, String tableName, float[] sensorValue, long timestamp)
	{
		try {
			ContentValues values = new ContentValues();
			switch(sensorValue.length) {
			case 3:
				values.put("value2", sensorValue[2]);
			case 2:
				values.put("value1", sensorValue[1]);
			case 1:
				values.put("value0", sensorValue[0]);
			}
			values.put("timestamp", timestamp);
			db.insertOrThrow(tableName, null, values);
		} catch (IllegalStateException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * Insert the sensor data into the table
	 * @param tableName Name of the table where we insert into
	 * @param sType sensor type
	 * @param values Array of sensor samples. The number of sensor
	 * 			samples is equal to the bundlesize of that sensor
	 * @param for each sensor sample it contains a corresponding timestamp 
	 * @return
	 */
	public boolean insertSensorData(String tableName, int sType, float[] values, long[] timestamp)
	{
		SQLiteDatabase db = null;

		db = this.getWritableDatabase();

		try {
			int valueLength = UniversalConstants.getValuesLength(sType);
			float[] sensorValue = new float[valueLength];
			for (int i = 0, k = 0; i < timestamp.length; i++) {
				for (int j = 0; j < valueLength; j++) {
					sensorValue[j] = values[k++];
				}
				try {
					_insert(db, tableName, sensorValue, timestamp[i]);
				} catch (SQLException e) {
					Log.d(tag, "insertSensorData:: caught exception, creating table " + tableName);
					db.close();
					createTable(tableName);
					db = this.getWritableDatabase();
					_insert(db, tableName, sensorValue, timestamp[i]);
				}
			}
		} finally {
			db.close();
		}
		//		if (once == true) {
		//			once = false;
		//			db = this.getReadableDatabase();
		//			mComputeEngine.avg(db, "phoneSensor1_3", 1, 5865455752l, 5865455754l);
		//			print_tables(db);
		//			db.close();
		//		}
		return true;
	}

	public String[] retrieve_all_tables()
	{
		int i = 0;
		String[] tableNames;
		Cursor c;
		SQLiteDatabase db;

		db = this.getReadableDatabase();
		c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

		Log.d(tag, "retrieve_all_tables:: number of tables: " + c.getCount());
		tableNames = new String[c.getCount() - 1]; // Skip the metadata table

		c.moveToFirst();
		c.moveToNext(); // Skip the metadata table
		while (c.isAfterLast() == false) {
			Log.i(tag, "" + c.getString(c.getColumnIndex("name")));
			tableNames[i++] = new String(c.getString(c.getColumnIndex("name")));
			c.moveToNext();
		}
		db.close();
		return tableNames;
	}

	public HashMap<String, Float> compute(String tableName, int sType, int function, long start, long end)
	{
		SQLiteDatabase db = null;
		HashMap<String, Float> result = null;

		db = this.getReadableDatabase();

		switch (function) {
		case UniversalConstants.COMPUTE_AVG:
			result = mComputeEngine.avg(db, tableName, sType, start, end);
			break;
			// compute min and max
		}
		db.close();
		return result;
	}
}
