package com.test.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * @author:As40115 2026/3/26
 * desc:实时天气数据
 */

public class Now {

//    云量
    public String cloud;

//    体感温度
    @SerializedName("fl")
    public String feelsLike;
//    相对湿度
    @SerializedName("hum")
    public String humidity;
//    降水量
    @SerializedName("pcpn")
    public String precipitation;
//    大气压强
    @SerializedName("pres")
    public String pressure;
//    温度
    @SerializedName("tmp")
    public String temperature;
//    能见度
    @SerializedName("vis")
    public String visibility;
//    风向角度
    @SerializedName("wind_deg")
    public String windDegree;
//    风向
    @SerializedName("wind_dir")
    public String windDirection;
//    风力等级
    @SerializedName("wind_sc")
    public String windScale;
//    风速
    @SerializedName("wind_spd")
    public String windSpeed;
//    天气状况
    @SerializedName("cond")
    public Condition condition;

    public class Condition {
        //    天气状况代码
        @SerializedName("cond_code")
        public String conditionCode;
        //    天气状况文本
        @SerializedName("cond_txt")
        public String conditionText;
    }
}
