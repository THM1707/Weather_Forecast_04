package com.minhth.weatherforecast.ui.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.minhth.weatherforecast.R;
import com.minhth.weatherforecast.data.model.ForecastResponseModel;
import com.minhth.weatherforecast.data.model.WeatherModel;
import com.minhth.weatherforecast.service.GpsService;
import com.minhth.weatherforecast.service.WeatherService;
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

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, View.OnClickListener,
    Callback<ForecastResponseModel> {
    private static final String UNIT = "si";
    private static final String EXCLUDE = "flags";
    private static final String KEY_TEMPERATURE = "KEY_TEMPERATURE";
    private static final String KEY_MEASURE = "KEY_MEASURE";
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final int REQUEST_ADDRESSES_NUMBER = 1;
    private static final int REQUEST_FINE_LOCATION = 101;
    private static final int REQUEST_PLACE_PICKER = 102;
    private static final int REQUEST_SETTING = 103;
    private static final int FIRST_DAY = 0;
    private static final int LAST_DAY = 7;
    private static final int LAST_HOUR = 23;
    private static final int FIRST_HOUR = 0;
    private static final int MEASURE_KM = 0;
    private static final int MEASURE_MILE = 1;
    private static final int UNIT_CELSIUS = 0;
    private static final int UNIT_FAHRENHEIT = 1;
    private int mMeasureChoice, mTemperatureChoice;
    private long mUnixTime = 0;
    private GoogleApiClient mGoogleApiClient;
    private GpsService mGpsService;
    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mTextTime, mTextTemperature, mTextCondition, mTextFeel, mTextDegree;
    private TextView mTextPromptHourly, mTextPromptDaily;
    private RecyclerView mRecyclerHourly, mRecyclerDaily;
    private HourlyAdapter mHourlyAdapter;
    private DailyAdapter mDailyAdapter;
    private ImageView mImageMainCondition;
    private List<WeatherModel> mHourlyData = new ArrayList<>();
    private List<WeatherModel> mDailyData = new ArrayList<>();
    private TextView mTextSunrise, mTextSunset, mTextVisibility, mTextPressure;
    private TextView mTextPrecipitation, mTextHumidity, mTextWindSpeed, mTextWindBearing;
    private FloatingActionButton mFloatingDatePicker;
    private ForecastResponseModel mWeatherData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        mTemperatureChoice = sharedPreferences.getInt(KEY_TEMPERATURE, UNIT_CELSIUS);
        mMeasureChoice = sharedPreferences.getInt(KEY_MEASURE, MEASURE_KM);
        initViews();
        buildGoogleApiClient();
        requestGps();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                requestPermission();
                break;
            case REQUEST_PLACE_PICKER:
                Place place = PlacePicker.getPlace(this, data);
                showPickedLocation(place.getLatLng().latitude, place.getLatLng().longitude);
                break;
            case REQUEST_SETTING:
                mMeasureChoice = data.getIntExtra(KEY_MEASURE, MEASURE_KM);
                mTemperatureChoice = data.getIntExtra(KEY_TEMPERATURE, UNIT_CELSIUS);
                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                editor.putInt(KEY_MEASURE, mMeasureChoice);
                editor.putInt(KEY_TEMPERATURE, mTemperatureChoice);
                editor.apply();
                showData(mWeatherData);
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast.makeText(MainActivity.this, R.string.msg_no_permission, Toast
                        .LENGTH_SHORT)
                        .show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_setting:
                setting();
                break;
            case R.id.item_place_picker:
                pickPlace();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setting() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, REQUEST_SETTING);
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
                DatePickerDialog dateDialog = new DatePickerDialog(this,
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
            default:
                break;
        }
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
        Toast.makeText(MainActivity.this, R.string.error_something_happen,
            Toast.LENGTH_SHORT).show();
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    private void initViews() {
        LinearLayoutManager layoutManager
            = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerDaily = (RecyclerView) findViewById(R.id.recycler_daily);
        mRecyclerDaily.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mTextSunrise = (TextView) findViewById(R.id.text_sunrise);
        mTextSunset = (TextView) findViewById(R.id.text_sunset);
        mTextVisibility = (TextView) findViewById(R.id.text_visibility);
        mTextPressure = (TextView) findViewById(R.id.text_pressure);
        mTextPrecipitation = (TextView) findViewById(R.id.text_precipitation);
        mTextHumidity = (TextView) findViewById(R.id.text_humidity);
        mTextWindBearing = (TextView) findViewById(R.id.text_wind_bearing);
        mTextWindSpeed = (TextView) findViewById(R.id.text_wind_speed);
        mTextPromptDaily = (TextView) findViewById(R.id.text_prompt_daily);
        mTextPromptHourly = (TextView) findViewById(R.id.text_prompt_hourly);
        mRecyclerHourly = (RecyclerView) findViewById(R.id.recycler_hourly);
        mRecyclerHourly.setLayoutManager(layoutManager);
        mTextDegree = (TextView) findViewById(R.id.text_degree);
        mTextTime = (TextView) findViewById(R.id.text_time);
        mTextFeel = (TextView) findViewById(R.id.text_real_feel);
        mTextTemperature = (TextView) findViewById(R.id.text_temperature);
        mTextCondition = (TextView) findViewById(R.id.text_condition);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_main);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                getLocation();
            }
        });
        mImageMainCondition = (ImageView) findViewById(R.id.image_condition);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);
        mFloatingDatePicker = (FloatingActionButton) findViewById(R.id.floating_date_picker);
        mFloatingDatePicker.setOnClickListener(this);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
    }

    public void getLocation() {
        mUnixTime = 0;
        if (mGpsService.isCanGetLocation()) {
            forecastResponse(mGpsService.getLatitude(), mGpsService.getLongitude());
            showPlaceName(mGpsService.getLatitude(), mGpsService.getLongitude());
        } else {
            Toast.makeText(MainActivity.this, R.string.error_no_service, Toast.LENGTH_SHORT)
                .show();
        }
    }

    public void pickPlace() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            Intent intent = builder.build(this);
            startActivityForResult(intent, REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    public void showPickedLocation(double latitude, double longitude) {
        mUnixTime = 0;
        forecastResponse(latitude, longitude);
        showPlaceName(latitude, longitude);
    }

    public void showTimeMachine(long unixTime) {
        mUnixTime = unixTime;
        if (mGpsService.isCanGetLocation()) {
            forecastResponse(mGpsService.getLatitude(), mGpsService.getLongitude());
            showPlaceName(mGpsService.getLatitude(), mGpsService.getLongitude());
        } else {
            Toast.makeText(MainActivity.this, R.string.error_no_service, Toast.LENGTH_SHORT)
                .show();
        }
    }

    public void forecastResponse(double latitude, double longitude) {
        WeatherService service = WeatherServiceGenerator.createService(WeatherService.class);
        if (mUnixTime == 0) {
            service
                .forecastResponse(WeatherServiceGenerator.API_KEY, latitude, longitude, UNIT,
                    EXCLUDE)
                .enqueue(this);
        } else {
            service
                .timeMachineResponse(WeatherServiceGenerator.API_KEY, latitude, longitude,
                    mUnixTime, UNIT, EXCLUDE).enqueue(this);
        }
    }

    private void showData(ForecastResponseModel model) {
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

    public void showPlaceName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        StringBuilder builder = new StringBuilder();
        try {
            List<Address> addresses =
                geocoder.getFromLocation(latitude, longitude, REQUEST_ADDRESSES_NUMBER);
            int maxLines = addresses.get(0).getMaxAddressLineIndex();
            for (int i = 0; i < maxLines; i++) {
                String addressStr = addresses.get(0).getAddressLine(i);
                builder.append(addressStr);
                builder.append(" ");
            }
            String finalAddress = builder.toString();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(finalAddress);
            }
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
        String probability = DataUtils.formatPercentage(data.getPrecipProbability()*100);
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
        switch (mTemperatureChoice){
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
                realFeel = getResources().getString(R.string.title_feel) + UnitUtils
                    .celsiusToFahrenheit((int) data.getApparentTemperature()) + temperatureUnit;
                break;
        }
//        String celsius = getResources().getString(R.string.symbol_celsius);
//        String temperature = String.valueOf((int) data.getTemperature());
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

    private void requestPermission() {
        mGpsService = new GpsService(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                .ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.msg_need_permission)
                    .setMessage(R.string.msg_explain_permission)
                    .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat
                                .requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission
                                        .ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
                        }
                    })
                    .setNegativeButton(R.string.action_no, null);
                builder.create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
            }
        } else {
            getLocation();
        }
    }

    public void requestGps() {
        mGoogleApiClient.connect();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
            .checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        requestPermission();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(MainActivity.this, R.string.error_something_happen, Toast
                            .LENGTH_SHORT)
                            .show();
                        requestPermission();
                        break;
                }
            }
        });
    }
}
