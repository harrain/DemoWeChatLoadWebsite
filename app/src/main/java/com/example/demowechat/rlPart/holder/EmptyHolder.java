package com.example.demowechat.rlPart.holder;

import android.view.View;
import android.widget.RelativeLayout;

import com.example.demowechat.R;
import com.example.demowechat.rlPart.base.AdapterDataOperation;
import com.example.demowechat.rlPart.base.BaseMyHolder;

/**
 * Created by data on 2017/8/24.
 */

public class EmptyHolder extends BaseMyHolder {


    private View view;

    public EmptyHolder(View itemView) {
        super(itemView);
        view = itemView.findViewById(R.id.empty_view);
    }

    public EmptyHolder(View itemView,int height){
        this(itemView);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }

    @Override
    public void bind(int position, AdapterDataOperation ado) {

    }

    @Override
    public BaseMyHolder newInstance() {
        return null;
    }
}
