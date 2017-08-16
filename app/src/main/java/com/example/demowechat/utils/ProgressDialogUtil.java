package com.example.demowechat.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

/**
 * Created by data on 2017/8/16.
 */

public class ProgressDialogUtil {

    private Context mContext;
    private ProgressDialog dialog;

    public ProgressDialogUtil(Context context){
        mContext = context;
    }

    public void createCircleProgressDialog(String title,String message){
        dialog = new ProgressDialog(mContext);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
        dialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
//        dialog.setIcon(R.drawable.ic_launcher);//
        // 设置提示的title的图标，默认是没有的，如果没有设置title的话只设置Icon是不会显示图标的
        dialog.setTitle(title);
        // dismiss监听
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // TODO Auto-generated method stub
            }
        });
        // 监听Key事件被传递给dialog
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                return false;
            }
        });
        // 监听cancel事件
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
            }
        });
        //设置可点击的按钮，最多有三个(默认情况下)
//        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // TODO Auto-generated method stub
//                    }
//                });
//        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // TODO Auto-generated method stub
//                    }
//                });
//        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "中立",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // TODO Auto-generated method stub
//                    }
//                });
        dialog.setMessage(message);
        dialog.show();
    }

    public void dismissDialog(){
        try {

            dialog.dismiss();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void createHorizontalProgressDialog(){
        dialog = new ProgressDialog(mContext);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
        dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
//        dialog.setIcon(R.drawable.ic_launcher);// 设置提示的title的图标，默认是没有的
        dialog.setTitle("提示");
        dialog.setMax(100);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "中立",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                });
        dialog.setMessage("这是一个水平进度条");
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                int i = 0;
                while (i < 100) {
                    try {
                        Thread.sleep(200);
                        // 更新进度条的进度,可以在子线程中更新进度条进度
                        dialog.incrementProgressBy(1);
                        // dialog.incrementSecondaryProgressBy(10)//二级进度条更新方式
                        i++;
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
                // 在进度条走完时删除Dialog
                dialog.dismiss();
            }
        }).start();
    }
}
