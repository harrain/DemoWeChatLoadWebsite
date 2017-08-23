package com.example.demowechat;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.example.demowechat.rlPart.base.BaseAdapter;
import com.example.demowechat.rlPart.FileListAdapter;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 加载网页的fragment
 */
public class TraceFragment extends Fragment {

    @BindView(R.id.trace_fname_tv_f)
    TextView mTraceFnameTv;
    @BindView(R.id.bmapView_f)
    MapView mMapView;
    private BaiduMap mBaiduMap;
    private Polyline mPolyline;
    private List<LatLng> mPolylines;
    private PolylineOptions polylineOptions;
    private boolean isDrawed = false;
    private boolean isDrawedStart = false;
    private boolean isDrawedEnd = false;
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

    public TraceFragment() {
        // Required empty public constructor
    }

    private ArrayList<LatLng> polylineCopy = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web, container, false);
        ButterKnife.bind(this, view);
        mContext = getActivity();
//        if (savedInstanceState != null) {
//
//            mMapView.onCreate(this, savedInstanceState);
//        }
        mBaiduMap = mMapView.getMap();

        mPolylines = new ArrayList<>();
        polylineOptions = new PolylineOptions();
        tracesFileNames = new Link<>();
        ((MainActivity) mContext).setToolbarTitle("轨迹展示");


        return view;
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
        LogUtils.i(tag,"onResume");
        String path = SharePrefrenceUtils.getInstance().getRecentTraceFilePath();

        obtainLocationDataFromFile(path);

//        if (!isDrawed) {
//            isDrawed = true;
//
//
//            isDrawed = true;
//        }
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
//        if (t.isAlive()){
//            LogUtils.i(tag,"t "+t.isAlive());
//            t.stop();
//        }
//        t.start();

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
        try {
            LogUtils.i(tag,"onDestroyView");
            mTraceTxtPath = null;
            clearOverlay();
            mMapView.onDestroy();
            mBaiduMap.clear();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void clearOverlay() {
        mMarkerS = null;
        mMarkerE = null;
        qw.recycle();
        qx.recycle();
    }


}
