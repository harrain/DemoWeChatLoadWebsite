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
        // 创建File对象，用于存储拍照后的图片
        File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
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
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//        intent.putExtra(MediaStore.CA)
        //启动相机程序
        startActivityForResult(intent,CAMERA_REQUEST);
        pw.dismiss();
    }

    public void front(View v){
        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fl,converf);
        fragmentTransaction.show(converf);
        fragmentTransaction.commit();
    }

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
                converf.addUri(imageUri);
                // 将拍摄的照片显示出来
//                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
//                pic.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                Log.e(TAG,"data !=null");
//                handleImageOnKitKat(data);
//            }

        }else if(data == null){
            Log.e(TAG,"data == null");
        }
    }

    /**
     * api 19以后
     *  4.4版本后 调用系统相机返回的不在是真实的uri 而是经过封装过后的uri，
     * 所以要对其记性数据解析，然后在调用displayImage方法尽心显示
     * @param data
     */

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.e("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        displayImage(imagePath); // 根据图片路径显示图片
    }

    /**
     * 通过 uri seletion选择来获取图片的真实uri
     * @param uri
     * @param
     * @return
     */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * 通过imagepath来绘制immageview图像
     * @param imagPath
     */
    private void displayImage(String imagPath){
        Log.e(TAG+":"+"imagePath",imagPath);
        if (imagPath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagPath);

        }else{
            Toast.makeText(this,"图片获取失败",Toast.LENGTH_SHORT).show();
        }
    }
}
