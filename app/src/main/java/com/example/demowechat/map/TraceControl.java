package com.example.demowechat.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.entity.LocRequest;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.track.DistanceRequest;
import com.baidu.trace.api.track.DistanceResponse;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.LatestPoint;
import com.baidu.trace.api.track.LatestPointRequest;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.QueryCacheTrackResponse;
import com.baidu.trace.api.track.SupplementMode;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.BaseRequest;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.LocationMode;
import com.baidu.trace.model.OnCustomAttributeListener;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.ProcessOption;
import com.baidu.trace.model.PushMessage;
import com.baidu.trace.model.SortType;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TraceLocation;
import com.baidu.trace.model.TransportMode;
import com.example.demowechat.MyApplication;
import com.example.demowechat.utils.LogUtils;
import com.example.demowechat.utils.NetworkUtils;
import com.example.demowechat.utils.ToastFactory;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
     * 轨迹点集合
     */
    private List<LatLng> trackPoints = new ArrayList<>();
    private List<String> trackStr = new ArrayList<>();

    /**
     * 轨迹排序规则
     */
    private SortType sortType = SortType.asc;
    // 分页索引
    private int pageIndex = 1;

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
    public long serviceId = 148936;//这里是申请的鹰眼服务id

    /**
     * Entity标识
     */
    public String entityName = "866146031694122";

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

    private TrackResultListener mListener;
    private TrackStringListener mTSListener;
    private CurrentLatlngListener mLatlngListener;
    private DistanceListener mDistanceListener;
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
    private final String tag = "TraceControl";
    private long mStartTime;
    private long mEndTime;
    private final Calendar calendar;
    private final SimpleDateFormat sdf;

    public static void init(Context context) {
        if (instance == null) {
            instance = new TraceControl(context);
        }
    }

    private TraceControl(Context context) {
        mContext = context;
//        entityName = DeviceInfoUtils.getImei(mContext);
        LogUtils.i(tag,"entity "+entityName);//866146031694122
        initClient();
        mapUtil = MapUtil.getInstance();
        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public static TraceControl getInstance() {
        if (instance == null) {
            instance = new TraceControl(MyApplication.getInstance());
        }
        return instance;
    }

    public void initClient() {
        mClient = new LBSTraceClient(mContext);
        mClient.setLocationMode(LocationMode.Device_Sensors);
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
        initListener();
        historyTrackRequest = new HistoryTrackRequest();
    }

    public void startTrace() {
        mClient.startTrace(TraceControl.getInstance().mTrace, traceListener);
    }

    public void stopTrace() {
        mClient.stopTrace(TraceControl.getInstance().mTrace, traceListener);
    }

    public void startGather() {

        mClient.startGather(traceListener);//开启采集
    }

    public void stopGather() {
        mClient.stopGather(traceListener);
    }

    /**
     * 获取当前位置
     */
    public void getCurrentLocation(CurrentLatlngListener listener) {
        mLatlngListener = listener;
        // 网络连接正常，开启服务及采集，则查询纠偏后实时位置；否则进行实时定位
        if (NetworkUtils.isConnected()
                && trackConf.contains("is_trace_started")
                && trackConf.contains("is_gather_started")
                && trackConf.getBoolean("is_trace_started", false)
                && trackConf.getBoolean("is_gather_started", false)) {
            LatestPointRequest request = new LatestPointRequest(getTag(), serviceId, entityName);
            ProcessOption processOption = new ProcessOption();
            processOption.setRadiusThreshold(Constants.DEFAULT_RADIUS_THRESHOLD);
            processOption.setTransportMode(TransportMode.walking);
            processOption.setNeedDenoise(true);
            processOption.setNeedMapMatch(true);
            request.setProcessOption(processOption);
            // 设置需要抽稀
            processOption.setNeedVacuate(true);
            mClient.queryLatestPoint(request, trackListener);
        } else {
            mClient.queryRealTimeLoc(locRequest, entityListener);
        }
    }

    public void queryRealTimeLocation(CurrentLatlngListener listener){
        mLatlngListener = listener;
        mClient.queryRealTimeLoc(locRequest, entityListener);
    }

    public void queryHistoryTrackPoints(long startTime,long endTime,TrackResultListener listener){
        mListener = listener;
        mStartTime = startTime;
        mEndTime = endTime;
        initHistoryTrackRequest();
        queryHistoryTrack();
    }

    public void queryHistoryTrackPoints(long startTime,long endTime,TrackStringListener listener){
        mTSListener = listener;
        mStartTime = startTime;
        mEndTime = endTime;
        initHistoryTrackRequest();
        queryHistoryTrack();
    }

    public void queryHistoryTrackPoints(boolean mapMatch,long startTime,long endTime,TrackResultListener listener){
        mListener = listener;
        mStartTime = startTime;
        mEndTime = endTime;
        initHistoryTrackRequest(mapMatch);
        queryHistoryTrack();
    }

    public void queryHistoryTrack() {

        initRequest(historyTrackRequest);
        mClient.queryHistoryTrack(historyTrackRequest, trackListener);
    }

    public void queryDistance(long startTime,long endTime,DistanceListener listener) {
        LogUtils.i(tag,"dt "+new DateTime(startTime).toString("yyyy-MM-dd HH:mm:ss"));
        LogUtils.i(tag,"dt1 "+new DateTime(endTime).toString("yyyy-MM-dd HH:mm:ss"));
        LogUtils.i(tag,startTime+"");
        LogUtils.i(tag,endTime+"");
        mDistanceListener = listener;
        DistanceRequest distanceRequest = new DistanceRequest(getTag(), serviceId, entityName);
        distanceRequest.setStartTime(startTime);// 设置开始时间
        distanceRequest.setEndTime(endTime);// 设置结束时间
        distanceRequest.setProcessed(true);// 纠偏
        ProcessOption processOption = new ProcessOption();// 创建纠偏选项实例
        processOption.setNeedDenoise(true);// 去噪
        processOption.setNeedMapMatch(true);// 绑路
        processOption.setTransportMode(TransportMode.walking);// 交通方式为步行
        distanceRequest.setProcessOption(processOption);// 设置纠偏选项
        distanceRequest.setSupplementMode(SupplementMode.driving);// 里程填充方式为无
        mClient.queryDistance(distanceRequest, trackListener);// 查询里程

    }

    private void initHistoryTrackRequest(){
        ProcessOption processOption = new ProcessOption();
        processOption.setRadiusThreshold(Constants.DEFAULT_RADIUS_THRESHOLD);//精度
        processOption.setTransportMode(TransportMode.walking);
        processOption.setNeedDenoise(true);//去燥
        processOption.setNeedMapMatch(true);//绑路
        processOption.setNeedVacuate(true);//抽稀
        LogUtils.i(tag,"needMapMatch "+true);
        historyTrackRequest.setProcessOption(processOption);
        historyTrackRequest.setProcessed(true);//纠偏
        historyTrackRequest.setEntityName(entityName);
        historyTrackRequest.setStartTime(mStartTime);
        historyTrackRequest.setEndTime(mEndTime);
        historyTrackRequest.setPageIndex(pageIndex);
        historyTrackRequest.setPageSize(Constants.PAGE_SIZE);
        historyTrackRequest.setSupplementMode(SupplementMode.driving);//里程填充 无
        historyTrackRequest.setSortType(SortType.asc);//排序规则
        historyTrackRequest.setCoordTypeOutput(CoordType.bd09ll);
    }

    private void initHistoryTrackRequest(boolean needMapMatch){
        ProcessOption processOption = new ProcessOption();
        processOption.setRadiusThreshold(Constants.DEFAULT_RADIUS_THRESHOLD);//精度
        processOption.setTransportMode(TransportMode.walking);
        processOption.setNeedDenoise(true);//去燥
        processOption.setNeedMapMatch(needMapMatch);//绑路
        processOption.setNeedVacuate(true);//抽稀
        LogUtils.i(tag,"needMapMatch "+needMapMatch);
        historyTrackRequest.setProcessOption(processOption);
        historyTrackRequest.setProcessed(true);//纠偏
        historyTrackRequest.setEntityName(entityName);
        historyTrackRequest.setStartTime(mStartTime);
        historyTrackRequest.setEndTime(mEndTime);
        historyTrackRequest.setPageIndex(pageIndex);
        historyTrackRequest.setPageSize(Constants.PAGE_SIZE);
        historyTrackRequest.setSupplementMode(SupplementMode.driving);//里程填充 无
        historyTrackRequest.setSortType(SortType.asc);//排序规则
        historyTrackRequest.setCoordTypeOutput(CoordType.bd09ll);
    }

    private void initListener() {

        trackListener = new OnTrackListener() {

            @Override
            public void onDistanceCallback(DistanceResponse response) {
                super.onDistanceCallback(response);
                LogUtils.i(tag,response.toString());
                mDistanceListener.onObtainDistance(response.getDistance());
            }

            @Override
            public void onQueryCacheTrackCallback(QueryCacheTrackResponse queryCacheTrackResponse) {
                super.onQueryCacheTrackCallback(queryCacheTrackResponse);

            }

            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse response) {
                super.onHistoryTrackCallback(response);
                try {
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
                                    if (mListener!=null) {
                                        trackPoints.add(MapUtil.convertTrace2Map(trackPoint.getLocation()));
                                    }
                                    if (mTSListener!=null) {
                                        calendar.setTimeInMillis(trackPoint.getLocTime()*1000);

                                        trackStr.add(sdf.format(calendar.getTime()) + "\t\n" + trackPoint.getLocation().getLongitude() + "-" + trackPoint.getLocation().getLatitude());
                                    }
                                }
                            }
                            ToastFactory.showShortToast("points " + points.size());
                            if (mListener!=null) {
                                mListener.onObtainTrackPointsList(trackPoints, points);
                            }
                            if (mTSListener!=null) {
                                mTSListener.onObtainTrackStringList(trackStr);
                            }
                        }
                    }
                    //查找下一页数据
                    if (total > Constants.PAGE_SIZE * pageIndex) {
                        Thread.sleep(80);
                        historyTrackRequest.setPageIndex(++pageIndex);
                        queryHistoryTrack();
                    }else {
                        if (mListener!=null) {
                            mListener.onComplete();
                        }
                    }
                }catch (Exception e){
                    LogUtils.i(tag,"error "+e.getMessage());}

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
                    Log.i("OnTrackListener", currentLatLng.longitude + "-" + currentLatLng.latitude);
                    if (firstLocate) {
                        firstLocate = false;
                        ToastFactory.showShortToast("起点获取中，请稍后...");
                        return;
                    }

                    //当前经纬度
                    CurrentLocation.locTime = point.getLocTime();
                    CurrentLocation.latitude = currentLatLng.latitude;
                    CurrentLocation.longitude = currentLatLng.longitude;
                    mLatlngListener.onObtainCurrentLatlng(CurrentLocation.longitude,CurrentLocation.latitude,CurrentLocation.locTime);

                    Log.i("current trackPoint", currentLatLng.longitude + "-" + currentLatLng.latitude);
//                    trackPoints.add(currentLatLng);
//
//                    mapUtil.drawHistoryTrack(trackPoints, false, mCurrentDirection);//时时动态的画出运动轨迹
                } catch (Exception e) {
                    LogUtils.i(tag,"error "+e.getMessage());
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

                    mLatlngListener.onObtainCurrentLatlng(CurrentLocation.longitude,CurrentLocation.latitude,CurrentLocation.locTime);
//                    if (null != mapUtil) {
//                        mapUtil.updateMapLocation(currentLatLng, mCurrentDirection);//显示当前位置
//                        mapUtil.animateMapStatus(currentLatLng);//缩放
//                    }

                } catch (Exception x) {

                }


            }

        };

    }

    public void initTraceListener() {
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

    public void resetTrackResultListener(){
        mListener = null;
    }

    public void resetTrackStringListener(){
        mTSListener = null;
    }

    public void resetCurrentLatlngListener(){
        mLatlngListener = null;
    }

    /**
     * 清除Trace状态：初始化app时，判断上次是正常停止服务还是强制杀死进程，根据trackConf中是否有is_trace_started字段进行判断。
     * <p>
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

    public interface TrackResultListener{
        void onObtainTrackPointsList(List trackList,List<TrackPoint> trackPointList);
        void onComplete();
    }

    public interface TrackStringListener{
        void onObtainTrackStringList(List<String> trackList);
    }

    public interface CurrentLatlngListener{
        void onObtainCurrentLatlng(double longitude,double latitude,long time);
    }

    public interface DistanceListener{
        void onObtainDistance(double distance);
    }
}
