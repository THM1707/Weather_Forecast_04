package com.minhth.weatherforecast.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by THM on 5/26/2017.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private final static String DB_NAME = "place.db";
    private final static int DB_VERSION = 1;
    private final static String COMMAND_CREATE_PLACE_TABLE = "CREATE TABLE "
        + PlaceContract.PlaceEntry.TABLE_NAME
        + " ( "
        + PlaceContract.PlaceEntry._ID
        + " INTEGER PRIMARY KEY, "
        + PlaceContract.PlaceEntry.COLUMN_LATITUDE
        + " REAL, "
        + PlaceContract.PlaceEntry.COLUMN_LONGITUDE
        + " REAL)";
    private static String COMMAND_DROP_PLACE_TABLE = "DROP_TABLE " + PlaceContract
        .PlaceEntry.TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(COMMAND_CREATE_PLACE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(COMMAND_CREATE_PLACE_TABLE);
        db.execSQL(COMMAND_DROP_PLACE_TABLE);
    }
}