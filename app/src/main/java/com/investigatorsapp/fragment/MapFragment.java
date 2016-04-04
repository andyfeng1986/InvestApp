package com.investigatorsapp.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.GsonRequest;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.google.gson.Gson;
import com.investigatorsapp.R;
import com.investigatorsapp.activity.DynamicStoreActivity;
import com.investigatorsapp.common.LocationReport;
import com.investigatorsapp.common.SalernoManager;
import com.investigatorsapp.common.UserSingleton;
import com.investigatorsapp.common.VolleySingleton;
import com.investigatorsapp.logger.Logger;
import com.investigatorsapp.model.BlocksResult;
import com.investigatorsapp.utils.Constant;
import com.investigatorsapp.utils.UrlWrapper;
import com.investigatorsapp.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fenglei on 15-12-23.
 */
public class MapFragment extends Fragment implements BaiduMap.OnMapLongClickListener,
        View.OnClickListener, OnGetGeoCoderResultListener {

    public static final String TAG = MapFragment.class.getSimpleName();

    private TextView jobNameTV;
    private Spinner polySpinner;

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private MyLocationListenner myListener = new MyLocationListenner();
    private boolean isFirstLoc = true; // 是否首次定位
    private BlocksResult mBlocksResult;
    private BitmapDescriptor mMarkerIcon;
    private Marker mMarker;
    private Button mEnterStoreBtn;
    private LatLng mCurLatLng;
    private GeoCoder mSearch;
    private AlertDialog mEnterStoreAlertDialog;
    private InfoWindow mInfoWindow;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.bmapView);
        jobNameTV = (TextView) rootView.findViewById(R.id.jobname);
        if(UserSingleton.getInstance().getUser() != null) {
            jobNameTV.setText(UserSingleton.getInstance().getUser().jobname);
        }
        polySpinner = (Spinner) rootView.findViewById(R.id.poly_spinner);
        polySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Logger.d(TAG, "onItemSelected, i = " + i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Logger.d(TAG, "onNothingSelected");
            }
        });
        mEnterStoreBtn = (Button) rootView.findViewById(R.id.enter_store_btn);
        mEnterStoreBtn.setOnClickListener(this);
        mBaiduMap = mMapView.getMap();
        mMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
        mBaiduMap.setOnMapLongClickListener(this);
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                Button button = new Button(getActivity());
                button.setBackgroundResource(R.drawable.popup);
                button.setText("删除");
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        marker.remove();
                        mBaiduMap.hideInfoWindow();
                        mMarker = null;
                    }
                });
                LatLng ll = marker.getPosition();
                mInfoWindow = new InfoWindow(button, ll, -47);
                mBaiduMap.showInfoWindow(mInfoWindow);
//                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(marker.getPosition());
//                mBaiduMap.setMapStatus(update);
                return true;
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getData();
        initLocation();
    }

//    private void initPolycountHashMap() {
//        MyApp.polycountHashMap.clear();
//        if(mBlocksResult != null) {
//            for(int i = 0; i < mBlocksResult.polygons.size(); i++) {
//                BlocksResult.Block block = mBlocksResult.polygons.get(i);
//                if(block != null) {
//                    int count = 0;
//                    try {
//                        count = Integer.parseInt(block.polycount);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    MyApp.polycountHashMap.put(block.polygonno, count);
//                }
//            }
//        }
//    }

    @Override
    public void onResume() {
        // activity 恢复时同时恢复地图控件
        super.onResume();
//        mMapView.onResume();
//        getData();
//        initLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mMarker != null) {
            mMarker.remove();
            mMarker = null;
        }
        // activity 暂停时同时暂停地图控件
//        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMarkerIcon.recycle();
        mMapView = null;
    }

    private void initSpinner() {
        if(mBlocksResult != null && mBlocksResult.polygons != null ) {
            ArrayList<String> polygonnameList = new ArrayList<>();
            polygonnameList.add("自动选择");
            for (int i = 0; i < mBlocksResult.polygons.size(); i++) {
                BlocksResult.Block block = mBlocksResult.polygons.get(i);
                if(block != null) {
                    polygonnameList.add(block.polygonname);
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    getActivity(),android.R.layout.simple_spinner_item, polygonnameList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            polySpinner.setAdapter(adapter);
        }
    }

    private void getData() {
        GsonRequest<BlocksResult> gsonRequest = new GsonRequest<BlocksResult>(Request.Method.POST,
                UrlWrapper.getBlockUrl(), BlocksResult.class,
                UrlWrapper.getBlockParams(), new Response.Listener<BlocksResult>() {
                    @Override
                    public void onResponse(BlocksResult response) {
                        if(response != null) {
                            mBlocksResult = response;
                            //getTestData();
                            SalernoManager.getInstance().initSalernoManager(
                                    getActivity(), mBlocksResult);
                            initSpinner();
                            drawLine();
                        }else {
                            Toast.makeText(getActivity(), "同步区块信息失败", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Logger.d(TAG, "get block result error, e = " + error);
                Toast.makeText(getActivity(), "同步区块信息失败", Toast.LENGTH_LONG).show();
            }
        });
        VolleySingleton.getInstance().addToRequestQueue(gsonRequest);
    }

    private void getTestData() {
        try {
            Gson gson = new Gson();
            mBlocksResult = gson.fromJson(Util.geoJsonString, BlocksResult.class);
            Logger.d(TAG, "blocksResult = " + mBlocksResult);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.d(TAG, "parse block result error, e = " + e.toString());
        }
    }

    private void drawLine() {
//        LatLng pt1 = new LatLng(39.93923, 116.357428);
//        LatLng pt2 = new LatLng(39.91923, 116.327428);
//        LatLng pt3 = new LatLng(39.89923, 116.347428);
//        LatLng pt4 = new LatLng(39.89923, 116.367428);
//        LatLng pt5 = new LatLng(39.91923, 116.387428);
//        List<LatLng> pts = new ArrayList<LatLng>();
//        pts.add(pt1);
//        pts.add(pt2);
//        pts.add(pt3);
//        pts.add(pt4);
//        pts.add(pt5);
        if(mBlocksResult != null && mBlocksResult.polygons != null) {
            for(int i = 0; i < mBlocksResult.polygons.size(); i++) {
                BlocksResult.Block block = mBlocksResult.polygons.get(i);
                if(block != null) {
                    ArrayList<BlocksResult.GeoInfo> geoInfos = block.polygon;
                    if(geoInfos != null) {
                        List<LatLng> pts = new ArrayList<LatLng>();
                        for(int j = 0; j < geoInfos.size(); j++) {
                            try {
                                pts.add(new LatLng(Double.parseDouble(geoInfos.get(j).lat),
                                        Double.parseDouble(geoInfos.get(j).lng)));
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        OverlayOptions ooPolygon = new PolygonOptions().points(pts)
                                .stroke(new Stroke(5, 0xAA00FF00)).fillColor(0x00000000);
                        mBaiduMap.addOverlay(ooPolygon);
                    }
                }
            }
        }
    }

    public void clearLine() {
        // 清除所有图层
        mMapView.getMap().clear();
    }

    // 定位初始化
    private void initLocation() {
        mBaiduMap.setMyLocationEnabled(true);
        mLocClient = new LocationClient(getActivity());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型 gcj02
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setPriority(LocationClientOption.GpsFirst);
        option.setScanSpan(15000);
        option.setAddrType("all");
        option.setNeedDeviceDirect(true);
        mLocClient.setLocOption(option);
        mLocClient.start();
        mLocClient.requestLocation();
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.FOLLOWING, true, null));
    }

    private long lastSaveLocTime = -1;

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null || location.getLatitude() == 4.9e-324) {
                return;
            }
            Logger.d(TAG, "p = " + location.getProvince()
                    + ", c = " + location.getCity() + ", d = " + location.getDistrict());
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            LatLng ll = new LatLng(location.getLatitude(),
                    location.getLongitude());
//            if (isFirstLoc) {
//                isFirstLoc = false;
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
            //}
            if(mMarker == null) { //不为空使用用户选点坐标
                mCurLatLng = ll;
            }
            if(lastSaveLocTime == -1) {
                lastSaveLocTime = System.currentTimeMillis();
            }else if(System.currentTimeMillis() - lastSaveLocTime
                    >= Constant.LOC_REPORT_INTERVAL) {
                LocationReport.saveLocation(getActivity(), location.getLatitude(),
                        location.getLongitude());
                lastSaveLocTime = System.currentTimeMillis();
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        MarkerOptions option = new MarkerOptions().icon(mMarkerIcon).position(latLng);
        if(mMarker != null) {
            mMarker.remove();
        }
        mCurLatLng = latLng;
        mMarker = (Marker) (mBaiduMap.addOverlay(option));
    }

    //private TextView customTitleView;

    @Override
    public void onClick(View v) {
        if(mBlocksResult == null || mBlocksResult.polygons == null) {
            Toast.makeText(getActivity(), "未分配区块，无法执行进店操作", Toast.LENGTH_LONG).show();
            return;
        }
        if(mCurLatLng == null) {
            Toast.makeText(getActivity(), "无法定位，不能进店操作", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                    .location(mCurLatLng));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        customTitleView = new TextView(getActivity());
//        customTitleView.setTextSize(30);
        mEnterStoreAlertDialog = new AlertDialog.Builder(getActivity()).
                setMessage(" ").
                setTitle("位置是否准确？(文字只是参考，以地图上的点为准, 长按可手动选点)").
//        setMessage(dialogMessagePre + dialogMessagePost).
        setPositiveButton("确定进店", new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {
        String polygonno;
        int selectedPolyPos = polySpinner.getSelectedItemPosition();
        if (selectedPolyPos != 0) {
            polygonno = getPolyNoByName(polySpinner.getSelectedItem().toString());
        } else {
            polygonno = getPointPolygonNo();
        }
        if (TextUtils.isEmpty(polygonno)) {
            Toast.makeText(getActivity(), "当前位置不在划分区域内，请重新选择",
                    Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getActivity(), DynamicStoreActivity.class);
            intent.putExtra("polygonid", polygonno);
            String salerno = String.valueOf(SalernoManager.getInstance().
                    getSalernoHashMap().get(polygonno));
            for (int count = 5 - salerno.length(); count > 0; count--) {
                salerno = "0" + salerno;
            }
            salerno = polygonno + salerno;
            intent.putExtra("salerno", salerno);
            intent.putExtra("polyname", getPolyNameByNo(polygonno));
            intent.putExtra("lat", String.valueOf(mCurLatLng.latitude));
            intent.putExtra("lng", String.valueOf(mCurLatLng.longitude));
            startActivity(intent);
        }
    }
}).
                setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
//        mEnterStoreAlertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mEnterStoreAlertDialog.show();
    }

    private String getPolyNoByName(String name) {
        if(mBlocksResult != null && mBlocksResult.polygons != null) {
            for (int i = 0; i < mBlocksResult.polygons.size(); i++) {
                BlocksResult.Block block = mBlocksResult.polygons.get(i);
                if(block != null && block.polygonname != null
                        && block.polygonname.equals(name)) {
                    return block.polygonno;
                }
            }
        }
        return "";
    }

    private String getPolyNameByNo(String no) {
        if(mBlocksResult != null && mBlocksResult.polygons != null) {
            for (int i = 0; i < mBlocksResult.polygons.size(); i++) {
                BlocksResult.Block block = mBlocksResult.polygons.get(i);
                if(block != null && block.polygonname != null
                        && block.polygonno.equals(no)) {
                    return block.polygonname;
                }
            }
        }
        return "";
    }

    private String getPointPolygonNo() {
        if(mBlocksResult != null && mBlocksResult.polygons != null) {
            for (int i = 0; i < mBlocksResult.polygons.size(); i++) {
                BlocksResult.Block block = mBlocksResult.polygons.get(i);
                if(block != null) {
                    ArrayList<BlocksResult.GeoInfo> geoInfos = mBlocksResult.polygons.get(i).polygon;
                    if (geoInfos != null) {
                        List<LatLng> pts = new ArrayList<LatLng>();
                        for (int j = 0; j < geoInfos.size(); j++) {
                            try {
                                pts.add(new LatLng(Double.parseDouble(geoInfos.get(j).lat),
                                        Double.parseDouble(geoInfos.get(j).lng)));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (SpatialRelationUtil.isPolygonContainsPoint(pts, mCurLatLng)) {
//                            PolaygonAndSalerNo polaygonAndSalerNo = new PolaygonAndSalerNo();
//                            polaygonAndSalerNo.polygonno = block.polygonno;
//                            polaygonAndSalerNo.salerno = String.valueOf(MyApp.polycountHashMap.get(polaygonAndSalerNo.polygonno));
//                            for(int count = 5 - polaygonAndSalerNo.salerno.length(); count > 0; count--) {
//                                polaygonAndSalerNo.salerno = "0" + polaygonAndSalerNo.salerno;
//                            }
//                            polaygonAndSalerNo.salerno = block.polygonno + polaygonAndSalerNo.salerno;
                            return block.polygonno;
                        }
                    }
                }
            }
        }
        return null;
    }

//    class PolaygonAndSalerNo{
//        String polygonno;
//        String salerno;
//    }

//    private PolaygonAndSalerNo getPointPolygonNo() {
//        if(mBlocksResult != null) {
//            for (int i = 0; i < mBlocksResult.polygons.size(); i++) {
//                BlocksResult.Block block = mBlocksResult.polygons.get(i);
//                if(block != null) {
//                    ArrayList<BlocksResult.GeoInfo> geoInfos = mBlocksResult.polygons.get(i).polygon;
//                    if (geoInfos != null) {
//                        List<LatLng> pts = new ArrayList<LatLng>();
//                        for (int j = 0; j < geoInfos.size(); j++) {
//                            try {
//                                pts.add(new LatLng(Double.parseDouble(geoInfos.get(j).lat),
//                                        Double.parseDouble(geoInfos.get(j).lng)));
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        if (SpatialRelationUtil.isPolygonContainsPoint(pts, mCurLatLng)) {
//                            PolaygonAndSalerNo polaygonAndSalerNo = new PolaygonAndSalerNo();
//                            polaygonAndSalerNo.polygonno = block.polygonno;
//                            polaygonAndSalerNo.salerno = String.valueOf(MyApp.polycountHashMap.get(polaygonAndSalerNo.polygonno));
//                            for(int count = 5 - polaygonAndSalerNo.salerno.length(); count > 0; count--) {
//                                polaygonAndSalerNo.salerno = "0" + polaygonAndSalerNo.salerno;
//                            }
//                            polaygonAndSalerNo.salerno = block.polygonno + polaygonAndSalerNo.salerno;
//                            return polaygonAndSalerNo;
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    String dialogMessagePre = "获取到的位置:  \n\n    ";
    String dialogMessagePost = "\n\n 位置是否准确？(文字只是参考，以地图上的点为准, 长按可手动选点)";

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getActivity(), "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if(mEnterStoreAlertDialog != null && mEnterStoreAlertDialog.isShowing()) {
            mEnterStoreAlertDialog.setMessage(result.getAddress());
//            mEnterStoreAlertDialog.setMessage(dialogMessagePre + result.getAddress() + dialogMessagePost);
        }
    }

}
