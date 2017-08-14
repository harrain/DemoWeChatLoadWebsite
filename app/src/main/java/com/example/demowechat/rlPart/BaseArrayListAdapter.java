package com.example.demowechat.rlPart;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.demowechat.utils.Link;

/**
 * Created by data on 2017/8/14.
 */

public class BaseArrayListAdapter<DT> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public Context mContext;

    private AdapterLinkOperation<DT> mADO;
    private BaseMyHolder myHolder;

    private OnClickListener mListener;
    private  final String TAG = "BaseArrayListAdapter";

    public BaseArrayListAdapter(Context context,Link data) {
        mContext = context;
        mADO = new AdapterLinkOperation<DT>(data,this);
    }


    public interface OnClickListener{
        void onShortClick(View v, int position);
        void onLongClick(View v, int position);
    }

    public void setOnClickListener(OnClickListener listener){
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
