package com.example.demowechat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.demowechat.diyCamera.ShowPicActivity;
import com.example.demowechat.listener.PermissionResultListener;
import com.example.demowechat.map.LocateActivity;
import com.example.demowechat.map.TrackShowDemo;
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.CameraUtil;
import com.example.demowechat.utils.LogUtils;
import com.example.demowechat.utils.NetworkUtils;
import com.example.demowechat.utils.ToastFactory;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.activity.QRCodeCreateActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xdandroid.hellodaemon.IntentWrapper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.functions.Consumer;

/**
 * 主容器，切换fragment
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private static final int CAMERA_REQUEST = 0;
    ImageButton addBtn;
    Context mContext;
    ImageView pic;
    ConverFragment converf;
    TraceFragment webFragment;
    FragmentManager fm;
    FragmentTransaction fragmentTransaction;
    private Toolbar toolbar;
    private Uri imageUri;
    private PopupWindow pw;
    private String time;
    private File outputImage;

    private boolean isLoad;
    private int cameraType = 0;
    private int locateForType = 0;
    private RxPermissions rxPermissions;
    private PermissionResultListener mPermissionResultListener;
    private LatlngFragment mLatlngFragment;
    private DisplayFragment mDeviceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        toolbar = (Toolbar) findViewById(R.id.tb);
        setSupportActionBar(toolbar);

        addBtn = (ImageButton) findViewById(R.id.add_btn);
        pic = (ImageView) findViewById(R.id.pic);

        converf = new ConverFragment();
        webFragment = new TraceFragment();
        mLatlngFragment = new LatlngFragment();
        mDeviceFragment = new DisplayFragment();
        fm = getSupportFragmentManager();
        fragmentTransaction = fm.beginTransaction();

        fragmentTransaction.add(R.id.fl, converf);
        fragmentTransaction.show(converf);
        fragmentTransaction.commit();

        isLoad = false;

        requestPermission(new PermissionResultListener() {
                              @Override
                              public void onHandlePermissionResult(boolean granted) {
                                  if (!granted) {

                                      ToastFactory.showShortToast("您没有授权应用权限，将无法正常使用本应用");
                                      finish();
                                  }
                              }
                          },Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE);


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        converf.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!NetworkUtils.isConnected()){
            Toast.makeText(MyApplication.getInstance(),"室内无法用GPS定位，如果需要，请打开网络连接！",Toast.LENGTH_LONG).show();
        }
    }

    public void requestPermission(final PermissionResultListener listener, final String... permissions) {
        rxPermissions = new RxPermissions(this);
        rxPermissions.request(permissions)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        listener.onHandlePermissionResult(granted);

                    }
                });
    }

    public void add(View v) {
        View popupView = View.inflate(mContext, R.layout.popupview_add_menu, null);
        LinearLayout ll = (LinearLayout) popupView.findViewById(R.id.scan_own);
        final LinearLayout captureNow = (LinearLayout) popupView.findViewById(R.id.capture_now);
        LinearLayout trackDraw = (LinearLayout) popupView.findViewById(R.id.track_draw);
        LinearLayout zxing = (LinearLayout) popupView.findViewById(R.id.qrcode_zxing);
        LinearLayout zxing_create = (LinearLayout) popupView.findViewById(R.id.qrcode_create);
        LinearLayout locate = (LinearLayout) popupView.findViewById(R.id.locate);
        pw = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pw.showAsDropDown(v, 0, 0, Gravity.BOTTOM);

        } else {
            pw.showAsDropDown(popupView);
        }

        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_REQUEST);
//                capture();
                cameraType = 0;
                requestCameraPermission();

            }
        });
        captureNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestPermission(new PermissionResultListener() {
                    @Override
                    public void onHandlePermissionResult(boolean granted) {
                        if (granted){
                            captureByDIYCameraNow();
                        }else {
                            ToastFactory.showShortToast("应用没有获得拍照、定位权限！");
                        }

                    }
                },Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
        trackDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locateForType = 0;
                requestLocationPermissions();
            }
        });
        zxing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraType = 2;
                requestCameraPermission();
            }
        });
        zxing_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, QRCodeCreateActivity.class));
                pw.dismiss();
            }
        });
        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locateForType = 1;
                requestLocationPermissions();
            }
        });
    }

    private void captureByDIYCameraNow() {
        CameraUtil.getInstance().camera(MainActivity.this, 800);
        pw.dismiss();
    }

    private void captureByDIYCamera() {
        CameraUtil.getInstance().camera(MainActivity.this);
        pw.dismiss();
    }

    private void goToSao1Sao(){
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, AppConstant.REQUEST_CODE.ZXING_CODE);
        pw.dismiss();
    }

    private void goToDrawTrace(){
        Intent intent = new Intent(mContext, TrackShowDemo.class);
        startActivity(intent);
        pw.dismiss();
    }

    private void goToLocateActivity(){
        startActivity(new Intent(mContext, LocateActivity.class));
        pw.dismiss();
    }

    public void setToolbarTitle() {
        getSupportActionBar().setTitle("照片(" + converf.getImageCount() + ")");
    }

    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != AppConstant.RESULT_CODE.RESULT_OK) {
            return;
        }

        goToShowPic(requestCode, data);

        updateAdapterData(requestCode, data);

        obtainZXingData(requestCode, data);
    }

    private void obtainZXingData(int requestCode, Intent data) {
        if (requestCode == AppConstant.REQUEST_CODE.ZXING_CODE) {

            if (data == null) {
                ToastFactory.showLongToast("扫描结果为null");
                return;
            }
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("qr_scan_result");

            if (scanResult != null) {
                if (scanResult.contains("http")) {
                    Intent intent = new Intent(mContext, WebsiteShowActivity.class);
                    intent.putExtra("text", scanResult);
                    startActivity(intent);
                    return;
                }

//                try {
//                    ToastFactory.showShortToast(new String(scanResult.getBytes("ISO-8859-1"),"UTF-8"));
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
                ToastFactory.showShortToast(scanResult);
                return;
            }
        }
    }

    private void updateAdapterData(int requestCode, Intent data) {
        if (requestCode == AppConstant.REQUEST_CODE.SHOW_PIC) {

            if (data == null) {
                ToastFactory.showLongToast("拍照数据为null");
                return;
            }
            Uri uri = Uri.parse(data.getStringExtra(AppConstant.KEY.IMG_PATH));
            LogUtils.i(TAG,"AppConstant.KEY.IMG_PATH "+ data.getStringExtra(AppConstant.KEY.IMG_PATH));
            String picTime = data.getStringExtra(AppConstant.KEY.PIC_TIME);
            String longitude = data.getStringExtra(AppConstant.KEY.LONGITUDE);
            String latitude = data.getStringExtra(AppConstant.KEY.LATITUDE);
            LogUtils.i(TAG,"updateAdapterData "+ longitude + "-" + latitude);
            try {
                converf.addUri(uri, picTime, longitude, latitude);//保存URI到fragment里，并更新adapter的数据源
                getSupportActionBar().setTitle("照片(" + converf.getImageCount() + ")");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void goToShowPic(int requestCode, Intent data) {
        if (requestCode == AppConstant.REQUEST_CODE.CAMERA) {
            String img_path = data.getStringExtra(AppConstant.KEY.IMG_PATH);
            LogUtils.i(TAG,"goToShowPic");
            int picWidth = data.getIntExtra(AppConstant.KEY.PIC_WIDTH, 0);
            int picHeight = data.getIntExtra(AppConstant.KEY.PIC_HEIGHT, 0);
            String millis = data.getStringExtra(AppConstant.KEY.PIC_TIME);
/*
            img.setLayoutParams(new RelativeLayout.LayoutParams(picWidth, picHeight));
            img.setImageURI(Uri.parse(img_path));
            */
            Intent intent = new Intent(mContext, ShowPicActivity.class);
            intent.putExtra(AppConstant.KEY.PIC_WIDTH, picWidth);
            intent.putExtra(AppConstant.KEY.PIC_HEIGHT, picHeight);
            intent.putExtra(AppConstant.KEY.IMG_PATH, img_path);
            intent.putExtra(AppConstant.KEY.PIC_TIME, millis);
            startActivityForResult(intent, AppConstant.REQUEST_CODE.SHOW_PIC);
        }
    }

    /**
     * 导航按钮1点击事件
     *
     * @param v
     */
    public void front(View v) {

        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fl, converf);
        fragmentTransaction.show(converf);
        fragmentTransaction.commit();

    }

    public void list(View view) {
        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fl, mLatlngFragment);
        fragmentTransaction.show(mLatlngFragment);
        fragmentTransaction.commit();
    }

    /**
     * 导航按钮2点击事件
     *
     * @param v
     */
    public void search(View v) {
        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fl, webFragment);
        fragmentTransaction.show(webFragment);
        fragmentTransaction.commit();
    }

    public void device(View v){
        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fl, mDeviceFragment);
        fragmentTransaction.show(mDeviceFragment);
        fragmentTransaction.commit();
    }

    //防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀
    @Override
    public void onBackPressed() {
        IntentWrapper.onBackPressed(this);
    }

    public void requestCameraPermission() {

        requestPermission(new PermissionResultListener() {
            @Override
            public void onHandlePermissionResult(boolean granted) {
                if (granted) {
                    switch (cameraType){
                        case 0: captureByDIYCamera(); break;

                        case 2: goToSao1Sao(); break;
                    }
                }else {
                    ToastFactory.showShortToast("没有开通摄像头权限，无法拍照，请前往 应用权限管理 打开授权");
                }
            }
        },Manifest.permission.CAMERA);

    }

    public void requestLocationPermissions(){
        requestPermission(new PermissionResultListener() {
            @Override
            public void onHandlePermissionResult(boolean granted) {
                if (granted){
                    switch (locateForType){
                        case 0: goToDrawTrace(); break;
                        case 1: goToLocateActivity(); break;
                    }
                }else {
                    ToastFactory.showShortToast("没有开通定位权限，无法拍照，请前往 应用权限管理 打开授权");
                }
            }
        },Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * 启动系统相机拍照，存储照片路径设定，得到Uri
     */
    private void capture() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//19个字符串  index : 0-18
        Date date = new Date();
        time = sdf.format(date);
        // 创建File对象，用于存储拍照后的图片
        outputImage = new File(getExternalCacheDir(), time + "_output_image.jpg");
        Log.e(TAG, "file:" + outputImage.getAbsolutePath());
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT < 24) {
            imageUri = Uri.fromFile(outputImage);
        } else {
            imageUri = FileProvider.getUriForFile(MainActivity.this, "com.example.demowechat.fileprovider", outputImage);
        }
        // 启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//        intent.putExtra("camerasensortype", 2); // 调用前置摄像头
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1); // 调用前置摄像头
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        //启动相机程序
        startActivityForResult(intent, CAMERA_REQUEST);

    }

    private void updateAdapterData(int requestCode, int resultCode) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Log.e(TAG, "imageUri:" + imageUri);
            try {
                converf.addUri(imageUri, time);//保存URI到fragment里，并更新adapter的数据源

                getSupportActionBar().setTitle("拍照(" + converf.getImageCount() + ")");

//                // 将拍摄的照片显示出来
//                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
//                pic.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
//            if (outputImage.exists()) {
//                outputImage.delete();
//            }
        }
    }


}
