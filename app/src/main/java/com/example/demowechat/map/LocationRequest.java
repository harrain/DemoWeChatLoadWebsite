package com.example.demowechat.map;

import android.content.Context;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.demowechat.MyApplication;
import com.example.demowechat.utils.LogUtils;

/**
 * baiduMap定位类
 */

public class LocationRequest {

    private Context mContext;
    private static LocationRequest instance;

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener;
    private BDLocateFinishListener listener;
    private String mCurrentLat;
    private String mCurrentLon;
    private String mCurrentAccracy;
    private final String TAG = "BaiduLocationRequest";

    public LocationRequest(Context context){
        mContext = context;
        mLocClient = new LocationClient(mContext);
        myListener = new MyLocationListenner();
        mLocClient.registerLocationListener(myListener);
    }

    public static LocationRequest getInstance(){
        if (instance == null){
            synchronized (LocationRequest.class){
                if (instance == null){
                    instance = new LocationRequest(MyApplication.getInstance());
                }
            }
        }
        return instance;
    }

    public void startLocate(BDLocateFinishListener finishListener){
        listener = finishListener;

        initLocation(-1);
//        LocationClientOption option = new LocationClientOption();
//        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
//        option.setOpenGps(true); // 打开gps
//        option.setCoorType("bd09ll"); // 设置坐标类型
//        option.setScanSpan(1000);
//        mLocClient.setLocOption(option);
        mLocClient.start();

//        mLocClient.requestLocation();
        LogUtils.i("startLocate", "---------------");
    }

    public void startLocate(BDLocateFinishListener finishListener,int timeMillis){
        listener = finishListener;

        initLocation(timeMillis);
        mLocClient.start();
        LogUtils.i("startLocate", "---------------");
    }

    private void initLocation(int timeMillis){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        if (timeMillis < 0) {
            option.setScanSpan(10000);
            //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        }else {
            option.setScanSpan(timeMillis);
        }

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocClient.setLocOption(option);
    }

    /**
     * 定位SDK监听函数, 需实现BDLocationListener里的方法
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null) {
                LogUtils.e(TAG,"location = null");
                return;
            }
            LogUtils.i("BDLocationListener-LocType ",location.getLocType ( )+"");

            mCurrentLat = String.valueOf(location.getLatitude());
            mCurrentLon = String.valueOf(location.getLongitude());
            mCurrentAccracy = String.valueOf(location.getRadius());
            LogUtils.i("BDLocationListener", "经度：" + mCurrentLon + "--" + "纬度：" + mCurrentLat);
            listener.onLocateCompleted(mCurrentLon,mCurrentLat);
        }

        @Override
        public void onConnectHotSpotMessage(String var1, int var2){}
    }

    public boolean isStartLocate(){
        return mLocClient != null && mLocClient.isStarted();
    }

    public LocationClient getmLocClient() {
        return mLocClient;
    }

    public void releaseLocate(){
        if (isStartLocate()) {
            // 退出时销毁定位
            mLocClient.stop();
        }
    }

    public interface BDLocateFinishListener{
        void onLocateCompleted(String longitude,String latitude);
    }
}
