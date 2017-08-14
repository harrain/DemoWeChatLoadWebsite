package com.example.demowechat.rlPart;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.demowechat.R;

import java.util.List;

/**
 * Created by stephen on 2017/8/14.
 */

public class LatlngListHolder extends BaseMyHolder<List<String>> {

    TextView latlngTv;

    public LatlngListHolder(View itemView) {
        super(itemView);
    }

    public LatlngListHolder(View itemView, Context context) {
        super(itemView, context);
        latlngTv = (TextView) itemView.findViewById(R.id.name_tv);
    }

    @Override
    public void bind(int position, AdapterDataOperation<List<String>> ado) {
        latlngTv.setText(ado.getDatas().get(position));
    }

    @Override
    public BaseMyHolder newInstance() {
        return null;
    }
}
