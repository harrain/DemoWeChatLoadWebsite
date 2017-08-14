package com.example.demowechat;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.demowechat.rlPart.ArrayListAdapter;
import com.example.demowechat.utils.LogUtils;
import com.example.demowechat.utils.SharePrefrenceUtils;
import com.example.demowechat.utils.ToastFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class LatlngFragment extends Fragment {


    private final String tag = "LatlngFragment";
    @BindView(R.id.rv)
    RecyclerView rv;
    Unbinder unbinder;
    private List<String> latlngList;
    private Context mContext;

    public LatlngFragment() {
        latlngList = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_latlng, container, false);
        unbinder = ButterKnife.bind(this, view);
        mContext = getContext();

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rv.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        rv.addItemDecoration(dividerItemDecoration);

        obtainLocationDataFromFile(SharePrefrenceUtils.getInstance().getRecentTraceFilePath());
        ArrayListAdapter adapter = new ArrayListAdapter(mContext,latlngList);
        rv.setAdapter(adapter);
        return view;
    }

    private void obtainLocationDataFromFile(String traceTxtPath) {
        if (TextUtils.isEmpty(traceTxtPath)) {
            LogUtils.e(tag, "TraceFilePath = null");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
