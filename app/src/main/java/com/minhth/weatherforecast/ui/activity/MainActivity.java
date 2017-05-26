package com.minhth.weatherforecast.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import com.minhth.weatherforecast.data.local.PlaceDataSource;
import com.minhth.weatherforecast.data.model.PlaceModel;
import com.minhth.weatherforecast.ui.adapter.PagerAdapter;
import com.minhth.weatherforecast.ui.fragment.PlaceFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final int REQUEST_FINE_LOCATION = 101;
    private static final int REQUEST_PLACE_PICKER = 102;
    private static final int REQUEST_SETTING = 103;
    private static final double INIT_LATITUDE = 0;
    private static final double INIT_LONGITUDE = 0;
    private static final int FIRST_FRAGMENT = 0;
    private GoogleApiClient mGoogleApiClient;
    private ViewPager mViewPager;
    private List<PlaceFragment> mFragments;
    private List<PlaceModel> mPlaces;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!isNetworkAvailable()) {
            Toast.makeText(this, R.string.msg_turn_on_internet, Toast.LENGTH_SHORT).show();
            finish();
        }
        initViews();
        buildGoogleApiClient();
        requestGps();
        initComponents();
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
                double latitude = place.getLatLng().latitude;
                double longitude = place.getLatLng().longitude;
                mPlaces.add(new PlaceModel(latitude, longitude));
                PlaceDataSource db = new PlaceDataSource(MainActivity.this);
                db.insertPlace(new PlaceModel(latitude, longitude));
                mFragments.add(PlaceFragment.newInstance(latitude, longitude, mFragments.size()));
                mPagerAdapter.notifyDataSetChanged();
                mViewPager.setCurrentItem(mFragments.size());
                break;
            case REQUEST_SETTING:
                for (PlaceFragment fragment : mFragments) {
                    fragment.refreshData();
                }
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
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, R.string.msg_no_permission, Toast
                        .LENGTH_SHORT)
                        .show();
                } else {
                    setPager();
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
            case R.id.item_delete:
                deletePlace();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    private void initViews() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_main));
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");
        mViewPager = (ViewPager) findViewById(R.id.viewpager_main);
    }

    private void initComponents() {
        PlaceDataSource db = new PlaceDataSource(this);
        mFragments = new ArrayList<>();
        mFragments.add(PlaceFragment.newInstance(INIT_LATITUDE, INIT_LONGITUDE, FIRST_FRAGMENT));
        mPlaces = db.getAllPlace();
        for (PlaceModel p : mPlaces) {
            mFragments.add(PlaceFragment.newInstance(p.getLatitude(), p.getLongitude(), p.getId()
                + 1));
        }
    }

    private void setting() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, REQUEST_SETTING);
    }

    private void setPager() {
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mPagerAdapter);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
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

    private void deletePlace() {
        int position = mViewPager.getCurrentItem();
        if (position == 0) {
            Toast.makeText(this, R.string.msg_cant_delete, Toast.LENGTH_SHORT).show();
        } else {
            mFragments.remove(position);
            PlaceDataSource db = new PlaceDataSource(this);
            db.deletePlace(mPlaces.get(position - 1).getId());
            mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), mFragments);
            mViewPager.setAdapter(mPagerAdapter);
        }
    }

    private void requestPermission() {
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
            setPager();
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

    public boolean isNetworkAvailable() {
        boolean status = false;
        try {
            ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                status = true;
            } else {
                netInfo = cm.getActiveNetworkInfo();
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
                    status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;
    }
}
