package com.minhth.weatherforecast.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.minhth.weatherforecast.R;
import com.minhth.weatherforecast.data.model.WeatherModel;
import com.minhth.weatherforecast.util.ConditionUtils;
import com.minhth.weatherforecast.util.TimeUtils;
import com.minhth.weatherforecast.util.UnitUtils;

import java.util.List;

/**
 * Created by THM on 5/21/2017.
 */
public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder> {
    public static final int UNIT_CELSIUS = 0;
    public static final int UNIT_FAHRENHEIT = 1;
    private List<WeatherModel> mHourlyData;
    private int mUnit;

    public HourlyAdapter(List<WeatherModel> hourlyData, int unit) {
        mHourlyData = hourlyData;
        mUnit = unit;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hourly, parent,
            false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(mHourlyData.get(position));
    }

    @Override
    public int getItemCount() {
        return mHourlyData == null ? 0 : mHourlyData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextTime, mTextTemperature;
        private ImageView mImage;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextTemperature = (TextView) itemView.findViewById(R.id.text_hourly_temperature);
            mTextTime = (TextView) itemView.findViewById(R.id.text_hourly_time);
            mImage = (ImageView) itemView.findViewById(R.id.image_hourly_condition);
        }

        public void bindData(WeatherModel item) {
            String temperature = "";
            if (item != null) {
                switch (mUnit) {
                    case UNIT_CELSIUS:
                        temperature = String.valueOf((int) item.getTemperature()) + itemView
                            .getContext().getResources().getString(R.string.symbol_celsius);
                        break;
                    case UNIT_FAHRENHEIT:
                        temperature = String.valueOf((int) UnitUtils.celsiusToFahrenheit(
                            item.getTemperature())) +
                            itemView.getContext().getResources()
                                .getString(R.string.symbol_fahrenheit);
                        break;
                }
                mTextTime.setText(TimeUtils.unixToHourString(item.getTime()));
                mTextTemperature.setText(temperature);
                mImage.setImageResource(ConditionUtils.getConditionResource(item.getIcon()));
            }
        }
    }
}

