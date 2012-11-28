package com.pic2griddler.awesomeness;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteGriddlerAdapter extends SQLiteOpenHelper
{

	static final String dbName = "Griddlers";
	static final String griddlerTable = "UserGriddlers";
	static final String id = "id";
	static final String author = "Author";
	static final String name = "Name";
	static final String rank = "Rank";
	static final String solution = "Solution";
	static final String current = "Current";
	static final String difficulty = "Difficulty";
	static final String width = "Width";
	static final String height = "Height";
	static final String status = "Status";

	// static final String tags = "Tags";

	public SQLiteGriddlerAdapter(Context context, String name, CursorFactory factory, int version)
	{
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// Create the database son.
		String query = "CREATE TABLE " + griddlerTable + " (" + id + " INT(32)," + author + " TEXT," + name + " TEXT," + rank + " INT(32)," + solution + " TEXT," + current + " TEXT," + difficulty
				+ " VARCHAR(16)," + width + " INT(12)," + height + " INT(12)," + status + " INT(12)," + "primary KEY (id));";
		db.execSQL(query);
		insertDefaults(db);
	}

	private void insertDefaults(SQLiteDatabase db)
	{
		// Create Custom and Tutorial blocks. Will ALWAYS be there.
		ContentValues cv = new ContentValues();
		cv.put(id, "".hashCode()); // Odd, to me, but nothing will ever have
									// this ;).
		cv.put(author, "justinwarner");
		cv.put(name, "Create a Griddler");
		cv.put(rank, 0);
		cv.put(solution, "0");
		cv.put(current, "0");
		cv.put(difficulty, "0");
		cv.put(width, "0");
		cv.put(height, "0");
		cv.put(status, "2");
		db.insert(griddlerTable, null, cv); // Custom Griddler.
		cv.put(id, "1111100110011111".hashCode()); // Now set up the tutorial.
		cv.put(author, "justinwarner");
		cv.put(name, "Tutorial");
		cv.put(rank, 0);
		cv.put(solution, "1111100110011111");
		cv.put(current, "1000100010001000");
		cv.put(difficulty, "0");
		cv.put(width, "4");
		cv.put(height, "4");
		cv.put(status, "0");
		db.insert(griddlerTable, null, cv); // Tutorial Griddler.
	}

	public long addUserGriddler(String id, String author, String name, String rank, String solution, String difficulty, String width, String height, String status)
	{
		// Do stuff. Unknown so far. Implement later.
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(this.id, id.hashCode());
		cv.put(this.author, author);
		cv.put(this.name, name);
		cv.put(this.rank, rank);
		cv.put(this.solution, solution);
		cv.put(this.difficulty, difficulty);
		cv.put(this.width, width);
		cv.put(this.height, height);
		cv.put(this.status, status);
		// All 0's.
		String curr = "";
		for (int i = 0; i < Integer.parseInt(width); i++)
		{
			for (int j = 0; j < Integer.parseInt(height); j++)
			{
				curr += "0";
			}
		}
		cv.put(this.current, curr);
		return db.insert(griddlerTable, null, cv);
	}

	public int updateCurrentGriddler(String id, String status, String current)
	{
		// info = id + " " + status + " " + current
		// Info should include hash and new current.
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(this.current, current);
		cv.put(this.status, status);
		cv.put(this.id, id);
		return db.update(griddlerTable, cv, this.id + "=" + id, null);
	}

	public int deleteGriddler(String info)
	{
		// Probably won't implement. Not a huge deal (Right now).
		SQLiteDatabase db = this.getWritableDatabase();
		String[] hash =
		{ info.split(" ")[0] };
		return db.delete(griddlerTable, "id=" + hash, null);
	}

	public String[][] getGriddlers(int page)
	{
		// Page is the page of Griddlers to get. Might change.
		// Returns String array of Griddler infos to be processed internally.
		// Maybe change this so it's easier to process?
		int numItemsPerPage = 6; // This should be passed, will implement later
									// on.
		SQLiteDatabase db = this.getWritableDatabase();
		String query = "";
		query = "SELECT * FROM " + griddlerTable + " LIMIT " + (page * numItemsPerPage);
		Cursor c = db.rawQuery(query, null);
		if (c.moveToFirst())
		{
			String[][] result = new String[c.getCount()][c.getColumnCount()];
			for (int i = 0; i < result.length; i++)
			{
				for (int j = 0; j < c.getColumnCount(); j++)
				{
					result[i][j] = c.getString(j);
				}
				c.moveToNext();
			}
			c.close();
			return result;
		}
		else
		{
			c.close();
			return new String[][]
			{}; // Should never happen because tutorial and custom will be
				// there.
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldV, int newV)
	{
		// Don't do anything... Yet. Need to read up on what/how this works.
	}

}
