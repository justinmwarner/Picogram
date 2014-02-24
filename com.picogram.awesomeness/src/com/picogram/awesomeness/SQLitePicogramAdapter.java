
package com.picogram.awesomeness;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;

public class SQLitePicogramAdapter extends SQLiteOpenHelper {

	static final String author = "Author";
	static final String current = "Current";
	static final String dbName = "Picograms";
	static final String difficulty = "Difficulty";
	static final String PicogramTable = "UserPicograms";
	static final String height = "Height";
	static final String id = "id";
	static final String name = "Name";
	static final String rank = "Rank";
	static final String solution = "Solution";
	static final String status = "Status";
	static final String width = "Width";
	static final String colors = "Colors";
	static final String numberOfColors = "NumColors";
	static final String highscore = "PersonalHighscore";
	static final String pRank = "PersonalRank";

	// static final String tags = "Tags";

	public SQLitePicogramAdapter(final Context context, final String name,
			final CursorFactory factory, final int version) {
		super(context, name, factory, version);
	}

	public int addHighscore(final String id, final long highscore) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues cv = new ContentValues();
		cv.put(SQLitePicogramAdapter.highscore, highscore);
		cv.put(SQLitePicogramAdapter.id, id);
		return db.update(PicogramTable, cv, SQLitePicogramAdapter.id + " = "
				+ id, null);
	}

	public int addPersonalRank(final String id, final int rank) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues cv = new ContentValues();
		cv.put(SQLitePicogramAdapter.pRank, rank);
		cv.put(SQLitePicogramAdapter.id, id);
		return db.update(PicogramTable, cv, SQLitePicogramAdapter.id + " = "
				+ id, null);
	}

	public long addUserPicogram(final Picogram g) {
		// If puzzle already exists, ignore.
		final long line = this.doesPuzzleExist(g);
		if (line != -1) {
			return line;
		}
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues cv = new ContentValues();
		cv.put(SQLitePicogramAdapter.id, g.getID());
		cv.put(SQLitePicogramAdapter.author, g.getAuthor());
		cv.put(SQLitePicogramAdapter.name, g.getName());
		cv.put(SQLitePicogramAdapter.rank, g.getRating());
		cv.put(SQLitePicogramAdapter.solution, g.getSolution());
		cv.put(SQLitePicogramAdapter.difficulty, g.getDiff());
		cv.put(SQLitePicogramAdapter.width, g.getWidth());
		cv.put(SQLitePicogramAdapter.height, g.getHeight());
		cv.put(SQLitePicogramAdapter.status, g.getStatus());
		cv.put(SQLitePicogramAdapter.current, g.getCurrent());
		cv.put(SQLitePicogramAdapter.numberOfColors, g.getNumberOfColors());
		cv.put(SQLitePicogramAdapter.colors, g.getColors());
		cv.put(SQLitePicogramAdapter.highscore, 0);
		cv.put(SQLitePicogramAdapter.pRank, 0);
		return db.insert(PicogramTable, null, cv);
	}

	public long addUserPicogram(final String id, final String author,
			final String name, final String rank, final String solution,
			String current, final String difficulty, final String width,
			final String height, final String status,
			final String numberOfColors, final String colors,
			final String personalRank, final String isUploaded) {
		// If puzzle already exists, ignore.
		final long line = this.doesPuzzleExist(id);
		if (line != -1) {
			return line;
		}
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues cv = new ContentValues();
		cv.put(SQLitePicogramAdapter.id, id);
		cv.put(SQLitePicogramAdapter.author, author);
		cv.put(SQLitePicogramAdapter.name, name);
		cv.put(SQLitePicogramAdapter.rank, rank);
		cv.put(SQLitePicogramAdapter.solution, solution);
		cv.put(SQLitePicogramAdapter.difficulty, difficulty);
		cv.put(SQLitePicogramAdapter.width, width);
		cv.put(SQLitePicogramAdapter.height, height);
		cv.put(SQLitePicogramAdapter.status, status);
		cv.put(SQLitePicogramAdapter.numberOfColors, numberOfColors);
		cv.put(SQLitePicogramAdapter.colors, colors);
		cv.put(SQLitePicogramAdapter.highscore, 0);
		cv.put(SQLitePicogramAdapter.pRank, 0);
		// All 0's, if not assigned.
		if (current == null) {
			current = "";
			for (int i = 0; i < Integer.parseInt(width); i++) {
				for (int j = 0; j < Integer.parseInt(height); j++) {
					current += "0";
				}
			}
		}
		cv.put(SQLitePicogramAdapter.current, current);
		return db.insert(PicogramTable, null, cv);
	}

	public int deletePicogram(final String id) {
		// Probably won't implement. Not a huge deal (Right now).
		final SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(PicogramTable, "id=" + id, null);
	}

	public long doesPuzzleExist(final Picogram go) {
		final String[][] picograms = this.getPicograms();
		final String id = go.getID();
		for (int i = 0; i != picograms.length; ++i) {
			if (picograms[i][0].equals(id)) {
				return i;
			}
		}
		return -1;
	}

	public long doesPuzzleExist(final String id) {
		final Picogram go = new Picogram();
		go.setID(id);
		return this.doesPuzzleExist(go);
	}

	public long getHighscore(final String id) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final String query = "SELECT * FROM " + PicogramTable + " WHERE "
				+ SQLitePicogramAdapter.id + "='" + id + "'";
		final Cursor c = db.rawQuery(query, null);
		if (c.moveToFirst()) {
			final String[][] result = new String[c.getCount()][c.getColumnCount()];
			for (int i = 0; i < result.length; i++) {
				for (int j = 0; j < c.getColumnCount(); j++) {
					result[i][j] = c.getString(j);
				}
				c.moveToNext();
			}
			c.close();
			return Long.parseLong(result[0][c.getColumnIndex(SQLitePicogramAdapter.highscore)]);
		} else {
			c.close();
			return 0;
		}
	}

	public long getPersonalRank(final String id) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final String query = "SELECT * FROM " + PicogramTable + " WHERE "
				+ SQLitePicogramAdapter.id + "='" + id + "'";
		final Cursor c = db.rawQuery(query, null);
		if (c.moveToFirst()) {
			final String[][] result = new String[c.getCount()][c.getColumnCount()];
			for (int i = 0; i < result.length; i++) {
				for (int j = 0; j < c.getColumnCount(); j++) {
					result[i][j] = c.getString(j);
				}
				c.moveToNext();
			}
			c.close();
			return Long.parseLong(result[0][c.getColumnIndex(SQLitePicogramAdapter.pRank)]);
		} else {
			c.close();
			return 0;
		}
	}

	public Picogram getPicogram(final String id) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final String query = "SELECT * FROM " + PicogramTable + " WHERE "
				+ SQLitePicogramAdapter.id + "='" + id + "'";
		final Cursor c = db.rawQuery(query, null);
		if (c.moveToFirst()) {
			final Picogram p = new Picogram();
			p.setName(c.getString(c.getColumnIndex(name)));
			p.setColors(c.getString(c.getColumnIndex(SQLitePicogramAdapter.colors)));
			p.setAuthor(c.getString(c.getColumnIndex(SQLitePicogramAdapter.author)));
			p.setCurrent(c.getString(c.getColumnIndex(SQLitePicogramAdapter.current)));
			p.setDiff(c.getString(c.getColumnIndex(SQLitePicogramAdapter.difficulty)));
			p.setHeight(c.getString(c.getColumnIndex(SQLitePicogramAdapter.height)));
			p.setHighscore(Long.parseLong(c.getString(c.getColumnIndex(SQLitePicogramAdapter.highscore))));
			p.setNumberOfColors(c.getString(c.getColumnIndex(SQLitePicogramAdapter.numberOfColors)));
			p.setRating(c.getString(c.getColumnIndex(SQLitePicogramAdapter.rank)));
			p.setSolution(c.getString(c.getColumnIndex(SQLitePicogramAdapter.solution)));
			p.setStatus(c.getString(c.getColumnIndex(SQLitePicogramAdapter.status)));
			p.setWidth(c.getString(c.getColumnIndex(SQLitePicogramAdapter.width)));
			p.setID(c.getString(c.getColumnIndex(SQLitePicogramAdapter.id)));
			p.setName(c.getString(c.getColumnIndex(name)));
			c.close();

			return p;
		} else {
			c.close();
			return null;
		}
	}

	public String[][] getPicograms() {
		// Page is the page of Picograms to get. Might change.
		// Returns String array of Picogram infos to be processed internally.
		// Maybe change this so it's easier to process?
		final SQLiteDatabase db = this.getWritableDatabase();
		final String query = "SELECT * FROM " + PicogramTable;
		final Cursor c = db.rawQuery(query, null);
		if (c.moveToFirst()) {
			final String[][] result = new String[c.getCount()][c
			                                                   .getColumnCount()];
			Log.d("SQL", "Up: " + c.getCount());
			for (int i = 0; i < result.length; i++) {
				for (int j = 0; j < c.getColumnCount(); j++) {
					result[i][j] = c.getString(j);
				}
				c.moveToNext();
			}
			c.close();
			return result;
		} else {
			c.close();
			return new String[][] {}; // Should never happen because tutorial
			// and custom will be
			// there.
		}
	}

	public long getValueByColumn(final String id, final String column) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final String query = "SELECT * FROM " + PicogramTable + " WHERE "
				+ SQLitePicogramAdapter.id + "='" + id + "'";
		final Cursor c = db.rawQuery(query, null);
		if (c.moveToFirst()) {
			final String[][] result = new String[c.getCount()][c.getColumnCount()];
			for (int i = 0; i < result.length; i++) {
				for (int j = 0; j < c.getColumnCount(); j++) {
					result[i][j] = c.getString(j);
				}
				c.moveToNext();
			}
			c.close();
			return Long.parseLong(result[0][c.getColumnIndex(column)]);
		} else {
			c.close();
			return 0;
		}
	}

	private void insertDefaults(final SQLiteDatabase db) {
		// Create Custom and Tutorial blocks. Will ALWAYS be there.
		final ContentValues cv = new ContentValues();
		cv.put(id, "".hashCode());
		cv.put(author, "justinwarner");
		cv.put(name, "Create a Picogram");
		cv.put(rank, 0);
		cv.put(solution, "0");
		cv.put(current, "0");
		cv.put(difficulty, "-1");
		cv.put(width, "0");
		cv.put(height, "0");
		cv.put(status, "2");
		cv.put(highscore, 0);
		cv.put(SQLitePicogramAdapter.pRank, 0);
		db.insert(PicogramTable, null, cv);
		cv.put(id, "0".hashCode());
		cv.put(author, "justinwarner");
		cv.put(name, "Random Picogram!");
		cv.put(rank, 0);
		cv.put(solution, "0");
		cv.put(current, "0");
		cv.put(difficulty, "-1");
		cv.put(width, "0");
		cv.put(height, "0");
		cv.put(status, "2");
		cv.put(highscore, 0);
		cv.put(SQLitePicogramAdapter.pRank, 0);
		db.insert(PicogramTable, null, cv);
		cv.put(id, "1111100110011111".hashCode());
		cv.put(author, "justinwarner");
		cv.put(name, "Tutorial");
		cv.put(rank, 0);
		cv.put(solution, "1111100110011111");
		cv.put(current, "1000100010001000");
		cv.put(difficulty, "0");
		cv.put(width, "4");
		cv.put(height, "4");
		cv.put(status, "0");
		cv.put(colors, Color.TRANSPARENT + "," + Color.BLACK);
		cv.put(numberOfColors, 2);
		cv.put(highscore, 0);
		cv.put(SQLitePicogramAdapter.pRank, 0);
		db.insert(PicogramTable, null, cv);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		// Create the database son.
		final String query = "CREATE TABLE " + PicogramTable + " (" + id
				+ " INT(32)," + author + " TEXT," + name + " TEXT," + rank
				+ " INT(32)," + solution + " TEXT," + current + " TEXT,"
				+ difficulty + " VARCHAR(16)," + width + " INT(12)," + height
				+ " INT(12)," + status + " INT(12)," + numberOfColors
				+ "  INT(12), " + colors + " TEXT,  " + highscore + " INT(12)," + pRank + " INT(12), primary KEY (id));";
		db.execSQL(query);
		this.insertDefaults(db);
	}


	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldV,
			final int newV) {
		// Don't do anything... Yet. Need to read up on what/how this works.
	}

	public void updateColorsById(final String gId, final String[] strColors) {
		String colors = "";
		for (int i = 0; i != strColors.length; ++i) {
			colors += strColors[i] + ",";
		}
		colors = colors.substring(0, colors.length() - 1);
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues cv = new ContentValues();
		cv.put(SQLitePicogramAdapter.colors, colors);
		db.update(SQLitePicogramAdapter.PicogramTable, cv, id + "=" + gId, null);
	}

	public int updateCurrentPicogram(final String id, final String status,
			final String current) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues cv = new ContentValues();
		cv.put(SQLitePicogramAdapter.current, current);
		cv.put(SQLitePicogramAdapter.status, status);
		return db.update(PicogramTable, cv, SQLitePicogramAdapter.id + " = "
				+ id, null);
	}

	public void updateRate(final String puzzleId, final int newRate) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues cv = new ContentValues();
		cv.put(SQLitePicogramAdapter.rank, newRate);
		db.update(PicogramTable, cv, SQLitePicogramAdapter.id + " = " + puzzleId,
				null);
	}

	public void updateScore(final String puzzleId, final long newScore) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues cv = new ContentValues();
		cv.put(SQLitePicogramAdapter.highscore, newScore);
		db.update(PicogramTable, cv, SQLitePicogramAdapter.id + " = " + puzzleId,
				null);
	}
}
