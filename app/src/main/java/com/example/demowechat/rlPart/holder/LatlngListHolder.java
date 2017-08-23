package com.example.demowechat.rlPart.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.demowechat.LatlngFragment;
import com.example.demowechat.R;
import com.example.demowechat.rlPart.base.AdapterDataOperation;
import com.example.demowechat.rlPart.base.BaseMyHolder;

import java.util.List;

/**
 * Created by stephen on 2017/8/14.
 */

public class LatlngListHolder extends BaseMyHolder<List<LatlngFragment.TracesType>> {

    TextView latlngTv;
    ImageView leftIv;
    ImageView rightIv;
    RelativeLayout tracesRl;

    public LatlngListHolder(View itemView) {
        super(itemView);
    }

    public LatlngListHolder(View itemView, Context context) {
        super(itemView, context);
        latlngTv = (TextView) itemView.findViewById(R.id.name_tv);
        leftIv = (ImageView) itemView.findViewById(R.id.filenamelist_left_iv);
        rightIv = (ImageView) itemView.findViewById(R.id.filenamelist_right_iv);
        tracesRl = (RelativeLayout) itemView.findViewById(R.id.latlng_rl);
    }

    @Override
    public void bind(int position, AdapterDataOperation<List<LatlngFragment.TracesType>> ado) {
        final int index = ado.getDatas().size() -1 - position;
        latlngTv.setText(ado.getDatas().get(index).getmContent());
        leftIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null){
                    mOnClickListener.onShortClick(v,index);
                }
            }
        });
        rightIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null){
                    mOnClickListener.onShortClick(v,index);
                }
            }
        });
//        tracesRl.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mOnClickListener != null) {
//                    mOnClickListener.onShortClick(v, index);
//                }
//            }
//        });
    }

    @Override
    public BaseMyHolder newInstance() {
        return null;
    }
}
