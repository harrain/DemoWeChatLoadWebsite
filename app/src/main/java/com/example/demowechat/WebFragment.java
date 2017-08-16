package com.example.demowechat;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import com.example.demowechat.rlPart.BaseAdapter;
import com.example.demowechat.rlPart.FileListAdapter;
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.Link;
import com.example.demowechat.utils.LogUtils;
import com.example.demowechat.utils.NumberValidationUtil;
import com.example.demowechat.utils.SharePrefrenceUtils;
import com.example.demowechat.utils.ToastFactory;
import com.example.demowechat.widget.SwipeMenuRecyclerView;

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
 * 加载网页的fragment
 */
public class WebFragment extends Fragment {

    @BindView(R.id.trace_fname_tv_f)
    TextView mTraceFnameTv;
    @BindView(R.id.bmapView_f)
    MapView mMapView;
    private BaiduMap mBaiduMap;
    private Polyline mPolyline;
    private List<LatLng> polylines;
    private PolylineOptions polylineOptions;
    private boolean isDrawed = false;
    private BitmapDescriptor qw;
    private BitmapDescriptor qx;
    private Marker mMarkerS;
    private Marker mMarkerE;
    private final String tag = "TrackShowDemo";

    private Link<String> tracesFileNames;
    @BindView(R.id.popup_rl_f)
    RelativeLayout popupRl;
    @BindView(R.id.pop_iv_f)
    ImageView popupIv;

    private Context mContext;
    private PopupWindow pw;

    public WebFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web, container, false);
        ButterKnife.bind(this,view);
        mContext = getActivity();
//        if (savedInstanceState != null) {
//
//            mMapView.onCreate(this, savedInstanceState);
//        }
        mBaiduMap = mMapView.getMap();

        polylines = new ArrayList<>();
        polylineOptions = new PolylineOptions();
        tracesFileNames = new Link<>();
        ((MainActivity)mContext).setToolbarTitle("轨迹展示");
        return view;
    }

    private void drawStartAndEnd() {
        qw = BitmapDescriptorFactory
                .fromResource(R.drawable.qw);
        MarkerOptions ooA = new MarkerOptions().position(polylines.get(0)).icon(qw)
                .zIndex(9);
        mMarkerS = (Marker) (mBaiduMap.addOverlay(ooA));

        qx = BitmapDescriptorFactory
                .fromResource(R.drawable.qx);
        MarkerOptions ooB = new MarkerOptions().position(polylines.get(polylines.size() - 1)).icon(qx)
                .zIndex(9);
        mMarkerE = (Marker) (mBaiduMap.addOverlay(ooB));
    }

    private void drawPolyLine() {

        polylineOptions.points(polylines).width(10).color(Color.RED);

        mPolyline = (Polyline) mBaiduMap.addOverlay(polylineOptions);
        mPolyline.setDottedLine(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        tracesFileNames.clean();
        File cacheDir = new File(AppConstant.TRACES_DIR);
        Log.e(tag, "file:" + cacheDir.getAbsolutePath());
        if (!cacheDir.exists()) {
            LogUtils.e(tag, "TRACES_DIR is not existed");
            return;
        }
        File[] files = cacheDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                tracesFileNames.add(file.getName());

            }

        }
        LogUtils.i(tag, "tracesFileNames size:   " + tracesFileNames.size());
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (!isDrawed) {
            isDrawed = true;
            String path = SharePrefrenceUtils.getInstance().getRecentTraceFilePath();

            obtainLocationDataFromFile(path);

            invalidateMapAndTrace();

            isDrawed = true;
        }
    }

    private void invalidateMapAndTrace() {
        if (polylines.size() == 0) {
            ToastFactory.showShortToast("本地文件无坐标点");
            return;
        }
        mBaiduMap.clear();
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(polylines.get(polylines.size() - 1));
        builder.zoom(16.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        drawStartAndEnd();

        mMapView.showZoomControls(false);

        drawPolyLine();
    }

    private void obtainLocationDataFromFile(String traceTxtPath) {
        if (TextUtils.isEmpty(traceTxtPath)) {
            LogUtils.e(tag, "TraceFilePath = null");
            ToastFactory.showShortToast("TraceFilePath = null");
            return;
        }
        polylines.clear();
        try {
            File file = new File(traceTxtPath);
            if (file.isFile() && file.exists()) { // 判断文件是否存在

                mTraceFnameTv.setText(file.getName());

                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file));// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null) {
                    if (lineTxt.contains(":")) {
                        String time = lineTxt.substring(0, 19);
                        lineTxt = lineTxt.substring(22, lineTxt.length() - 1);
//                        LogUtils.i(tag,time+"____"+lineTxt);
                    }
                    String[] split = lineTxt.split("-");
                    if (NumberValidationUtil.isPositiveDecimal(split[0]) && NumberValidationUtil.isPositiveDecimal(split[1])) {
//                    LogUtils.i(tag,split[0]+"____"+split[1]);
                        LatLng latLng = new LatLng(Double.parseDouble(split[1]), Double.parseDouble(split[0]));
                        polylines.add(latLng);
                    }
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

    @OnClick(R.id.popup_rl_f)
    public void popWindowShowFile() {
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
                pw.dismiss();

                String path = AppConstant.TRACES_DIR + "/" + tracesFileNames.get(position);
                LogUtils.i(tag, "filelistitem: " + path);
                obtainLocationDataFromFile(path);
                invalidateMapAndTrace();

            }

            @Override
            public void onLongClick(View v, int position) {

            }
        });
        rv.setAdapter(adapter);


        int[] location = new int[2];
        popupRl.getLocationOnScreen(location);

        pw = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, MyApplication.getInstance().getScreenHeight() / 3, true);
        pw.showAtLocation(popupRl, Gravity.NO_GRAVITY, location[0], location[1] - MyApplication.getInstance().getScreenHeight() / 3);
        pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popupIv.setImageResource(R.drawable.upred_downblack);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearOverlay();
        mMapView.onDestroy();
        mBaiduMap.clear();
    }

    private void clearOverlay() {
        mMarkerS = null;
        mMarkerE = null;
        qw.recycle();
        qx.recycle();
    }



}
