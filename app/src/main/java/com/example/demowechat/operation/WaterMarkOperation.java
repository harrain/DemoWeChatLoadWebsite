package com.example.demowechat.operation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.baidu.baidumap.LocationRequest;
import com.example.applibrary.LogUtils;
import com.example.applibrary.NumberValidationUtil;
import com.example.applibrary.ProgressDialogUtil;
import com.example.applibrary.ToastFactory;
import com.example.demowechat.utils.BitmapUtils;

import java.io.File;

import static android.content.Context.LOCATION_SERVICE;


/**
 * 地理经纬度数据获取，水印操作
 */

public class WaterMarkOperation {

    private LocationManager lm;
    private MyLocationListener mLocationListener;
    private String mLongitude;
    private String mLatitude;
    private String picTime;
    private String img_path;

    private boolean fileModified = false;

    private Bitmap watermarkBitmap;
    private Bitmap bitmap;
    private Context mContext;
    private OnFinishListener onFinshListener;
   private LocationRequest lr;
    private ProgressDialogUtil mPdu;
    private final  String tag = "WaterMarkOperation";


    public WaterMarkOperation(Context context) {
        mContext = context;
//        lr = new LocationRequest(MyApplication.getInstance());
    }

    public void initWaterMark(String picTime, String img_path) {

        this.picTime = picTime;
        this.img_path = img_path;
//        requestLocation();
        bdLocate();
    }


    public void initWaterMark(String picTime, String img_path,Bitmap bitmap) {

        this.picTime = picTime;
        this.img_path = img_path;
        this.bitmap = bitmap;
//        requestLocation();
        bdLocate();
    }

    private void bdLocate() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ToastFactory.showLongToast("应用没有定位权限！");
            LogUtils.i("locationPermission", "禁止");
            return;
        }

        if (LocationRequest.getInstance().isStartLocate()) {
            LogUtils.i(tag,"isStartLocate "+LocationRequest.getInstance().isStartLocate());
            LocationRequest.getInstance().requestLocation(new LocationRequest.BDLocateOneListener() {
                @Override
                public void onLocateCompleted(String longitude, String latitude) {
                    LogUtils.i("requestLocation", "经度：" + longitude + "--" + "纬度：" + latitude);
                    dataAfterOperation(longitude, latitude);
                }
            });
        }else {
            LogUtils.i(tag,"isStartLocate "+LocationRequest.getInstance().isStartLocate());
            LocationRequest.getInstance().requestLocation(new LocationRequest.BDLocateOneListener() {
                @Override
                public void onLocateCompleted(String longitude, String latitude) {
                    LogUtils.i(tag, "requestLocation "+"经度：" + longitude + "--" + "纬度：" + latitude);
                    dataAfterOperation(longitude, latitude);

                }
            });
        }

    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            LogUtils.i("locationPermission", "禁止");
            return;
        }
        lm = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        mLocationListener = new MyLocationListener();

        Criteria crite = new Criteria();
        crite.setAccuracy(Criteria.ACCURACY_FINE); //精度
        crite.setPowerRequirement(Criteria.POWER_HIGH); //功耗类型选择
        String provider = lm.getBestProvider(crite, true);

        lm.requestLocationUpdates(provider, 0, 0, mLocationListener);
        LogUtils.i("requestLocation", "---------------");
        mPdu = new ProgressDialogUtil(mContext);
        mPdu.createCircleProgressDialog("Tips","正在获取位置信息...");
    }

    class MyLocationListener implements LocationListener {
        // 当位置发生变化 执行者方法
        @Override
        public void onLocationChanged(Location location) {
//            String l = String.valueOf(location.getLongitude());
//            mLongitude = l.substring(0,l.indexOf(".")+3);
//            String la = String.valueOf(location.getLatitude());
//            mLatitude = la.substring(0,la.indexOf(".")+3);
            String longitude = String.valueOf(location.getLongitude());
            String latitude = String.valueOf(location.getLatitude());
            String accuracy = "精度：" + location.getAccuracy() + "\n";
            LogUtils.i("MyLocation Listener", "经度：" + mLongitude + "--" + "纬度：" + mLatitude + "--" + "精度：" + accuracy);
            mPdu.dismissDialog();
            dataAfterOperation(longitude,latitude);
        }

        // 当某一个位置提供者状态发生变化的时候 关闭--》开启 或者开启--》关闭
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    private void dataAfterOperation(String longitude,String latitude) {
        mLongitude = longitude;
        mLatitude = latitude;
        if (!TextUtils.isEmpty(mLongitude) && !TextUtils.isEmpty(mLatitude)) {
            if (!NumberValidationUtil.isPositiveDecimal(longitude) || !NumberValidationUtil.isPositiveDecimal(latitude)){
                if (bitmap == null) {
                    bitmap = BitmapUtils.getBitemapFromFile(new File(img_path));
                }
                BitmapUtils.saveJPGE_After(mContext, bitmap, img_path, 100);
                if (onFinshListener != null) {
                    onFinshListener.onfinish(img_path, "", "");
                }
                return;
            }
            if (!fileModified) {
                modifyPicName();

                String markText = picTime + "\n" + "经:" + mLongitude + "-" + "纬:" + mLatitude;
                LogUtils.i("markText", markText);
                if (bitmap == null) {
                    bitmap = BitmapUtils.getBitemapFromFile(new File(img_path));
                }
                watermarkBitmap = createWatermark(mContext, bitmap, markText);

                BitmapUtils.saveJPGE_After(mContext, watermarkBitmap, img_path, 100);

                watermarkBitmap.recycle();
                bitmap.recycle();

                if (onFinshListener != null) {
                    onFinshListener.onfinish(img_path, mLongitude, mLatitude);
                }

            }
        }
    }

    private void modifyPicName() {


        File file = new File(img_path);
        StringBuilder sb = new StringBuilder();
        sb.append(file.getName());
        sb.insert(19, "-" + mLongitude + "-" + mLatitude);
        sb.insert(0, file.getParent() + File.separator);
//        LogUtils.i("modifyPicName",sb.toString());
        file.renameTo(new File(sb.toString()));
        img_path = sb.toString();
        fileModified = true;

    }

    /**
     * 添加水印
     *
     * @param context  上下文
     * @param bitmap   原图
     * @param markText 水印文字
     * @return bitmap      打了水印的图
     */
    public Bitmap createWatermark(Context context, Bitmap bitmap, String markText) {

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
            textX = bitmapWidth - textBounds.width() - 10;//这里的-10和下面的+6都是微调的结果
            textY = bitmapHeight - textBounds.height() - 15;
//            LogUtils.i("bitmapWidth",bitmapWidth+"");
//            LogUtils.i("bitmapHeight",bitmapHeight+"");
//            LogUtils.i("textBounds.width",textBounds.width()+"");
//            LogUtils.i("textBounds.height",textBounds.height()+"");
            // 画文字
            canvas.drawText(split[0], textX, textY, mPaint);

            mPaint.getTextBounds(split[1], 0, split[1].length(), textBounds);
            textX = bitmapWidth - textBounds.width() - 10;
            textY = textY + 21;
            canvas.drawText(split[1], textX, textY, mPaint);

        }

        //保存所有元素
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
//        LogUtils.i("createWatermark","complete");

        return bmp;
    }

    private void releaseLocationService() {
        lm.removeUpdates(mLocationListener);
        mLocationListener = null;
    }

    public void release() throws Exception{
//        releaseLocationService();
//        lr.releaseLocate();
        LocationRequest.getInstance().stop();
        if(watermarkBitmap!=null) {
            watermarkBitmap.recycle();
        }
        if (bitmap!=null) {
            bitmap.recycle();
        }
    }

    public void setOnFinishListener(OnFinishListener listener) {
        this.onFinshListener = listener;
    }

    public interface OnFinishListener {
        void onfinish(String imgPath, String longitude, String latitude);
    }
}
