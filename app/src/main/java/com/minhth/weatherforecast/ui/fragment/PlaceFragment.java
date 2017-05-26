package com.minhth.weatherforecast.ui.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.minhth.weatherforecast.R;
import com.minhth.weatherforecast.data.model.ForecastResponseModel;
import com.minhth.weatherforecast.data.model.WeatherModel;
import com.minhth.weatherforecast.data.remote.SettingReceiver;
import com.minhth.weatherforecast.service.GpsService;
import com.minhth.weatherforecast.service.WeatherService;
import com.minhth.weatherforecast.ui.activity.MainActivity;
import com.minhth.weatherforecast.ui.adapter.DailyAdapter;
import com.minhth.weatherforecast.ui.adapter.HourlyAdapter;
import com.minhth.weatherforecast.util.ConditionUtils;
import com.minhth.weatherforecast.util.DataUtils;
import com.minhth.weatherforecast.util.TimeUtils;
import com.minhth.weatherforecast.util.UnitUtils;
import com.minhth.weatherforecast.util.WeatherServiceGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceFragment extends Fragment implements View.OnClickListener,
    Callback<ForecastResponseModel>, SettingReceiver.OnReceiveListener {
    private static final String UNIT = "si";
    private static final String EXCLUDE = "flags";
    private static final int FIRST_DAY = 0;
    private static final int LAST_DAY = 7;
    private static final int LAST_HOUR = 23;
    private static final int FIRST_HOUR = 0;
    private static final int FIRST_FRAGMENT = 0;
    private static final String BUNDLE_LATITUDE = "LATITUDE";
    private static final String BUNDLE_LONGITUDE = "LONGITUDE";
    private static final String BUNDLE_NUMBER = "NUMBER";
    private static final int MEASURE_KM = 0;
    private static final int MEASURE_MILE = 1;
    private static final int UNIT_CELSIUS = 0;
    private static final int UNIT_FAHRENHEIT = 1;
    private static final int NUMBER_OF_PLACES = 1;
    private static final String KEY_TEMPERATURE = "KEY_TEMPERATURE";
    private static final String KEY_MEASURE = "KEY_MEASURE";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mTextTime, mTextTemperature, mTextCondition, mTextFeel, mTextDegree;
    private TextView mTextPromptHourly, mTextPromptDaily;
    private RecyclerView mRecyclerHourly, mRecyclerDaily;
    private HourlyAdapter mHourlyAdapter;
    private DailyAdapter mDailyAdapter;
    private ImageView mImageMainCondition;
    private List<WeatherModel> mHourlyData = new ArrayList<>();
    private List<WeatherModel> mDailyData = new ArrayList<>();
    private TextView mTextSunrise, mTextSunset, mTextVisibility, mTextPressure, mTextAddress;
    private TextView mTextPrecipitation, mTextHumidity, mTextWindSpeed, mTextWindBearing;
    private FloatingActionButton mFloatingDatePicker;
    private ForecastResponseModel mWeatherData;
    private ProgressBar mProgressDaily, mProgressHourly;
    private int mMeasureChoice, mTemperatureChoice;
    private double mLongitude, mLatitude;
    private int mNumber;
    private long mUnixTime;
    private GpsService mGpsService;
    private SettingReceiver mSettingReceiver;

    public PlaceFragment() {
    }

    public static PlaceFragment newInstance(double latitude, double longitude, int
        number) {
        PlaceFragment fragment = new PlaceFragment();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_NUMBER, number);
        args.putDouble(BUNDLE_LATITUDE, latitude);
        args.putDouble(BUNDLE_LONGITUDE, longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) return;
        mLongitude = getArguments().getDouble(BUNDLE_LONGITUDE);
        mLatitude = getArguments().getDouble(BUNDLE_LATITUDE);
        mNumber = getArguments().getInt(BUNDLE_NUMBER);
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        mTemperatureChoice = sharedPreferences.getInt(KEY_TEMPERATURE, UNIT_CELSIUS);
        mMeasureChoice = sharedPreferences.getInt(KEY_MEASURE, MEASURE_KM);
        mGpsService = new GpsService(getActivity());
        mUnixTime = 0;
    }

    @Override
    public void onStart() {
        super.onStart();
        mSettingReceiver = new SettingReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.ACTION_SETTING);
        getActivity().registerReceiver(mSettingReceiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_place, container, false);
        LinearLayoutManager layoutManager
            = new LinearLayoutManager(v.getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerDaily = (RecyclerView) v.findViewById(R.id.recycler_daily);
        mRecyclerDaily.setLayoutManager(new LinearLayoutManager(v.getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mTextSunrise = (TextView) v.findViewById(R.id.text_sunrise);
        mTextSunset = (TextView) v.findViewById(R.id.text_sunset);
        mTextVisibility = (TextView) v.findViewById(R.id.text_visibility);
        mTextPressure = (TextView) v.findViewById(R.id.text_pressure);
        mTextPrecipitation = (TextView) v.findViewById(R.id.text_precipitation);
        mTextHumidity = (TextView) v.findViewById(R.id.text_humidity);
        mTextWindBearing = (TextView) v.findViewById(R.id.text_wind_bearing);
        mTextWindSpeed = (TextView) v.findViewById(R.id.text_wind_speed);
        mTextPromptDaily = (TextView) v.findViewById(R.id.text_prompt_daily);
        mTextPromptHourly = (TextView) v.findViewById(R.id.text_prompt_hourly);
        mRecyclerHourly = (RecyclerView) v.findViewById(R.id.recycler_hourly);
        mRecyclerHourly.setLayoutManager(layoutManager);
        mTextDegree = (TextView) v.findViewById(R.id.text_degree);
        mTextTime = (TextView) v.findViewById(R.id.text_time);
        mTextFeel = (TextView) v.findViewById(R.id.text_real_feel);
        mTextTemperature = (TextView) v.findViewById(R.id.text_temperature);
        mTextCondition = (TextView) v.findViewById(R.id.text_condition);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_main);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                getData();
            }
        });
        mProgressDaily = (ProgressBar) v.findViewById(R.id.progress_daily);
        mProgressHourly = (ProgressBar) v.findViewById(R.id.progress_hourly);
        mImageMainCondition = (ImageView) v.findViewById(R.id.image_condition);
        mFloatingDatePicker = (FloatingActionButton) v.findViewById(R.id.floating_date_picker);
        mFloatingDatePicker.setOnClickListener(this);
        mTextAddress = (TextView) v.findViewById(R.id.text_address);
        forecastResponse();
        getData();
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floating_date_picker:
                Calendar calendar = Calendar.getInstance();
                int day, year, month;
                day = calendar.get(Calendar.DAY_OF_MONTH);
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                DatePickerDialog dateDialog = new DatePickerDialog(getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month,
                                              int dayOfMonth) {
                            Calendar result = Calendar.getInstance();
                            result.set(year, month, dayOfMonth);
                            mUnixTime = result.getTimeInMillis() / 1000;
                            showTimeMachine(mUnixTime);
                        }
                    }, year, month, day);
                dateDialog.show();
                break;
        }
    }

    @Override
    public void changeSetting(Intent intent) {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        mTemperatureChoice = sharedPreferences.getInt(KEY_TEMPERATURE, UNIT_CELSIUS);
        mMeasureChoice = sharedPreferences.getInt(KEY_MEASURE, MEASURE_KM);
        showData(mWeatherData);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mSettingReceiver);
    }

    @Override
    public void onResponse(Call<ForecastResponseModel> call,
                           Response<ForecastResponseModel> response) {
        if (response != null) {
            ForecastResponseModel model = response.body();
            mWeatherData = model;
            showData(model);
        }
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onFailure(Call<ForecastResponseModel> call, Throwable t) {
        Toast.makeText(getActivity(), R.string.error_something_happen,
            Toast.LENGTH_SHORT).show();
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void getData() {
        mUnixTime = 0;
        if (mNumber == FIRST_FRAGMENT) {
            getLocation();
        } else {
            forecastResponse();
        }
    }

    public void getLocation() {
        if (mGpsService.isCanGetLocation()) {
            mLatitude = mGpsService.getLatitude();
            mLongitude = mGpsService.getLongitude();
            forecastResponse();
        } else {
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
            Toast.makeText(getActivity(), R.string.error_no_service, Toast.LENGTH_SHORT)
                .show();
        }
    }

    public void showTimeMachine(long unixTime) {
        mUnixTime = unixTime;
        forecastResponse();
    }

    public void forecastResponse() {
        WeatherService service = WeatherServiceGenerator.createService(WeatherService.class);
        if (mUnixTime == 0) {
            service
                .forecastResponse(WeatherServiceGenerator.API_KEY, mLatitude, mLongitude, UNIT,
                    EXCLUDE)
                .enqueue(this);
        } else {
            service
                .timeMachineResponse(WeatherServiceGenerator.API_KEY, mLatitude, mLongitude,
                    mUnixTime, UNIT, EXCLUDE).enqueue(this);
        }
    }

    private void showData(ForecastResponseModel model) {
        showPlaceName();
        mProgressDaily.setVisibility(View.GONE);
        mProgressHourly.setVisibility(View.GONE);
        mTextPromptHourly.setText(R.string.title_hourly_forecast);
        mTextPromptDaily.setText(R.string.title_daily_forecast);
        if (model.getCurrently() != null) {
            showCurrentlyData(model.getCurrently());
        }
        if (model.getHourly() != null) {
            showHourlyData(model.getHourly());
        }
        if (model.getDaily() != null) {
            showDailyData(model.getDaily());
            showDetails(model.getDaily().getWeatherModels().get(FIRST_DAY));
        }
    }

    public void showPlaceName() {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        StringBuilder builder = new StringBuilder();
        try {
            List<Address> addresses =
                geocoder.getFromLocation(mLatitude, mLongitude, NUMBER_OF_PLACES);
            int maxLines = addresses.get(0).getMaxAddressLineIndex();
            for (int i = 0; i < maxLines; i++) {
                String addressStr = addresses.get(0).getAddressLine(i);
                builder.append(addressStr);
                builder.append(" ");
            }
            String finalAddress = builder.toString();
            mTextAddress.setText(finalAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDetails(WeatherModel data) {
        mTextSunrise.setText(TimeUtils.unixToHourString(data.getSunriseTime()));
        mTextSunset.setText(TimeUtils.unixToHourString(data.getSunsetTime()));
        String visibility = "";
        String windSpeed = "";
        switch (mMeasureChoice) {
            case MEASURE_KM:
                visibility = DataUtils.formatValue(data.getVisibility(), getResources()
                    .getString(R.string.measure_km));
                windSpeed = DataUtils.formatSpeed(data.getWindSpeed(), getResources()
                    .getString(R.string.speed_kmph));
                break;
            case MEASURE_MILE:
                visibility = DataUtils.formatValue(UnitUtils.kmToMile(data.getVisibility()),
                    getResources().getString(R.string.measure_mile));
                windSpeed = DataUtils.formatSpeed(UnitUtils.kmToMile(data.getWindSpeed()),
                    getResources().getString(R.string.speed_mph));
                break;
        }
        mTextVisibility.setText(visibility);
        String humidity = DataUtils.formatPercentage(data.getHumidity() * 100);
        mTextHumidity.setText(humidity);
        String pressure = DataUtils.formatValue(data.getPressure(), getResources()
            .getString(R.string.measure_ha));
        mTextPressure.setText(pressure);
        String probability = DataUtils.formatPercentage(data.getPrecipProbability() * 100);
        mTextPrecipitation.setText(probability);
        mTextWindSpeed.setText(windSpeed);
        String windBearing = getWindBearing(data.getWindBearing());
        mTextWindBearing.setText(windBearing);
    }

    public String getWindBearing(int windBearing) {
        String result = getResources().getString(R.string.bearing_immobile);
        if (windBearing == DataUtils.NORTH) {
            return getResources().getString(R.string.bearing_north);
        } else if (windBearing > DataUtils.NORTH && windBearing < DataUtils.EAST) {
            return getResources().getString(R.string.bearing_north_east);
        } else if (windBearing == DataUtils.EAST) {
            return getResources().getString(R.string.bearing_east);
        } else if (windBearing > DataUtils.EAST && windBearing < DataUtils.SOUTH) {
            return getResources().getString(R.string.bearing_south_east);
        } else if (windBearing == DataUtils.SOUTH) {
            return getResources().getString(R.string.bearing_south);
        } else if (windBearing > DataUtils.SOUTH && windBearing < DataUtils.WEST) {
            return getResources().getString(R.string.bearing_south_west);
        } else if (windBearing == DataUtils.WEST) {
            return getResources().getString(R.string.bearing_west);
        } else if (windBearing > DataUtils.WEST && windBearing < DataUtils.MAX_DEGREE) {
            return getResources().getString(R.string.bearing_north_west);
        }
        return result;
    }

    private void showDailyData(ForecastResponseModel.WeatherBlock data) {
        mDailyData.clear();
        if (mUnixTime == 0) {
            mDailyData.addAll(data.getWeatherModels().subList(FIRST_DAY, LAST_DAY));
        } else {
            mDailyData.addAll(data.getWeatherModels());
        }
        mDailyAdapter = new DailyAdapter(mDailyData, mTemperatureChoice);
        mRecyclerDaily.setAdapter(mDailyAdapter);
    }

    private void showHourlyData(ForecastResponseModel.WeatherBlock data) {
        mHourlyData.clear();
        List<WeatherModel> hourly = data.getWeatherModels().subList(FIRST_HOUR, LAST_HOUR);
        mHourlyData.addAll(hourly);
        switch (mTemperatureChoice) {
            case UNIT_CELSIUS:
                mHourlyAdapter = new HourlyAdapter(mHourlyData, UNIT_CELSIUS);
                break;
            case UNIT_FAHRENHEIT:
                mHourlyAdapter = new HourlyAdapter(mHourlyData, UNIT_FAHRENHEIT);
                break;
        }
        mRecyclerHourly.setAdapter(mHourlyAdapter);
    }

    private void showCurrentlyData(WeatherModel data) {
        String temperatureUnit = "";
        String temperature = "";
        String realFeel = "";
        switch (mTemperatureChoice) {
            case UNIT_CELSIUS:
                temperatureUnit = getResources().getString(R.string.symbol_celsius);
                temperature = String.valueOf((int) data.getTemperature());
                realFeel = getResources().getString(R.string.title_feel) + (int) data
                    .getApparentTemperature() + temperatureUnit;
                break;
            case UNIT_FAHRENHEIT:
                temperatureUnit = getResources().getString(R.string.symbol_fahrenheit);
                temperature = String.valueOf((int) UnitUtils.celsiusToFahrenheit(
                    data.getTemperature()));
                realFeel =
                    getResources().getString(R.string.title_feel) + String.valueOf((int) UnitUtils
                        .celsiusToFahrenheit(data.getApparentTemperature())) + temperatureUnit;
                break;
        }
        String updateTime;
        if (mUnixTime == 0) {
            updateTime =
                getResources().getString(R.string.title_last_update) + TimeUtils.nowToString();
        } else {
            updateTime = getResources().getString(R.string.title_date) + TimeUtils
                .unixToDateString(mUnixTime);
        }
        mTextTemperature.setText(temperature);
        mTextTime.setText(updateTime);
        mTextFeel.setText(realFeel);
        mTextCondition.setText(ConditionUtils.getCondition(data.getIcon()));
        mImageMainCondition
            .setImageResource(ConditionUtils.getConditionResource(data.getIcon()));
        mTextDegree.setText(temperatureUnit);
    }
}
