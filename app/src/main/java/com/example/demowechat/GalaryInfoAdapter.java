package com.example.demowechat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;



/**
 * 列表内容适配器，维系数据源和布局的显示
 */

public class GalaryInfoAdapter extends RecyclerView.Adapter <GalaryInfoAdapter.ImageHolder>{


    private Context mContext;
    private List<ConverFragment.Pic> imagePaths;

    private OnClickListener listener;
    private  final String TAG = "GalaryInfoAdapter";

    public GalaryInfoAdapter(Context context, List<ConverFragment.Pic> images) {
        mContext = context;
        imagePaths = images;
        if (imagePaths == null) {
            Log.e(TAG, "image集合空");
        }else {
            Log.e(TAG,"imagepath0:"+imagePaths.size());
        }
    }



    public interface OnClickListener{
        void onShortClick(View v,int position);
        void onLongClick(View v,int position);
    }

    public void setOnClickListener(OnClickListener listener){
        this.listener = listener;
    }


    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_left_and_right_menu, parent, false);
        ImageHolder holder = new ImageHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ImageHolder holder, final int position) {
//        Log.e(TAG,"imagePath:"+imagePaths.get(position));
        Log.e(TAG,"position:-----"+position);
        final int index = imagePaths.size()-1-position;
        Log.e(TAG,"index:bind-----"+index);
//        Glide.with(mContext).load(imagePaths.get(position)).into(holder.galaryinfoIv);
        // 将拍摄的照片显示出来
//        Bitmap bitmap = null;
        try {
//            bitmap = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(imagePaths.get(position).getUri()));
//            bitmap = BitmapUtils.getBitmap(mContext,imagePaths.get(position).getUri(),holder.imageView.getWidth(),holder.imageView.getHeight());
            Glide.with(mContext).load(imagePaths.get(index).getUri()).into(holder.imageView);
//            holder.imageView.setImageBitmap(bitmap);
            holder.textView.setText(imagePaths.get(index).getData());

            holder.tv1.setText(imagePaths.get(index).getSize());
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.mContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,ImageActivity.class);
                intent.putExtra("imagepath",imagePaths.get(index).getUri().toString());
                mContext.startActivity(intent);

//                listener.onShortClick(holder.rl,position); //传递短按事件
            }
        });

        holder.mLeftMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mSwipeItemLayout.close();
                Toast.makeText(mContext,"zuo",Toast.LENGTH_SHORT).show();
            }
        });

        holder.mRightMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mSwipeItemLayout.close();
//                Toast.makeText(mContext,"you",Toast.LENGTH_SHORT).show();

                deleteFile(position);
                notifyItemRemoved(position);
//                notifyDataSetChanged();
                Log.e(TAG,"position:点击-----"+position);

            }
        });

//        holder.rl.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                listener.onLongClick(v,position);//传递长按事件
//                return false;
//            }
//        });
    }

    private void deleteFile(int position) {
        Log.e(TAG,"imageSize----"+imagePaths.size());
        int index = imagePaths.size() - position -  1;
        File file = new File(imagePaths.remove(imagePaths.size()-1-position).getUri().getPath());
        Log.e(TAG,"delete ----"+String.valueOf(index));
        Log.e(TAG,"imageSize----"+imagePaths.size());
        Log.e(TAG,"deFile size: "+file.length());
        if (file.exists()) {
            boolean delete = file.delete();
            if (delete){
                Toast.makeText(mContext,"删除成功！",Toast.LENGTH_SHORT).show();
            }
        }

    }

//    public void addUri(Uri uri){
//        imagePaths.add(uri);
//
//    }

    @Override
    public int getItemCount() {
        if (imagePaths == null) {
            Log.e(TAG, "imagecount空");
        }else {
//            Log.e(TAG,"imagepath1:"+imagePaths.size());
        }
        return imagePaths == null ? 0 : imagePaths.size();
    }

    class ImageHolder extends RecyclerView.ViewHolder{

//        ImageView galaryinfoIv;
//        RelativeLayout rl;
        ImageView imageView;
        TextView textView;
        TextView tv1;

        private  View mLeftMenu;
        private  View mRightMenu;
        private  RelativeLayout mContent;
        private  SwipeItemLayout mSwipeItemLayout;

        ImageHolder(View view) {
            super(view);
//            rl = (RelativeLayout) view.findViewById(R.id.relative);
            imageView = (ImageView) view.findViewById(R.id.iv);
            textView = (TextView) view.findViewById(R.id.tv);
            tv1 = (TextView) view.findViewById(R.id.tv1);

            mSwipeItemLayout = (SwipeItemLayout) itemView.findViewById(R.id.swipe_layout);
            mContent = (RelativeLayout) itemView.findViewById(R.id.relative);
            mLeftMenu = itemView.findViewById(R.id.left_menu);
            mRightMenu = itemView.findViewById(R.id.right_menu);

        }
    }
}
