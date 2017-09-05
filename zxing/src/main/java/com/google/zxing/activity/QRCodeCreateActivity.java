package com.google.zxing.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.applibrary.AppConstant;
import com.example.applibrary.LogUtils;
import com.google.zxing.BitmapUtils;
import com.google.zxing.R;
import com.google.zxing.R2;
import com.google.zxing.encoding.EncodingHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class QRCodeCreateActivity extends AppCompatActivity {

    @BindView(R2.id.scanner_toolbar_back)
    ImageView mTBack;
    @BindView(R2.id.scanner_toolbar_title)
    TextView mTTitle;
    @BindView(R2.id.scanner_toolbar_more)
    ImageView mTMore;
    @BindView(R2.id.titlebar)
    Toolbar mToolbar;
    @BindView(R2.id.text)
    EditText mText;
    @BindView(R2.id.CreateQrCode)
    Button mCreateQrCodeBtn;
    @BindView(R2.id.QrCode)
    ImageView mQrCodeIv;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_create);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mTTitle.setText("二维码生成");
        mTMore.setImageResource(R.drawable.ic_download);

    }

    @OnClick(R2.id.CreateQrCode)
    public void createQRCode() {

        try {
            //获取输入的文本信息
            String str = mText.getText().toString().trim();
            if(str != null && !"".equals(str.trim())){
                //根据输入的文本生成对应的二维码并且显示出来
                String contents = new String(mText.getText().toString().getBytes("UTF-8"), "ISO-8859-1");
                mBitmap = EncodingHandler.createQRCode(contents, 500);

                if(mBitmap != null){

                    Toast.makeText(this,"二维码生成成功！",Toast.LENGTH_SHORT).show();
                    mQrCodeIv.setImageBitmap(mBitmap);
                }
            }else{
                Toast.makeText(this,"文本信息不能为空！",Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R2.id.scanner_toolbar_more)
    public void saveToLocal(){
        if (mBitmap==null){
            LogUtils.i("mbitmap","null");
            return;
        }
        String path = AppConstant.CAMERA_DIR+ mText.getText().toString()+"-"+ SystemClock.elapsedRealtime()+".png";
        try {
            BitmapUtils.saveBitmapToSDCard(mBitmap, path);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @OnClick(R2.id.scanner_toolbar_back)
    public void back(){
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmap!=null) {
            mBitmap.recycle();
        }
    }
}
