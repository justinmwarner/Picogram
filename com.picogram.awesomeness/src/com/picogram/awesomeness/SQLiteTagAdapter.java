
package com.picogram.awesomeness;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteTagAdapter extends SQLiteOpenHelper {

	static final String tagTable = "TagTable";
	static final String tag = "Tag";
	private static final String TAG = "SQLiteTagAdapter";

	public SQLiteTagAdapter(final Context context, final String name,
			final CursorFactory factory, final int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public String[][] getAllNeededUpdates() {
		final SQLiteDatabase db = this.getWritableDatabase();
		final String query = "SELECT * FROM "
				+ SQLiteTagAdapter.tagTable;
		final Cursor c = db.rawQuery(query, null);
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

	public String[] getTags() {
		final SQLiteDatabase db = this.getReadableDatabase();
		final String query = "SELECT " + tag + " FROM " + tagTable + ";";
		final Cursor c = db.rawQuery(query, null);
		final String[] result = new String[c.getCount()];
		if (c.moveToFirst()) {
			for (int i = 0; i != result.length; ++i)
			{
				result[i] = c.getString(c.getColumnIndex(tag));
				c.moveToNext();
			}
			return result;
		} else {
			return null;
		}
	}

	public boolean hasTag(final String tag) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final String query = "SELECT * FROM "
				+ SQLiteTagAdapter.tagTable + " WHERE "
				+ SQLiteTagAdapter.tag + " = '" + tag + "'";
		final Cursor c = db.rawQuery(query, null);
		if (c.moveToFirst()) {
			return true;
		}
		return false;
	}

	public long insertCreate(final String tag) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues cv = new ContentValues();
		if (this.hasTag(tag)) {
			return -1;
		}
		cv.put(SQLiteTagAdapter.tag, tag.toLowerCase());
		return db.insert(tagTable, null, cv);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		// Create the database sawn.
		final String query = "CREATE TABLE " + tagTable + " ("
				+ tag + " TEXT, primary KEY (" + tag + "));";
		db.execSQL(query);
		final ContentValues cv = new ContentValues();
		cv.put(tag, "tutorial");
		db.insert(tagTable, null, cv);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		// Don't do anything... Yet. Need to read up on yadda yadda yadda...
	}
}
