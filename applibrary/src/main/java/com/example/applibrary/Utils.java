package com.example.applibrary;

import android.content.Context;

/**
 * Created by data on 2017/9/6.
 */

public class Utils {
    public static Context mContext;

    public static void init(Context context){
        mContext = context;
    }

    public static Context getContext(){
        return mContext;
    }
}
