package com.example.demowechat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.demowechat.utils.LogUtils;
import com.example.demowechat.utils.ToastFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class LatlngFragment extends Fragment {


    private final String tag = "LatlngFragment";
    private List<String> latlngList;

    public LatlngFragment() {
        latlngList = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_latlng, container, false);
    }

    private void obtainLocationDataFromFile(String traceTxtPath) {
        if (TextUtils.isEmpty(traceTxtPath)){
            LogUtils.e(tag,"TraceFilePath = null");
            ToastFactory.showShortToast("TraceFilePath = null");
            return;
        }
        latlngList.clear();
        try {
            File file = new File(traceTxtPath);
            if (file.isFile() && file.exists()) { // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file));// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null) {
//                    if(lineTxt.contains(":")){
//                        String time = lineTxt.substring(0,19);
//                        lineTxt = lineTxt.substring(22,lineTxt.length()-1);
////                        LogUtils.i(tag,time+"____"+lineTxt);
//                    }
//                    String[] split = lineTxt.split("-");
//                    LogUtils.i(tag,split[0]+"____"+split[1]);
                   latlngList.add(lineTxt);
                }
                bufferedReader.close();
                read.close();
            } else {
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
    }

}
