package com.example.demowechat.map;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.demowechat.R;
import com.example.demowechat.utils.AlertDialogUtil;
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.DeviceInfoUtils;
import com.example.demowechat.utils.GpsUtil;
import com.example.demowechat.utils.LogUtils;
import com.example.demowechat.utils.SharePrefrenceUtils;
import com.xdandroid.hellodaemon.IntentWrapper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocateActivity extends AppCompatActivity implements View.OnClickListener,SensorEventListener{
    @BindView(R.id.btn_white)
    Button mBtnStartManger;
    @BindView(R.id.btn_start)
    Button mBtnStarService;
    @BindView(R.id.btn_stop)
    Button mBtnStopService;
    private final String tag = "LocateActivity";
    @BindView(R.id.scanner_toolbar_back)
    ImageView mTBack;
    @BindView(R.id.scanner_toolbar_title)
    TextView mTTitle;
    @BindView(R.id.titlebar)
    Toolbar toolbar;
    @BindView(R.id.locate_more_iv)
    FloatingActionButton mMoreIv;
    @BindView(R.id.tip_tv)
    TextView tipTv;
    private boolean isSelected = false;
    private List<View> list = new ArrayList<>();

    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private static final int accuracyCircleFillColor = 0xAAFFFF88;//定位光圈填充色
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;//定位光圈边线色
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;//方向信息，顺时针0-360
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;//测量精度，用于定位图标的光圈显示，0则无光圈。

    MapView mMapView;
    BaiduMap mBaiduMap;


    Button requestLocButton;
    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData;
    private boolean isLocated = false;
    private LocationBroadcastReceiver mLBR;
    private Context mContext;
    private int animHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mTTitle.setTextColor(getResources().getColor(android.R.color.black));
        mTTitle.setText("");
        toolbar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mTBack.setImageResource(R.drawable.back_map);
        mContext = this;
        list.add(mBtnStarService);
        list.add(mBtnStartManger);
        list.add(mBtnStopService);
        mMoreIv.setOnClickListener(this);

        locateMap();

        mBtnStopService.setVisibility(View.INVISIBLE);
        mBtnStarService.setVisibility(View.INVISIBLE);
        mBtnStartManger.setVisibility(View.INVISIBLE);

        registerLocationBroadcastReceiver();
        animHeight = DeviceInfoUtils.getScreenHeight(mContext) / 10;
        mTTitle.setText("GPS定位");
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> infos = am.getRunningServices(100);
        for (ActivityManager.RunningServiceInfo info : infos) {
            String className = info.service.getClassName();
//            LogUtils.i("service","-----"+className);
            if (className.equals("com.example.demowechat.map.TraceServiceImpl")) {
                if (!SharePrefrenceUtils.getInstance().getNeedLocate()) {
                    isLocated = true;
                } else {
                    isLocated = false;
                }
                return;
            }

        }
    }

    private void registerLocationBroadcastReceiver() {
        mLBR = new LocationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(AppConstant.LOCATION_BROADCAST);
        registerReceiver(mLBR,intentFilter);
    }

    private void locateMap() {
        requestLocButton = (Button) findViewById(R.id.button1);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取传感器管理服务
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        requestLocButton.setText("普通");
        View.OnClickListener btnClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        requestLocButton.setText("跟随");
                        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.overlook(0);//俯仰角
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        break;
                    case COMPASS:
                        requestLocButton.setText("普通");
                        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder1 = new MapStatus.Builder();
                        builder1.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
                        break;
                    case FOLLOWING:
                        requestLocButton.setText("罗盘");
                        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
                        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        break;
                    default:
                        break;
                }
            }
        };
        requestLocButton.setOnClickListener(btnClickListener);
        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

    }

    private void locationForMap(String longitude, String latitude) {
        if (mMapView == null) {
            return;
        }
        if (TextUtils.isEmpty(longitude) || TextUtils.isEmpty(latitude)){
            tipTv.setVisibility(View.VISIBLE);
            return;
        }
        mCurrentLat = Double.parseDouble(latitude);
        mCurrentLon = Double.parseDouble(longitude);

        locData = new MyLocationData.Builder()
                .accuracy(0)
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(mCurrentDirection).latitude(mCurrentLat)
                .longitude(mCurrentLon).build();
        mBaiduMap.setMyLocationData(locData);

        if (isFirstLoc) {
            isFirstLoc = false;
            LatLng ll = new LatLng(mCurrentLat,
                    mCurrentLon);//经纬度容器.支持写parcel
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(20.0f);//target 设置地图中心点（会显示位置图标） ； zoom 设置缩放级别
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));//animateMapStatus 以动画方式更新地图状态，动画耗时 300 ms
        }
        mTTitle.setText("后台持续获取位置坐标中···");
        tipTv.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        tipTv.setVisibility(View.VISIBLE);
        TraceControl.getInstance().queryRealTimeLocation(new TraceControl.CurrentLatlngListener() {
            @Override
            public void onObtainCurrentLatlng(double longitude, double latitude, long time) {
                locationForMap(String.valueOf(longitude),String.valueOf(latitude));
            }
        });
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        //为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
        if (!SharePrefrenceUtils.getInstance().getAddWhiteList()) {
            IntentWrapper.whiteListMatters(this, "行驶轨迹追踪服务的持续运行");
            SharePrefrenceUtils.getInstance().setAddWhiteList(true);
        }
        if (!GpsUtil.isOpenNetWork(mContext)){
            AlertDialogUtil.showTipDialog(mContext,"网络无法使用","检查网络是否打开");
        }
        if (!GpsUtil.isOpenGPS(mContext)){
            LogUtils.i(tag,"gps no open");
            AlertDialogUtil.showAlertDialogTwo(mContext,"GPS未开启","如果您在室内，点击 [取消] 忽略此消息；在室外需要开启GPS，点击 [确认] 前去开启",new AlertDialogUtil.AlertListener() {
                @Override
                public void positiveResult(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    GpsUtil.goToOpenGPS(LocateActivity.this,AppConstant.REQUEST_CODE.OPEN_GPS);
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstant.REQUEST_CODE.OPEN_GPS ){
            if (resultCode == AppConstant.RESULT_CODE.RESULT_OK){
                LogUtils.i(tag,"opengps ok");
                return;
            }
            LogUtils.i(tag,"opengps no");
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:

                SharePrefrenceUtils.getInstance().setNeedLocate(true);
//                LogUtils.e(tag, "SharePrefrenceUtils" + SharePrefrenceUtils.getInstance().getNeedLocate());
//                mBtnStarService.setEnabled(false);
//                mBtnStarService.setClickable(false);
//                mBtnStarService.setBackgroundColor(getResources().getColor(R.color.light_grey));
                startService(new Intent(this, TraceServiceImpl.class));
                mBtnStarService.setVisibility(View.INVISIBLE);
                mBtnStopService.setVisibility(View.VISIBLE);
//                bindService(intent,sc,BIND_AUTO_CREATE);
                break;
            case R.id.btn_white:
                IntentWrapper.whiteListMatters(this, "后台定位");
//                mBtnStartManger.setEnabled(false);
//                mBtnStartManger.setClickable(false);
                mBtnStartManger.setBackgroundColor(getResources().getColor(R.color.light_grey));
                break;
            case R.id.btn_stop:
//                mBtnStopService.setEnabled(false);
//                mBtnStopService.setClickable(false);
//                mBtnStopService.setBackgroundColor(getResources().getColor(R.color.light_grey));
                mBtnStarService.setVisibility(View.VISIBLE);
                mBtnStopService.setVisibility(View.INVISIBLE);
                SharePrefrenceUtils.getInstance().setNeedLocate(false);
//                LogUtils.e(tag, "SharePrefrenceUtils" + SharePrefrenceUtils.getInstance().getNeedLocate());
                TraceServiceImpl.stopService();
                mTTitle.setText("GPS定位");
//                stopService(intent);
//                mTTitle.setText("行驶轨迹追踪服务");
                break;
            case R.id.locate_more_iv:
                if (!isSelected) {
                    startAnimator();
                } else {
                    endAnimator();
                }
                break;
        }
    }

    private void endAnimator() {
        isSelected = false;
        LogUtils.i(tag,"animHeight "+animHeight);
        for (int i = 0; i < 3; i++) {
            ObjectAnimator translationY = ObjectAnimator.ofFloat(list.get(i), "translationY", -animHeight * (i + 1), 0F);
            translationY.setInterpolator(new AccelerateInterpolator());
            ObjectAnimator alpha = ObjectAnimator.ofFloat(list.get(i), "alpha", 1f, 0);
            AnimatorSet set = new AnimatorSet();
            set.play(translationY).with(alpha);

            final int finalI = i;
            set.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    list.get(finalI).setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            set.setDuration(1000).start();

        }
    }

    private void startAnimator() {
        isSelected = true;


        for (int i = 0; i < 3; i++) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(list.get(i), "translationY", 0F, -animHeight * (i + 1));
            animator.setInterpolator(new AccelerateDecelerateInterpolator());//设置插值器

            ObjectAnimator alpha = ObjectAnimator.ofFloat(list.get(i), "alpha", 0, 1f);
            AnimatorSet set = new AnimatorSet();
            set.play(animator).with(alpha);

            final int finalI = i;
            set.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (isLocated){
                        mBtnStarService.setVisibility(View.VISIBLE);
                        mBtnStartManger.setVisibility(View.VISIBLE);
                    }else {
                        mBtnStopService.setVisibility(View.VISIBLE);
                        mBtnStartManger.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            set.setDuration(1000).start();

        }


    }

    @OnClick(R.id.scanner_toolbar_back)
    public void onBack() {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];//android手机传感器返回的x坐标
        if (Math.abs(x - lastX) > 1.0) { //差的绝对值 > 1 ，修正方向  (x的值为 0 - 360)。
            mCurrentDirection = (int) x;
            Log.e("sensorEvent","mCurrentDirection:"+mCurrentDirection);
            locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
            mBaiduMap.setMyLocationData(locData);
        }
        lastX = x;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onStop() {
        //取消注册传感器监听
        mSensorManager.unregisterListener(this);
        super.onStop();
        TraceControl.getInstance().resetCurrentLatlngListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        unregisterReceiver(mLBR);
    }

    class LocationBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                LogUtils.w(tag,"receiver data null");
                return;
            }
            LogUtils.w(tag,"receiver  "+intent.getStringExtra("longitude")+"-"+intent.getStringExtra("latitude"));
            locationForMap(intent.getStringExtra("longitude"),intent.getStringExtra("latitude"));
        }
    }

}
