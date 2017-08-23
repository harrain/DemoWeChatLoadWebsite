package com.example.demowechat.rlPart;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.demowechat.R;
import com.example.demowechat.rlPart.base.AdapterLinkOperation;
import com.example.demowechat.rlPart.base.BaseAdapter;
import com.example.demowechat.rlPart.holder.FileItemHolder;
import com.example.demowechat.utils.Link;

/**
 * Created by stephen on 2017/8/13.
 */

public class FileListAdapter extends BaseAdapter {

    public FileListAdapter(Context context, Link<String> data) {
        super(context);
        setAdapterDataOperation(new AdapterLinkOperation<String>(data,this));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View swipeView = LayoutInflater.from(mContext).inflate(R.layout.fileitem_left_and_right_menu, parent, false);
        return new FileItemHolder(swipeView,mContext);
    }
}
