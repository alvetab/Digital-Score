package com.mirhoseini.itag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DBHandler {

    String TAG = "Tennis Score Keeper";

    String DB_NAME = "TennisScoreKeeper";
    String DB_TABLE = "scores";
    int DB_VERSION = 1;

    String SQL_CREATE = "CREATE TABLE " + DB_TABLE +
            " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "player1name TEXT NOT NULL, " +
            "player2name TEXT NOT NULL, " +
            "winner TEXT NOT NULL, " +
            "player1sets INTEGER NOT NULL, " +
            "player2sets INTEGER NOT NULL, " +
            "player1games INTEGER NOT NULL, " +
            "player2games INTEGER NOT NULL, " +
            "player1points INTEGER NOT NULL, " +
            "player2points INTEGER NOT NULL, " +
            "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "time TEXT NOT NULL); ";

    String SQL_DROP = "DROP TABLE IF EXISTS " + DB_TABLE;

    DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public DBHandler(Context ctx){
        dbHelper = new DatabaseHelper(ctx);
    }

    //-----------------------------------------
    class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context){
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE);
            Log.d(TAG, "Table contacts created");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DROP);
            onCreate(db);
            Log.d(TAG, "Table contacts recreated");
        }

    }
    //---------------------------------------------

    public void open() throws SQLException {
        db= dbHelper.getWritableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    // returns number of rows inserted or -1 if an error occurred
    public long insertContact(String player1name, String player2name, String winner, Integer player1sets, Integer player2sets, Integer player1games, Integer player2games, Integer player1points, Integer player2points, String time){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String date = simpleDateFormat.format(new Date());
        ContentValues vals = new ContentValues();
        vals.put("player1name", player1name);
        vals.put("player2name", player2name);
        vals.put("winner", winner);
        vals.put("player1sets", player1sets);
        vals.put("player2sets", player2sets);
        vals.put("player1games", player1games);
        vals.put("player2games", player2games);
        vals.put("player1points", player1points);
        vals.put("player2points", player2points);
        vals.put("date", date);
        vals.put("time", time);
        Log.d(TAG, "VALUES : "+vals);
        return db.insert(DB_TABLE,   // table
                null,       // null when some value is provided
                vals );     // initial values
    }

    // returns number of rows updated
    public int updateContact(String name, String email){
        ContentValues vals = new ContentValues();
        //vals.put("name", name);
        vals.put("email", email);
        return db.update(DB_TABLE,               // table
                vals,                   // new values
                "name=?",               // where clause
                new String[]{ name } ); // where arguments
    }

    // returns a Cursor object positioned before the first entry
    public Cursor getContact(String gid){
        Cursor cursor = db.query(
                DB_TABLE,                         // table to perform the query
                new String[] { "_id", "player1name", "player2name", "winner", "player1sets", "player2sets", "player1games", "player2games", "player1points", "player1points", "date", "time" }, // resultset columns/fields
                "_id=?",                         // condition or selection
                new String[] { String.valueOf(gid) }, // selection arguments (fills in '?' above)
                null,                             // groupBy
                null,                             // having
                null );                           // orderBy

        return cursor;
    }

    // to use the ListView with SimpleCursorAdapter, the _id column is needed
    public Cursor getAllContacts(){
        Cursor cursor = db.query(
                DB_TABLE,                         // table to perform the query
                new String[] { "_id", "player1name", "player2name", "winner", "player1sets", "player2sets", "player1games", "player2games", "player1points", "player1points", "date", "time" }, // resultset columns/fields
                null,                             // condition or selection
                null,                             // selection arguments (fills in '?' above)
                null,                             // groupBy
                null,                             // having
                null );                           // orderBy
        Log.d(TAG, "VALUES : "+cursor);
        return cursor;
    }

    // returns number of rows deleted
    public int deleteContact(Integer gid){
        return db.delete(DB_TABLE,               // table
                "_id=?",               // where clause
                new String[]{ String.valueOf(gid) } );  // where arguments

    }

}