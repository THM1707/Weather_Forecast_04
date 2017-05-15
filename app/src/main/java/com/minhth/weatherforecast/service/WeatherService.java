package com.minhth.weatherforecast.service;

import com.minhth.weatherforecast.data.model.ForecastResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("forecast/{key}/{longitude},{latitude}")
    Call<ForecastResponseModel> forecastResponse(@Path("key") String key,
                                                 @Path("longitude") double longitude,
                                                 @Path("latitude") double latitude,
                                                 @Query("units") String unit,
                                                 @Query("exclude") String exclude);
    @GET("forecast/{key}/{longitude},{latitude},{time}")
    Call<ForecastResponseModel> timeMachineResponse(@Path("key") String key,
                                                    @Path("longitude") double longitude,
                                                    @Path("latitude") double latitude,
                                                    @Path("time") long time,
                                                    @Query("units") String unit,
                                                    @Query("exclude") String exclude);
}
