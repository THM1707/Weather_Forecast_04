package com.minhth.weatherforecast.ui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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
import com.minhth.weatherforecast.util.WeatherServiceGenerator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    private static final String UNIT = "si";
    private static final String EXCLUDE = "flags";
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final int REQUEST_FINE_LOCATION = 101;
    private static final int FIRST_DAY = 0;
    private static final int LAST_DAY = 7;
    private static final int LAST_HOUR = 23;
    private static final int FIRST_HOUR = 0;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                requestPermission();
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
        mDailyAdapter = new DailyAdapter(mDailyData);
        mRecyclerDaily.setAdapter(mDailyAdapter);
        mRecyclerHourly = (RecyclerView) findViewById(R.id.recycler_hourly);
        mRecyclerHourly.setLayoutManager(layoutManager);
        mHourlyAdapter = new HourlyAdapter(mHourlyData);
        mRecyclerHourly.setAdapter(mHourlyAdapter);
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
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
    }

    private void getLocation() {
        if (mGpsService.isCanGetLocation()) {
            WeatherService service = WeatherServiceGenerator.createService(WeatherService.class);
            service.forecastResponse(WeatherServiceGenerator.API_KEY, mGpsService.getLatitude(),
                mGpsService.getLongitude(), UNIT, EXCLUDE).enqueue(
                new Callback<ForecastResponseModel>() {
                    @Override
                    public void onResponse(Call<ForecastResponseModel> call,
                                           Response<ForecastResponseModel> response) {
                        if (response != null) {
                            ForecastResponseModel model = response.body();
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
                });
        } else {
            Toast.makeText(MainActivity.this, R.string.error_no_service, Toast.LENGTH_SHORT)
                .show();
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

    private void showDetails(WeatherModel data) {
        mTextSunrise.setText(TimeUtils.unixToString(data.getSunriseTime()));
        mTextSunset.setText(TimeUtils.unixToString(data.getSunsetTime()));
        String visibility = DataUtils.formatValue(data.getVisibility(), getResources()
            .getString(R.string.measure_km));
        mTextVisibility.setText(visibility);
        String humidity = DataUtils.formatPercentage(data.getHumidity()*100);
        mTextHumidity.setText(humidity);
        String pressure = DataUtils.formatValue(data.getPressure(),  getResources()
            .getString(R.string.measure_ha));
        mTextPressure.setText(pressure);
        String probability = DataUtils.formatPercentage(data.getPrecipProbability());
        mTextPrecipitation.setText(probability);
        String windSpeed = DataUtils.formatSpeed(data.getWindSpeed(),  getResources()
            .getString(R.string.speed_kmph));
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
        } else if (windBearing > DataUtils.EAST && windBearing < DataUtils.SOUTH){
            return getResources().getString(R.string.bearing_south_east);
        } else if (windBearing == DataUtils.SOUTH){
            return getResources().getString(R.string.bearing_south);
        } else if (windBearing > DataUtils.SOUTH && windBearing < DataUtils.WEST){
            return getResources().getString(R.string.bearing_south_west);
        } else if(windBearing == DataUtils.WEST){
            return getResources().getString(R.string.bearing_west);
        } else if(windBearing > DataUtils.WEST && windBearing < DataUtils.MAX_DEGREE){
            return getResources().getString(R.string.bearing_north_west);
        }
        return result;
    }

    private void showDailyData(ForecastResponseModel.WeatherBlock data) {
        mDailyData.clear();
        mDailyData.addAll(data.getWeatherModels().subList(FIRST_DAY, LAST_DAY));
        mDailyAdapter.notifyDataSetChanged();
    }

    private void showHourlyData(ForecastResponseModel.WeatherBlock data) {
        mHourlyData.clear();
        List<WeatherModel> hourly = data.getWeatherModels().subList(FIRST_HOUR, LAST_HOUR);
        mHourlyData.addAll(hourly);
        mHourlyAdapter.notifyDataSetChanged();
    }

    private void showCurrentlyData(WeatherModel data) {
        String celsius = getResources().getString(R.string.symbol_celsius);
        String temperature = String.valueOf((int) data.getTemperature());
        String updateTime =
            getResources().getString(R.string.title_last_update) + TimeUtils.nowToString();
        String realFeel =
            getResources().getString(R.string.title_feel) + (int) data.getApparentTemperature() +
                celsius;
        mTextTemperature.setText(temperature);
        mTextTime.setText(updateTime);
        mTextFeel.setText(realFeel);
        mTextCondition.setText(ConditionUtils.getCondition(data.getIcon()));
        mImageMainCondition
            .setImageResource(ConditionUtils.getConditionResource(data.getIcon()));
        mTextDegree.setText(celsius);
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
