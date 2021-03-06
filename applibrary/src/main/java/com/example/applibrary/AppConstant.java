package com.example.applibrary;

import android.os.Environment;


public class AppConstant {


    public static final int CAPTURE_NOW = 10;
    public static final String  TRACE_TXT_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/" + "trace.txt";
//    public static final String  TRACES_DIR = mContext
//            .getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/" + "traces";
    public static final String  CRASH_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/" + "crashes";
    public static final String  TRACES_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/traces";
    public static final String CAMERA_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/Camera/";
    public static final String GALARY_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/";
    public static final String LOCATION_BROADCAST = "com.example.demowechat.map.latlng";
    public static final int START_WORK = 1;
    public static final int STOP_WORK = 2;

    //WHAT 0-10 预留值
    public interface WHAT {
        int SUCCESS = 0;
        int FAILURE = 1;
        int ERROR = 2;
    }

    public interface KEY{
        String IMG_PATH = "IMG_PATH";
        String VIDEO_PATH = "VIDEO_PATH";
        String PIC_WIDTH = "PIC_WIDTH";
        String PIC_HEIGHT = "PIC_HEIGHT";
        String PIC_TIME = "PIC_TIME";

        String LONGITUDE = "LONGITUDE";
        String LATITUDE = "LATITUDE";
//        String IMG_DIR = "/storage/emulated/0/Android/data/com.example.demowechat/files/DCIM";
        String IMG_DIR = GALARY_DIR+"DemoWeChat";
    }

    public interface REQUEST_CODE {
        int CAMERA = 0;
        int SHOW_PIC = 1;
        int ZXING_CODE = 2;
        int OPEN_GPS = 3;
    }

    public interface RESULT_CODE {
        int RESULT_OK = -1;
        int RESULT_CANCELED = 0;
        int RESULT_ERROR = 1;
    }

}
