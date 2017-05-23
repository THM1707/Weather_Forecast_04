package com.minhth.weatherforecast.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by THM on 5/18/2017.
 */
public class TimeUtils {
    public static String nowToString(){
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        DateFormat df = new SimpleDateFormat("dd/MM\tHH:mm");
        return df.format(today);
    }
    public static String unixToHourString(long unix){
        DateFormat df = new SimpleDateFormat("HH:mm");
        Date date = new Date(unix*1000);
        return df.format(date);
    }
    public static String unixToDateString(long unix){
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date(unix*1000);
        return df.format(date);
    }
    public static String unixToDayOfWeek(long unix){
        DateFormat df = new SimpleDateFormat("EEEE");
        Date date = new Date(unix*1000);
        return df.format(date);
    }
}
