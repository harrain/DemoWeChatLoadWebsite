package com.example.demowechat.diyCamera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
import com.example.demowechat.utils.BitmapUtils;
import com.example.demowechat.utils.LogUtils;

import java.io.File;


public class ShowPicActivity extends Activity {

    private ImageView img;
    private int picWidth;
    private int picHeight;
    private String picTime;
    private Bitmap watermarkBitmap;
    private Bitmap bitmap;

    private LocationManager lm;
    private MyLocationListener mLocationListener;

    private String img_path;
    private String longitude;
    private String latitude;
    private boolean fileModified = false;
    private Context mContext;

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
//            String l = String.valueOf(location.getLongitude());
//            longitude = l.substring(0,l.indexOf(".")+3);
//            String la = String.valueOf(location.getLatitude());
//            latitude = la.substring(0,la.indexOf(".")+3);
            longitude = String.valueOf(location.getLongitude());
            latitude = String.valueOf(location.getLatitude());
            String accuracy = "精度：" + location.getAccuracy() + "\n";
            LogUtils.i("MyLocation Listener","经度："+ longitude +"--"+"纬度："+ latitude +"--"+"精度："+accuracy);
            if (!TextUtils.isEmpty(longitude)&& !TextUtils.isEmpty(latitude)){
                if (!fileModified) {
                    modifyPicName();

                    String markText = picTime+"\n"+"经:"+ longitude +"-"+"纬:"+ latitude;
                    LogUtils.i("markText",markText);
                    bitmap = BitmapUtils.getBitemapFromFile(new File(img_path));
                    watermarkBitmap = createWatermark(mContext, bitmap, markText);

                    img.setImageBitmap(watermarkBitmap);
                    img.invalidate();

                    BitmapUtils.saveJPGE_After(mContext, watermarkBitmap, img_path, 100);
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
//        LogUtils.i("modifyPicName",sb.toString());
        file.renameTo(new File(sb.toString()));
        img_path = sb.toString();
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

    /**
     * 添加水印
     *
     * @param context      上下文
     * @param bitmap       原图
     * @param markText     水印文字
     * @return bitmap      打了水印的图
     */
    public  Bitmap createWatermark(Context context, Bitmap bitmap, String markText) {

        // 当水印文字与水印图片都没有的时候，返回原图
        if (TextUtils.isEmpty(markText)) {
            return bitmap;
        }

        String[] split = markText.split("\n");

        // 获取图片的宽高
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        // 创建一个和图片一样大的背景图
        Bitmap bmp = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmp);
        // 画背景图
        canvas.drawBitmap(bitmap, 0, 0, null);
        //-------------开始绘制文字-------------------------------

        // 文字开始的坐标,默认为左上角
        float textX = 0;
        float textY = 0;

        if (!TextUtils.isEmpty(markText)) {
//            LogUtils.i("createWatermark","创建画笔");
            // 创建画笔
            Paint mPaint = new Paint();
            // 文字矩阵区域
            Rect textBounds = new Rect();
            // 获取屏幕的密度，用于设置文本大小
            //float scale = context.getResources().getDisplayMetrics().density;
            // 水印的字体大小
            //mPaint.setTextSize((int) (11 * scale));
            mPaint.setTextSize(20);
            // 文字阴影
            mPaint.setShadowLayer(0.5f, 0f, 1f, Color.BLACK);
            // 抗锯齿
            mPaint.setAntiAlias(true);
            // 水印的区域
            mPaint.getTextBounds(split[0], 0, split[0].length(), textBounds);
            // 水印的颜色
            mPaint.setColor(Color.WHITE);


//            if (textBounds.width() > bitmapWidth / 3 || textBounds.height() > bitmapHeight / 3) {
//                LogUtils.i("createWatermark","文字太大");
//                return bitmap;
//            }

            // 文字开始的坐标
            textX = bitmapWidth - textBounds.width()-10;//这里的-10和下面的+6都是微调的结果
            textY = bitmapHeight - textBounds.height() - 15;
//            LogUtils.i("bitmapWidth",bitmapWidth+"");
//            LogUtils.i("bitmapHeight",bitmapHeight+"");
//            LogUtils.i("textBounds.width",textBounds.width()+"");
//            LogUtils.i("textBounds.height",textBounds.height()+"");
            // 画文字
            canvas.drawText(split[0], textX, textY, mPaint);

            mPaint.getTextBounds(split[1], 0, split[1].length(), textBounds);
            textX = bitmapWidth - textBounds.width()-10;
            textY = textY + 21;
            canvas.drawText(split[1],textX, textY, mPaint);

        }

        //保存所有元素
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
//        LogUtils.i("createWatermark","complete");

        return bmp;
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
        bitmap.recycle();
        watermarkBitmap.recycle();
    }
}
