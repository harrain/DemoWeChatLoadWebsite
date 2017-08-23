package com.example.demowechat;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.demowechat.utils.DeviceInfoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceActivity extends AppCompatActivity {

    @BindView(R.id.Manufacturer)
    TextView Manufacturer;
    @BindView(R.id.Model)
    TextView Model;
    @BindView(R.id.DeviceName)
    TextView DeviceName;
    @BindView(R.id.Version)
    TextView Version;

    @BindView(R.id.ROM)
    TextView ROM;
    @BindView(R.id.CPU)
    TextView CPU;
    @BindView(R.id.CPU_ABI)
    TextView cpuABI;
    @BindView(R.id.widthPixel)
    TextView widthPixel;
    @BindView(R.id.heightPixel)
    TextView heightPixel;
    @BindView(R.id.physical)
    TextView realSize;
    @BindView(R.id.titlebar)
    Toolbar toolbar;
    @BindView(R.id.scanner_toolbar_back)
    ImageView mTBack;
    @BindView(R.id.scanner_toolbar_title)
    TextView mTTitle;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mContext = this;
        mTTitle.setText("我的设备");
        Manufacturer.setText(DeviceInfoUtils.getManufacturer());
        Model.setText(DeviceInfoUtils.getDeviceModel());
        DeviceName.setText(DeviceInfoUtils.getDeviceName());
        Version.setText(DeviceInfoUtils.getSDK());

        ROM.setText(DeviceInfoUtils.getOSName() + " " + DeviceInfoUtils.getOsVersion());
        CPU.setText(DeviceInfoUtils.getCpuType());
        cpuABI.setText(DeviceInfoUtils.getCPU_ABI());
        widthPixel.setText(DeviceInfoUtils.getScreenWidth(mContext) + "");
        heightPixel.setText(DeviceInfoUtils.getScreenHeight(mContext) + "");
        realSize.setText("屏幕尺寸 " + DeviceInfoUtils.getScreenInches(this) + "寸" + "  dpi " + DeviceInfoUtils.getDPI(mContext));
    }

    @OnClick(R.id.scanner_toolbar_back)
    public void onViewClicked() {
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
