package com.example.demowechat.diyCamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demowechat.R;
import com.example.demowechat.operation.WaterMarkOperation;
import com.example.demowechat.utils.AppConstant;
import com.example.demowechat.utils.BitmapUtils;
import com.example.demowechat.utils.CameraUtil;
import com.example.demowechat.utils.LogUtils;
import com.example.demowechat.utils.SharePrefrenceUtils;
import com.example.demowechat.utils.ToastFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener {
    private Camera mCamera;
    private SurfaceView surfaceView;
    private SurfaceHolder mHolder;
    private int mCameraId = 1; // 1前置摄像头自拍，0后置
    private Context context;

    //屏幕宽高
    private int screenWidth;
    private int screenHeight;
    private LinearLayout home_custom_top_relative;
    private RelativeLayout homecamera_bottom_relative;
    private ImageView flash_light;
    private TextView camera_delay_time_text;
    private int index;


    //闪光灯模式 0:关闭 1: 开启 2: 自动
    private int light_num = 0;
    //延迟时间
    private int delay_time;
    private int delay_time_temp;
    private boolean isview = false;
    private boolean is_camera_delay;
    private ImageView camera_frontback;
    private ImageView camera_close;
    private ImageView img_camera;
    private int picHeight;
    private int captureMills;
    private WaterMarkOperation waterMarkOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        context = this;
        Intent intent = getIntent();
        captureMills = intent.getIntExtra("time",0);
        initView();
        initData();

        mCameraId = SharePrefrenceUtils.getInstance().getCameraTurn();

    }

    private void startAutoCapture() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(captureMills);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(AppConstant.CAPTURE_NOW);
            }
        }).start();
    }

    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//?? SURFACE_TYPE_PUSH_BUFFERS
        img_camera = (ImageView) findViewById(R.id.img_camera);
        img_camera.setOnClickListener(this);

        //关闭相机界面按钮
        camera_close = (ImageView) findViewById(R.id.camera_close);
        camera_close.setOnClickListener(this);

        //top 的view
        home_custom_top_relative = (LinearLayout) findViewById(R.id.home_custom_top_relative);
        home_custom_top_relative.setAlpha(0.5f);

        //前后摄像头切换
        camera_frontback = (ImageView) findViewById(R.id.camera_frontback);
        camera_frontback.setOnClickListener(this);


        //闪光灯
        flash_light = (ImageView) findViewById(R.id.flash_light);
        flash_light.setOnClickListener(this);

        camera_delay_time_text = (TextView) findViewById(R.id.camera_delay_time_text);

        homecamera_bottom_relative = (RelativeLayout) findViewById(R.id.homecamera_bottom_relative);
    }

    private void initData() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        LogUtils.i("screenWidth-screenHeight",screenWidth+"-"+screenHeight);

        //这里相机取景框我这是为宽高比3:4 所以限制底部控件的高度是剩余部分
//        RelativeLayout.LayoutParams bottomParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, menuPopviewHeight);
//        bottomParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//        homecamera_bottom_relative.setLayoutParams(bottomParam);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int what = msg.what;
            switch (what) {
                case AppConstant.WHAT.SUCCESS:
                    if (delay_time > 0) {
                        camera_delay_time_text.setText("" + delay_time);
                    }

                    try {
                        if (delay_time == 0) {
                            captrue();
                            is_camera_delay = false;
                            camera_delay_time_text.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        return;
                    }

                    break;

                case AppConstant.WHAT.ERROR:
                    is_camera_delay = false;
                    break;
                case AppConstant.CAPTURE_NOW:
                    img_camera.performClick();
                    img_camera.setVisibility(View.INVISIBLE);
                    break;

            }
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_camera:
                if (isview) {
                    if (delay_time == 0) {
                        switch (light_num) {
                            case 0:
                                //关闭
                                CameraUtil.getInstance().turnLightOff(mCamera);
                                break;
                            case 1:
                                CameraUtil.getInstance().turnLightOn(mCamera);
                                break;
                            case 2:
                                //自动
                                CameraUtil.getInstance().turnLightAuto(mCamera);
                                break;
                        }
                        captrue();
                    } else {
                        camera_delay_time_text.setVisibility(View.VISIBLE);
                        camera_delay_time_text.setText(String.valueOf(delay_time));
                        is_camera_delay = true;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (delay_time > 0) {
                                    //按秒数倒计时
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        mHandler.sendEmptyMessage(AppConstant.WHAT.ERROR);
                                        return;
                                    }
                                    delay_time--;
                                    mHandler.sendEmptyMessage(AppConstant.WHAT.SUCCESS);
                                }
                            }
                        }).start();
                    }
                    isview = false;
                }
                break;

            //前后置摄像头拍照
            case R.id.camera_frontback:
                switchCamera();
                break;

            //退出相机界面 释放资源
            case R.id.camera_close:
                if (is_camera_delay) {
                    Toast.makeText(CameraActivity.this, "正在拍照请稍后...", Toast.LENGTH_SHORT).show();
                    return;
                }
                finish();
                break;

            //闪光灯
            case R.id.flash_light:
                if(mCameraId == 1){
                    //前置
                    ToastFactory.showLongToast(context, "请切换为后置摄像头开启闪光灯");
                    return;
                }
                Camera.Parameters parameters = mCamera.getParameters();
                switch (light_num) {
                    case 0:
                        //打开
                        light_num = 1;
                        flash_light.setImageResource(R.drawable.btn_camera_flash_on);
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//开启
                        mCamera.setParameters(parameters);
                        break;
                    case 1:
                        //自动
                        light_num = 2;
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                        mCamera.setParameters(parameters);
                        flash_light.setImageResource(R.drawable.btn_camera_flash_auto);
                        break;
                    case 2:
                        //关闭
                        light_num = 0;
                        //关闭
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(parameters);
                        flash_light.setImageResource(R.drawable.btn_camera_flash_off);
                        break;
                }

                break;

        }
    }

    public void switchCamera() {
        releaseCamera();
        mCameraId = (mCameraId + 1) % mCamera.getNumberOfCameras();
        LogUtils.i("CameraId", String.valueOf(mCameraId));
        mCamera = getCamera(mCameraId);
        if (mHolder != null) {
            startPreview(mCamera, mHolder);
        }
        SharePrefrenceUtils.getInstance().setCameraTurn(mCameraId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = getCamera(mCameraId);
            if (mHolder != null) {
                startPreview(mCamera, mHolder);
            }
        }
        if (captureMills != 0){
            startAutoCapture();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    /**
     * 获取Camera实例
     *
     * @return
     */
    private Camera getCamera(int id) {
        Camera camera = null;
        try {
            camera = Camera.open(id);
        } catch (Exception e) {

        }
        return camera;
    }

    /**
     * 预览相机
     */
    private void startPreview(Camera camera, SurfaceHolder holder) {
        try {
            setupCamera(camera);
            camera.setPreviewDisplay(holder);
            //亲测的一个方法 基本覆盖所有手机 将预览矫正
            CameraUtil.getInstance().setCameraDisplayOrientation(this, mCameraId, camera);
//            camera.setDisplayOrientation(90);
            camera.startPreview();
            isview = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void captrue() {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                isview = false;
                //将data 转换为位图 或者你也可以直接保存为文件使用 FileOutputStream
                //这里我相信大部分都有其他用处把 比如加个水印 后续再讲解
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap saveBitmap = CameraUtil.getInstance().setTakePicktrueOrientation(mCameraId, bitmap);

                saveBitmap = Bitmap.createScaledBitmap(saveBitmap, screenWidth, picHeight, true);



                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//19个字符串  index : 0-18
                Date date = new Date();
                final String time = sdf.format(date);
                String img_path = AppConstant.KEY.IMG_DIR +
                        File.separator + time + ".jpeg";

//                String img_path = "/storage/emulated/0/DCIM/Camera/"+ time + ".jpg";
//                String img_path = "/storage/0D99-9B24/DCIM/Camera/"+ time + ".jpg";

                LogUtils.i("img_path",img_path);

                //即拍去掉拍照预览，直接获取经纬度，加水印
                if (captureMills > 0){
                    waterMarkOperation = new WaterMarkOperation(CameraActivity.this);
                    waterMarkOperation.setOnFinishListener(new WaterMarkOperation.OnFinishListener() {
                        @Override
                        public void onfinish(String imgPath, String longitude, String latitude) {

                            Intent intent = new Intent();
                            intent.putExtra(AppConstant.KEY.IMG_PATH, imgPath);
                            intent.putExtra(AppConstant.KEY.PIC_TIME, time);
                            intent.putExtra(AppConstant.KEY.LONGITUDE,longitude);
                            intent.putExtra(AppConstant.KEY.LATITUDE,latitude);

                            LogUtils.i("CameraActivity",longitude+"-"+latitude);
                            setResult(RESULT_OK,intent);
                            finish();
                            waterMarkOperation.release();
                        }
                    });
                    waterMarkOperation.initWaterMark(time,img_path,saveBitmap);

                    return;
                }

                //正常预览下的操作

                BitmapUtils.saveJPGE_After(context, saveBitmap, img_path, 100);

                if(!bitmap.isRecycled()){
                    bitmap.recycle();
                }

                if(!saveBitmap.isRecycled()){
                    saveBitmap.recycle();
                }

                setResultToMainActivity(time, img_path);

                //这里打印宽高 就能看到 CameraUtil.getInstance().getPropPictureSize(parameters.getSupportedPictureSizes(), 200);
                // 这设置的最小宽度影响返回图片的大小 所以这里一般这是1000左右把我觉得
//                Log.d("bitmapWidth==", bitmap.getWidth() + "");
//                Log.d("bitmapHeight==", bitmap.getHeight() + "");
            }
        });
    }

    private void setResultToMainActivity(String time, String img_path) {
        Intent intent = new Intent();
        intent.putExtra(AppConstant.KEY.IMG_PATH, img_path);
        intent.putExtra(AppConstant.KEY.PIC_WIDTH, screenWidth);
        intent.putExtra(AppConstant.KEY.PIC_HEIGHT, picHeight);
        intent.putExtra(AppConstant.KEY.PIC_TIME,time);
        setResult(AppConstant.RESULT_CODE.RESULT_OK, intent);
        finish();
    }

    /**
     * 设置
     */
    private void setupCamera(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        //这里第三个参数为最小尺寸 getPropPreviewSize方法会对从最小尺寸开始升序排列 取出所有支持尺寸的最小尺寸
//        Camera.Size previewSize = CameraUtil.getInstance().getPropSizeForHeight(parameters.getSupportedPreviewSizes(), 800);
//        parameters.setPreviewSize(previewSize.width, previewSize.height);
        parameters.setPreviewSize(screenHeight, screenWidth);
//        Camera.Size pictrueSize = CameraUtil.getInstance().getPropSizeForHeight(parameters.getSupportedPictureSizes(), 800);
//        parameters.setPictureSize(pictrueSize.width, pictrueSize.height);
        parameters.setPictureSize(screenHeight, screenWidth);
        camera.setParameters(parameters);

        /**
         * 设置surfaceView的尺寸 因为camera默认是横屏，所以取得支持尺寸也都是横屏的尺寸
         * 我们在startPreview方法里面把它矫正了过来，但是这里我们设置surfaceView的尺寸的时候要注意 previewSize.height<previewSize.width
         * previewSize.width才是surfaceView的高度
         * 一般相机都是屏幕的宽度 这里设置为屏幕宽度 高度自适应 你也可以设置自己想要的大小
         *
         */

        picHeight = screenWidth * 4 / 3;
//        LogUtils.i("previewSize.width-previewSize.height",previewSize.width+"-"+previewSize.height);
//        LogUtils.i("pictrueSize.width-pictrueSize.height",pictrueSize.width+"-"+pictrueSize.height);
//        LogUtils.i("picHeight",picHeight+"");

//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth, (screenWidth * pictrueSize.width) / pictrueSize.height);
        //这里当然可以设置拍照位置 比如居中 我这里就置顶了
        //params.gravity = Gravity.CENTER;
//        surfaceView.setLayoutParams(params);
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview(mCamera, holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        startPreview(mCamera, holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        waterMarkOperation.release();

    }
}
