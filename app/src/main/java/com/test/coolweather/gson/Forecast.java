package com.test.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * @author:As40115 2026/3/26
 * desc:未来天气预报
 */

public class Forecast {
//    日期
    public String date;
//    天气状况
    @SerializedName("cond")
    public Condition condition;

//    温度
    @SerializedName("tmp")
    public Temperature temperature;
    public class Condition {
//        白天天气
        @SerializedName("txt_d")
        public String info;
    }
    public class Temperature {
//        最高温度
        @SerializedName("max")
        public String maxTemperature;
//        最低温度
        @SerializedName("min")
        public String minTemperature;
    }
}
