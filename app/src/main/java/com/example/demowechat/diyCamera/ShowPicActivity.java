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
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.LogUtils;


public class ShowPicActivity extends Activity {

    private ImageView img;
    private int picWidth;
    private int picHeight;
    private String picTime;

    private String img_path;
    private String longitude;
    private String latitude;
    private Context mContext;

    private WaterMarkOperation mWaterMarkOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pic);
        mContext = this;
        img_path = getIntent().getStringExtra(AppConstant.KEY.IMG_PATH);

        picWidth = getIntent().getIntExtra(AppConstant.KEY.PIC_WIDTH, 0);
        picHeight = getIntent().getIntExtra(AppConstant.KEY.PIC_HEIGHT, 0);
        picTime = getIntent().getStringExtra(AppConstant.KEY.PIC_TIME);
        img = (ImageView) findViewById(R.id.img);
        img.setImageURI(Uri.parse(img_path));
        img.setLayoutParams(new RelativeLayout.LayoutParams(picWidth, picHeight));

        mWaterMarkOperation = new WaterMarkOperation(mContext);
        mWaterMarkOperation.setOnFinishListener(new WaterMarkOperation.OnFinishListener() {
            @Override
            public void onfinish(String imgPath, String longitude, String latitude) {
                ShowPicActivity.this.img_path = imgPath;
                ShowPicActivity.this.longitude = longitude;
                ShowPicActivity.this.latitude = latitude;

                setResultToMainActivity();
            }
        });
        mWaterMarkOperation.initWaterMark(picTime,img_path);
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
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }




    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = new Intent();
            intent.putExtra(AppConstant.KEY.IMG_PATH, img_path);
            intent.putExtra(AppConstant.KEY.PIC_TIME, picTime);
            intent.putExtra(AppConstant.KEY.LONGITUDE,longitude);
            intent.putExtra(AppConstant.KEY.LATITUDE,latitude);

//            String longitude = intent.getStringExtra(AppConstant.KEY.LONGITUDE);
//            String latitude = intent.getStringExtra(AppConstant.KEY.LATITUDE);
            LogUtils.i("showpic",longitude+"-"+latitude);
            setResult(RESULT_OK,intent);
            finish();
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWaterMarkOperation.release();
    }
}
