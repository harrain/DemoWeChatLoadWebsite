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

import com.example.demowechat.rlPart.ArrayListAdapter;
import com.example.demowechat.rlPart.BaseAdapter;
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.LogUtils;

import java.io.File;
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
    private List<String> tracesFileNames;
    private ArrayListAdapter adapter;

    public LatlngFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_latlng, container, false);
        unbinder = ButterKnife.bind(this, view);
        mContext = getActivity();

        tracesFileNames = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rv.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        rv.addItemDecoration(dividerItemDecoration);

        adapter = new ArrayListAdapter(mContext,tracesFileNames);
        adapter.setOnClickListener(new BaseAdapter.OnClickListener() {
            @Override
            public void onShortClick(View v, int position) {
                Intent intent = new Intent(mContext,LatlngActivity.class);
                intent.putExtra("tracePath",AppConstant.TRACES_DIR + "/" + tracesFileNames.get(position));
                startActivity(intent);
            }

            @Override
            public void onLongClick(View v, int position) {

            }
        });
        rv.setAdapter(adapter);

        ((MainActivity)mContext).setToolbarTitle("定位文件("+tracesFileNames.size()+")");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        tracesFileNames.clear();
        File cacheDir = new File(AppConstant.TRACES_DIR);
        Log.e(tag, "file:" + cacheDir.getAbsolutePath());
        if (!cacheDir.exists()) {
            LogUtils.e(tag, "TRACES_DIR is not existed");
            return;
        }
        File[] files = cacheDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                tracesFileNames.add(file.getName());
            }
        }
        adapter.notifyDataSetChanged();
        ((MainActivity)mContext).setToolbarTitle("定位文件("+tracesFileNames.size()+")");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
