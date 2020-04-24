package com.qmdluck.camera1demo;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

public class MainActivity  extends Activity  {



    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });


    }

    public static final int requestOpenCamera = 10;

    private void openCamera() {
        //打开摄像头
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestOpenCamera);

            } else {
                startActivity();
            }
        } else {
            startActivity();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        if (requestCode == MainActivity.requestOpenCamera) {

            boolean isCan = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    isCan = false;
                }
            }

            if (isCan) {
                startActivity();
            }else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }

        }

    }

    private void startActivity() {
        startActivity(new Intent(this,Camera2Activity.class));
    }

}
