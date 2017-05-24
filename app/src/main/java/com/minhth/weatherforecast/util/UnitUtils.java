package com.minhth.weatherforecast.util;

/**
 * Created by THM on 5/24/2017.
 */
public class UnitUtils {
    public static double celsiusToFahrenheit(double celsius){
        return celsius*1.8 + 32;
    }
    public static double kmToMile(double km){
        return km*0.621371192;
    }
}
