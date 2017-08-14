package com.example.demowechat.rlPart;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by stephen on 2017/8/13.
 */

public abstract class BaseMyHolder<DT> extends RecyclerView.ViewHolder {
    private Context mContext;
    private BaseAdapter.OnClickListener onClickListener;


    public BaseMyHolder(View itemView) {
        super(itemView);
    }

    public BaseMyHolder(View itemView,Context context){
        this(itemView);
        mContext = context;
    }

    public abstract void bind(int position, AdapterDataOperation<DT> ado);

    public abstract BaseMyHolder newInstance();

    public void setOnClickListener(BaseAdapter.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public BaseAdapter.OnClickListener getOnClickListener() {
        return onClickListener;
    }
}
