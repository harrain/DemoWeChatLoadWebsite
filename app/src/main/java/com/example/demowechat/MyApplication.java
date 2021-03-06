package com.example.demowechat;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baidu.baidumap.CommonUtil;
import com.baidu.baidumap.TraceControl;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.example.applibrary.AppConstant;
import com.example.applibrary.LogUtils;
import com.example.applibrary.ToastFactory;
import com.example.applibrary.Utils;
import com.example.demowechat.utils.AppConfig;
import com.example.demowechat.utils.CrashUtils;
import com.example.demowechat.utils.FileUtil;
import com.tbs.webview.APIWebviewTBS;
import com.xdandroid.hellodaemon.DaemonEnv;

/**
 * Created by Wxcily on 15/10/30.
 */
public class MyApplication extends Application {

    //Application单例
    private static MyApplication instance;
    //Context
    private Context context;
    //屏幕尺寸
    private int screenWidth;
    private int screenHeight;

    public String entityName = "baidumaptrace";


    public static MyApplication getInstance() {

        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        context = this.getApplicationContext();

        entityName = CommonUtil.getImei(this);

        // 若为创建独立进程，则不初始化成员变量
        if ("com.baidu.location:location".equals(CommonUtil.getCurProcessName(this))) {
            return;
        }

        if ("com.baidu.track:trace".equals(CommonUtil.getCurProcessName(this))) {
            return;
        }

        if ("com.example.demowechat:watch".equals(CommonUtil.getCurProcessName(this))) {
            return;
        }

//        CrashReport.initCrashReport(this, "900011702", AppConfig.DEBUG);//bugly
        LogUtils.init(this, AppConfig.TAG, AppConfig.DEBUG);//初始化LOG
        ToastFactory.init(context);
        ToastFactory.setIsToast(true);
        Utils.init(context);

        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(getApplicationContext());
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
        TraceControl.init(this);
        TraceControl.getInstance().startTrace();

        //个人封装，针对升级----开始  腾讯tbs浏览服务
        APIWebviewTBS mAPIWebviewTBS= APIWebviewTBS.getAPIWebview();
        mAPIWebviewTBS.initTbs(getApplicationContext());
        //个人封装，针对升级----结束


        CrashUtils.init(FileUtil.createFile(AppConstant.CRASH_DIR));

        Log.e("pro ",getApplicationInfo().processName);
        //需要在 Application 的 onCreate() 中调用一次 DaemonEnv.initialize()
        DaemonEnv.initialize(this, TraceServiceImpl.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        startService(new Intent(this, TraceServiceImpl.class));


//        ActivityManager am = (ActivityManager)getSystemService(context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
//        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
//            Log.e("process",processInfo.processName);
//        }
//        List<ActivityManager.RunningServiceInfo> infos = am.getRunningServices(100);
//        for(ActivityManager.RunningServiceInfo info:infos){
//            String className = info.service.getClassName();
//            if(className.equals(serviceName)){
//
//            }
//        }

//        //12076
//        if (getApplicationInfo().processName.equals("com.example.demowechat:watch")){
//            Log.e("process",getApplicationInfo().processName);
//            return;
//        }
//        Log.e("组件初始化","--------------------------------------");





    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }
}
