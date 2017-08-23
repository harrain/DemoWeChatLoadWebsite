package com.example.demowechat;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.demowechat.rlPart.MeFragmentAdapter;
import com.example.demowechat.rlPart.base.BaseAdapter;
import com.example.demowechat.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class DisplayFragment extends Fragment {


    Unbinder unbinder;
    @BindView(R.id.me_rv)
    RecyclerView meRv;
    private Context mContext;
    private List<MeFragmentAdapter.ItemModel> mItemList;
    private final String tag = "DisplayFragment";
    public DisplayFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display, container, false);
        unbinder = ButterKnife.bind(this, view);
        mContext = getActivity();
        ((MainActivity) mContext).setToolbarTitle("我");
        initItemModel();
        initRecyclerView();

        return view;
    }
    private void initItemModel() {
        mItemList.add(new MeFragmentAdapter.ItemModel(MeFragmentAdapter.HEAD_LAYOUT
                ,new String[]{"","昵称 暂无","账号 暂无"},null));
        mItemList.add(new MeFragmentAdapter.ItemModel(MeFragmentAdapter.TEXT_ONOFF_LAYOUT
                ,new String[]{"自动定位"}));
        mItemList.add(new MeFragmentAdapter.ItemModel(MeFragmentAdapter.TEXTONLY_LAYOUT
                ,new String[]{"设备信息"},new Intent(mContext,DeviceActivity.class)));
        mItemList.add(new MeFragmentAdapter.ItemModel(MeFragmentAdapter.TEXTONLY_LAYOUT,new String[]{"关于应用"}));
    }

    private void initRecyclerView() {
        LogUtils.i(tag,"shuju "+mItemList.size());
        LinearLayoutManager lm = new LinearLayoutManager(mContext);
        meRv.setLayoutManager(lm);
        DividerItemDecoration decor = new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL);
        meRv.addItemDecoration(decor);
        MeFragmentAdapter adapter = new MeFragmentAdapter(mContext,mItemList);
        adapter.setOnClickListener(new BaseAdapter.OnClickListener() {
            @Override
            public void onShortClick(View v, int position) {
                Intent i = mItemList.get(position).getIntent();
                if (i!=null){
                    startActivity(i);
                }
            }

            @Override
            public void onLongClick(View v, int position) {

            }
        });
        meRv.setAdapter(adapter);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
