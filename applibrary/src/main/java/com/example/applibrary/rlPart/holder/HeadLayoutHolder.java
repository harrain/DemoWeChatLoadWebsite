package com.example.applibrary.rlPart.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
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

public class HeadLayoutHolder extends BaseMyHolder<List<MeFragmentAdapter.ItemModel>> {
    private final String tag = "HeadLayoutHolder";
    ImageView mHeadIv;
    TextView mNickTv;
    TextView mAccountTv;
    RelativeLayout mHeadRl;

    public HeadLayoutHolder(View itemView, Context context) {
        super(itemView, context);
        mHeadRl = (RelativeLayout) itemView.findViewById(R.id.layout_profile_view);
        mHeadIv = (ImageView) itemView.findViewById(R.id.iv_profile_avatar);
        mNickTv = (TextView) itemView.findViewById(R.id.tv_profile_nickname);
        mAccountTv = (TextView) itemView.findViewById(R.id.tv_profile_username);
    }

    @Override
    public void bind(final int position, AdapterDataOperation<List<MeFragmentAdapter.ItemModel>> ado) {

        try {
            MeFragmentAdapter.ItemModel itemModel = ado.getDatas().get(position);
//            Glide.with(mContext).load(itemModel.content[0]).into(mHeadIv);
            mNickTv.setText(itemModel.content[1]);
            mAccountTv.setText(itemModel.content[2]);

            mHeadRl.setOnClickListener(new View.OnClickListener() {
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
