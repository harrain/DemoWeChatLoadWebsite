/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.example.demowechat.map;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.demowechat.R;
import com.example.demowechat.utils.AppConstant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 轨迹运行demo展示
 */
public class TrackShowDemo extends AppCompatActivity {
    @BindView(R.id.scanner_toolbar_back)
    ImageView mTBack;
    @BindView(R.id.scanner_toolbar_title)
    TextView mTTitle;
    @BindView(R.id.scanner_toolbar_more)
    ImageView mTMore;
    @BindView(R.id.titlebar)
    Toolbar toolbar;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private Polyline mPolyline;
    private Handler mHandler;


    // 通过设置间隔时间和距离可以控制速度和图标移动的距离
    private static final int TIME_INTERVAL = 800;
    private static final double DISTANCE = 0.00002;
    private List<LatLng> polylines;
    private PolylineOptions polylineOptions;
    private boolean isDrawed = false;
    private Marker mMarkerS;
    private BitmapDescriptor qw;
    private Marker mMarkerE;
    private BitmapDescriptor qx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_map);
        ButterKnife.bind(this);
        mMapView = (MapView) findViewById(R.id.bmapView);
        setSupportActionBar(toolbar);
        mTTitle.setText("轨迹绘制");

        if (savedInstanceState != null) {

            mMapView.onCreate(this, savedInstanceState);
        }
        mBaiduMap = mMapView.getMap();

        polylines = new ArrayList<>();
        polylineOptions = new PolylineOptions();

    }

    private void drawStartAndEnd() {
        qw = BitmapDescriptorFactory
                .fromResource(R.drawable.qw);
        MarkerOptions ooA = new MarkerOptions().position(polylines.get(0)).icon(qw)
                .zIndex(9);
        mMarkerS = (Marker) (mBaiduMap.addOverlay(ooA));

        qx = BitmapDescriptorFactory
                .fromResource(R.drawable.qx);
        MarkerOptions ooB = new MarkerOptions().position(polylines.get(polylines.size()-1)).icon(qx)
                .zIndex(9);
        mMarkerE = (Marker) (mBaiduMap.addOverlay(ooB));
    }

    private void drawPolyLine() {

        polylineOptions.points(polylines).width(10).color(Color.RED);

        mPolyline = (Polyline) mBaiduMap.addOverlay(polylineOptions);
        mPolyline.setDottedLine(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        for (int index = 0; index < latlngs.length; index++) {
//            polylines.add(latlngs[index]);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        if (!isDrawed) {

            obtainLocationDataFromFile(AppConstant.TRACE_TXT_PATH);

            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(polylines.get(polylines.size()-1));
            builder.zoom(16.0f);
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

            drawStartAndEnd();

            mMapView.showZoomControls(false);

            drawPolyLine();
            isDrawed = true;
        }
    }

    private void obtainLocationDataFromFile(String traceTxtPath) {
        try {
            File file = new File(traceTxtPath);
            if (file.isFile() && file.exists()) { // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file));// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null) {
                    if(lineTxt.contains(":")){
                        String time = lineTxt.substring(0,19);
                        lineTxt = lineTxt.substring(23,lineTxt.length()-1);
                    }
                    String[] split = lineTxt.split("-");
                    LatLng latLng = new LatLng(Double.parseDouble(split[1]), Double.parseDouble(split[0]));
                    polylines.add(latLng);
                }
                bufferedReader.close();
                read.close();
            } else {
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearOverlay();
        mMapView.onDestroy();
        mBaiduMap.clear();
    }


    private static final LatLng[] latlngs = new LatLng[]{
            new LatLng(40.055826, 116.307917),
            new LatLng(40.055916, 116.308455),
            new LatLng(40.055967, 116.308549),
            new LatLng(40.056014, 116.308574),
            new LatLng(40.056440, 116.308485),
            new LatLng(40.056816, 116.308352),
            new LatLng(40.057997, 116.307725),
            new LatLng(40.058022, 116.307693),
            new LatLng(40.058029, 116.307590),
            new LatLng(40.057913, 116.307119),
            new LatLng(40.057850, 116.306945),
            new LatLng(40.057756, 116.306915),
            new LatLng(40.057225, 116.307164),
            new LatLng(40.056134, 116.307546),
            new LatLng(40.055879, 116.307636),
            new LatLng(40.055826, 116.307697),
    };

    private void clearOverlay() {
        mMarkerS = null;
        mMarkerE = null;
        qw.recycle();
        qx.recycle();
    }

    @OnClick(R.id.scanner_toolbar_back)
    public void onBack() {
        finish();
    }
}

