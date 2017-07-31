package com.example.demowechat.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.demowechat.MyApplication;


/**
 * Created by clawpo on 2017/3/21.
 */

public class SharePrefrenceUtils {
    private static final String PREFRENCE_NAME = "APP";
    private static final String CAMERA_TURN = "cameraId";
    private static SharePrefrenceUtils instance;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public SharePrefrenceUtils() {
        /*sharedPreferences = FuLiCenterApplication.getInstance().
                getSharedPreferences(SHARE_PREFRENCE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();*/
    }

    public static SharePrefrenceUtils getInstance(){
        if (instance==null){
            instance = new SharePrefrenceUtils();
            sharedPreferences = MyApplication.getInstance().
                    getSharedPreferences(PREFRENCE_NAME, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        return instance;
    }

    public void setCameraTurn(int cameraId){
        editor.putInt(CAMERA_TURN,cameraId).commit();
    }

    public int getCameraTurn(){
        return sharedPreferences.getInt(CAMERA_TURN,1);
    }

    public void removeCameraTurn(){
        editor.remove(CAMERA_TURN).commit();
    }

}
