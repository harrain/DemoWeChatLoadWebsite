package com.example.demowechat.rlPart;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.demowechat.R;

import java.util.List;

/**
 * Created by stephen on 2017/8/14.
 */

public class LatlngListHolder extends BaseMyHolder<List<String>> {

    TextView latlngTv;
    RelativeLayout tracesRl;

    public LatlngListHolder(View itemView) {
        super(itemView);
    }

    public LatlngListHolder(View itemView, Context context) {
        super(itemView, context);
        latlngTv = (TextView) itemView.findViewById(R.id.name_tv);
        tracesRl = (RelativeLayout) itemView.findViewById(R.id.latlng_rl);
    }

    @Override
    public void bind(int position, AdapterDataOperation<List<String>> ado) {
        final int index = ado.getDatas().size() -1 - position;
        latlngTv.setText(ado.getDatas().get(index));
        tracesRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    mOnClickListener.onShortClick(v, index);
                }
            }
        });
    }

    @Override
    public BaseMyHolder newInstance() {
        return null;
    }
}