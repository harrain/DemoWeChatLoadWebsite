package com.example.demowechat.rlPart;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demowechat.R;
import com.example.demowechat.SwipeItemLayout;
import com.example.demowechat.utils.Link;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by stephen on 2017/8/13.
 */

public class FileItemHolder extends BaseMyHolder {

    @BindView(R.id.file_tv)
    TextView fileTv;
    @BindView(R.id.left_menu)
    TextView mLeftMenu;
    @BindView(R.id.right_menu)
    TextView mRightMenu;
    @BindView(R.id.file_swipe_layout)
    SwipeItemLayout mSwipeItemLayout;
    private View contentView;

    private Context mContext;
    private final String tag = "fileItemHolder";

    public FileItemHolder(View itemView, Context context) {
        super(itemView);
        contentView = itemView;
        ButterKnife.bind(itemView);
        mContext = context;
    }

    @Override
    public BaseMyHolder newInstance() {
        return new FileItemHolder(contentView,mContext);
    }

    @Override
    public void bind(final int position, final AdapterDataOperation ado) {
        fileTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileItemHolder.this.getOnClickListener().onShortClick(v,position);
            }
        });
        mRightMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwipeItemLayout.close();
                deleteFile(position,ado);
            }
        });

    }

    private void deleteFile(int position,AdapterDataOperation ado) {
        Link<String> datas = ado.getDatas();
        Log.e(tag,"imageSize----"+datas.size());
        int index = position;
        File file = new File(datas.remove(index));
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
