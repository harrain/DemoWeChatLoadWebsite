package com.example.applibrary.rlPart.holder;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.applibrary.LogUtils;
import com.example.applibrary.R;
import com.example.applibrary.rlPart.MeFragmentAdapter;
import com.example.applibrary.rlPart.base.AdapterDataOperation;
import com.example.applibrary.rlPart.base.BaseMyHolder;

import java.util.List;

/**
 * Created by data on 2017/8/23.
 */

public class TextOnlyHolder extends BaseMyHolder<List<MeFragmentAdapter.ItemModel>> {
    private final String tag = "TextOnlyHolder";
    TextView mContentTv;
    RelativeLayout mTextRl;

    public TextOnlyHolder(View itemView, Context context) {
        super(itemView, context);
        mContentTv = (TextView) itemView.findViewById(R.id.me_item_tv_content);
        mTextRl = (RelativeLayout) itemView.findViewById(R.id.me_textonly_item_layout);
    }

    @Override
    public void bind(final int position, AdapterDataOperation<List<MeFragmentAdapter.ItemModel>> ado) {
        try {
            mContentTv.setText(ado.getDatas().get(position).content[0]);
            mTextRl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClickListener.onShortClick(v,position);
                }
            });
        }catch (Exception e){
            LogUtils.e(tag,e.getMessage());
        }
    }

    @Override
    public BaseMyHolder newInstance() {
        return null;
    }
}
