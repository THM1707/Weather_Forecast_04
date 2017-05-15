package com.minhth.weatherforecast.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ForecastResponseModel {
    @SerializedName("latitude")
    private double mLatitude;
    @SerializedName("longitude")
    private double mLongitude;
    @SerializedName("currently")
    private WeatherModel mCurrently;
    @SerializedName("hourly")
    private WeatherBlock mHourly;
    @SerializedName("daily")
    private WeatherBlock mDaily;

    public ForecastResponseModel(double latitude, double longitude,
                                 WeatherModel currently,
                                 WeatherBlock hourly,
                                 WeatherBlock daily) {
        mLatitude = latitude;
        mLongitude = longitude;
        mCurrently = currently;
        mHourly = hourly;
        mDaily = daily;
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

    public WeatherModel getCurrently() {
        return mCurrently;
    }

    public void setCurrently(WeatherModel currently) {
        mCurrently = currently;
    }

    public WeatherBlock getHourly() {
        return mHourly;
    }

    public void setHourly(WeatherBlock hourly) {
        mHourly = hourly;
    }

    public WeatherBlock getDaily() {
        return mDaily;
    }

    public void setDaily(WeatherBlock daily) {
        mDaily = daily;
    }

    public class WeatherBlock {
        @SerializedName("summary")
        private String mSummary;
        @SerializedName("icon")
        private String mIcon;
        @SerializedName("data")
        private List<WeatherModel> mWeatherModels = new ArrayList<>();

        public String getSummary() {
            return mSummary;
        }

        public void setSummary(String summary) {
            mSummary = summary;
        }

        public String getIcon() {
            return mIcon;
        }

        public void setIcon(String icon) {
            mIcon = icon;
        }

        public List<WeatherModel> getWeatherModels() {
            return mWeatherModels;
        }

        public void setWeatherModels(List<WeatherModel> weatherModels) {
            mWeatherModels = weatherModels;
        }
    }
}