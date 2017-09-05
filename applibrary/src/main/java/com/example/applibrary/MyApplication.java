package com.example.applibrary;

import android.app.Application;

/**
 * Created by data on 2017/9/5.
 */

public class MyApplication extends Application {

    private static MyApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static MyApplication getInstance() {
        return instance;
    }
}
