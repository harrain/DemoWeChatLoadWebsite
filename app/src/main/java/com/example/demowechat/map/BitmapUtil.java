package com.example.demowechat.map;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.example.demowechat.R;

public class BitmapUtil {

    public static BitmapDescriptor bmArrowPoint = null;

    public static BitmapDescriptor bmStart = null;

    public static BitmapDescriptor bmEnd = null;

    /**
     * 创建bitmap，在MainActivity onCreate()中调用
     */
    public static void init() {
        bmArrowPoint = BitmapDescriptorFactory.fromResource(R.drawable.icon_point);
        bmStart = BitmapDescriptorFactory.fromResource(R.drawable.qw);
        bmEnd = BitmapDescriptorFactory.fromResource(R.drawable.qx);
    }

    /**
     * 回收bitmap，在MainActivity onDestroy()中调用
     */
    public static void clear() {
        bmArrowPoint.recycle();
        bmStart.recycle();
        bmEnd.recycle();
    }
}
