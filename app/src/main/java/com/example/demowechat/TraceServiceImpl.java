package com.example.demowechat;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.example.demowechat.map.LocationRequest;
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.LogUtils;
import com.example.demowechat.utils.SharePrefrenceUtils;
import com.xdandroid.hellodaemon.AbsWorkService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import io.reactivex.disposables.Disposable;

public class TraceServiceImpl extends AbsWorkService {

    //是否 任务完成, 不再需要服务运行?
    public static boolean sShouldStopService;
    public static Disposable sDisposable;

    private final String tag = "TraceServiceImpl";
    private static BufferedWriter bw;
    private static FileWriter fileWriter;

    public static void stopService() {
        //我们现在不再需要服务运行了, 将标志位置为 true
        sShouldStopService = true;
        //取消对任务的订阅
        if (sDisposable != null) sDisposable.dispose();

        LocationRequest.getInstance().releaseLocate();
        releaseIO();

        //取消 Job / Alarm / Subscription
        cancelJobAlarmSub();

        if (SharePrefrenceUtils.getInstance().getNeedLocate()){
            SharePrefrenceUtils.getInstance().setLocateInterrupt(true);
        }
    }

    /**
     * 是否 任务完成, 不再需要服务运行?
     *
     * @return 应当停止服务, true; 应当启动服务, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean shouldStopService(Intent intent, int flags, int startId) {
        return sShouldStopService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("onCreate");

//        LogUtils.e(tag,"SharePrefrenceUtils"+SharePrefrenceUtils.getInstance().getNeedLocate());
        if (!SharePrefrenceUtils.getInstance().getNeedLocate()) return;

        try {
            String path = AppConstant.TRACE_TXT_PATH;
            LogUtils.i(tag, "trace path---" + path);
            File file = new File(path);

            if (SharePrefrenceUtils.getInstance().getLocateInterrupt()) {
                fileWriter = new FileWriter(file,true);
            }else {
                fileWriter = new FileWriter(file);
            }

            bw = new BufferedWriter(fileWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LocationRequest.getInstance().startLocate(new LocationRequest.BDLocateFinishListener() {
            @Override
            public void onLocateCompleted(String longitude, String latitude) {
                LogUtils.i(tag, "经度：" + longitude + "--" + "纬度：" + latitude);
                saveLocationToLocal(longitude, latitude);
            }
        });
    }

    @Override
    public void startWork(Intent intent, int flags, int startId) {
        System.out.println("startWork");

//        LogUtils.e(tag,"SharePrefrenceUtils"+SharePrefrenceUtils.getInstance().getNeedLocate());
        if (!SharePrefrenceUtils.getInstance().getNeedLocate()) return;

//        if (LocationRequest.getInstance().getmLocClient() == null) {
//            ToastFactory.showLongToast("定位客户端为空");
//            LogUtils.e(tag, "定位客户端为空");
//            return;
//        }
//
//        if (!LocationRequest.getInstance().isStartLocate()) {
//            ToastFactory.showLongToast("定位客户端没有开启");
//            LogUtils.e(tag, "定位客户端没有开启");
//            LocationRequest.getInstance().getmLocClient().start();
//        }

//        sDisposable = Flowable
//                .interval(3, TimeUnit.SECONDS)
//                //取消任务时取消定时唤醒
//                .doOnTerminate(new Action() {
//                    @Override
//                    public void run() throws Exception {
//                        System.out.println("任务中断。");
//                        cancelJobAlarmSub();
//                    }
//                })
//                .subscribe(new Consumer<Long>() {
//                    @Override
//                    public void accept(Long count) throws Exception {
//                        System.out.println("每 3 秒采集一次数据... count = " + count);
//                        if (count > 0 && count % 18 == 0) System.out.println("保存数据到磁盘。 saveCount = " + (count / 18 - 1));
//                    }
//                    });


//        lr = new LocationRequest(MyApplication.getInstance());


    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void saveLocationToLocal(String longitude, String latitude) {
        try {
            bw.write(longitude + "-" + latitude);
            bw.newLine();
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void releaseIO() {
        if (bw == null || fileWriter == null) return;
        try {
            bw.flush();
            bw.close();
            fileWriter.close();
            if (bw != null) {
                bw = null;
            }
            if (fileWriter != null) {
                fileWriter = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stopWork(Intent intent, int flags, int startId) {

        stopService();
    }

    /**
     * 任务是否正在运行?
     *
     * @return 任务正在运行, true; 任务当前不在运行, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean isWorkRunning(Intent intent, int flags, int startId) {
        //若还没有取消订阅, 就说明任务仍在运行.
        return LocationRequest.getInstance().isStartLocate();
//        return sDisposable != null && !sDisposable.isDisposed();
    }

    @Override
    public IBinder onBind(Intent intent, Void v) {
        return null;
    }

    @Override
    public void onServiceKilled(Intent rootIntent) {
        System.out.println("onServiceKilled。");
    }
}
