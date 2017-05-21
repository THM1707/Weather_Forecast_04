package com.minhth.weatherforecast.util;

import com.minhth.weatherforecast.R;

public class ConditionUtils {
    private static final String CLEAR_DAY = "clear-day";
    private static final String CLEAR_NIGHT = "clear-night";
    private static final String RAIN = "rain";
    private static final String SNOW = "snow";
    private static final String SLEET = "sleet";
    private static final String WIND = "wind";
    private static final String FOG = "fog";
    private static final String CLOUDY = "cloudy";
    private static final String PARTY_CLOUDY_DAY = "partly-cloudy-day";
    private static final String PARTY_CLOUDY_NIGHT = "partly-cloudy-night";
    private static final String[] CONDITION = {
        CLEAR_DAY,
        CLEAR_NIGHT,
        RAIN,
        SNOW,
        SLEET,
        WIND,
        FOG,
        CLOUDY,
        PARTY_CLOUDY_DAY,
        PARTY_CLOUDY_NIGHT};
    private static final String[] FIXED_CONDITION = {
        "Clear day",
        "Clear night",
        "Rain",
        "Snow",
        "Sleet",
        "Wind",
        "Fog",
        "Cloudy",
        "Partly cloudy day",
        "Partly cloudy night"
    };
    private static final int[] CONDITION_RESOURCE = {
        R.drawable.ic_clear_day,
        R.drawable.ic_clear_night,
        R.drawable.ic_rain,
        R.drawable.ic_snow,
        R.drawable.ic_sleet,
        R.drawable.ic_wind,
        R.drawable.ic_fog,
        R.drawable.ic_cloudy,
        R.drawable.ic_party_cloudy_day,
        R.drawable.ic_party_cloudy_night
    };

    public static String getCondition(String condition) {
        int position = -1;
        for (int i = 0; i < CONDITION.length; i++) {
            if (CONDITION[i].equals(condition)) {
                position = i;
            }
        }
        return FIXED_CONDITION[position];
    }

    public static int getConditionResource(String condition) {
        int position = -1;
        for (int i = 0; i < CONDITION.length; i++) {
            if (CONDITION[i].equals(condition)) {
                position = i;
            }
        }
        return CONDITION_RESOURCE[position];
    }
}
