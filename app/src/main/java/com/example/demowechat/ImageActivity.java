package com.example.demowechat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


/**
 * 显示大图
 */
public class ImageActivity extends AppCompatActivity {


    ImageView imaIv;
    Toolbar toolbar;
    private String TAG = "ImageActivity";
    Context mContext;
    private String imagePath;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        imaIv = (ImageView) findViewById(R.id.ima_iv);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });
        mContext = this;

        intent = getIntent();
        imagePath = intent.getStringExtra("imagepath");
        Log.e(TAG+"imagepath",imagePath);
//        Bitmap bitmap = null;
        try {
//            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(imagePath)));
//            imaIv.setImageBitmap(bitmap);
            Glide.with(mContext).load(imagePath).into(imaIv);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Glide.with(this).load(bitmap).into(imaIv);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        intent = null;
        imagePath = null;
    }
}
