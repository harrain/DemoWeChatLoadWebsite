package com.loonggg.lib.alarmmanager.clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.demowechat.MyApplication;
import com.example.demowechat.map.TraceServiceImpl;
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.SharePrefrenceUtils;

/**
 * Created by loongggdroid on 2016/3/21.
 */
public class LoongggAlarmReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        long intervalMillis = intent.getLongExtra("intervalMillis", 0);
        if (intervalMillis != 0) {
            AlarmManagerUtil.setAlarmTime(context, System.currentTimeMillis() + intervalMillis,
                    intent);
        }
        if (intent.getIntExtra("id", 0) == AppConstant.START_WORK){
            SharePrefrenceUtils.getInstance().setNeedLocate(true);
            MyApplication.getInstance().startService(new Intent(MyApplication.getInstance(), TraceServiceImpl.class));
        }

        if (intent.getIntExtra("id", 0) == AppConstant.STOP_WORK){
            if (SharePrefrenceUtils.getInstance().getNeedLocate()) {
                SharePrefrenceUtils.getInstance().setNeedLocate(false);
                TraceServiceImpl.stopService();
            }
        }


    }


}
