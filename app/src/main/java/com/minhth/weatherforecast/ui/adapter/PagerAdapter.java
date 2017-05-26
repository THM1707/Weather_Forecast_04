package com.minhth.weatherforecast.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.minhth.weatherforecast.ui.fragment.PlaceFragment;

import java.util.List;

/**
 * Created by THM on 5/26/2017.
 */
public class PagerAdapter extends FragmentPagerAdapter {
    private List<PlaceFragment> mPlaceFragments;

    public PagerAdapter(FragmentManager fm,
                        List<PlaceFragment> placeFragments) {
        super(fm);
        mPlaceFragments = placeFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mPlaceFragments.get(position);
    }

    @Override
    public int getCount() {
        return mPlaceFragments == null ? 0 : mPlaceFragments.size();
    }
}
