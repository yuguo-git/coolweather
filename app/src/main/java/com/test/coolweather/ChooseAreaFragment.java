package com.test.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.test.coolweather.common.Constant;
import com.test.coolweather.db.City;
import com.test.coolweather.db.County;
import com.test.coolweather.db.Province;
import com.test.coolweather.gson.Weather;
import com.test.coolweather.util.HttpUtil;
import com.test.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChooseAreaFragment
 */
public class ChooseAreaFragment extends Fragment {
    private static final String TAG = ChooseAreaFragment.class.getSimpleName(); 

    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ImageButton backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private final List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectProvince;
    private City selectCity;
    private int currentLevel;
    private final Gson gson = new Gson();
    private SharedPreferences preferences;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: 1");
                if (currentLevel == LEVEL_PROVINCE) {
                    selectProvince = provinceList.get(position);
                    SharedPreferences.Editor edit = preferences.edit();
                    String provinceJson = gson.toJson(selectProvince);
                    edit.putString("selectProvince" , provinceJson);
                    edit.apply();
                    queryCites();
                } else if (currentLevel == LEVEL_CITY) {
                    selectCity = cityList.get(position);
                    SharedPreferences.Editor edit = preferences.edit();
                    String cityJson = gson.toJson(selectCity);
                    edit.putString("selectCity" , cityJson);
                    edit.apply();
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }

                }
            }
        });
        Log.e(TAG, "onItemClick 已设置完成");
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCites();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });


        String cityJson = preferences.getString("selectCity", null);
        String provinceJson = preferences.getString("selectProvince", null);
        Log.d(TAG, "onActivityCreated: ");
        if ( provinceJson != null && cityJson != null) {
            selectCity = gson.fromJson(cityJson, City.class);
            selectProvince = gson.fromJson(provinceJson , Province.class);
            queryCounties();
        } else {
            Log.d(TAG, "onActivityCreated: ");
            queryProvinces();
        }

    }

    private void queryCounties() {
        titleText.setText(selectCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf
                (selectCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectProvince.getProvinceCode();
            int cityCode = selectCity.getCityCode();
            String address = Constant.PROVINCE_URL_BASIC + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    private void queryCites() {
        Log.d("queryCites", "queryCites: ");
        titleText.setText(selectProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf
                (selectProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            Log.d("queryCites", "size>0");
            dataList.clear();
            for (City city:cityList) {
                dataList.add(city.getCityName());
            }
            //通知 ListView（或 RecyclerView）：
            // 适配器（Adapter）背后的数据源已经改变了，请重新读取数据并刷新整个列表的显示
            adapter.notifyDataSetChanged();
            //强制将 ListView 的滚动位置设置到第 0 个 item（即列表的第一项）
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            Log.d("queryCites", "size<0");
            int provinceCode = selectProvince.getProvinceCode();
            String address = Constant.PROVINCE_URL_BASIC + provinceCode;
            queryFromServer(address, "city");
        }
    }

    private void queryProvinces() {
        titleText.setText(Constant.COUNTRY);
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province:provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            Log.d(TAG, "queryProvinces: notifyDataSetChanged 已调用");
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = Constant.PROVINCE_URL_BASIC;
            queryFromServer(address, "province");
        }
    }

    private void queryFromServer(String address, String type) {
        showProgressDialog();
        Log.d("begin", "queryFromServer: ");
        HttpUtil.sendOKHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("HttpUtil", "onFailure: " + e.getMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), Constant.FAILED_LOAD, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean flag = false;
                String responseText = response.body().string();
                Log.d("response", "onResponse: " + responseText);
                if ("province".equals(type)) {
                    flag = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    flag = Utility.handleCityResponse(responseText, selectProvince.getId());
                } else if("county".equals(type)) {
                    flag = Utility.handleCountyResponse(responseText, selectCity.getId());
                }
                if (flag) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCites();
                            } else if("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });

    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(Constant.LOADING);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (ImageButton) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        // 检查 ListView 是否可点击
        listView.setClickable(true);
        listView.setEnabled(true);
        Log.d(TAG, "listView.isEnabled() = " + listView.isEnabled());
        Log.d(TAG, "listView.isClickable() = " + listView.isClickable());
        adapter = new ArrayAdapter<>(getContext(), R.layout.my_simple_list_item , R.id.text_view , dataList);
        listView.setAdapter(adapter);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return view;
    }
}