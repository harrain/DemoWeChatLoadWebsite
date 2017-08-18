package com.example.demowechat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.demowechat.utils.DeviceInfoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class DisplayFragment extends Fragment {


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
    Unbinder unbinder;
    private Object mContext;

    public DisplayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display, container, false);
        unbinder = ButterKnife.bind(this, view);
        mContext = getActivity();
        ((MainActivity)mContext).setToolbarTitle("我的设备");
        Manufacturer.setText(DeviceInfoUtils.getManufacturer());
        Model.setText(DeviceInfoUtils.getDeviceModel());
        DeviceName.setText(DeviceInfoUtils.getDeviceName());
        Version.setText(DeviceInfoUtils.getSDK());

        ROM.setText(DeviceInfoUtils.getOSName()+" "+DeviceInfoUtils.getOsVersion());
        CPU.setText(DeviceInfoUtils.getCpuType());
        cpuABI.setText(DeviceInfoUtils.getCPU_ABI());
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}