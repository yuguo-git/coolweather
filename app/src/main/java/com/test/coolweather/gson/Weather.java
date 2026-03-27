package com.test.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author:As40115 2026/3/26
 * desc:
 */

public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
