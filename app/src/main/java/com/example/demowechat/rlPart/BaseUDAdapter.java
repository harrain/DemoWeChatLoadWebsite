package com.example.demowechat.rlPart;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.demowechat.utils.Link;


/**
 * 列表内容适配器，维系数据源和布局的显示
 */

public class BaseUDAdapter<DataType> extends RecyclerView.Adapter <RecyclerView.ViewHolder>{


    private Context mContext;

    private AdapterDataOperation<DataType> mADO;
    private BaseMyHolder myHolder;

    private OnClickListener listener;
    private  final String TAG = "BaseUDAdapter";

    public BaseUDAdapter(Context context,Link data) {
        mContext = context;
        mADO = new AdapterDataOperation<>(data,this);
    }

    public void setContentHolder(BaseMyHolder holder){
        myHolder = holder;
    }

    public interface OnClickListener{
        void onShortClick(View v, int position);
        void onLongClick(View v, int position);
    }

    public void setOnClickListener(OnClickListener listener){
        this.listener = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return myHolder.newInstance();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ((BaseMyHolder)holder).bind(position,mADO);
        ((BaseMyHolder)holder).setOnClickListener(listener);
    }


    @Override
    public int getItemCount() {

        return mADO.getSafeCount();
    }


}
