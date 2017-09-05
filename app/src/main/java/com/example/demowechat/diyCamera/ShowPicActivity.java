package com.example.demowechat.diyCamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.demowechat.R;
import com.example.demowechat.operation.WaterMarkOperation;
import com.example.applibrary.AppConstant;
import com.example.applibrary.LogUtils;


public class ShowPicActivity extends Activity {

    private ImageView img;
    private int picWidth;
    private int picHeight;
    private String picTime;

    private String mImg_path;
    private String mLongitude;
    private String mLatitude;
    private Context mContext;

    private WaterMarkOperation mWaterMarkOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pic);
        mContext = this;
        mImg_path = getIntent().getStringExtra(AppConstant.KEY.IMG_PATH);

        picWidth = getIntent().getIntExtra(AppConstant.KEY.PIC_WIDTH, 0);
        picHeight = getIntent().getIntExtra(AppConstant.KEY.PIC_HEIGHT, 0);
        picTime = getIntent().getStringExtra(AppConstant.KEY.PIC_TIME);
        img = (ImageView) findViewById(R.id.img);
        img.setImageURI(Uri.parse(mImg_path));
        img.setLayoutParams(new RelativeLayout.LayoutParams(picWidth, picHeight));

        mWaterMarkOperation = new WaterMarkOperation(mContext);
        mWaterMarkOperation.setOnFinishListener(new WaterMarkOperation.OnFinishListener() {
            @Override
            public void onfinish(String imgPath, String longitude, String latitude) {
                Message msg = Message.obtain();
                msg.what = 1;
                Bundle bundle = new Bundle();
                bundle.putString("mImg_path",imgPath);
                bundle.putString("mLongitude",longitude);
                bundle.putString("mLatitude",latitude);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
                setResultToMainActivity();
            }
        });
        mWaterMarkOperation.initWaterMark(picTime, mImg_path);
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private void setResultToMainActivity(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
               Message msg = Message.obtain();
                msg.what = 0;

                mHandler.sendMessage(msg);
            }
        }).start();
    }




    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    Intent intent = new Intent();
                    intent.putExtra(AppConstant.KEY.IMG_PATH, mImg_path);
                    intent.putExtra(AppConstant.KEY.PIC_TIME, picTime);
                    intent.putExtra(AppConstant.KEY.LONGITUDE, mLongitude);
                    intent.putExtra(AppConstant.KEY.LATITUDE, mLatitude);

//            String mLongitude = intent.getStringExtra(AppConstant.KEY.LONGITUDE);
//            String mLatitude = intent.getStringExtra(AppConstant.KEY.LATITUDE);
                    LogUtils.i("showpic", mLongitude +"-"+ mLatitude);
                    setResult(AppConstant.RESULT_CODE.RESULT_OK,intent);
                    finish();
                    break;
                case 1:
                    Bundle data = msg.getData();
                    mImg_path = data.getString("mImg_path");
                    mLongitude = data.getString("mLongitude","");
                    mLatitude = data.getString("mLatitude","");
                    img.setImageURI(Uri.parse(mImg_path));
                    break;
            }

        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mWaterMarkOperation.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
