package com.example.demowechat.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.entity.LocRequest;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.LatestPoint;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.QueryCacheTrackResponse;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.BaseRequest;
import com.baidu.trace.model.OnCustomAttributeListener;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.ProcessOption;
import com.baidu.trace.model.PushMessage;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TraceLocation;
import com.baidu.trace.model.TransportMode;
import com.example.demowechat.MyApplication;
import com.example.demowechat.utils.DeviceInfoUtils;
import com.example.demowechat.utils.NetworkUtils;
import com.example.demowechat.utils.ToastFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by data on 2017/8/24.
 */

public class TraceControl {

    /**
     * 轨迹客户端
     */
    public LBSTraceClient mClient = null;

    public Context mContext = null;

    /**
     * 轨迹服务
     */
    public Trace mTrace = null;

    /**
     * 轨迹服务ID
     */
    public long serviceId = 148904;//这里是申请的鹰眼服务id

    /**
     * Entity标识
     */
    public String entityName = "baidumaptrace";

    public boolean isRegisterReceiver = false;

    /**
     * 服务是否开启标识
     */
    public boolean isTraceStarted = false;

    /**
     * 采集是否开启标识
     */
    public boolean isGatherStarted = false;

    private LocRequest locRequest = null;

    public SharedPreferences trackConf = null;

    private AtomicInteger mSequenceGenerator = new AtomicInteger();

    private static TraceControl instance;
    private boolean firstLocate = true;
    private MapUtil mapUtil;
    private LocationRequest.BDLocateFinishListener mListener;

    /**
     * 轨迹服务监听器
     */
    private OnTraceListener traceListener = null;

    /**
     * 轨迹监听器(用于接收纠偏后实时位置回调)
     */
    private OnTrackListener trackListener = null;

    /**
     * Entity监听器(用于接收实时定位回调)
     */
    private OnEntityListener entityListener = null;

    /**
     * 打包周期
     */
    public int packInterval = Constants.DEFAULT_PACK_INTERVAL;
    private HistoryTrackRequest historyTrackRequest;

    public static void init(Context context){
        if (instance == null){
            instance = new TraceControl(context);
        }
    }

    private TraceControl(Context context){
        mContext = context;
        entityName = DeviceInfoUtils.getImei(mContext);
        initClient();
        mapUtil = MapUtil.getInstance();
    }

    public static TraceControl getInstance(){
        if (instance == null){
            instance = new TraceControl(MyApplication.getInstance());
        }
        return instance;
    }

    public void initClient(){
        mClient = new LBSTraceClient(mContext);
        mTrace = new Trace(serviceId, entityName);

        trackConf = mContext.getSharedPreferences("track_conf", MODE_PRIVATE);
        locRequest = new LocRequest(serviceId);

        mClient.setOnCustomAttributeListener(new OnCustomAttributeListener() {
            @Override
            public Map<String, String> onTrackAttributeCallback() {
                Map<String, String> map = new HashMap<>();
                map.put("key1", "value1");
                map.put("key2", "value2");
                return map;
            }
        });
        mClient.setInterval(Constants.DEFAULT_GATHER_INTERVAL, packInterval);
        initTraceListener();
        historyTrackRequest = new HistoryTrackRequest();
    }

    public void startTrace(){
        mClient.startTrace(TraceControl.getInstance().mTrace, traceListener);
    }

    public void stopTrace(){
        mClient.stopTrace(TraceControl.getInstance().mTrace, traceListener);
    }

    public void startGather(LocationRequest.BDLocateFinishListener listener){
        mListener = listener;
        mClient.startGather(traceListener);//开启采集
    }

    public void stopGather(){
        mClient.stopGather(traceListener);
    }

    /**
    * 获取当前位置
    */
    public void getCurrentLocation(OnEntityListener entityListener, OnTrackListener trackListener) {
        // 网络连接正常，开启服务及采集，则查询纠偏后实时位置；否则进行实时定位
        if (NetworkUtils.isConnected()
                && trackConf.contains("is_trace_started")
                && trackConf.contains("is_gather_started")
                && trackConf.getBoolean("is_trace_started", false)
                && trackConf.getBoolean("is_gather_started", false)) {
//            LatestPointRequest request = new LatestPointRequest(getTag(), serviceId, entityName);
            ProcessOption processOption = new ProcessOption();
            processOption.setRadiusThreshold(50);
            processOption.setTransportMode(TransportMode.walking);
            processOption.setNeedDenoise(true);
            processOption.setNeedMapMatch(true);
//            request.setProcessOption(processOption);
            // 设置需要抽稀
            processOption.setNeedVacuate(true);
//            mClient.queryLatestPoint(request, trackListener);
            int startTime = (int)(System.currentTimeMillis()/1000);
            int endTime = startTime+60;
            // 分页索引
            int pageIndex = 1;
            historyTrackRequest.setProcessOption(processOption);
            historyTrackRequest.setEntityName(entityName);
            historyTrackRequest.setStartTime(startTime);
            historyTrackRequest.setEndTime(endTime);
            historyTrackRequest.setPageIndex(pageIndex);
            historyTrackRequest.setPageSize(Constants.PAGE_SIZE);
            mClient.queryHistoryTrack(historyTrackRequest,trackListener);
        } else {}
    }

    private void initListener() {

        trackListener = new OnTrackListener() {

            @Override
            public void onQueryCacheTrackCallback(QueryCacheTrackResponse queryCacheTrackResponse) {
                super.onQueryCacheTrackCallback(queryCacheTrackResponse);

            }

            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse response) {
                super.onHistoryTrackCallback(response);
                int total = response.getTotal();
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    ToastFactory.showShortToast(response.getMessage());
                } else if (0 == total) {
                    ToastFactory.showShortToast("no_track_data");
                } else {
                    List<TrackPoint> points = response.getTrackPoints();
                    if (null != points) {
                        for (TrackPoint trackPoint : points) {
                            if (!CommonUtil.isZeroPoint(trackPoint.getLocation().getLatitude(),
                                    trackPoint.getLocation().getLongitude())) {
                                ToastFactory.showShortToast(""+MapUtil.convertTrace2Map(trackPoint.getLocation()));
                            }
                        }
                    }
                }
            }

            @Override
            public void onLatestPointCallback(LatestPointResponse response) {
                //经过服务端纠偏后的最新的一个位置点，回调
                try {
                    if (StatusCodes.SUCCESS != response.getStatus()) {
                        return;
                    }

                    LatestPoint point = response.getLatestPoint();
                    if (null == point || CommonUtil.isZeroPoint(point.getLocation().getLatitude(), point.getLocation()
                            .getLongitude())) {
                        return;
                    }

                    LatLng currentLatLng = mapUtil.convertTrace2Map(point.getLocation());
                    if (null == currentLatLng) {
                        return;
                    }
                    Log.i("OnTrackListener",currentLatLng.longitude+"-"+currentLatLng.latitude);
                    if(firstLocate){
                        firstLocate = false;
                        ToastFactory.showShortToast("起点获取中，请稍后...");
                        return;
                    }

                    //当前经纬度
                    CurrentLocation.locTime = point.getLocTime();
                    CurrentLocation.latitude = currentLatLng.latitude;
                    CurrentLocation.longitude = currentLatLng.longitude;


                    Log.i("to add trackPoint",currentLatLng.longitude+"-"+currentLatLng.latitude);
//                    trackPoints.add(currentLatLng);
//
//                    mapUtil.drawHistoryTrack(trackPoints, false, mCurrentDirection);//时时动态的画出运动轨迹
                } catch (Exception x) {

                }


            }
        };

        entityListener = new OnEntityListener() {

            @Override
            public void onReceiveLocation(TraceLocation location) {
                //本地LBSTraceClient客户端获取的位置
                try {
                    if (StatusCodes.SUCCESS != location.getStatus() || CommonUtil.isZeroPoint(location.getLatitude(),
                            location.getLongitude())) {
                        return;
                    }
                    LatLng currentLatLng = mapUtil.convertTraceLocation2Map(location);
                    if (null == currentLatLng) {
                        return;
                    }
                    CurrentLocation.locTime = CommonUtil.toTimeStamp(location.getTime());
                    CurrentLocation.latitude = currentLatLng.latitude;
                    CurrentLocation.longitude = currentLatLng.longitude;

//                    if (null != mapUtil) {
//                        mapUtil.updateMapLocation(currentLatLng, mCurrentDirection);//显示当前位置
//                        mapUtil.animateMapStatus(currentLatLng);//缩放
//                    }

                } catch (Exception x) {

                }


            }

        };

    }

    public void initTraceListener(){
        traceListener = new OnTraceListener() {

            @Override
            public void onBindServiceCallback(int errorNo, String message) {
                ToastFactory.showShortToast(String.format("onBindServiceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            @Override
            public void onStartTraceCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.START_TRACE_NETWORK_CONNECT_FAILED <= errorNo) {
                    TraceControl.getInstance().isTraceStarted = true;
                    SharedPreferences.Editor editor = TraceControl.getInstance().trackConf.edit();
                    editor.putBoolean("is_trace_started", true);
                    editor.apply();

                }
                ToastFactory.showShortToast(String.format("onStartTraceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            @Override
            public void onStopTraceCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.CACHE_TRACK_NOT_UPLOAD == errorNo) {
                    TraceControl.getInstance().isTraceStarted = false;
                    TraceControl.getInstance().isGatherStarted = false;
                    // 停止成功后，直接移除is_trace_started记录（便于区分用户没有停止服务，直接杀死进程的情况）
                    SharedPreferences.Editor editor = TraceControl.getInstance().trackConf.edit();
                    editor.remove("is_trace_started");
                    editor.remove("is_gather_started");
                    editor.apply();

                    firstLocate = true;
                }
                ToastFactory.showShortToast(String.format("onStopTraceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            @Override
            public void onStartGatherCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STARTED == errorNo) {
                    TraceControl.getInstance().isGatherStarted = true;
                    SharedPreferences.Editor editor = TraceControl.getInstance().trackConf.edit();
                    editor.putBoolean("is_gather_started", true);
                    editor.apply();

                }
                ToastFactory.showShortToast(String.format("onStartGatherCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            @Override
            public void onStopGatherCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STOPPED == errorNo) {
                    TraceControl.getInstance().isGatherStarted = false;
                    SharedPreferences.Editor editor = TraceControl.getInstance().trackConf.edit();
                    editor.remove("is_gather_started");
                    editor.apply();

                    firstLocate = true;

                }
                ToastFactory.showShortToast(String.format("onStopGatherCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            @Override
            public void onPushCallback(byte messageType, PushMessage pushMessage) {

            }
        };
    }

    /**
     * 清除Trace状态：初始化app时，判断上次是正常停止服务还是强制杀死进程，根据trackConf中是否有is_trace_started字段进行判断。
     *
     * 停止服务成功后，会将该字段清除；若未清除，表明为非正常停止服务。
     */
    public void clearTraceStatus() {
        if (trackConf.contains("is_trace_started") || trackConf.contains("is_gather_started")) {
            SharedPreferences.Editor editor = trackConf.edit();
            editor.remove("is_trace_started");
            editor.remove("is_gather_started");
            editor.apply();
        }
    }

    /**
     * 初始化请求公共参数
     *
     * @param request
     */
    public void initRequest(BaseRequest request) {
        request.setTag(getTag());
        request.setServiceId(serviceId);
    }

    /**
     * 获取请求标识
     *
     * @return
     */
    public int getTag() {
        return mSequenceGenerator.incrementAndGet();
    }
}
