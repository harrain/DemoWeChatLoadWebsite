package com.example.demowechat.rlPart;

import android.support.v7.widget.RecyclerView;

/**
 * Created by data on 2017/8/14.
 */

public abstract class AdapterDataOperation<DT> {

    public DT datas;

    public RecyclerView.Adapter mAdapter;

    public AdapterDataOperation(DT data, RecyclerView.Adapter adapter){
        datas = data;
        mAdapter = adapter;
    }

    public DT getDatas(){
        return datas;
    }

    public abstract int getSafeCount();

}
