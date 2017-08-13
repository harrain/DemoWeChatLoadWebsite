package com.example.demowechat.rlPart;

import com.example.demowechat.utils.Link;

/**
 * Created by stephen on 2017/8/13.
 */

public class AdapterDataOperation<DataType> {

    private Link<DataType> datas;
    private BaseUDAdapter mAdapter;

    public AdapterDataOperation(Link data,BaseUDAdapter adapter){
        datas = data;
        mAdapter = adapter;
    }

    public void deleteItem(int position){
        mAdapter.notifyItemRemoved(position);
    }



    public Link<DataType> getDatas(){
        return datas;
    }

    public int getSafeCount(){
        return datas == null ? 0 : datas.size();
    }
}
