package com.example.demowechat;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * 显示网页的activity
 */
public class WebsiteShowActivity extends AppCompatActivity {

    Toolbar toolbar;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        setContentView(R.layout.activity_website_show);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.e("title",intent.getStringExtra("title"));
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle(intent.getStringExtra("title"));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        WebView webView = (WebView) findViewById(R.id.ww);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(intent.getStringExtra("text"));
        Log.e("url",intent.getStringExtra("text"));

    }
}
