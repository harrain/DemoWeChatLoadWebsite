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

import com.example.demowechat.map.TraceControl;
import com.example.demowechat.map.TrackShowDemo;
import com.example.demowechat.rlPart.LatlngFragmentAdapter;
import com.example.demowechat.rlPart.base.BaseAdapter;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    private boolean isEagleEye = false;
    private boolean isOnCreate = false;
    private SimpleDateFormat sdf;
    private Calendar calendar;
    private DateTime[] dt;

    public LatlngFragment() {

    }

    public static LatlngFragment newInstance(boolean eagleEye) {
        LatlngFragment myFragment = new LatlngFragment();

        Bundle args = new Bundle();
        args.putBoolean("isEagleEye", eagleEye);
        myFragment.setArguments(args);

        return myFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_latlng, container, false);
        unbinder = ButterKnife.bind(this, view);
        mContext = getActivity();
        try {isEagleEye = getArguments().getBoolean("isEagleEye",false);
        }catch (Exception e){e.printStackTrace();}

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
                        intent.putExtra("isEagleEye",isEagleEye);
                        intent.putExtra("date",mTracesData.get(position).getDateTime());
                        intent.putExtra("tracePath", mTracesData.get(position).getmFilePath());
                        startActivity(intent);
                        break;
                    case R.id.filenamelist_right_iv:
                        Intent intent1 = new Intent(mContext, TrackShowDemo.class);
                        intent1.putExtra("isEagleEye",isEagleEye);
                        intent1.putExtra("date",mTracesData.get(position).getDateTime());
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

//        ((MainActivity) mContext).setToolbarTitle("坐标文件(" + mTracesData.size() + ")");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isOnCreate) return;
        mTracesData.clear();
        if (isEagleEye){
            loadTrackFromEagleEye();
        }else {
            loadLocalTrackFiles();
            adapter.notifyDataSetChanged();
            ((MainActivity) mContext).setToolbarTitle("定位文件(" + mTracesData.size() + ")");
        }
        isOnCreate = true;
    }

    private void loadTrackFromEagleEye() {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
//        final DateTime[] dts = new DateTime[2];
//
//        dts[0] = DateTime.parse("2017-08-28 07:00",dtf);
//        dts[1] = DateTime.parse("2017-08-28 23:00",dtf);
        dt = new DateTime[1];
        dt[0] = DateTime.parse("2017-08-24",dtf);
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        calendar = Calendar.getInstance();
        calendar.set(2017,7,24,7,0);
        final String[] t = new String[1];
//        t[0] = dts[0].toString("yyyy-MM-dd");
        t[0] = sdf.format(calendar.getTime());
        Long[] dts = new Long[2];
        dts[0] = calendar.getTime().getTime()/1000;
        calendar.set(2017,7,24,23,0);
        dts[1] = calendar.getTime().getTime()/1000;
//        final int[] i = {0};
        getTrack(dts, t);

    }

    private void getTrack(final DateTime[] dts, final String[] t, final int[] i) {
        calendar.setTimeInMillis(dts[0].getMillis());
        long d0 = calendar.getTime().getTime()/1000;
        calendar.setTimeInMillis(dts[1].getMillis());
        long d1 = calendar.getTime().getTime()/1000;
        TraceControl.getInstance().queryDistance(d0
                , d1, new TraceControl.DistanceListener() {
            @Override
            public void onObtainDistance(double distance) {
                LogUtils.i(tag,"dt "+t[0]);
                if (distance >0 ){
                    mTracesData.add(new TracesType(true,t[0],t[0]));
                    adapter.notifyDataSetChanged();
                    ((MainActivity) mContext).setToolbarTitle("鹰眼轨迹(" + mTracesData.size() + ")");
                }

                i[0]++;
//                LogUtils.i(tag,"dt "+t[0]+"-"+dts[1].toString("yyyy-MM-dd HH:mm:ss"));
                if (i[0] <= DateTime.now().getDayOfMonth()){

                    dts[0] = dts[0].plusDays(1);
                    dts[1] = dts[1].plusDays(1);
                    t[0] = dts[0].toString("yyyy-MM-dd");
                    getTrack(dts, t, i);
                }
            }
        });
    }

    private void getTrack(final Long[] dts, final String[] t) {
        TraceControl.getInstance().queryDistance(dts[0],dts[1], new TraceControl.DistanceListener() {
                    @Override
                    public void onObtainDistance(double distance) {
                        LogUtils.i(tag,"dt "+t[0]);
                        if (distance >0 ){
                            mTracesData.add(new TracesType(true,t[0],t[0]));
                            adapter.notifyDataSetChanged();
                            ((MainActivity) mContext).setToolbarTitle("鹰眼轨迹(" + mTracesData.size() + ")");
                        }

                        if (dt[0].isBeforeNow()){
                            dt[0] = dt[0].plusDays(1);
                            calendar.set(dt[0].getYear(),dt[0].getMonthOfYear()-1,dt[0].getDayOfMonth(),7,0);
                            dts[0] = calendar.getTime().getTime()/1000;
                            t[0] = sdf.format(calendar.getTime());
                            calendar.set(dt[0].getYear(),dt[0].getMonthOfYear()-1,dt[0].getDayOfMonth(),23,0);
                            dts[1] = calendar.getTime().getTime()/1000;
                            getTrack(dts, t);
                        }
                    }
                });
    }


    private boolean loadLocalTrackFiles() {
        File cacheDir = new File(AppConstant.TRACES_DIR);
        Log.e(tag, "file:" + cacheDir.getAbsolutePath());
        if (!cacheDir.exists()) {
            LogUtils.e(tag, "TRACES_DIR is not existed");
            return true;
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

                        if (!path.equals(SharePrefrenceUtils.getInstance().getRecentTraceFilePath()) && !name.contains("$")) {
                            file.renameTo(new File(new StringBuilder(path).insert(path.indexOf(".txt"), "$" + t).toString()));
                        }

                    } else if (name.contains("$")) {
                        String t = name.substring(name.indexOf("$")+1, name.indexOf(".txt"));
                        sb.append(t);

                    }
                    mTracesData.add(new TracesType(path, sb.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
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
        isOnCreate = false;
        unbinder.unbind();
    }

    public class TracesType {
        String mFilePath;
        String mContent;

        String dateTime;
        boolean isEagleEye = false;

        public TracesType(String mFilePath, String mContent) {
            this.mFilePath = mFilePath;
            this.mContent = mContent;
        }

        public TracesType(boolean isEagleEye ,String dateTime, String mContent) {
            this.isEagleEye = isEagleEye;
            this.dateTime = dateTime;
            this.mContent = mContent;
        }

        public String getmFilePath() {
            return mFilePath;
        }

        public String getmContent() {
            return mContent;
        }

        public String getDateTime() {
            return dateTime;
        }
    }
}
