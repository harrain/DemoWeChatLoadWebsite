package com.example.demowechat.rlPart.base;

import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created by data on 2017/8/14.
 */

public class AdapterArrayListOperation<DT> extends AdapterDataOperation<List<DT>> {

    public AdapterArrayListOperation(List<DT> data, RecyclerView.Adapter adapter) {
        super(data, adapter);
    }

    @Override
    public List<DT> getDatas() {
        return datas;
    }

    @Override
    public int getSafeCount() {
        return datas == null ? 0 : datas.size();
    }
}
