package com.example.demowechat;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.demowechat.map.TrackShowDemo;
import com.example.demowechat.rlPart.BaseAdapter;
import com.example.demowechat.rlPart.LatlngFragmentAdapter;
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.LogUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    private Context mContext;
    private List<TracesType> mTracesData;
    private LatlngFragmentAdapter adapter;

    public LatlngFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_latlng, container, false);
        unbinder = ButterKnife.bind(this, view);
        mContext = getActivity();

        mTracesData = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rv.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        rv.addItemDecoration(dividerItemDecoration);

        adapter = new LatlngFragmentAdapter(mContext, mTracesData);
        adapter.setOnClickListener(new BaseAdapter.OnClickListener() {
            @Override
            public void onShortClick(View v, int position) {
                switch (v.getId()) {
                    case R.id.filenamelist_left_iv:
                        Intent intent = new Intent(mContext, LatlngActivity.class);
                        intent.putExtra("tracePath", mTracesData.get(position).getmFilePath());
                        startActivity(intent);
                        break;
                    case R.id.filenamelist_right_iv:
                        Intent intent1 = new Intent(mContext, TrackShowDemo.class);
                        intent1.putExtra("tracePath", mTracesData.get(position).getmFilePath());
                        startActivity(intent1);
                        break;
                }

            }

            @Override
            public void onLongClick(View v, int position) {

            }
        });
        rv.setAdapter(adapter);

        ((MainActivity) mContext).setToolbarTitle("坐标文件(" + mTracesData.size() + ")");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mTracesData.clear();
        File cacheDir = new File(AppConstant.TRACES_DIR);
        Log.e(tag, "file:" + cacheDir.getAbsolutePath());
        if (!cacheDir.exists()) {
            LogUtils.e(tag, "TRACES_DIR is not existed");
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm:ss");

        File[] files = cacheDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();
//                LogUtils.i(tag,"filename "+name);
                String path = file.getAbsolutePath();
//                if (name.contains("trace")) {
//                    file.renameTo(new File(new StringBuilder(path).insert(path.indexOf(".txt"), "$").toString()));
//                }
                if (name.indexOf(".txt") == 19) {
                    try {

                        String time0 = name.substring(0, 19);
                        String time1 = readLastStr(path).substring(0, 19);

                        long time = sdf.parse(time1).getTime() - sdf.parse(time0).getTime();
                        Date date = new Date(time);
                        LogUtils.i(tag, "date " + date);
                        String s = timeSdf.format(date);
                        String s1 = s.substring(0, 2);
                        String s2 = s.substring(2, s.length());
                        int hour = Integer.parseInt(s1);
                        if (hour >= 8){
                            hour -= 8;
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append(time0.substring(0, 10));
                        sb.append("  ");
                        sb.append("时长 ");
                        String h = null;
                        if (hour < 10) {
                            sb.append("0");
                            h = "0";
                        }
                        h += hour;
                        LogUtils.i(tag,"hour "+hour+" ");
                        sb.append(hour);
                        sb.append(s2);
//                        LogUtils.i(tag,sb.toString());
                        mTracesData.add(new TracesType(path,sb.toString()));
//                        if (!file.getAbsolutePath().equals(SharePrefrenceUtils.getInstance().getRecentTraceFilePath())){
//                            file.renameTo(new File(new StringBuilder(path).insert(path.indexOf(".txt"),"$"+h+s2).toString()));
//                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        adapter.notifyDataSetChanged();
        ((MainActivity) mContext).setToolbarTitle("定位文件(" + mTracesData.size() + ")");
    }

    private String readLastStr(String path) {
        try {
            RandomAccessFile raf = new RandomAccessFile(path, "r");
            long len = raf.length();
            String lastLine = "";
            if (len != 0L) {
                long pos = len - 1;
                while (pos > 0) {
                    pos--;
                    raf.seek(pos);
                    if (raf.readByte() == '\n') {
                        lastLine = raf.readLine();
                        break;
                    }
                }
            }
            raf.close();
            return lastLine;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public class TracesType {
        String mFilePath;
        String mContent;

        public TracesType(String mFilePath, String mContent) {
            this.mFilePath = mFilePath;
            this.mContent = mContent;
        }

        public String getmFilePath() {
            return mFilePath;
        }

        public String getmContent() {
            return mContent;
        }
    }
}
