package com.google.zxing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;

import com.example.applibrary.LogUtils;
import com.example.applibrary.ToastFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 图片操作类 压缩，本地文件读取，保存到本地
 */
public final class BitmapUtils {

	public static Bitmap getBitmap(Context context,Uri uri, int width, int height) throws FileNotFoundException {
		Options options=new Options();
		options.inJustDecodeBounds=true;
		//只获取图片的宽和高
		BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri),null,options);
		int scaleX=1;
		if(width>0 && width<options.outWidth){
			scaleX=options.outWidth/width;
		}
		int scaleY=1;
		if (height > 0 && height<options.outHeight) {
			scaleY=options.outHeight/height;
		}
		int scale=scaleX;
		if(scale<scaleY){
			scale=scaleY;
		}
		options.inJustDecodeBounds=false;
		options.inSampleSize=scale;
		//使用Bitmap.Config.RGB_565比默认的Bitmap.Config.RGB_8888节省一半的内存。
		options.inPreferredConfig=Bitmap.Config.RGB_565;
		Bitmap bitmap=BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri),null,options);
		return bitmap;
	}


	/**
	 * 按指定尺寸转换图片
	 * @param data：图片的二进制数据
	 * @param width：图片的预期宽度
	 * @param height：图片的预期高度
	 * @return Bitmap类型
	 */
	public static Bitmap getBitmap(byte[] data,int width,int height){
		Options options=new Options();
		options.inJustDecodeBounds=true;
		//只获取图片的宽和高
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		int scaleX=1;
		if(width>0 && width<options.outWidth){
			scaleX=options.outWidth/width;
		}
		int scaleY=1;
		if (height > 0 && height<options.outHeight) {
			scaleY=options.outHeight/height;
		}
		int scale=scaleX;
		if(scale<scaleY){
			scale=scaleY;
		}
		options.inJustDecodeBounds=false;
		options.inSampleSize=scale;
		//使用Bitmap.Config.RGB_565比默认的Bitmap.Config.RGB_8888节省一半的内存。
		options.inPreferredConfig=Bitmap.Config.RGB_565;
		Bitmap bitmap=BitmapFactory.decodeByteArray(data, 0, data.length,options);
		return bitmap;
	}
	/**
	 * 从本地文件读取图片
	 * @param path：图片文件的本地路径
	 * @return 图片的Bitmap类型
	 */
	public static Bitmap getBitmap(String path){
		File file=new File(path);
		if(!file.exists()){
			return null;
		}
		if(file.length()==0){
			file.delete();
			return null;
		}
		Bitmap bitmap= BitmapFactory.decodeFile(path);
		return bitmap;
	}
	/**
	 *  将图片保存至本地
	 * @param bitmap：图片
	 * @param path：保存的路径
	 * @throws IOException
	 */
	public static void saveBitmap(Bitmap bitmap,String path) {
		File file=new File(path);

		if(!file.getParentFile().exists()){//若不存在目录，则创建
			boolean isSuccess = file.getParentFile().mkdirs();
			if(!isSuccess){//若文件所在目录创建失败，则返回
				return ;
			}
		}
		FileOutputStream out = null;
		try {
			out=new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 100, out);
			LogUtils.i("QRcode path",path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将位图对象转换为字节数组
	 * @param bm
	 * @return
	 */
	private static byte[] Bitmap2Bytes(Bitmap bitmap) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
		return outputStream.toByteArray();
	}
	/**
	 * 保存二维码至SD卡
	 * @param filename
	 * @param bitmap
	 */
	public static void saveBitmapToSDCard(Bitmap bitmap,String path) throws Exception {
		// 获取SD卡的路径：Environment.getExternalStorageDirectory()
		File file = new File(path);
		FileOutputStream outStream = new FileOutputStream(file);
		outStream.write(Bitmap2Bytes(bitmap));
		ToastFactory.showLongToast("二维码保存在"+path);
		outStream.close();
	}
}
