package com.investigatorsapp.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.investigatorsapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fenglei on 15/12/26.
 */
public class AddressLayout extends LinearLayout implements AdapterView.OnItemSelectedListener{

    private JSONObject mJsonObj;
    private Spinner pronviceView;
    private Spinner cityView;
    private Spinner areaView;

    /**
     * 所有省
     */
    private String[] mProvinceDatas;
    /**
     * key - 省 value - 市s
     */
    private Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();
    /**
     * key - 市 values - 区s
     */
    private Map<String, String[]> mAreaDatasMap = new HashMap<String, String[]>();

    /**
     * 当前省的名称
     */
    private String mCurrentProviceName;
    /**
     * 当前市的名称
     */
    private String mCurrentCityName;
    /**
     * 当前区的名称
     */
    private String mCurrentAreaName = "";

    private int initPronvicePos = -1;
    private int initCityPos = -1;
    private int initAreaPos = -1;

    public AddressLayout(Context context) {
        super(context);
        initUI(context);
    }

    public AddressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI(context);
    }

    public AddressLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initUI(context);
    }

    private void initUI(Context context) {
        LayoutInflater.from(context).inflate(R.layout.area_layout, this, true);
        pronviceView = (Spinner)findViewById(R.id.province_spinner);
        cityView = (Spinner)findViewById(R.id.city_spinner);
        areaView = (Spinner)findViewById(R.id.area_spinner);
//        setOrientation(LinearLayout.VERTICAL);
//        pronviceView = new Spinner(context);
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        addView(pronviceView, layoutParams);
//        cityView = new Spinner(context);
//        addView(cityView, layoutParams);
//        areaView = new Spinner(context);
//        addView(areaView, layoutParams);
        pronviceView.setOnItemSelectedListener(this);
        cityView.setOnItemSelectedListener(this);
        areaView.setOnItemSelectedListener(this);

        initJsonData();
        initDatas();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(),android.R.layout.simple_spinner_item, mProvinceDatas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pronviceView.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(adapterView == pronviceView) {
            mCurrentProviceName = mProvinceDatas[i];
            updateCities();
//            updateAreas();
        }else if(adapterView == cityView) {
            mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[i];
            updateAreas();
        }else if(adapterView == areaView) {
            mCurrentAreaName = mAreaDatasMap.get(mCurrentCityName)[i];
        }
    }

    public void setPronvice(String pronvice) {
        try {
            if(!TextUtils.isEmpty(pronvice)) {
                for(int i = 0; i < mProvinceDatas.length; i++) {
                    if(mProvinceDatas[i].equals(pronvice)) {
                        pronviceView.setSelection(i);
                        mCurrentProviceName = pronvice;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCity(String city) {
        try {
            if(!TextUtils.isEmpty(city)) {
                for(int i = 0; i < mCitisDatasMap.get(mCurrentProviceName).length; i++) {
                    if(mCitisDatasMap.get(mCurrentProviceName)[i].equals(city)) {
                        //cityView.setSelection(i);
                        initCityPos = i;
                        mCurrentCityName = city;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setArea(String area) {
        try {
            if(!TextUtils.isEmpty(area)) {
                for(int i = 0; i < mAreaDatasMap.get(mCurrentCityName).length; i++) {
                    if(mAreaDatasMap.get(mCurrentCityName)[i].equals(area)) {
                        //areaView.setSelection(i);
                        initAreaPos = i;
                        mCurrentAreaName = area;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCities() {
        String[] cities = mCitisDatasMap.get(mCurrentProviceName);
        if (cities == null) {
            cities = new String[] { "" };
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(),android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cityView.setAdapter(adapter);
        if(initCityPos != -1) {
            cityView.setSelection(initCityPos);
            initCityPos = -1;
        }
    }

    private void updateAreas() {
        String[] areas = mAreaDatasMap.get(mCurrentCityName);
        if (areas == null || areas.length == 0) {
            areas = new String[] { "" };
            mCurrentAreaName = "";
        } else if (areas.length > 0) {
            mCurrentAreaName = areas[0];
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(),android.R.layout.simple_spinner_item, areas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaView.setAdapter(adapter);
        if(initAreaPos != -1) {
            areaView.setSelection(initAreaPos);
            initAreaPos = -1;
        }
    }

    private void initJsonData() {
        try {
            StringBuffer sb = new StringBuffer();
            InputStream is = getContext().getAssets().open("city.json");
            InputStreamReader reader = new InputStreamReader(is);
//			int len = -1;
            char[] buffer = new char[1024];
            while (reader.read(buffer) != -1) {
                sb.append(buffer);
//				sb.append(new String(buffer, 0, len, "utf8"));
            }
            reader.close();
            is.close();
            mJsonObj = new JSONObject(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initDatas() {
        try {
            JSONArray jsonArray = mJsonObj.getJSONArray("citylist");
            mProvinceDatas = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonP = jsonArray.getJSONObject(i);// 每个省的json对象
                String province = jsonP.getString("p");// 省名字

                mProvinceDatas[i] = province;

                JSONArray jsonCs = null;
                try {
                    /**
                     * Throws JSONException if the mapping doesn't exist or is
                     * not a JSONArray.
                     */
                    jsonCs = jsonP.getJSONArray("c");
                } catch (Exception e1) {
                    continue;
                }
                String[] mCitiesDatas = new String[jsonCs.length()];
                for (int j = 0; j < jsonCs.length(); j++) {
                    JSONObject jsonCity = jsonCs.getJSONObject(j);
                    String city = jsonCity.getString("n");// 市名字
                    mCitiesDatas[j] = city;
                    JSONArray jsonAreas = null;
                    try {
                        /**
                         * Throws JSONException if the mapping doesn't exist or
                         * is not a JSONArray.
                         */
                        jsonAreas = jsonCity.getJSONArray("a");
                    } catch (Exception e) {
                        continue;
                    }

                    String[] mAreasDatas = new String[jsonAreas.length()];// 当前市的所有区
                    for (int k = 0; k < jsonAreas.length(); k++) {
                        String area = jsonAreas.getJSONObject(k).getString("s");// 区域的名称
                        mAreasDatas[k] = area;
                    }
                    mAreaDatasMap.put(city, mAreasDatas);
                }

                mCitisDatasMap.put(province, mCitiesDatas);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mJsonObj = null;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public int getPronvicePos(String pronvice) {
        try {
            if(!TextUtils.isEmpty(pronvice)) {
                for(int i = 0; i < mProvinceDatas.length; i++) {
                    if(mProvinceDatas[i].equals(pronvice)) {
                        return i;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getCityPos(String city) {
        try {
            if(!TextUtils.isEmpty(city)) {
                for(int i = 0; i < mCitisDatasMap.get(mCurrentProviceName).length; i++) {
                    if(mCitisDatasMap.get(mCurrentProviceName)[i].equals(city)) {
                        return i;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getAreaPos(String area) {
        try {
            if(!TextUtils.isEmpty(area)) {
                for(int i = 0; i < mAreaDatasMap.get(mCurrentCityName).length; i++) {
                    if(mAreaDatasMap.get(mCurrentCityName)[i].equals(area)) {
                        return i;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getPronvice() {
        return mCurrentProviceName;
    }

    public String getCity() {
        return mCurrentCityName;
    }

    public String getArea() {
        return mCurrentAreaName;
    }

}
