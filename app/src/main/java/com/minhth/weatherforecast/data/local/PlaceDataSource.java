package com.minhth.weatherforecast.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.minhth.weatherforecast.data.model.PlaceModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by THM on 5/26/2017.
 */
public class PlaceDataSource extends DatabaseHelper {
    public PlaceDataSource(Context context) {
        super(context);
    }

    public List<PlaceModel> getAllPlace() {
        List<PlaceModel> result = new ArrayList<>();
        String[] columns = {
            PlaceContract.PlaceEntry._ID,
            PlaceContract.PlaceEntry.COLUMN_LATITUDE,
            PlaceContract.PlaceEntry.COLUMN_LONGITUDE
        };
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(
            PlaceContract.PlaceEntry.TABLE_NAME,
            columns,
            null, // collection
            null, // collection args
            null, // group by
            null, // group by args
            null, // order
            null //limit
        );
        if (cursor != null && cursor.moveToFirst()) {
            do {
                result.add(new PlaceModel(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return result;
    }

    public long insertPlace(PlaceModel placeModel) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PlaceContract.PlaceEntry.COLUMN_LATITUDE, placeModel.getLatitude());
        values.put(PlaceContract.PlaceEntry.COLUMN_LONGITUDE, placeModel.getLongitude());
        long result = db.insert(PlaceContract.PlaceEntry.TABLE_NAME, null, values);
        db.close();
        return result;
    }

    public boolean updatePlace(PlaceModel placeModel) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PlaceContract.PlaceEntry.COLUMN_LATITUDE, placeModel.getLatitude());
        values.put(PlaceContract.PlaceEntry.COLUMN_LONGITUDE, placeModel.getLongitude());
        String whereClause = PlaceContract.PlaceEntry._ID + "=?";
        String[] whereClauseArgs = {String.valueOf(placeModel.getId())};
        int result =
            db.update(PlaceContract.PlaceEntry.TABLE_NAME, values, whereClause, whereClauseArgs);
        db.close();
        return result > 0;
    }

    public boolean deletePlace(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = PlaceContract.PlaceEntry._ID + "=?";
        String[] whereClauseArgs = {String.valueOf(id)};
        long result = db.delete(PlaceContract.PlaceEntry.TABLE_NAME, whereClause, whereClauseArgs);
        db.close();
        return result > 0;
    }
}