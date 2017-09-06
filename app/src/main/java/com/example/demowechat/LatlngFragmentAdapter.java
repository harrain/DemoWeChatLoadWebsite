package com.example.demowechat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.applibrary.rlPart.base.AdapterArrayListOperation;
import com.example.applibrary.rlPart.base.BaseAdapter;

import java.util.List;

/**
 * Created by data on 2017/8/22.
 */

public class LatlngFragmentAdapter extends BaseAdapter<List<LatlngFragment.TracesType>> {

    public LatlngFragmentAdapter(Context context, List<LatlngFragment.TracesType> data) {
        super(context);
        setAdapterDataOperation(new AdapterArrayListOperation<LatlngFragment.TracesType>(data,this));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View swipeView = LayoutInflater.from(mContext).inflate(R.layout.simple_list_tv, parent, false);
        return new LatlngListHolder(swipeView,mContext);
    }
}
