package com.example.demowechat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.demowechat.map.TraceControl;
import com.example.demowechat.rlPart.ArrayListAdapter;
import com.example.demowechat.utils.LogUtils;
import com.example.demowechat.utils.ToastFactory;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LatlngActivity extends AppCompatActivity {

    @BindView(R.id.scanner_toolbar_back)
    ImageView mTBack;
    @BindView(R.id.scanner_toolbar_title)
    TextView mTTitle;
    @BindView(R.id.titlebar)
    Toolbar toolbar;
    @BindView(R.id.latlng_rv)
    RecyclerView mLatlngRv;
    private List<String> latlngList;
    private final String tag = "LatlngActivity";
    private Context mContext;
    private String tracePath;
    private ArrayListAdapter adapter;
    private boolean isEagleEye = false;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latlng);
        ButterKnife.bind(this);
        mContext = this;
        setSupportActionBar(toolbar);
        mTTitle.setText("定位坐标");

        Intent intent = getIntent();
        tracePath = intent.getStringExtra("tracePath");
        isEagleEye = intent.getBooleanExtra("isEagleEye",false);
        date = intent.getStringExtra("date");
        latlngList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mLatlngRv.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        mLatlngRv.addItemDecoration(dividerItemDecoration);
        adapter = new ArrayListAdapter(mContext,latlngList);
        mLatlngRv.setAdapter(adapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isEagleEye){
            obtainLatlngFromEagleEye();
        }else {
            obtainLocationDataFromFile(tracePath);
        }
        adapter.notifyDataSetChanged();
        mTTitle.setText("定位坐标("+latlngList.size()+")");
    }

    private void obtainLatlngFromEagleEye() {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt = DateTime.parse(date,dtf).plusHours(7);
        DateTime dt1 = DateTime.parse(date,dtf).plusHours(22);
        TraceControl.getInstance().queryHistoryTrackPoints(dt.getMillis(),dt1.getMillis(),new TraceControl.TrackStringListener() {
            @Override
            public void onObtainTrackStringList(List<String> trackList) {
                latlngList.addAll(trackList);
            }
        });
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

    @OnClick(R.id.scanner_toolbar_back)
    public void onViewClicked() {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
