package com.example.demowechat.utils;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.example.demowechat.MyApplication;

/**
 * Created by data on 2017/8/18.
 */

public class AlertDialogUtil {


    public static void showAlertDialog(final AlertListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(MyApplication.getInstance());
        builder.setMessage("确认退出吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.positiveResult(dialog,which);
            }
        });
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });
        builder.create().show();
    }

    public interface AlertListener{
        void positiveResult(DialogInterface dialog, int which);
    }
}
