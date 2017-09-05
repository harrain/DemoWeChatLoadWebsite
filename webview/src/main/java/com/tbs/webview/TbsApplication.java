package com.tbs.webview;

import android.app.Application;

/**
 * Created by data on 2017/9/5.
 */

public class TbsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //个人封装，针对升级----开始  腾讯tbs浏览服务
        APIWebviewTBS mAPIWebviewTBS= APIWebviewTBS.getAPIWebview();
        mAPIWebviewTBS.initTbs(getApplicationContext());
        //个人封装，针对升级----结束
    }
}
