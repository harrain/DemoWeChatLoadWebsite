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
import com.example.demowechat.rlPart.base.BaseAdapter;
import com.example.demowechat.rlPart.LatlngFragmentAdapter;
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.LogUtils;
import com.example.demowechat.utils.SharePrefrenceUtils;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.RandomAccessFile;
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

        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        File[] files = cacheDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();
//                LogUtils.i(tag,"filename "+name);
                String path = file.getAbsolutePath();
                try {
                    String time0 = name.substring(0, 19);
                    StringBuilder sb = new StringBuilder();
                    sb.append(time0.substring(0, 10));
                    sb.append("  ");
                    sb.append("时长 ");
                    if (name.indexOf(".txt") == 19) {

                        String time1 = readLastStr(path).substring(0, 19);

                        DateTime dateTime = DateTime.parse(time0, format);
                        DateTime dt1 = DateTime.parse(time1, format);
                        Period p = new Period(dateTime, dt1, PeriodType.time());
                        LogUtils.i(tag, name + "  " + p.getHours() + "-" + p.getMinutes() + "-" + p.getSeconds());
                        Duration d = new Duration(dateTime.getMillis(), dt1.getMillis());
                        String t;
                        if (p.getHours() > 24) {
                            t = new DateTime(d.getMillis()).toString("dd-HH:mm:ss");
                        } else {
                            t = new DateTime(d.getMillis()).toString("HH:mm:ss");
                        }
                        sb.append(t);
                        mTracesData.add(new TracesType(path, sb.toString()));
                        if (!path.equals(SharePrefrenceUtils.getInstance().getRecentTraceFilePath()) && !name.contains("$")) {
                            file.renameTo(new File(new StringBuilder(path).insert(path.indexOf(".txt"), "$" + t).toString()));
                        }


                    } else if (name.contains("$")) {
                        String t = name.substring(name.indexOf("$"), name.indexOf(".txt"));
                        sb.append(t);
                        mTracesData.add(new TracesType(path, t));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
