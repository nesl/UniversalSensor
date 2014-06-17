package com.ucla.nesl.universaldatastore;

import java.util.HashMap;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.ucla.nesl.lib.UniversalConstants;

public class ComputeEngine {
	private static String tag = ComputeEngine.class.getCanonicalName();

	private String constructAVGQuery(String tableName, int sType, long start, long end)
	{
		StringBuilder Query = new StringBuilder();

		Query.append("SELECT ");

		switch (UniversalConstants.getValuesLength(sType)) {
		case 3:
			Query.append("AVG(value0), ");
			Query.append("AVG(value1), ");
			Query.append("AVG(value2) ");
			break;
		case 2:
			Query.append("AVG(value0), ");
			Query.append("AVG(value1) ");
			break;
		case 1:
			Query.append("AVG(value0) ");
			break;
		}

		Query.append("FROM ");
		Query.append(tableName);
		Query.append(" WHERE timestamp >= ");
		Query.append(start);
		Query.append(" and timestamp <= ");
		Query.append(end);

		return Query.toString();
	}

	public HashMap<String, Float> avg(SQLiteDatabase db, String tableName, int sType, long start, long end)
	{
		Cursor cursor;
		int valueLenght = UniversalConstants.getValuesLength(sType);
		HashMap<String, Float> result = new HashMap<String, Float>();

		try {
			cursor = db.rawQuery(constructAVGQuery(tableName, sType, start, end), null);
			Log.i(tag, "number of columns " + cursor.getColumnCount());

			cursor.moveToFirst();
			for (int i = 0; i < valueLenght; i++) {
				result.put("avg.value" + i, cursor.getFloat(i));
				Log.d(tag, "" + cursor.getColumnName(i) + ": " + cursor.getFloat(i));
			}
		} catch (SQLiteException e) {
			// Mostly because the table might not exists
		}
		return result;
	}
}
