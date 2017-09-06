package com.example.applibrary.rlPart;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.applibrary.R;
import com.example.applibrary.rlPart.base.AdapterArrayListOperation;
import com.example.applibrary.rlPart.base.BaseAdapter;
import com.example.applibrary.rlPart.holder.EmptyHolder;
import com.example.applibrary.rlPart.holder.HeadLayoutHolder;
import com.example.applibrary.rlPart.holder.TextOnOffHolder;
import com.example.applibrary.rlPart.holder.TextOnlyHolder;

import java.util.List;

/**
 * Created by data on 2017/8/23.
 */

public class MeFragmentAdapter extends BaseAdapter<List<MeFragmentAdapter.ItemModel>> {
    public static final int HEAD_LAYOUT = 0;
    public static final int TEXTONLY_LAYOUT = 1;
    public static final int TEXT_ONOFF_LAYOUT = 2;
    public static final int EMPTY_LAYOUT = 3;
    private final String tag = "MeFragmentAdapter";
    private List<ItemModel> mItems;

    public MeFragmentAdapter(Context context,List<ItemModel> items) {
        super(context);
        mItems = items;
        setAdapterDataOperation(new AdapterArrayListOperation<ItemModel>(items,this));
    }

    @Override
    public int getItemViewType(int position) {
//        LogUtils.i(tag,"itemType "+mItems.get(position).itemType );
        return mItems.get(position).itemType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case HEAD_LAYOUT:
//                LogUtils.i(tag,"onCreateViewHolder 0");
                View headLayout = LayoutInflater.from(mContext).inflate(R.layout.me_headlayout,parent,false);
                return new HeadLayoutHolder(headLayout,mContext);
            case TEXTONLY_LAYOUT:
//                LogUtils.i(tag,"onCreateViewHolder 1");
                View textOnlyLayout = LayoutInflater.from(mContext).inflate(R.layout.me_textonly_layout,parent,false);
                return new TextOnlyHolder(textOnlyLayout,mContext);
            case TEXT_ONOFF_LAYOUT:
//                LogUtils.i(tag,"onCreateViewHolder 2");
                View onOffLayout = LayoutInflater.from(mContext).inflate(R.layout.me_text_onoff_layout,parent,false);
                return new TextOnOffHolder(onOffLayout,mContext);
            case EMPTY_LAYOUT:
                View emptyLayout = LayoutInflater.from(mContext).inflate(R.layout.empty_view,parent,false);
                return new EmptyHolder(emptyLayout);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    public static class ItemModel{

        public int itemType;
        public String[] content;
        private Intent intent;

        public ItemModel(int itemType, String[] content) {
            this.itemType = itemType;
            this.content = content;
        }

        public ItemModel(int itemType, String[] content, Intent intent) {
            this.itemType = itemType;
            this.content = content;
            this.intent = intent;
        }

        public Intent getIntent() {
            return intent;
        }
    }
}
