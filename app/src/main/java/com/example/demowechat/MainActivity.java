package com.example.demowechat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.demowechat.diyCamera.ShowPicActivity;
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.CameraUtil;
import com.example.demowechat.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    WebFragment webFragment;
    FragmentManager fm;
    FragmentTransaction fragmentTransaction;
    private Toolbar toolbar;
    private Uri imageUri;
    private PopupWindow pw;
    private String time;
    private File outputImage;

    private boolean isLoad ;

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
        webFragment = new WebFragment();
        fm = getSupportFragmentManager();
        fragmentTransaction = fm.beginTransaction();

        fragmentTransaction.add(R.id.fl, converf);
        fragmentTransaction.show(converf);
        fragmentTransaction.commit();

        isLoad = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isLoad) {
            loadFromLocal();
        }
        converf.notifyDataSetChanged();
    }

    /**
     * 应用启动后从本地读取保存的照片，显示到列表上
     */
    private void loadFromLocal() {
        converf.clearPics();

        File cacheDir = new File(AppConstant.KEY.IMG_DIR);
        Log.e(TAG, "file:" + cacheDir.getAbsolutePath());
        if (!cacheDir.exists()) {
            setToolbarTitle();
            return;
        }
        File[] files = cacheDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                Uri uri = Uri.fromFile(file);
                converf.loadData(uri, parseTime(file.getName()), caculateFileSize(file.length()),parseLocation(file.getName()));
//                Log.e(TAG, "pic:" + uri.getPath());

            }

        }
        setToolbarTitle();//更新mainactivity的标题
        converf.notifyDataSetChanged();
        isLoad = true;
    }

    /**
     * 计算文件大小
     *
     * @param length
     * @return
     */
    private String caculateFileSize(long length) {
        return Formatter.formatFileSize(mContext, length);
    }

    /**
     * 从文件名中解析出时间点
     */
    private String parseTime(String fileName) {
//        Log.e(TAG, "parseTime:" + fileName.substring(0, 19));
        return fileName.substring(0, 19);

    }

    private String parseLocation(String name) {
        if (name.indexOf(".jpeg") == 19){
            return "";
        }else {
//            LogUtils.i("parseLocation",name.indexOf(".jpeg")+"");
//            LogUtils.i("parseLocation",name.substring(20,name.indexOf(".jpeg")));
            return name.substring(20,name.indexOf(".jpeg"));
        }
    }

    public void add(View v) {
        View popupView = View.inflate(mContext, R.layout.popupview_add_menu, null);
        LinearLayout ll = (LinearLayout) popupView.findViewById(R.id.scan_own);
        LinearLayout captureNow = (LinearLayout) popupView.findViewById(R.id.capture_now);
        pw = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pw.showAsDropDown(v,  0, 0,Gravity.BOTTOM);

        }else {
            pw.showAsDropDown(popupView);
        }

        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_REQUEST);
//                capture();
                captureByDIYCamera();
                pw.dismiss();
            }
        });
        captureNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraUtil.getInstance().camera(MainActivity.this,800);
                pw.dismiss();
            }
        });
    }

    private void captureByDIYCamera() {
        CameraUtil.getInstance().camera(MainActivity.this);
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

    public void setToolbarTitle() {
        getSupportActionBar().setTitle("记录(" + converf.getImageCount() + ")");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        goToShowPic(requestCode, resultCode, data);

        updateAdapterData(requestCode, resultCode,data);
    }

    private void updateAdapterData(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppConstant.REQUEST_CODE.SHOW_PIC && resultCode == RESULT_OK && data != null){
            Uri uri = Uri.parse(data.getStringExtra(AppConstant.KEY.IMG_PATH));
            LogUtils.i("AppConstant.KEY.IMG_PATH",data.getStringExtra(AppConstant.KEY.IMG_PATH));
            String picTime = data.getStringExtra(AppConstant.KEY.PIC_TIME);
            String longitude = data.getStringExtra(AppConstant.KEY.LONGITUDE);
            String latitude = data.getStringExtra(AppConstant.KEY.LATITUDE);
            LogUtils.i("updateAdapterData",longitude+"-"+latitude);
            try {
                converf.addUri(uri, picTime,longitude,latitude);//保存URI到fragment里，并更新adapter的数据源
                getSupportActionBar().setTitle("记录(" + converf.getImageCount() + ")");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    private void goToShowPic(int requestCode, int resultCode, Intent data) {
        if(requestCode == AppConstant.REQUEST_CODE.CAMERA && resultCode == RESULT_OK){
            String img_path = data.getStringExtra(AppConstant.KEY.IMG_PATH);

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
            intent.putExtra(AppConstant.KEY.PIC_TIME,millis);
            startActivityForResult(intent,AppConstant.REQUEST_CODE.SHOW_PIC);
        }
    }


}
