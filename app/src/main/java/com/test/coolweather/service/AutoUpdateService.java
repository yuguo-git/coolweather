package com.test.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.test.coolweather.WeatherActivity;
import com.test.coolweather.common.Constant;
import com.test.coolweather.gson.Weather;
import com.test.coolweather.util.HttpUtil;
import com.test.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateBingPic() {
        HttpUtil.sendOKHttpRequest(Constant.BING_PIC_URL, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(AutoUpdateService.this).
                        edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
        });
    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            //有缓存直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = Constant.WEATHER_URL_BASIC + "?cityid=" + weatherId
                    + "&key=" + Constant.WEATHER_KEY;
            HttpUtil.sendOKHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.
                                getDefaultSharedPreferences(AutoUpdateService.this).
                                edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}