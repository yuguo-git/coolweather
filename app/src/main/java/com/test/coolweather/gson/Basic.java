package com.test.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * @author:As40115 2026/3/26
 * desc:基本信息
 */

public class Basic {
//    城市id

    @SerializedName("cid")
    public String cityId;
//    城市名称

    @SerializedName("location")
    public String cityName;

//    上级城市
    @SerializedName("parent_city")
    public String parentCity;
//    行政区划（省份）
    @SerializedName("admin_area")
    public String adminArea;

//    国家
    @SerializedName("cnty")
    public String country;

//    纬度
    @SerializedName("lat")
    public String latitude;
//    经度
    @SerializedName("lon")
    public String longitude;
//    时区
    @SerializedName("tz")
    public String timeZone;

//    更新时间
    public Update update;

    public class Update {
//        当前城市所在时区的时间
        @SerializedName("loc")
        public String locTime;
//        国际标准时间
        @SerializedName("utc")
        public String utcTime;
    }
}
