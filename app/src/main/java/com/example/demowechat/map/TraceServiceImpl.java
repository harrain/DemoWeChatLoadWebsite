package com.example.demowechat.map;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;

import com.example.demowechat.MyApplication;
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.FileUtil;
import com.example.demowechat.utils.LogUtils;
import com.example.demowechat.utils.NumberValidationUtil;
import com.example.demowechat.utils.SharePrefrenceUtils;
import com.example.demowechat.utils.ToastFactory;
import com.loonggg.lib.alarmmanager.clock.AlarmManagerUtil;
import com.xdandroid.hellodaemon.AbsWorkService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.disposables.Disposable;

public class TraceServiceImpl extends AbsWorkService {

    //是否 任务完成, 不再需要服务运行?
    public static boolean sShouldStopService;
    public static Disposable sDisposable;

    private final String tag = "TraceServiceImpl";
    private static BufferedWriter bw;
    private static FileWriter fileWriter;
    private String path;
    private Date mDate;
    private SimpleDateFormat mSdf;
    private String mLongitude;
    private String mLatitude;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManagerUtil.setAlarm(MyApplication.getInstance(),1,7,0,AppConstant.START_WORK,0);
        AlarmManagerUtil.setAlarm(MyApplication.getInstance(),1,23,59,AppConstant.STOP_WORK,0);
        return super.onStartCommand(intent, flags, startId);
    }

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
        }else {
            SharePrefrenceUtils.getInstance().setLocateInterrupt(false);
        }
    }

    /**
     * 是否 任务完成, 不再需要服务运行?
     *
     * @return 应当停止服务, true; 应当启动服务, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean shouldStopService(Intent intent, int flags, int startId) {
        if (!SharePrefrenceUtils.getInstance().getNeedLocate()) {
            ToastFactory.showShortToast("trace service alive,but locateClent not started");
            LogUtils.i(tag,"trace service alive,but locateClent not started");
        }
        return !SharePrefrenceUtils.getInstance().getNeedLocate();
    }

    @Override
    public void startWork(Intent intent, int flags, int startId) {

        if (bw != null) return;
        System.out.println("startWork");
        //19个字符串  index : 0-18
        mSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            if (SharePrefrenceUtils.getInstance().getLocateInterrupt()){
                path = SharePrefrenceUtils.getInstance().getRecentTraceFilePath();
                File file = FileUtil.createFile(path);
                fileWriter = new FileWriter(file,true);

                if (fileWriter!=null && file != null){
                    ToastFactory.showShortToast("locateInterrupt continue\t\npath is "+ path);
                    LogUtils.i(tag,"locateInterrupt continue\t\npath is "+ path);
                }

            }else {

                Date date = new Date();

                path = AppConstant.TRACES_DIR + File.separator + mSdf.format(date) + ".txt";
                SharePrefrenceUtils.getInstance().setRecentTracePath(path);
                LogUtils.i(tag, "trace path---" + path);
                File file = FileUtil.createFile(path);
                fileWriter = new FileWriter(file);

                if (fileWriter!=null && file != null){
                    ToastFactory.showShortToast("create new file\t\npath is "+ path);
                    LogUtils.i(tag,"create new file\t\npath is "+ path);
                }

            }

            bw = new BufferedWriter(fileWriter);

        } catch (Exception e) {
            LogUtils.e(tag,e.getMessage());
        }

        LocationRequest.getInstance().start(new LocationRequest.BDLocateFinishListener() {
            @Override
            public void onLocateCompleted(String longitude, String latitude) {
                if (NumberValidationUtil.isPositiveDecimal(longitude) && NumberValidationUtil.isPositiveDecimal(latitude))
                {
                    if (!TextUtils.isEmpty(mLongitude) && !TextUtils.isEmpty(mLatitude)){
                        if (mLongitude.equals(longitude) && mLatitude.equals(latitude)){
                            return;
                        }
                    }
                    Intent intent1 = new Intent(AppConstant.LOCATION_BROADCAST);
                    intent1.putExtra("longitude", longitude);
                    intent1.putExtra("latitude", latitude);

                    sendBroadcast(intent1);
                    saveLocationToLocal(longitude, latitude);
                    mLatitude = latitude;
                    mLongitude = longitude;
                }
            }
        });
        SharePrefrenceUtils.getInstance().setLocateInterrupt(true);
//        LocationRequest.getInstance().startLocate(new LocationRequest.BDLocateFinishListener() {
//            @Override
//            public void onLocateCompleted(String longitude, String latitude) {
//                Intent intent1 = new Intent(AppConstant.LOCATION_BROADCAST);
//                intent1.putExtra("longitude",longitude);
//                intent1.putExtra("latitude",latitude);
//                sendBroadcast(intent1);
//                saveLocationToLocal(longitude, latitude);
//            }
//        },60*1000);

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
        if (bw !=null){
            ToastFactory.showShortToast("write to local : "+longitude+"-"+latitude);
            LogUtils.i(tag, "write to local : "+"经度：" + longitude + "--" + "纬度：" + latitude);
        }else {
            ToastFactory.showShortToast("buffered writer is null");
            LogUtils.e(tag,"buffered writer is null");
            return;
        }

        try {

            Date date = new Date();
            if (mDate!=null) {
                if (date.getTime() - mDate.getTime() < 58000) {
                    return;
                }
            }

            mDate = date;
            bw.write(mSdf.format(date)+"   ");

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
        ToastFactory.showShortToast("work is stoped");
        LogUtils.w(tag,"work is stoped");
        stopService();
    }

    /**
     * 任务是否正在运行?
     *
     * @return 任务正在运行, true; 任务当前不在运行, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean isWorkRunning(Intent intent, int flags, int startId) {
        if (!LocationRequest.getInstance().isStartLocate()){
            LogUtils.i(tag,"locateClent is not started");
        }
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
        ToastFactory.showShortToast("trace service is killed");
        System.out.println("onServiceKilled。");
    }
}
