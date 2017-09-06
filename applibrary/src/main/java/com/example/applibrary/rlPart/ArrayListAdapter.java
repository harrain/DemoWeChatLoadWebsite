package com.example.applibrary.rlPart;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.applibrary.R;
import com.example.applibrary.rlPart.base.AdapterArrayListOperation;
import com.example.applibrary.rlPart.base.BaseAdapter;
import com.example.applibrary.rlPart.holder.LatlngActivityHolder;

import java.util.List;

/**
 * Created by stephen on 2017/8/14.
 */

public class ArrayListAdapter extends BaseAdapter<List<String>> {

    public ArrayListAdapter(Context context, List<String> data) {
        super(context);
        setAdapterDataOperation(new AdapterArrayListOperation<String>(data,this));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View swipeView = LayoutInflater.from(mContext).inflate(R.layout.simple_list_tv, parent, false);
        return new LatlngActivityHolder(swipeView,mContext);
    }
}
