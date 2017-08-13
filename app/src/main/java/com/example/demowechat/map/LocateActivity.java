package com.example.demowechat.map;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.demowechat.R;
import com.example.demowechat.TraceServiceImpl;
import com.example.demowechat.utils.SharePrefrenceUtils;
import com.xdandroid.hellodaemon.DaemonEnv;
import com.xdandroid.hellodaemon.IntentWrapper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocateActivity extends AppCompatActivity {
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
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mTTitle.setText("行驶轨迹追踪服务");

        intent = new Intent(this, TraceServiceImpl.class);

        if (!SharePrefrenceUtils.getInstance().getNeedLocate()){
            mBtnStopService.setVisibility(View.INVISIBLE);
        }else {
            mBtnStarService.setVisibility(View.INVISIBLE);
            mTTitle.setText("行驶轨迹追踪服务\r\n"+ "后台持续获取位置坐标中···");
        }



//        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> infos = am.getRunningServices(100);
//        for (ActivityManager.RunningServiceInfo info : infos) {
//            String className = info.service.getClassName();
////            LogUtils.i("service","-----"+className);
//            if (className.equals("com.example.demowechat.TraceServiceImpl")) {
//                mBtnStarService.setVisibility(View.INVISIBLE);
//                return;
//            }
//
//        }
//        mBtnStopService.setVisibility(View.INVISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!SharePrefrenceUtils.getInstance().getAddWhiteList()) {
            IntentWrapper.whiteListMatters(this, "行驶轨迹追踪服务的持续运行");
            SharePrefrenceUtils.getInstance().setAddWhiteList(true);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:

                SharePrefrenceUtils.getInstance().setNeedLocate(true);
//                LogUtils.e(tag, "SharePrefrenceUtils" + SharePrefrenceUtils.getInstance().getNeedLocate());

                DaemonEnv.startServiceSafely(intent);
                mBtnStarService.setEnabled(false);
                mBtnStarService.setClickable(false);
//                mBtnStarService.setBackgroundColor(getResources().getColor(R.color.light_grey));
                mBtnStarService.setVisibility(View.INVISIBLE);
                mBtnStopService.setVisibility(View.VISIBLE);

                mTTitle.setText("行驶轨迹追踪服务\r\n"+ "后台持续获取位置坐标中···");
//                bindService(intent,sc,BIND_AUTO_CREATE);

                break;
            case R.id.btn_white:
                IntentWrapper.whiteListMatters(this, "行驶轨迹追踪服务的持续运行");
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

                SharePrefrenceUtils.getInstance().setNeedLocate(false);
//                LogUtils.e(tag, "SharePrefrenceUtils" + SharePrefrenceUtils.getInstance().getNeedLocate());
                TraceServiceImpl.stopService();

                stopService(intent);
                mTTitle.setText("行驶轨迹追踪服务");

                break;
        }
    }

    @OnClick(R.id.scanner_toolbar_back)
    public void onBack(){
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
