package com.example.demowechat.rlPart.holder;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.demowechat.R;
import com.example.demowechat.rlPart.MeFragmentAdapter;
import com.example.demowechat.rlPart.base.AdapterDataOperation;
import com.example.demowechat.rlPart.base.BaseMyHolder;
import com.example.demowechat.utils.LogUtils;

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
