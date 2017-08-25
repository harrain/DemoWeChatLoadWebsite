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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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
import com.example.demowechat.MyApplication;
import com.example.demowechat.R;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

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

        mMapView = (MapView) findViewById(R.id.bmapView);
        mTTitle.setText("");
        toolbar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mTBack.setImageResource(R.drawable.back_map);

        if (savedInstanceState != null) {

            mMapView.onCreate(this, savedInstanceState);
        }
        mBaiduMap = mMapView.getMap();

        mPolylines = new ArrayList<>();
        polylineOptions = new PolylineOptions();
        tracesFileNames = new Link<>();

    }

    private void drawStart() {
        qw = BitmapDescriptorFactory
                .fromResource(R.drawable.qw);
        MarkerOptions ooA = new MarkerOptions().position(mPolylines.get(0)).icon(qw)
                .zIndex(9);
        mMarkerS = (Marker) (mBaiduMap.addOverlay(ooA));

        isDrawedStart = true;
    }

    private void drawEnd() {
        qx = BitmapDescriptorFactory
                .fromResource(R.drawable.qx);
        MarkerOptions ooB = new MarkerOptions().position(mPolylines.get(mPolylines.size() - 1)).icon(qx)
                .zIndex(9);
        mMarkerE = (Marker) (mBaiduMap.addOverlay(ooB));
        isDrawedEnd = false;
    }

    private void drawPolyLine() throws Exception {

        polylineOptions.points(mPolylines).width(10).color(Color.RED);

        mPolyline = (Polyline) mBaiduMap.addOverlay(polylineOptions);
        mPolyline.setDottedLine(true);
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

            }else {
                
            }
            obtainLocationDataFromFile(mPath);

            isDrawed = true;
        }
    }

    private void invalidateMapAndTrace() {
        if (mPolylines.size() == 0) {
            ToastFactory.showShortToast("本地文件无坐标点");
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
                    mTraceFnameTv.setText((String) msg.obj);
                    break;
                case 1:
                    Bundle data = msg.peekData();
                    LogUtils.i(tag, "bundle size0 " + data.getParcelableArrayList("polylines").size());
                    if (data.getParcelableArrayList("polylines") != null && data.getParcelableArrayList("polylines").size() != 0) {

                        mPolylines = data.getParcelableArrayList("polylines");
                        LogUtils.i(tag, "handleMessage 1");
                        invalidateMapAndTrace();
                    }
                    LogUtils.i(tag, "1 mPolylines size " + mPolylines.size());
                    break;
                case 2:
                    Bundle datas = msg.peekData();
//                    mPolylines = datas.getParcelableArrayList("polylineCopy");

                    LogUtils.i(tag, "bundle size1 " + datas.getParcelableArrayList("polylineCopy").size());
                    if (datas.getParcelableArrayList("polylineCopy") != null && datas.getParcelableArrayList("polylineCopy").size() != 0) {
                        mPolylines = datas.getParcelableArrayList("polylineCopy");

                        LogUtils.i(tag, "bundle size1 " + datas.getParcelableArrayList("polylineCopy").size());
                    }

                    LogUtils.i(tag, "handleMessage 2");
                    LogUtils.i(tag, "2 mPolylines size " + mPolylines.size());
                    isDrawedEnd = true;
                    invalidateMapAndTrace();
                    break;

            }
        }
    };

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
            clearOverlay();
            mMapView.onDestroy();
            mBaiduMap.clear();
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

