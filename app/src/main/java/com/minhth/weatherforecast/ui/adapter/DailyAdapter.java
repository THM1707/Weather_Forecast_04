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
public class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.ViewHolder> {
    public static final int UNIT_CELSIUS = 0;
    public static final int UNIT_FAHRENHEIT = 1;
    private static final int TODAY = 0;
    private static final int TOMORROW = 1;
    private List<WeatherModel> mDailyData;
    private int mUnit;

    public DailyAdapter(List<WeatherModel> dailyData, int unit) {
        mDailyData = dailyData;
        mUnit = unit;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily, parent,
            false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(mDailyData.get(position));
    }

    @Override
    public int getItemCount() {
        return mDailyData == null ? 0 : mDailyData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextMin, mTextMax, mTextDay;
        private ImageView mImageCondition;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextDay = (TextView) itemView.findViewById(R.id.text_day);
            mTextMax = (TextView) itemView.findViewById(R.id.text_daily_max);
            mTextMin = (TextView) itemView.findViewById(R.id.text_daily_min);
            mImageCondition = (ImageView) itemView.findViewById(R.id.image_daily_condition);
        }

        public void bindData(WeatherModel item) {
            if (item != null) {
                switch (getAdapterPosition()) {
                    case TODAY:
                        mTextDay.setText(R.string.txt_today);
                        break;
                    case TOMORROW:
                        mTextDay.setText(R.string.txt_tomorrow);
                        break;
                    default:
                        mTextDay.setText(TimeUtils.unixToDayOfWeek(item.getTime()));
                        break;
                }
                String celsius = itemView.getContext().getResources().getString(R.string
                    .symbol_celsius);
                String fahrenheit = itemView.getContext().getResources().getString(R.string
                    .symbol_fahrenheit);
                String minTemperature = "";
                String maxTemperature = "";
                switch (mUnit) {
                    case UNIT_CELSIUS:
                        minTemperature = String.valueOf((int) item.getTemperatureMin()) + celsius;
                        maxTemperature = String.valueOf((int) item.getTemperatureMax()) + celsius;
                        break;
                    case UNIT_FAHRENHEIT:
                        minTemperature = String.valueOf((int) UnitUtils.celsiusToFahrenheit(item
                            .getTemperatureMin())) + fahrenheit;
                        maxTemperature = String.valueOf((int) UnitUtils.celsiusToFahrenheit(item
                            .getTemperatureMax())) + fahrenheit;
                        break;
                }
                mTextMin.setText(minTemperature);
                mTextMax.setText(maxTemperature);
                mImageCondition
                    .setImageResource(ConditionUtils.getConditionResource(item.getIcon()));
            }
        }
    }
}