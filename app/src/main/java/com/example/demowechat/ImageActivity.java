package com.example.demowechat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;



public class ImageActivity extends AppCompatActivity {


    ImageView imaIv;
    Toolbar toolbar;
    private String TAG = "ImageActivity";
    Context mContext;
    private String imagePath;

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

        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imagepath");
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(imagePath)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        Glide.with(this).load(bitmap).into(imaIv);
        imaIv.setImageBitmap(bitmap);


    }



}
