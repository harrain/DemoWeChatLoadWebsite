package com.example.demowechat.map;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.demowechat.R;
import com.example.demowechat.TraceServiceImpl;
import com.xdandroid.hellodaemon.DaemonEnv;
import com.xdandroid.hellodaemon.IntentWrapper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocateActivity extends AppCompatActivity {
    @BindView(R.id.btn_white)
    Button mBtnStartManger;
    @BindView(R.id.btn_start)
    Button mBtnStarService;
    @BindView(R.id.btn_stop)
    Button mBtnStopService;
    private final String tag = "LocateActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        ButterKnife.bind(this);

        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> infos = am.getRunningServices(50);
        for(ActivityManager.RunningServiceInfo info:infos){
            String className = info.service.getClassName();
            if(className.equals("com.example.demowechat.TraceServiceImpl")){
                mBtnStarService.setVisibility(View.INVISIBLE);
                return;
            }

        }
        mBtnStopService.setVisibility(View.INVISIBLE);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                Intent intent = new Intent(this, TraceServiceImpl.class);
                DaemonEnv.startServiceSafely(intent);
                mBtnStarService.setEnabled(false);
                mBtnStarService.setClickable(false);
//                mBtnStarService.setBackgroundColor(getResources().getColor(R.color.light_grey));
                mBtnStarService.setVisibility(View.INVISIBLE);
                mBtnStopService.setVisibility(View.VISIBLE);
//                bindService(intent,sc,BIND_AUTO_CREATE);
                break;
            case R.id.btn_white:
                IntentWrapper.whiteListMatters(this, "轨迹跟踪服务的持续运行");
                mBtnStartManger.setEnabled(false);
                mBtnStartManger.setClickable(false);
                mBtnStartManger.setBackgroundColor(getResources().getColor(R.color.light_grey));
                break;
            case R.id.btn_stop:
                mBtnStopService.setEnabled(false);
                mBtnStopService.setClickable(false);
//                mBtnStopService.setBackgroundColor(getResources().getColor(R.color.light_grey));
                mBtnStarService.setVisibility(View.VISIBLE);
                mBtnStopService.setVisibility(View.INVISIBLE);
                TraceServiceImpl.stopService();

                break;
        }
    }

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }



}
