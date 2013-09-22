
package com.pic2griddler.awesomeness;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteGriddlerAdapter extends SQLiteOpenHelper {

    static final String author = "Author";
    static final String current = "Current";
    static final String dbName = "Griddlers";
    static final String difficulty = "Difficulty";
    static final String griddlerTable = "UserGriddlers";
    static final String height = "Height";
    static final String id = "id";
    static final String name = "Name";
    static final String rank = "Rank";
    static final String solution = "Solution";
    static final String status = "Status";
    static final String width = "Width";

    // static final String tags = "Tags";

    public SQLiteGriddlerAdapter(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public long addUserGriddler(String id, String author, String name, String rank,
            String solution, String difficulty, String width, String height, String status) {
        // Do stuff. Unknown so far. Implement later.
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SQLiteGriddlerAdapter.id, id);
        cv.put(SQLiteGriddlerAdapter.author, author);
        cv.put(SQLiteGriddlerAdapter.name, name);
        cv.put(SQLiteGriddlerAdapter.rank, rank);
        cv.put(SQLiteGriddlerAdapter.solution, solution);
        cv.put(SQLiteGriddlerAdapter.difficulty, difficulty);
        cv.put(SQLiteGriddlerAdapter.width, width);
        cv.put(SQLiteGriddlerAdapter.height, height);
        cv.put(SQLiteGriddlerAdapter.status, status);
        // All 0's.
        String curr = "";
        for (int i = 0; i < Integer.parseInt(width); i++) {
            for (int j = 0; j < Integer.parseInt(height); j++) {
                curr += "0";
            }
        }
        cv.put(SQLiteGriddlerAdapter.current, curr);
        return db.insert(griddlerTable, null, cv);
    }

    public long addUserGriddler(Griddler g) {
        // Do stuff. Unknown so far. Implement later.
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SQLiteGriddlerAdapter.id, g.getId());
        cv.put(SQLiteGriddlerAdapter.author, g.getAuthor());
        cv.put(SQLiteGriddlerAdapter.name, g.getName());
        cv.put(SQLiteGriddlerAdapter.rank, g.getRank());
        cv.put(SQLiteGriddlerAdapter.solution, g.getSolution());
        cv.put(SQLiteGriddlerAdapter.difficulty, g.getDiff());
        cv.put(SQLiteGriddlerAdapter.width, g.getWidth());
        cv.put(SQLiteGriddlerAdapter.height, g.getHeight());
        cv.put(SQLiteGriddlerAdapter.status, g.getStatus());
        cv.put(SQLiteGriddlerAdapter.current, g.getCurrent());
        return db.insert(griddlerTable, null, cv);
    }

    public int deleteGriddler(String info) {
        // Probably won't implement. Not a huge deal (Right now).
        SQLiteDatabase db = this.getWritableDatabase();
        String[] hash = {
                info.split(" ")[0]
        };
        return db.delete(griddlerTable, "id=" + hash, null);
    }

    public String[][] getGriddlers() {
        // Page is the page of Griddlers to get. Might change.
        // Returns String array of Griddler infos to be processed internally.
        // Maybe change this so it's easier to process?
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "";
        query = "SELECT * FROM " + griddlerTable;
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            String[][] result = new String[c.getCount()][c.getColumnCount()];
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

    private void insertDefaults(SQLiteDatabase db) {
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

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the database son.
        String query = "CREATE TABLE " + griddlerTable + " (" + id + " INT(32)," + author
                + " TEXT," + name + " TEXT," + rank + " INT(32)," + solution + " TEXT," + current
                + " TEXT," + difficulty + " VARCHAR(16)," + width + " INT(12)," + height
                + " INT(12)," + status + " INT(12)," + "primary KEY (id));";
        db.execSQL(query);
        insertDefaults(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // Don't do anything... Yet. Need to read up on what/how this works.
    }

    public int updateCurrentGriddler(String id, String status, String current) {
        // info = id + " " + status + " " + current
        // Info should include hash and new current.
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SQLiteGriddlerAdapter.current, current);
        cv.put(SQLiteGriddlerAdapter.status, status);
        cv.put(SQLiteGriddlerAdapter.id, id);
        return db.update(griddlerTable, cv, SQLiteGriddlerAdapter.id + " = ? ", new String[] {
                String.valueOf(id)
        });
    }

}
