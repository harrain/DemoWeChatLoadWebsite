package com.example.applibrary;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by clawpo on 2017/3/21.
 */

public class SharePrefrenceUtils {
    private static final String PREFRENCE_NAME = "APP";
    private static final String CAMERA_TURN = "cameraId";
    private static final String NEED_LOCATE = "need_locate";
    private static final String LOCATE_INTERRUPT = "locate_interrupt";
    private static final String APP_WHITELIST = "app_whiteList";
    private static final String RECENT_TRACE_PATH = "recent_trace_path";

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
            sharedPreferences = Utils.getContext().
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

    public void setNeedLocate(boolean needLocate){ editor.putBoolean(NEED_LOCATE,needLocate).commit();}
    public boolean getNeedLocate(){ return sharedPreferences.getBoolean(NEED_LOCATE,false);}

    public void setLocateInterrupt(boolean isInterrupt){ editor.putBoolean(LOCATE_INTERRUPT,isInterrupt).commit();}
    public boolean getLocateInterrupt(){ return sharedPreferences.getBoolean(LOCATE_INTERRUPT,false);}

    public void setAddWhiteList(boolean isAdded){ editor.putBoolean(APP_WHITELIST,isAdded).commit();}
    public boolean getAddWhiteList() { return sharedPreferences.getBoolean(APP_WHITELIST,false);}

    public void removeCameraTurn(){
        editor.remove(CAMERA_TURN).commit();
    }

    public void setRecentTracePath(String path){ editor.putString(RECENT_TRACE_PATH,path).commit();}

    public String getRecentTraceFilePath() {
        return sharedPreferences.getString(RECENT_TRACE_PATH,null);
    }
}
