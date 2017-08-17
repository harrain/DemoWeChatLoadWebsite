package com.example.demowechat.rlPart;

import android.support.v7.widget.RecyclerView;

import com.example.demowechat.utils.Link;

/**
 * Created by stephen on 2017/8/13.
 */

public class AdapterLinkOperation<DT> extends AdapterDataOperation<Link<DT>>{



    public AdapterLinkOperation(Link data, RecyclerView.Adapter adapter){
        super(data,adapter);

    }

    public void deleteItem(int position){
        datas.remove(position);
        mAdapter.notifyItemRemoved(position);
        mAdapter.notifyDataSetChanged();
    }



    @Override
    public Link<DT> getDatas(){
        return datas;
    }

    @Override
    public int getSafeCount(){
        return datas == null ? 0 : datas.size();
    }
}
