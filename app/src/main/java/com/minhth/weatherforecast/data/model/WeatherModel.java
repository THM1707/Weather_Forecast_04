package com.minhth.weatherforecast.data.model;

import com.google.gson.annotations.SerializedName;

public class WeatherModel {
    @SerializedName("icon")
    private String mIcon;
    @SerializedName("time")
    private long mTime;
    @SerializedName("summary")
    private String mSummary;
    @SerializedName("precipIntensity")
    private double mPrecipIntensity;
    @SerializedName("precipProbability")
    private double mPrecipProbability;
    @SerializedName("precipType")
    private String mPrecipType;
    @SerializedName("temperature")
    private double mTemperature;
    @SerializedName("apparentTemperature")
    private double mApparentTemperature;
    @SerializedName("dewPoint")
    private double mDewPoint;
    @SerializedName("humidity")
    private double mHumidity;
    @SerializedName("windSpeed")
    private double mWindSpeed;
    @SerializedName("windBearing")
    private int mWindBearing;
    @SerializedName("cloudCover")
    private double mCloudCover;
    @SerializedName("pressure")
    private double mPressure;
    @SerializedName("sunriseTime")
    private long mSunriseTime;
    @SerializedName("sunsetTime")
    private long mSunsetTime;
    @SerializedName("temperatureMin")
    private double mTemperatureMin;
    @SerializedName("temperatureMax")
    private double mTemperatureMax;

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public double getPrecipIntensity() {
        return mPrecipIntensity;
    }

    public void setPrecipIntensity(double precipIntensity) {
        mPrecipIntensity = precipIntensity;
    }

    public double getPrecipProbability() {
        return mPrecipProbability;
    }

    public void setPrecipProbability(double precipProbability) {
        mPrecipProbability = precipProbability;
    }

    public String getPrecipType() {
        return mPrecipType;
    }

    public void setPrecipType(String precipType) {
        mPrecipType = precipType;
    }

    public double getTemperature() {
        return mTemperature;
    }

    public void setTemperature(double temperature) {
        mTemperature = temperature;
    }

    public double getApparentTemperature() {
        return mApparentTemperature;
    }

    public void setApparentTemperature(double apparentTemperature) {
        mApparentTemperature = apparentTemperature;
    }

    public double getDewPoint() {
        return mDewPoint;
    }

    public void setDewPoint(double dewPoint) {
        mDewPoint = dewPoint;
    }

    public double getHumidity() {
        return mHumidity;
    }

    public void setHumidity(double humidity) {
        mHumidity = humidity;
    }

    public double getWindSpeed() {
        return mWindSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        mWindSpeed = windSpeed;
    }

    public int getWindBearing() {
        return mWindBearing;
    }

    public void setWindBearing(int windBearing) {
        mWindBearing = windBearing;
    }

    public double getCloudCover() {
        return mCloudCover;
    }

    public void setCloudCover(double cloudCover) {
        mCloudCover = cloudCover;
    }

    public double getPressure() {
        return mPressure;
    }

    public void setPressure(double pressure) {
        mPressure = pressure;
    }

    public long getSunriseTime() {
        return mSunriseTime;
    }

    public void setSunriseTime(long sunriseTime) {
        mSunriseTime = sunriseTime;
    }

    public long getSunsetTime() {
        return mSunsetTime;
    }

    public void setSunsetTime(long sunsetTime) {
        mSunsetTime = sunsetTime;
    }

    public double getTemperatureMin() {
        return mTemperatureMin;
    }

    public void setTemperatureMin(double temperatureMin) {
        mTemperatureMin = temperatureMin;
    }

    public double getTemperatureMax() {
        return mTemperatureMax;
    }

    public void setTemperatureMax(double temperatureMax) {
        mTemperatureMax = temperatureMax;
    }
}
