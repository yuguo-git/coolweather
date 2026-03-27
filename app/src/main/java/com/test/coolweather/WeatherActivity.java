package com.test.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.test.coolweather.common.Constant;
import com.test.coolweather.gson.AQI;
import com.test.coolweather.gson.Forecast;
import com.test.coolweather.gson.Weather;
import com.test.coolweather.util.HttpUtil;
import com.test.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    public SwipeRefreshLayout swipeRefreshLayout;
    public DrawerLayout drawerLayout;
    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;
    private LinearLayout forecastLayout;

    private TextView qltyText;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private ImageView binPicImg;
    private Button navButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weather);

        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        qltyText = (TextView) findViewById(R.id.qlty_textview);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        binPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        final String weatherId;
        if (weatherString != null) {
            //有缓存直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询数据
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(binPicImg);
        } else {
            loadBingPic();
        }

    }

    public void loadBingPic() {
        String bingPicRequest = Constant.BING_PIC_URL;
        HttpUtil.sendOKHttpRequest(bingPicRequest, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(WeatherActivity.this, Constant.FAILED_GET_PIC, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).
                        edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(binPicImg);
                    }
                });
            }
        });
    }

    /*
    *     根据id请求城市天气
    * */
    public void requestWeather(final String weatherId) {
        String requestUrl = Constant.WEATHER_URL_BASIC + "?cityid=" + weatherId
                + "&key=" + Constant.WEATHER_KEY;
        HttpUtil.sendOKHttpRequest(requestUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("1", "run: ");
                        Toast.makeText(WeatherActivity.this, Constant.FAILED_GET_WEATHER,
                                Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!= null && "ok".equals(weather.status)) {
                            Log.d("2", "run: ");
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).
                                    edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Log.d("3", "run: ");
                            Toast.makeText(WeatherActivity.this, Constant.FAILED_GET_WEATHER,
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /*
    *    处理并展示Weather实体类的数据
    * */
    public void showWeatherInfo(Weather weather) {
        if (weather != null && "ok".equals(weather.status)) {
            String cityName = weather.basic.cityName;
            String updateTime = weather.basic.update.locTime.split(" ")[1];
            String degree = weather.now.temperature + "℃";
            String conditionText = weather.now.condition.conditionText;
            titleCity.setText(cityName);
            titleUpdateTime.setText(updateTime);
            degreeText.setText(degree);
            weatherInfoText.setText(conditionText);
            forecastLayout.removeAllViews();
            for (Forecast forecast : weather.forecastList) {
                View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                        forecastLayout, false);
                TextView dateText = (TextView) view.findViewById(R.id.date_text);
                TextView infoText = (TextView) view.findViewById(R.id.info_text);
                TextView maxText = (TextView) view.findViewById(R.id.max_text);
                TextView minText = (TextView) view.findViewById(R.id.min_text);

                dateText.setText(forecast.date);
                infoText.setText(forecast.condition.info);
                maxText.setText(forecast.temperature.maxTemperature);
                minText.setText(forecast.temperature.minTemperature);
                forecastLayout.addView(view);
            }

            if (weather.aqi != null) {
                String quality = "空气质量 " + weather.aqi.city.quality;
                qltyText.setText(quality);
                aqiText.setText(weather.aqi.city.aqi);
                pm25Text.setText(weather.aqi.city.pm25);
            }


            if (weather.suggestion != null) {
                String comfort = "舒适度" + weather.suggestion.comfortable.text;
                String carWash = "洗车指数" + weather.suggestion.carWash.text;
                String sport = "运动建议" + weather.suggestion.sport.text;
                comfortText.setText(comfort);
                carWashText.setText(carWash);
                sportText.setText(sport);
            }

            weatherLayout.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, Constant.FAILED_GET_WEATHER, Toast.LENGTH_SHORT).show();
        }


    }
}