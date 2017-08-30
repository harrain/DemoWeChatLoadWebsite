/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.example.demowechat.map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.trace.api.analysis.DrivingBehaviorResponse;
import com.baidu.trace.api.analysis.OnAnalysisListener;
import com.baidu.trace.api.analysis.StayPoint;
import com.baidu.trace.api.analysis.StayPointRequest;
import com.baidu.trace.api.analysis.StayPointResponse;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.Point;
import com.baidu.trace.model.StatusCodes;
import com.example.demowechat.MyApplication;
import com.example.demowechat.R;
import com.example.demowechat.rlPart.ArrayListAdapter;
import com.example.demowechat.rlPart.FileListAdapter;
import com.example.demowechat.rlPart.base.BaseAdapter;
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.Link;
import com.example.demowechat.utils.LogUtils;
import com.example.demowechat.utils.NumberValidationUtil;
import com.example.demowechat.utils.SharePrefrenceUtils;
import com.example.demowechat.utils.ThreadPoolUtils;
import com.example.demowechat.utils.ToastFactory;
import com.example.demowechat.widget.SwipeMenuRecyclerView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 轨迹运行demo展示
 */
public class TrackShowDemo extends AppCompatActivity implements OnGetGeoCoderResultListener {
    @BindView(R.id.scanner_toolbar_back)
    ImageView mTBack;
    @BindView(R.id.scanner_toolbar_title)
    TextView mTTitle;
    @BindView(R.id.scanner_toolbar_more)
    ImageView mTMore;
    @BindView(R.id.titlebar)
    Toolbar toolbar;
    @BindView(R.id.trace_fname_tv)
    TextView mTraceFnameTv;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private Polyline mPolyline;

    private List<LatLng> mPolylines;
    private PolylineOptions polylineOptions;
    private boolean isDrawed = false;
    private boolean isDrawedStart = false;
    private boolean isDrawedEnd = false;
    private Marker mMarkerS;
    private BitmapDescriptor qw;
    private Marker mMarkerE;
    private BitmapDescriptor qx;
    private final String tag = "TrackShowDemo";

    private Link<String> tracesFileNames;
    @BindView(R.id.popup_rl)
    RelativeLayout popupRl;
    @BindView(R.id.pop_iv)
    ImageView popupIv;

    private Context mContext;
    private PopupWindow pw;
    private String mPath;
    private ArrayList<LatLng> polylineCopy = new ArrayList<>();
    private boolean isEagleEye = false;
    private String date;
    private List<TrackPoint> mTracks;
    private Calendar calendar;
    private SimpleDateFormat sdf;
    /**
     * 轨迹分析上一次请求时间
     */
    private long lastQueryTime = 0;
    /**
     * 轨迹分析  停留点覆盖物集合
     */
    private List<Marker> stayPointMarkers = new ArrayList<>();
    /**
     * 轨迹分析  停留点集合
     */
    private List<Point> stayPoints = new ArrayList<>();
    /**
     * 是否查询停留点
     */
    private boolean isStayPoint = true;
    /**
     * 停留点请求
     */
    private StayPointRequest stayPointRequest = new StayPointRequest();

    /**
     * 轨迹分析监听器
     */
    private OnAnalysisListener mAnalysisListener = null;
    /**
     * 当前轨迹分析详情框对应的marker
     */
    private Marker analysisMarker = null;
    /**
     * 轨迹分析详情框布局
     */
    private TrackAnalysisInfoLayout trackAnalysisInfoLayout = null;
    private long start;
    private long end;
    private List<ArrayList<LatLng>> latlngContainer;
    volatile boolean mapMatch = true;
    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private Marker mClickMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_map);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        mPath = intent.getStringExtra("tracePath");
        isEagleEye = intent.getBooleanExtra("isEagleEye",false);
        date = intent.getStringExtra("date");
        mContext = this;

        mTTitle.setText("");
        toolbar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mTBack.setImageResource(R.drawable.back_map);

        mPolylines = new ArrayList<>();
        polylineOptions = new PolylineOptions();
        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        initMap(savedInstanceState);
        initRadioGroup();
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
    }

    private void initMap(Bundle savedInstanceState) {
        mMapView = (MapView) findViewById(R.id.bmapView);
        if (savedInstanceState != null) {

            mMapView.onCreate(this, savedInstanceState);
        }
        mBaiduMap = mMapView.getMap();
        initListener();
        trackAnalysisInfoLayout = new TrackAnalysisInfoLayout(this, mBaiduMap);
//        tracesFileNames = new Link<>();
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker == mMarkerS){
                    mClickMarker = mMarkerS;
                    mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                            .location(MapUtil.convertTrace2Map(mTracks.get(0).getLocation())));
//                    LogUtils.i(tag,"mClickMarker = mMarkerS");

                }else if (marker == mMarkerE){
                    mClickMarker = mMarkerE;
                    mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                            .location(MapUtil.convertTrace2Map(mTracks.get(mTracks.size()-1).getLocation())));

                }
                Bundle bundle = marker.getExtraInfo();
                if (bundle!=null){
                    int type = bundle.getInt("type");
                    if (type == 0){
                        trackAnalysisInfoLayout.titleText.setText(R.string.track_analysis_stay_title);
                        trackAnalysisInfoLayout.key1.setText(R.string.stay_start_time);
                        trackAnalysisInfoLayout.value1.setText(sdf.format(new java.sql.Timestamp(bundle.getLong("startTime") * 1000)));
                        trackAnalysisInfoLayout.key2.setText(R.string.stay_end_time);
                        trackAnalysisInfoLayout.value2.setText(sdf.format(new java.sql.Timestamp(bundle.getLong("endTime") * 1000)));
                        trackAnalysisInfoLayout.key3.setText(R.string.stay_duration);
                        trackAnalysisInfoLayout.value3.setText(CommonUtil.formatSecond(bundle.getInt("duration")));
                        trackAnalysisInfoLayout.key4.setText("");
                        trackAnalysisInfoLayout.value4.setText("");
                        trackAnalysisInfoLayout.key5.setText("");
                        trackAnalysisInfoLayout.value5.setText("");
                        //  保存当前操作的marker
                        analysisMarker = marker;

                        //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
                        InfoWindow trackAnalysisInfoWindow = new InfoWindow(trackAnalysisInfoLayout.mView, marker.getPosition(), -47);
                        //显示InfoWindow
                        mBaiduMap.showInfoWindow(trackAnalysisInfoWindow);
                    }
                }
                return true;
            }
        });

        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mBaiduMap.hideInfoWindow();
                }
            }
        });
    }

    private void initRadioGroup(){
        RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.raw_rb) {
                    mapMatch = false;
                    mBaiduMap.clear();
                    isDrawedStart = false;
                    obtainLatlngFromEagleEye();
                }
                if (checkedId == R.id.match_rb) {
                    mapMatch = true;
                    mBaiduMap.clear();
                    obtainLatlngFromEagleEye();
                }
            }
        });
    }

    private void showTrackPointInfo(LatLng ll,String address,int index){
//        LogUtils.i(tag,"showTrackPointInfo");
        calendar.setTimeInMillis(mTracks.get(index).getLocTime()*1000);
        String latitude = String.valueOf(mTracks.get(index).getLocation().getLatitude());
        if (latitude.length()>8){
            latitude = latitude.substring(0,8);
        }
        String longitude = String.valueOf(mTracks.get(index).getLocation().getLongitude());
        if (longitude.length()>8){
            longitude = longitude.substring(0,8);
        }
        String speed = String.valueOf(mTracks.get(index).getSpeed());
        if (speed.length()>4){
            speed = speed.substring(0,4);
        }
        String radius = String.valueOf(mTracks.get(index).getRadius());
        if (radius.length()>4){
            radius = radius.substring(0,4);
        }
        trackAnalysisInfoLayout.titleText.setText("覆盖点详情");
        trackAnalysisInfoLayout.key1.setText("坐标");
        trackAnalysisInfoLayout.value1.setText(longitude+","+latitude);
        trackAnalysisInfoLayout.key2.setText("位置");
        if (address!=null) {
            trackAnalysisInfoLayout.value2.setText(address);
        }
        trackAnalysisInfoLayout.key3.setText("时间");
        trackAnalysisInfoLayout.value3.setText(sdf.format(calendar.getTime()));
        trackAnalysisInfoLayout.key4.setText("速度");
        trackAnalysisInfoLayout.value4.setText(speed+"km/h");
        trackAnalysisInfoLayout.key5.setText("精度");
        trackAnalysisInfoLayout.value5.setText(radius+"米");
        InfoWindow trackAnalysisInfoWindow = new InfoWindow(trackAnalysisInfoLayout.mView, ll, -47);
        mBaiduMap.showInfoWindow(trackAnalysisInfoWindow);
    }

    private void showTrackPointInfo(Marker marker,int index){
        View view = View.inflate(mContext,R.layout.baidumap_infowindow,null);

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.baidumap_infowindow_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rv.setLayoutManager(layoutManager);
        List<String> list = new ArrayList<String>();
        ArrayListAdapter adapter = new ArrayListAdapter(mContext,list);
        rv.setAdapter(adapter);
        rv.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        ImageView closeIv = (ImageView) view.findViewById(R.id.close_iv);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaiduMap.hideInfoWindow();
            }
        });
        LatLng ll = marker.getPosition();
        InfoWindow infoWindow = new InfoWindow(view,ll,-47);

        calendar.setTimeInMillis(mTracks.get(index).getLocTime()*1000);
        String longitude = String.valueOf(mTracks.get(index).getLocation().getLongitude()).substring(0,8);
        String latitude = String.valueOf(mTracks.get(index).getLocation().getLatitude()).substring(0,8);
        String speed = String.valueOf(mTracks.get(index).getSpeed()*3.6).substring(0,4);
        String radius = String.valueOf(mTracks.get(index).getRadius()).substring(0,3);
        list.add("精度:  "+radius+"米");
        list.add("速度:  "+ speed+"km/h");
        list.add("时间:  "+sdf.format(calendar.getTime()));
        list.add("位置:  ");
        list.add("定位:  "+longitude+","+latitude);
        adapter.notifyDataSetChanged();
        mBaiduMap.showInfoWindow(infoWindow);
        LogUtils.i(tag,"track string "+mTracks.get(index).toString());
    }

    @Override
    protected void onStart() {
        super.onStart();

//        tracesFileNames.clean();
//        File cacheDir = new File(AppConstant.TRACES_DIR);
//        Log.e(tag, "file:" + cacheDir.getAbsolutePath());
//        if (!cacheDir.exists()) {
//            LogUtils.e(tag, "TRACES_DIR is not existed");
//            return;
//        }
//        File[] files = cacheDir.listFiles();
//        for (File file : files) {
//            if (file.isFile()) {
//                tracesFileNames.add(file.getName());
//
//            }
//
//        }
//        LogUtils.i(tag, "tracesFileNames size:   " + tracesFileNames.size());
//        for (int index = 0; index < latlngs.length; index++) {
//            mPolylines.add(latlngs[index]);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        if (!isDrawed) {
            isDrawed = true;
            if (TextUtils.isEmpty(mPath)) {
                mPath = SharePrefrenceUtils.getInstance().getRecentTraceFilePath();
            }
            if (isEagleEye){
                obtainLatlngFromEagleEye();
            }else {
                obtainLocationDataFromFile(mPath);
            }
        }
    }

    private void invalidateMapAndTrace() {
        if (mPolylines.size() == 0) {
            ToastFactory.showShortToast("文件无坐标点");
            return;
        }

        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(mPolylines.get(mPolylines.size() - 1));
        builder.zoom(16.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        mMapView.showZoomControls(false);

        try {
            if (!isDrawedStart) {
                drawStart();
            }
            if (isDrawedEnd) {
                drawEnd();
            }
            drawPolyLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawStart() {
        qw = BitmapDescriptorFactory
                .fromResource(R.drawable.qw);
        MarkerOptions ooA = new MarkerOptions().position(mPolylines.get(0)).icon(qw)
                .zIndex(1);
        mMarkerS = (Marker) (mBaiduMap.addOverlay(ooA));

        isDrawedStart = true;
    }

    private void drawEnd() {
        qx = BitmapDescriptorFactory
                .fromResource(R.drawable.qx);
        MarkerOptions ooB = new MarkerOptions().position(mPolylines.get(mPolylines.size() - 1)).icon(qx)
                .zIndex(2);
        mMarkerE = (Marker) (mBaiduMap.addOverlay(ooB));
        isDrawedEnd = false;
    }

    private void drawPolyLine() throws Exception {

        polylineOptions.points(mPolylines).width(10).color(Color.RED);

        mPolyline = (Polyline) mBaiduMap.addOverlay(polylineOptions);
        mPolyline.setDottedLine(true);
    }

    private void obtainLatlngFromEagleEye() {
        mTracks = new ArrayList<>();
        latlngContainer = new ArrayList<ArrayList<LatLng>>(10);
        latlngContainer.add(new ArrayList<LatLng>());
        latlngContainer.add(new ArrayList<LatLng>());
        latlngContainer.add(new ArrayList<LatLng>());
        latlngContainer.add(new ArrayList<LatLng>());
        latlngContainer.add(new ArrayList<LatLng>());
        latlngContainer.add(new ArrayList<LatLng>());
        latlngContainer.add(new ArrayList<LatLng>());
        latlngContainer.add(new ArrayList<LatLng>());
        latlngContainer.add(new ArrayList<LatLng>());
        latlngContainer.add(new ArrayList<LatLng>());
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt = DateTime.parse(date,dtf);
//                DateTime dt1 = DateTime.parse(date,dtf).plusHours(23);
        Calendar calendar = Calendar.getInstance();
        calendar.set(dt.getYear(),dt.getMonthOfYear()-1,dt.getDayOfMonth(),7,0);
        start = calendar.getTime().getTime()/1000;
        calendar.set(dt.getYear(),dt.getMonthOfYear()-1,dt.getDayOfMonth(),23,0);
        end = calendar.getTime().getTime()/1000;
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int[] count = {0};
                if (mapMatch){
                    TraceControl.getInstance().queryHistoryTrackPoints(start, end, new TraceControl.TrackResultListener() {
                        @Override
                        public void onObtainTrackPointsList(List trackList, List<TrackPoint> points) {
                            obtainTrackPointsList(count, trackList, points);
                        }
                        @Override
                        public void onComplete() {
                            LogUtils.i(tag, "onComplete");
                            obtainTrackComplete();
                        }
                    });

                }else {
                    LogUtils.i(tag,"mapmatch false");
                    TraceControl.getInstance().queryHistoryTrackPoints(false, start, end, new TraceControl.TrackResultListener() {
                        @Override
                        public void onObtainTrackPointsList(List trackList, List<TrackPoint> points) {
                            obtainTrackPointsList(count, trackList, points);
                        }
                        @Override
                        public void onComplete() {
                            obtainTrackComplete();
                        }
                    });
                }
            }
        }).start();

    }

    private void obtainTrackComplete(){
        Message message = Message.obtain();
        message.what = 3;
        mHandler.sendMessage(message);
    }

    private void obtainTrackPointsList(int[] count,List trackList, List<TrackPoint> points){
        if (count[0] > 9) {
            count[0] = 0;
        }
        if (latlngContainer.get(count[0]) != null && latlngContainer.get(count[0]).size() > 0) {
            latlngContainer.get(count[0]).clear();
        }
        latlngContainer.get(count[0]).addAll(trackList);

        mTracks.addAll(points);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("polylines", latlngContainer.remove(count[0]));
        Message message = Message.obtain();
        message.what = 1;
        message.setData(bundle);
        mHandler.sendMessage(message);
        trackList.clear();
        count[0]++;
    }

    private void initListener() {
        mAnalysisListener = new OnAnalysisListener() {
            @Override
            public void onStayPointCallback(StayPointResponse response) {
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    lastQueryTime = 0;
                    ToastFactory.showShortToast(response.getMessage());
                    LogUtils.i(tag,response.getMessage());
                    return;
                }
                if (0 == response.getStayPointNum()) {
                    LogUtils.i(tag,response.getStayPointNum()+"");
                    return;
                }
                stayPoints.addAll(response.getStayPoints());
                LogUtils.i(tag,"stayPoints size "+stayPoints.size());
                handleOverlays(stayPointMarkers, stayPoints, isStayPoint);
            }

            @Override
            public void onDrivingBehaviorCallback(DrivingBehaviorResponse drivingBehaviorResponse) {

            }
        };

    }

    private void handleOverlays(List<Marker> markers, List<? extends com.baidu.trace.model.Point> points, boolean
            isVisible) {
        if (null == markers || null == points) {
            return;
        }
        for (com.baidu.trace.model.Point point : points) {
            OverlayOptions overlayOptions = new MarkerOptions()
                    .position(MapUtil.convertTrace2Map(point.getLocation()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding)).zIndex(9).draggable(true);
            Marker marker = (Marker) mBaiduMap.addOverlay(overlayOptions);
            Bundle bundle = new Bundle();
            if (point instanceof StayPoint) {
                StayPoint stayPoint = (StayPoint) point;
                bundle.putInt("type", 0);
                bundle.putLong("startTime", stayPoint.getStartTime());
                bundle.putLong("endTime", stayPoint.getEndTime());
                bundle.putInt("duration", stayPoint.getDuration());
            }
            marker.setExtraInfo(bundle);
            markers.add(marker);
        }
        handleMarker(markers, isVisible);
    }

    private void handleMarker(List<Marker> markers, boolean isVisible) {
        if (null == markers || markers.isEmpty()) {
            return;
        }
        for (Marker marker : markers) {
            marker.setVisible(isVisible);
        }

        if (markers.contains(analysisMarker)) {
            mBaiduMap.hideInfoWindow();
        }

    }

    /**
     * 查询停留点
     */
    private void queryStayPoint() {
        TraceControl.getInstance().initRequest(stayPointRequest);
        stayPointRequest.setEntityName( TraceControl.getInstance().entityName);
        stayPointRequest.setStartTime(start);
        stayPointRequest.setEndTime(end);
        stayPointRequest.setStayTime(Constants.STAY_TIME);
        TraceControl.getInstance().mClient.queryStayPoint(stayPointRequest, mAnalysisListener);
    }

    private void obtainLocationDataFromFile(final String traceTxtPath) {
        if (TextUtils.isEmpty(traceTxtPath)) {
            LogUtils.e(tag, "TraceFilePath = null");
            ToastFactory.showShortToast("TraceFilePath = null");
            return;
        }
        if (!TextUtils.isEmpty(mTraceTxtPath)) {
            if (mTraceTxtPath.equals(traceTxtPath)) {
                return;
            }
        }
        try {
            ThreadPoolUtils.getInstance().cancel();
            mPolylines.clear();
            mBaiduMap.clear();
            mTraceTxtPath = traceTxtPath;
            ThreadPoolUtils.getInstance().execute(mFRT);
            LogUtils.i(tag, "obtainLocationDataFromFile end---------------");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    FileReaderTask mFRT = new FileReaderTask();
    private String mTraceTxtPath;

    class FileReaderTask implements Runnable {
        @Override
        public void run() {
            try {
                File file = new File(mTraceTxtPath);
                if (file.isFile() && file.exists()) { // 判断文件是否存在
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = file.getName();
                    mHandler.sendMessage(msg);

                    InputStreamReader read = new InputStreamReader(
                            new FileInputStream(file));// 考虑到编码格式
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String lineTxt = null;
                    String line = "";
                    String times = "";
                    ArrayList<LatLng> polylines = new ArrayList<>();

                    List<ArrayList<LatLng>> latlngContainer = new ArrayList<ArrayList<LatLng>>(10);
                    latlngContainer.add(new ArrayList<LatLng>());
                    latlngContainer.add(new ArrayList<LatLng>());
                    latlngContainer.add(new ArrayList<LatLng>());
                    latlngContainer.add(new ArrayList<LatLng>());
                    latlngContainer.add(new ArrayList<LatLng>());
                    latlngContainer.add(new ArrayList<LatLng>());
                    latlngContainer.add(new ArrayList<LatLng>());
                    latlngContainer.add(new ArrayList<LatLng>());
                    latlngContainer.add(new ArrayList<LatLng>());
                    latlngContainer.add(new ArrayList<LatLng>());

                    int count = 0;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d HH:mm:ss");
                    while ((lineTxt = bufferedReader.readLine()) != null) {

                        if (lineTxt.contains(":")) {

                            String time = lineTxt.substring(0, 19);

                            if (!TextUtils.isEmpty(times)) {
                                Date front = sdf.parse(times);
                                Date after = sdf.parse(time);
                                if (after.getTime() - front.getTime() < 58000) {
                                    continue;
                                }
//                                LogUtils.i(tag,"front "+times+"-"+"after "+time);
                            }
                            times = time;
                            lineTxt = lineTxt.substring(22, lineTxt.length() - 1);

//                        LogUtils.i(tag,time+"____"+lineTxt);
                        }
                        if (lineTxt.equals(line)) {
                            continue;
                        }
                        String[] split = lineTxt.split("-");
                        if (NumberValidationUtil.isPositiveDecimal(split[0]) && NumberValidationUtil.isPositiveDecimal(split[1])) {
//                    LogUtils.i(tag,split[0]+"____"+split[1]);
                            LatLng latLng = new LatLng(Double.parseDouble(split[1]), Double.parseDouble(split[0]));
                            polylines.add(latLng);

                            line = lineTxt;
//                            LogUtils.i(tag,"number "+split[0]);
                            if (polylines.size() > 300) {
//                                polylineCopy.clear();
//                                polylineCopy.addAll(polylines);
                                if (count > 9) {
                                    count = 0;
                                }
                                if (latlngContainer.get(count) != null && latlngContainer.get(count).size() > 0) {
                                    latlngContainer.get(count).clear();
                                }
                                latlngContainer.get(count).addAll(polylines);
                                Bundle bundle = new Bundle();
                                bundle.putParcelableArrayList("polylines", latlngContainer.get(count));
                                Message message = Message.obtain();
                                message.what = 1;
                                message.setData(bundle);
                                mHandler.sendMessage(message);
                                LogUtils.i(tag, "polylines size " + polylines.size());
                                count++;
                                polylines.clear();
                                Thread.sleep(100);
                            }

                        }

                    }
                    LogUtils.i(tag, "end polylines size " + polylines.size());
                    polylineCopy.clear();
                    polylineCopy.addAll(polylines);
                    Bundle bundles = new Bundle();
                    bundles.putParcelableArrayList("polylineCopy", polylineCopy);
                    Message messages = new Message();
                    messages.what = 2;
                    messages.setData(bundles);
                    mHandler.sendMessage(messages);
//                    LogUtils.i(tag,"polylines size "+polylines.size());
                    polylines.clear();
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
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    try {
                        mTraceFnameTv.setText((String) msg.obj);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    Bundle data = msg.peekData();
                    try {
                        if (data.getParcelableArrayList("polylines") != null && data.getParcelableArrayList("polylines").size() != 0) {

                            mPolylines = data.getParcelableArrayList("polylines");
                            LogUtils.i(tag, "handleMessage 1");
                            invalidateMapAndTrace();
                        }
                        LogUtils.i(tag, "1 mPolylines size " + mPolylines.size());
                    }catch (Exception e){
                        LogUtils.e(tag,e.getMessage());
                    }
                    break;
                case 2:
                    Bundle datas = msg.peekData();
                    try {
                        if (datas.getParcelableArrayList("polylineCopy") != null && datas.getParcelableArrayList("polylineCopy").size() != 0) {
                            mPolylines = datas.getParcelableArrayList("polylineCopy");

                            LogUtils.i(tag, "bundle size1 " + datas.getParcelableArrayList("polylineCopy").size());
                        }
                        LogUtils.i(tag, "handleMessage 2");
                        LogUtils.i(tag, "2 mPolylines size " + mPolylines.size());
                    }catch (Exception e){
                        LogUtils.e(tag,e.getMessage());

                    }finally {
                        isDrawedEnd = true;
                        invalidateMapAndTrace();

                    }

                    break;
                case 3:
                    LogUtils.i(tag, "handleMessage 3");
                    isDrawedEnd = true;
                    invalidateMapAndTrace();
                    queryStayPoint();
                    break;

            }
        }
    };

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
//        LogUtils.i(tag,"onGetReverseGeoCodeResult");
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(mContext, "抱歉，未能获得地理位置信息", Toast.LENGTH_LONG)
                    .show();
        }
        if (mClickMarker == mMarkerS){

            try {
                showTrackPointInfo(mMarkerS.getPosition(), result.getAddress(), 0);
            }catch (Exception e){e.printStackTrace();}
        }else if (mClickMarker == mMarkerE){
            try {
                showTrackPointInfo(mMarkerE.getPosition(), result.getAddress(), mTracks.size()-1);
            }catch (Exception e){e.printStackTrace();}

        }
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }


    public void popWindowShowFile(View v) {
        popupIv.setImageResource(R.drawable.upblack_downred);

        View popupView = View.inflate(mContext, R.layout.popup_file_rv, null);
        LinearLayout popLl = (LinearLayout) popupView.findViewById(R.id.pop_ll);


        SwipeMenuRecyclerView rv = (SwipeMenuRecyclerView) popupView.findViewById(R.id.traces_rl);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rv.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        rv.addItemDecoration(dividerItemDecoration);

        FileListAdapter adapter = new FileListAdapter(mContext, tracesFileNames);
        adapter.setOnClickListener(new BaseAdapter.OnClickListener() {
            @Override
            public void onShortClick(View v, int position) {
                switch (v.getId()) {
                    case R.id.file_tv:
                        pw.dismiss();

                        String path = AppConstant.TRACES_DIR + "/" + tracesFileNames.get(position);
                        LogUtils.i(tag, "filelistitem: " + path);
                        obtainLocationDataFromFile(path);
                        break;
                    case R.id.right_menu:
                        tracesFileNames.remove(position);
                        break;
                }

            }

            @Override
            public void onLongClick(View v, int position) {

            }
        });
        rv.setAdapter(adapter);


        int[] location = new int[2];
        popupRl.getLocationOnScreen(location);

//        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.AT_MOST);
//        popupView.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.UNSPECIFIED);
        LogUtils.i(tag, "popll: " + popupView.getMeasuredHeight() + "---" + "screenheight/3: " + MyApplication.getInstance().getScreenHeight() / 3);
//        if (popLl.getMeasuredHeight() > MyApplication.getInstance().getScreenHeight() / 3) {
//            LogUtils.i(tag, "popll: " + popLl.getMeasuredHeight() + "---" + "screenheight/3: " + MyApplication.getInstance().getScreenHeight() / 3);
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//            layoutParams.height = 450;
//            popLl.setLayoutParams(layoutParams);
//        }

        pw = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, MyApplication.getInstance().getScreenHeight() / 3, true);
//        pw.setFocusable(true);
//        pw.setOutsideTouchable(true);//没多大用
        pw.showAtLocation(popupRl, Gravity.NO_GRAVITY, location[0], location[1] - MyApplication.getInstance().getScreenHeight() / 3);
        pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popupIv.setImageResource(R.drawable.upred_downblack);
            }
        });
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
        try {
            mBaiduMap.hideInfoWindow();
            clearOverlay();
            mMapView.onDestroy();
            mBaiduMap.clear();
            TraceControl.getInstance().resetTrackResultListener();

            if (null != trackAnalysisInfoLayout) {
                trackAnalysisInfoLayout = null;
            }

            if (null != stayPoints) {
                stayPoints.clear();
            }
            stayPoints = null;
            stayPointMarkers = null;
        } catch (Exception e) {
            LogUtils.e(tag, e.getMessage());
        }

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

