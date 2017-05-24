package com.minhth.weatherforecast.util;

/**
 * Created by THM on 5/23/2017.
 */
public class DataUtils {
    public static final int NORTH = 0;
    public static final int EAST = 90;
    public static final int SOUTH = 180;
    public static final int WEST = 270;
    public static final int MAX_DEGREE = 360;

    public static String formatValue(double value, String measure) {
        return String.format("%.2f %s", value, measure);
    }

    public static String formatPercentage(double value) {
        return String.format("%.0f %%", value);
    }

    public static String formatSpeed(double speed, String measure) {
        return String.format("%.1f%s", speed, measure);
    }
}
