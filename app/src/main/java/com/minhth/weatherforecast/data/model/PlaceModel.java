package com.minhth.weatherforecast.data.model;

import android.database.Cursor;

import com.minhth.weatherforecast.data.local.PlaceContract;

/**
 * Created by THM on 5/25/2017.
 */
public class PlaceModel {
    private int mId;
    private String mName;
    private double mLatitude, mLongitude;



    public PlaceModel(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public PlaceModel(Cursor cursor) {
        mId = cursor.getInt(cursor.getColumnIndex(PlaceContract.PlaceEntry._ID));
        mLatitude = cursor.getDouble(cursor.getColumnIndex(PlaceContract.PlaceEntry
            .COLUMN_LATITUDE));
        mLongitude = cursor.getDouble(cursor.getColumnIndex(PlaceContract.PlaceEntry
            .COLUMN_LONGITUDE));
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }
}
