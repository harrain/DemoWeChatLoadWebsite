package com.example.demowechat.rlPart;

import android.support.v7.widget.RecyclerView;

import com.example.demowechat.utils.Link;

/**
 * Created by stephen on 2017/8/13.
 */

public class AdapterLinkOperation<DT> {

    private Link<DT> datas;
    private RecyclerView.Adapter mAdapter;

    public AdapterLinkOperation(Link data, RecyclerView.Adapter adapter){
        datas = data;
        mAdapter = adapter;
    }

    public void deleteItem(int position){
        mAdapter.notifyItemRemoved(position);
    }



    public Link<DT> getDatas(){
        return datas;
    }

    public int getSafeCount(){
        return datas == null ? 0 : datas.size();
    }
}
