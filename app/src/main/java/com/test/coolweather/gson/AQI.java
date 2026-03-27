package com.test.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * @author:As40115 2026/3/26
 * desc:当前城市空气情况
 */

public class AQI {
//    当前城市空气情况

    public AQICity city;
    public class AQICity {
//        空气指数
        public String aqi;
//        PM2.5
        public String pm25;

//        空气质量等级
        @SerializedName("qlty")
        public String quality;
    }
}
