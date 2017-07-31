package com.example.demowechat.diyCamera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.demowechat.R;
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.LogUtils;

import java.io.File;


public class ShowPicActivity extends Activity {

    private ImageView img;
    private int picWidth;
    private int picHeight;
    private String picTime;

    private LocationManager lm;
    private MyLocationListener mLocationListener;

    private String img_path;
    private String longitude;
    private String latitude;
    private boolean fileModified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pic);

        img_path = getIntent().getStringExtra(AppConstant.KEY.IMG_PATH);

        picWidth = getIntent().getIntExtra(AppConstant.KEY.PIC_WIDTH, 0);
        picHeight = getIntent().getIntExtra(AppConstant.KEY.PIC_HEIGHT, 0);
        picTime = getIntent().getStringExtra(AppConstant.KEY.PIC_TIME);
        img = (ImageView) findViewById(R.id.img);
        img.setImageURI(Uri.parse(img_path));
        img.setLayoutParams(new RelativeLayout.LayoutParams(picWidth, picHeight));

        requestLocation();

    }

    private void requestLocation() {
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            LogUtils.i("locationPermission","禁止");
            return;
        }
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, mLocationListener);
        LogUtils.i("requestLocation","---------------");
    }

    class MyLocationListener implements LocationListener {
        // 当位置发生变化 执行者方法
        @Override
        public void onLocationChanged(Location location) {
            String l = String.valueOf(location.getLongitude());
            longitude = l.substring(0,l.indexOf(".")+3);
            String la = String.valueOf(location.getLatitude());
            latitude = la.substring(0,la.indexOf(".")+3);
            String accuracy = "精度：" + location.getAccuracy() + "\n";
            LogUtils.i("MyLocation Listener","经度："+ longitude +"--"+"纬度："+ latitude +"--"+"精度："+accuracy);
            if (!TextUtils.isEmpty(longitude)&& !TextUtils.isEmpty(latitude)){
                if (!fileModified) {
                    modifyPicName();

                    setResultToMainActivity();

                }
            }
        }
        // 当某一个位置提供者状态发生变化的时候 关闭--》开启 或者开启--》关闭
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) { }
    }

    private void modifyPicName() {

        File file = new File(img_path);
        StringBuilder sb = new StringBuilder();
        sb.append(file.getName());
        sb.insert(19,"-"+longitude+"-"+latitude);
        sb.insert(0,file.getParent()+File.separator);
        LogUtils.i("modifyPicName",sb.toString());
        file.renameTo(new File(sb.toString()));

        fileModified = true;

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

    private void releaseLocationService(){
        lm.removeUpdates(mLocationListener);
        mLocationListener = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseLocationService();
    }
}
