package com.example.demowechat.rlPart;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demowechat.R;
import com.example.demowechat.widget.SwipeItemLayout;
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.Link;

import java.io.File;

/**
 * Created by stephen on 2017/8/13.
 */

public class FileItemHolder extends BaseMyHolder<String> {

    TextView mFileTv;
    TextView mLeftMenu;
    TextView mRightMenu;
    SwipeItemLayout mSwipeItemLayout;
    private View contentView;

    private Context mContext;
    private final String tag = "fileItemHolder";

    public FileItemHolder(View itemView, Context context) {
        super(itemView);
        contentView = itemView;
        mFileTv = (TextView) itemView.findViewById(R.id.file_tv);
        mLeftMenu = (TextView) itemView.findViewById(R.id.left_menu);
        mRightMenu = (TextView) itemView.findViewById(R.id.right_menu);
        mSwipeItemLayout = (SwipeItemLayout) itemView.findViewById(R.id.file_swipe_layout);
        mContext = context;
    }

    @Override
    public BaseMyHolder newInstance() {
        return new FileItemHolder(contentView,mContext);
    }

    @Override
    public void bind(final int position, final AdapterLinkOperation<String> ado) {

//        LogUtils.i("fileholder","ado data size: "+ado.getDatas().size() + "---"+ado.getDatas().get(position));
        final int index = ado.getDatas().size() -1 - position;
        mFileTv.setText(ado.getDatas().get(index));

        mFileTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileItemHolder.this.getOnClickListener().onShortClick(v,index);
            }
        });
        mRightMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwipeItemLayout.close();
                deleteFile(index,ado);
            }
        });

    }

    private void deleteFile(int position,AdapterLinkOperation ado) {
        Link<String> datas = ado.getDatas();
        Log.e(tag,"imageSize----"+datas.size());
        int index = position;
        File file = new File(AppConstant.TRACES_DIR + "/" +datas.remove(index));
        Log.e(tag,"delete item----"+String.valueOf(index));
        Log.e(tag,"delete File item----"+String.valueOf(index));
        Log.e(tag,"deFile: "+file.getAbsolutePath()+" - "+file.length());
        if (file.exists()) {
            boolean delete = file.delete();
            if (delete){

                Toast.makeText(mContext,"删除成功！",Toast.LENGTH_SHORT).show();
                ado.deleteItem(position);

            }
        }

    }
}
