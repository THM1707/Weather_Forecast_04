package com.minhth.weatherforecast.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.minhth.weatherforecast.R;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener,
    PopupMenu.OnMenuItemClickListener {
    private static final int MEASURE_KM = 0;
    private static final int MEASURE_MILE = 1;
    private static final int TEMPERATURE_CELSIUS = 0;
    private static final int TEMPERATURE_FAHRENHEIT = 1;
    private static final String KEY_TEMPERATURE = "KEY_TEMPERATURE";
    private static final String KEY_MEASURE = "KEY_MEASURE";
    private static final String PREFERENCE_NAME = "SETTING";
    public static final String ACTION_SETTING = "com.minhth.weatherforecast.ACTION_SETTING";
    private int mTemperatureChoice, mMeasureChoice;
    private TextView mTextTemp, mTextMeasure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_setting));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.title_settings));
        }
        mTextTemp = (TextView) findViewById(R.id.text_temp_choice);
        mTextMeasure = (TextView) findViewById(R.id.text_measure_choice);
        findViewById(R.id.linear_temp).setOnClickListener(this);
        findViewById(R.id.linear_measure).setOnClickListener(this);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME, Context
            .MODE_PRIVATE);
        mTemperatureChoice = sharedPreferences.getInt(KEY_TEMPERATURE, TEMPERATURE_CELSIUS);
        mMeasureChoice = sharedPreferences.getInt(KEY_MEASURE, MEASURE_KM);
        switch (mTemperatureChoice) {
            case TEMPERATURE_CELSIUS:
                mTextTemp.setText(getResources().getString(R.string.symbol_celsius));
                break;
            case TEMPERATURE_FAHRENHEIT:
                mTextTemp.setText(getResources().getString(R.string.symbol_fahrenheit));
        }
        switch (mMeasureChoice) {
            case MEASURE_KM:
                mTextMeasure.setText(getResources().getString(R.string.measure_kph_km));
                break;
            case MEASURE_MILE:
                mTextMeasure.setText(getResources().getString(R.string.measure_mph_mile));
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linear_measure:
                PopupMenu measurePopup = new PopupMenu(SettingActivity.this, mTextMeasure);
                getMenuInflater().inflate(R.menu.menu_measure, measurePopup.getMenu());
                measurePopup.setOnMenuItemClickListener(this);
                measurePopup.show();
                break;
            case R.id.linear_temp:
                PopupMenu tempPopup = new PopupMenu(SettingActivity.this, mTextTemp);
                getMenuInflater().inflate(R.menu.menu_temperature, tempPopup.getMenu());
                tempPopup.setOnMenuItemClickListener(this);
                tempPopup.show();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        SharedPreferences.Editor editor =
            getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE).edit();
        switch (item.getItemId()) {
            case R.id.menu_kph:
                mTextMeasure.setText(getResources().getString(R.string.measure_kph_km));
                mMeasureChoice = MEASURE_KM;
                editor.putInt(KEY_MEASURE, MEASURE_KM);
                editor.apply();
                break;
            case R.id.menu_mph:
                mTextMeasure.setText(getResources().getString(R.string.measure_mph_mile));
                mMeasureChoice = MEASURE_MILE;
                editor.putInt(KEY_MEASURE, MEASURE_MILE);
                editor.apply();
                break;
            case R.id.menu_celsius:
                mTextTemp.setText(getResources().getString(R.string.symbol_celsius));
                mTemperatureChoice = TEMPERATURE_CELSIUS;
                editor.putInt(KEY_TEMPERATURE, TEMPERATURE_CELSIUS);
                editor.apply();
                break;
            case R.id.menu_fahrenheit:
                mTextTemp.setText(getResources().getString(R.string.symbol_fahrenheit));
                mTemperatureChoice = TEMPERATURE_FAHRENHEIT;
                editor.putInt(KEY_TEMPERATURE, TEMPERATURE_FAHRENHEIT);
                editor.apply();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(KEY_TEMPERATURE, mTemperatureChoice);
        returnIntent.putExtra(KEY_MEASURE, mMeasureChoice);
        setResult(RESULT_OK, returnIntent);
        super.onBackPressed();
    }
}
