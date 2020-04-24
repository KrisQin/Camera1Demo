package com.qmdluck.camera1demo;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.Toast;


/**
 * 获取相机图片
 */
public class Camera2Activity extends Activity implements CameraSurfaceView.onPreviewChangedListener {



    private CameraSurfaceView surfaceView;
    private ImageView image;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        surfaceView = (CameraSurfaceView) findViewById(R.id.surfaceView);
        surfaceView.setOnPreviewChangedListener(this);

        image = findViewById(R.id.image);

        if (!checkedHardware(this)) {
            Toast.makeText(this, "no camera", Toast.LENGTH_LONG).show();
        }else {
            surfaceView.openCamera();
        }
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        surfaceView.cameraResume();

    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        surfaceView.cameraPause();

    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub

        surfaceView.cameraDestory();

        super.onDestroy();
    }


    @SuppressWarnings("unused")
    private boolean checkedHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        return false;
    }


    @Override
    public void onPreviewChanged(Bitmap bitmap) {
        image.setImageBitmap(bitmap);
    }
}