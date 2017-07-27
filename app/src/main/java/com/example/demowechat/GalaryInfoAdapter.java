package com.example.demowechat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import java.io.FileNotFoundException;
import java.util.Stack;



/**
 * Created by stephen on 2017/5/30.
 */

public class GalaryInfoAdapter extends RecyclerView.Adapter <GalaryInfoAdapter.ImageHolder>{


    private Context mContext;
    private Stack<ConverFragment.Pic> imagePaths;
    private static  final String TAG = "GalaryInfoAdapter";

    public GalaryInfoAdapter(Context context, Stack<ConverFragment.Pic> images) {
        mContext = context;
        imagePaths = images;
        if (imagePaths == null) {
            Log.e(TAG, "image集合空");
        }else {
            Log.e(TAG,"imagepath0:"+imagePaths.size());
        }
    }


    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        ImageHolder holder = new ImageHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, final int position) {
        Log.e(TAG,"imagePath:"+imagePaths.get(position));
//        Glide.with(mContext).load(imagePaths.get(position)).into(holder.galaryinfoIv);
        // 将拍摄的照片显示出来
//        Bitmap bitmap = null;
        try {
//            bitmap = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(imagePaths.get(position).getUri()));
//            bitmap = BitmapUtils.getBitmap(mContext,imagePaths.get(position).getUri(),holder.imageView.getWidth(),holder.imageView.getHeight());
            Glide.with(mContext).load(imagePaths.get(position).getUri()).into(holder.imageView);
//            holder.imageView.setImageBitmap(bitmap);
            holder.textView.setText(imagePaths.get(position).getData());
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,ImageActivity.class);
                intent.putExtra("imagepath",imagePaths.get(position).getUri().toString());
                mContext.startActivity(intent);
            }
        });
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
            Log.e(TAG,"imagepath1:"+imagePaths.size());
        }
        return imagePaths == null ? 0 : imagePaths.size();
    }

    class ImageHolder extends RecyclerView.ViewHolder{

//        ImageView galaryinfoIv;
        RelativeLayout rl;
        ImageView imageView;
        TextView textView;
        TextView content;

        ImageHolder(View view) {
            super(view);
            rl = (RelativeLayout) view.findViewById(R.id.relative);
            imageView = (ImageView) view.findViewById(R.id.iv);
            textView = (TextView) view.findViewById(R.id.tv);
            content = (TextView) view.findViewById(R.id.content);
            content.setVisibility(View.INVISIBLE);
        }
    }
}
