package com.picogram.awesomeness;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteRatingAdapter extends SQLiteOpenHelper {

	static final String offlineRatingTable = "OfflineRating";
	static final String picogramId = "PID";
	static final String pastRank = "PastRank";
	static final String futureRank = "FutureRank";
	private static final String TAG = "SQLiteRatingAdapter";

	public SQLiteRatingAdapter(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create the database sawn.
		final String query = "CREATE TABLE " + offlineRatingTable + " ("
				+ picogramId + " INT(32)," + pastRank + " INT(4)," + futureRank
				+ " INT(4), primary KEY (" + picogramId + "));";
		Log.d("HERE", "Offline creating.");
		db.execSQL(query);
	}

	public String[][] getAllNeededUpdates() {
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "SELECT * FROM "
				+ SQLiteRatingAdapter.offlineRatingTable;
		Cursor c = db.rawQuery(query, null);
		if (c.moveToFirst()) {
			final String[][] result = new String[c.getCount()][c
					.getColumnCount()];
			for (int i = 0; i < result.length; i++) {
				for (int j = 0; j < c.getColumnCount(); j++) {
					result[i][j] = c.getString(j);
				}
				c.moveToNext();
			}
			c.close();
			return result;
		}
		c.close();
		return null;

	}

	public long insert(String picogramId, String pastRank, String futureRank) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues cv = new ContentValues();
		cv.put(SQLiteRatingAdapter.picogramId, picogramId);
		cv.put(SQLiteRatingAdapter.pastRank, pastRank);
		cv.put(SQLiteRatingAdapter.futureRank, futureRank);
		Log.d(TAG, "Adding: " + picogramId + " Past: " + pastRank + " Future: "
				+ futureRank);
		// Check if PID exists, if it does, just update it.
		String query = "SELECT * FROM "
				+ SQLiteRatingAdapter.offlineRatingTable + " WHERE "
				+ SQLiteRatingAdapter.picogramId + " ='" + picogramId + "'";
		Cursor c = db.rawQuery(query, null);
		if (c.moveToFirst()) {
			return db.update(SQLiteRatingAdapter.offlineRatingTable, cv,
					SQLiteRatingAdapter.picogramId + " = "
							+ SQLiteRatingAdapter.picogramId, null);
		}
		return db.insert(offlineRatingTable, null, cv);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Don't do anything... Yet. Need to read up on yadda yadda yadda...
	}

	public String getPastRatingForPID(String id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "SELECT * FROM "
				+ SQLiteRatingAdapter.offlineRatingTable + " WHERE "
				+ SQLiteRatingAdapter.picogramId + " = '" + id + "'";
		Cursor c = db.rawQuery(query, null);
		if (c.moveToFirst()) {
			return c.getString(1); // Past rating is second column.
		}
		return "0";// Never played, so 0.
	}

	/**
	 * Makes this.futureRank = 0 and this.pastRank = newRating;
	 * 
	 * @param id
	 * @param newPastRating
	 */
	public void updateRecord(String id, String newPastRating,
			String newFutureRating) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(SQLiteRatingAdapter.futureRank, newFutureRating);
		cv.put(SQLiteRatingAdapter.pastRank, newPastRating);
		db.update(SQLiteRatingAdapter.offlineRatingTable, cv,
				SQLiteRatingAdapter.picogramId + "=" + id, null);
	}
}
