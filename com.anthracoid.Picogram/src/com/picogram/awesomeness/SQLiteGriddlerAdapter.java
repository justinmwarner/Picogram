
package com.picogram.awesomeness;

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
    static final String colors = "Colors";
    static final String numberOfColors = "NumColors";

    // static final String tags = "Tags";

    public SQLiteGriddlerAdapter(final Context context, final String name,
            final CursorFactory factory, final int version) {
        super(context, name, factory, version);
    }

    public long addUserGriddler(final Griddler g) {
        // Do stuff. Unknown so far. Implement later.
        final SQLiteDatabase db = this.getWritableDatabase();
        final ContentValues cv = new ContentValues();
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
        cv.put(SQLiteGriddlerAdapter.numberOfColors, g.getNumberOfColors());
        cv.put(SQLiteGriddlerAdapter.colors, g.getColors());
        return db.insert(griddlerTable, null, cv);
    }

    public long addUserGriddler(final String id, final String author, final String name,
            final String rank, final String solution, final String difficulty, final String width,
            final String height, final String status, final String numberOfColors,
            final String colors) {
        // Do stuff. Unknown so far. Implement later.
        final SQLiteDatabase db = this.getWritableDatabase();
        final ContentValues cv = new ContentValues();
        cv.put(SQLiteGriddlerAdapter.id, id);
        cv.put(SQLiteGriddlerAdapter.author, author);
        cv.put(SQLiteGriddlerAdapter.name, name);
        cv.put(SQLiteGriddlerAdapter.rank, rank);
        cv.put(SQLiteGriddlerAdapter.solution, solution);
        cv.put(SQLiteGriddlerAdapter.difficulty, difficulty);
        cv.put(SQLiteGriddlerAdapter.width, width);
        cv.put(SQLiteGriddlerAdapter.height, height);
        cv.put(SQLiteGriddlerAdapter.status, status);
        cv.put(SQLiteGriddlerAdapter.numberOfColors, numberOfColors);
        cv.put(SQLiteGriddlerAdapter.colors, colors);
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

    public int deleteGriddler(final String info) {
        // Probably won't implement. Not a huge deal (Right now).
        final SQLiteDatabase db = this.getWritableDatabase();
        final String[] hash = {
                info.split(" ")[0]
        };
        return db.delete(griddlerTable, "id=" + hash, null);
    }

    public String[][] getGriddlers() {
        // Page is the page of Griddlers to get. Might change.
        // Returns String array of Griddler infos to be processed internally.
        // Maybe change this so it's easier to process?
        final SQLiteDatabase db = this.getWritableDatabase();
        String query = "";
        query = "SELECT * FROM " + griddlerTable;
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
            return result;
        } else {
            c.close();
            return new String[][] {}; // Should never happen because tutorial
                                      // and custom will be
                                      // there.
        }
    }

    private void insertDefaults(final SQLiteDatabase db) {
        // Create Custom and Tutorial blocks. Will ALWAYS be there.
        final ContentValues cv = new ContentValues();
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
        cv.put(colors, "000000");// Black
        cv.put(numberOfColors, 1);
        db.insert(griddlerTable, null, cv); // Tutorial Griddler.
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        // Create the database son.
        final String query = "CREATE TABLE " + griddlerTable + " (" + id + " INT(32)," + author
                + " TEXT," + name + " TEXT," + rank + " INT(32)," + solution + " TEXT," + current
                + " TEXT," + difficulty + " VARCHAR(16)," + width + " INT(12)," + height
                + " INT(12)," + status + " INT(12)," + numberOfColors + "  INT(12), " + colors
                + " TEXT, " + " primary KEY (id));";
        db.execSQL(query);
        this.insertDefaults(db);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldV, final int newV) {
        // Don't do anything... Yet. Need to read up on what/how this works.
    }

    public int updateCurrentGriddler(final String id, final String status, final String current) {
        // info = id + " " + status + " " + current
        // Info should include hash and new current.
        final SQLiteDatabase db = this.getWritableDatabase();
        final ContentValues cv = new ContentValues();
        cv.put(SQLiteGriddlerAdapter.current, current);
        cv.put(SQLiteGriddlerAdapter.status, status);
        cv.put(SQLiteGriddlerAdapter.id, id);
        return db.update(griddlerTable, cv, SQLiteGriddlerAdapter.id + " = ? ", new String[] {
                String.valueOf(id)
        });
    }

}
