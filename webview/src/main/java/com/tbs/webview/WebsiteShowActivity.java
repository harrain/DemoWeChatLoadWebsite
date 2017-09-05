package com.tbs.webview;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.applibrary.ToastFactory;


/**
 * 显示网页的activity
 */
public class WebsiteShowActivity extends AppCompatActivity {

    Toolbar toolbar;
    Intent intent;

    private WebView mX5WebView;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        setContentView(R.layout.activity_website_show);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        Log.e("title",intent.getStringExtra("title"));
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("扫描结果");

        url = intent.getStringExtra("text");
//        getSupportActionBar().setTitle(intent.getStringExtra("title"));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        APIWebviewTBS.getAPIWebview().initTBSActivity(this);   //api接口注册二次封装
        mX5WebView = (WebView) findViewById(R.id.ww);
        loadUrl();

//        WebView webView = (WebView) findViewById(R.id.ww);
//        webView.setWebViewClient(new WebViewClient());
//        webView.loadUrl(intent.getStringExtra("text"));
//        Log.e("url",intent.getStringExtra("text"));

    }

    private void loadUrl() {
        WebSettings webSettings = mX5WebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mX5WebView.loadUrl(url);//可以加载短链接 "http://qr18.cn/A4oa1s"
        mX5WebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView var1, int var2, String var3, String var4) {
                ToastFactory.showToast(getApplicationContext(),"网页加载失败");
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mX5WebView.canGoBack()) {
            mX5WebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
