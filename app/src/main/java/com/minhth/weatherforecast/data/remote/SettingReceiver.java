package com.minhth.weatherforecast.data.remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.minhth.weatherforecast.ui.activity.MainActivity;

/**
 * Created by THM on 5/26/2017.
 */
public class SettingReceiver extends BroadcastReceiver {
    private OnReceiveListener mReceiveListener;

    public SettingReceiver(
        OnReceiveListener receiveListener) {
        mReceiveListener = receiveListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()){
            case MainActivity.ACTION_SETTING:
                Toast.makeText(context, "Receiver", Toast.LENGTH_SHORT).show();
                mReceiveListener.changeSetting(intent);
                break;
            default:
                break;
        }
    }

    public interface OnReceiveListener{
        void changeSetting(Intent intent);
    }
}
