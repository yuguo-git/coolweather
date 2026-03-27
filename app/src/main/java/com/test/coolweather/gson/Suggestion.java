package com.test.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * @author:As40115 2026/3/26
 * desc:天气应用的生活指数建议
 */

public class Suggestion {
//    舒适度
    @SerializedName("comf")
    public Comfortable comfortable;
//    运动
    public Sport sport;

//    洗车
    @SerializedName("cw")
    public CarWash carWash;


    public class Comfortable {
        public String type;

//        简述
        @SerializedName("brf")
        public String brief;

//        详述
        @SerializedName("txt")
        public String text;
    }

    public class Sport {
        public String type;

        @SerializedName("brf")
        public String brief;

        @SerializedName("txt")
        public String text;
    }

    public class CarWash {
        public String type;

        @SerializedName("brf")
        public String brief;

        @SerializedName("txt")
        public String text;
    }
}
