package com.example.demowechat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.demowechat.map.LocationDemo;


/**
 * 显示大图
 */
public class ImageActivity extends AppCompatActivity implements View.OnClickListener {


    ImageView imaIv;
    Toolbar toolbar;
    private String TAG = "ImageActivity";
    Context mContext;
    private String imagePath;
    private Intent intent;
    private String longitude;
    private String latitude;
    private TextView geoTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        imaIv = (ImageView) findViewById(R.id.ima_iv);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        geoTv = (TextView) findViewById(R.id.geo_tv);
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
        longitude = intent.getStringExtra("longitude");
        latitude = intent.getStringExtra("latitude");
        Log.e(TAG+"imagepath",imagePath);
//        Bitmap bitmap = null;
        try {
//            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(imagePath)));
//            imaIv.setImageBitmap(bitmap);

            Glide.with(mContext).load(imagePath).into(imaIv);
            geoTv.setText("经:"+longitude+"  "+"纬:"+latitude);
            geoTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
            geoTv.getPaint().setAntiAlias(true);//抗锯齿
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Glide.with(this).load(bitmap).into(imaIv);

        geoTv.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        intent = null;
        imagePath = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.geo_tv){
            Intent intent = new Intent(mContext, LocationDemo.class);
            intent.putExtra("longitude",longitude);
            intent.putExtra("latitude",latitude);
            startActivity(intent);
        }
    }
}
