package com.example.demowechat;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 主容器，切换fragment
 */
public class MainActivity extends AppCompatActivity {

    private  final String TAG = "MainActivity";
    private static final int CAMERA_REQUEST = 0;
    ImageButton addBtn;
    Context mContext;
    ImageView pic;
    ConverFragment converf ;
    WebFragment webFragment;
    FragmentManager fm;
    FragmentTransaction fragmentTransaction;
    private Toolbar toolbar;
    private Uri imageUri;
    private PopupWindow pw;
    private String time;

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


    }

    public void add(View v){
        View popupView = View.inflate(mContext,R.layout.popupview_add_menu,null);
        LinearLayout ll = (LinearLayout) popupView.findViewById(R.id.scan_own);
        pw = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pw.showAsDropDown(v, Gravity.BOTTOM,0,0);
        }
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                capture();
            }
        });
    }

    private void capture() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//19个字符串  index : 0-18
        Date date = new Date();
        time = sdf.format(date);
        // 创建File对象，用于存储拍照后的图片
        File outputImage = new File(getExternalCacheDir(), time +"_output_image.jpg");
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
        startActivityForResult(intent,CAMERA_REQUEST);
        pw.dismiss();
    }

    /**
     * 导航按钮1点击事件
     * @param v
     */
    public void front(View v){
        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fl,converf);
        fragmentTransaction.show(converf);
        fragmentTransaction.commit();
    }

    /**
     * 导航按钮2点击事件
     * @param v
     */
    public void search(View v){
        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fl,webFragment);
        fragmentTransaction.show(webFragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK ) {
            Log.e(TAG,"imageUri:"+imageUri);
            try {
                converf.addUri(imageUri,time);//保存URI到fragment里，并更新adapter的数据源

                getSupportActionBar().setTitle("拍照("+converf.getImageCount()+")");

                // 将拍摄的照片显示出来
//                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
//                pic.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
