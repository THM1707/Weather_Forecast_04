package com.minhth.weatherforecast.data.local;

import android.provider.BaseColumns;

/**
 * Created by THM on 5/26/2017.
 */
public class PlaceContract {
    public class PlaceEntry implements BaseColumns {
        public static final String TABLE_NAME = "tbl_place";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
    }
}
