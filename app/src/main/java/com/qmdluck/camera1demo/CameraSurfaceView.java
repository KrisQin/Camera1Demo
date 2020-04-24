package com.qmdluck.camera1demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private final static String TAG = "CameraSurfaceView";
    private SurfaceHolder surfaceHolder;
    private int mCameraIndex = 0;
    private Camera mCamera;
    private int mRotate = 0;
    private int mWidth = 0;
    private int mHeight = 0;

    private onPreviewChangedListener mOnPreviewChangedListener = null;
    private ImageStack imgStack = null;

    public static final int DEFAULT_CAMERA_IMAGE_WIDTH = 320;
    public static final int DEFAULT_CAMERA_IMAGE_HEIGHT = 240;

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 打开 cameraId = 1 的摄像头
     * 预览画面默认是横屏的，需要旋转90度
     */
    public void openCamera() {

        openCamera(1, 90, DEFAULT_CAMERA_IMAGE_WIDTH, DEFAULT_CAMERA_IMAGE_HEIGHT);

    }

    /**
     * 打开摄像头
     *
     * @param cameraId          摄像头序号
     * @param rotate            预览画面旋转角度
     * @param cameraImageWidth  图像preview宽度
     * @param cameraImageHeight 图像preview高度
     */
    public void openCamera(int cameraId, int rotate, int cameraImageWidth, int cameraImageHeight) {

        log("开始 打开摄像头--------");

        int mNumberOfCameras = Camera.getNumberOfCameras();
        log(" mNumberOfCameras:  " + mNumberOfCameras);

        if (mNumberOfCameras > cameraId) {
            initCameraView(cameraId, rotate, cameraImageWidth, cameraImageHeight);
        }
    }


    /**
     * @param cameraIndex 摄像头序号
     * @param rotate      预览画面旋转角度
     * @param width       图像preview宽度
     * @param height      图像preview高度
     */
    private void initCameraView(int cameraIndex, int rotate, int width, int height) {
        log(String.format("CameraView initCameraView() cameraIndex:%d rotate:%d width:%d height:%d", cameraIndex, rotate, width, height));
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
        mCameraIndex = cameraIndex;
        mRotate = rotate;
        mWidth = width;
        mHeight = height;
        imgStack = new ImageStack(mWidth, mHeight);
    }


    /**
     * 初始化SurfaceView时调用一次，另外更改surface或者onpause->onresume时调用
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        log("CameraView surfaceChanged()");
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            // 选择合适的图片尺寸，必须是手机支持的尺寸
            /*
            List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
            // 如果sizeList只有一个我们也没有必要做什么了，因为就他一个别无选择
            if (sizeList.size() > 1) {
            	int w = 0;
            	int h = 0;
                for (int j = 0; j < sizeList.size(); j++) {
                    Camera.Size size = sizeList.get(j);
                    w = size.width;
                    h = size.height;
                    log("w:" + w + " h:" + h);
                }
            }
            */
            //设置照片的大小
            log("setPictureSize mWidth:" + mWidth + " mHeight:" + mHeight);
            parameters.setPreviewSize(mWidth, mHeight);
            parameters.setPictureSize(mWidth, mHeight);
            parameters.setPreviewFpsRange(10, 20);
            try {
                mCamera.setParameters(parameters);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.setPreviewCallback(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //调用相机预览功能
            mCamera.startPreview();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        log("CameraView surfaceCreated()");
        if (null == mCamera) {
            //Camera.open()方法说明：2.3以后支持多摄像头，所以开启前可以通过getNumberOfCameras先获取摄像头数目，
            // 再通过 getCameraInfo得到需要开启的摄像头id，然后传入Open函数开启摄像头，
            // 假如摄像头开启成功则返回一个Camera对象
            try {
                mCamera = Camera.open(mCameraIndex);
                //预览画面默认是横屏的，需要旋转90度
                mCamera.setDisplayOrientation(mRotate);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        log("CameraView surfaceDestroyed()");
        if (null != mCamera) {
            try {
                mCamera.setPreviewDisplay(null);
                mCamera.setPreviewCallback(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //停止预览
            mCamera.stopPreview();
            //释放相机资源
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        boolean isPreview = false;
        if (data != null) {
            isPreview = true;
            if (imgStack != null) {
                //log("imgStack.pushImageInfo() mCameraIndex:" + mCameraIndex);
                imgStack.pushImageInfo(data, System.currentTimeMillis());
            }
        }

        onPreviewChanged(isPreview);

    }

    public void setOnPreviewChangedListener(onPreviewChangedListener mPreviewChangedListener) {
        this.mOnPreviewChangedListener = mPreviewChangedListener;
    }

    public interface onPreviewChangedListener {
        void onPreviewChanged(Bitmap bitmap);
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }

    private static boolean isDisplay = false;

    public void cameraResume() {
        // TODO Auto-generated method stub
        isDisplay = true;
        if (faceDetectTask != null) {
            faceDetectTask.setStop(false);
        }

    }

    public void cameraPause() {
        // TODO Auto-generated method stub
        isDisplay = false;
        if (faceDetectTask != null) {
            faceDetectTask.setStop(true);
        }

    }


    public void cameraDestory() {
        // TODO Auto-generated method stub
        if (faceDetectTask != null) {
            faceDetectTask.setStop(true);
            faceDetectTask.cancel(true);
            faceDetectTask = null;
        }
    }


    public void onPreviewChanged(boolean isPreview) {
        if (isDisplay && isPreview && !isRunning) {
            log("====onPreviewChanged() isPreview:" + isPreview);
            startRecognizeUser();
        }
    }


    private boolean isRunning = false;
    private FaceDetectTask faceDetectTask;

    public void startRecognizeUser() {
        log("startRecognizeUser isRunning: " + isRunning);
        if (!isRunning) {
            isRunning = true;
            log("startRecognizeUser isRunning: " + isRunning);

            if (imgStack != null) {
                // 清空缓存记录
                imgStack.clearAll();
            }

            if (faceDetectTask != null) {
                faceDetectTask.setStop(true);
                faceDetectTask = null;
            }
            faceDetectTask = new FaceDetectTask();
            faceDetectTask.setStop(false);
            faceDetectTask.execute();
        }
    }

    /**
     * 定义一个类，让其继承AsyncTask这个类
     */
    class FaceDetectTask extends AsyncTask<Void, Rect, Void> {

        private boolean isStop = false;

        public void setStop(boolean isStop) {
            this.isStop = isStop;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            ImageInfo imgInfo = null;

            while (!isStop && isRunning) {

                if (imgStack != null)
                    imgInfo = imgStack.pullImageInfo();

                if (!isStop && isRunning && imgInfo != null && imgInfo.isNew()) {

                    log("doInBackground imgInfo.isNew(): " + imgInfo.isNew());
                    byte[] data = imgInfo.getData();

                    Log.d(TAG, data.toString());

                    Bitmap bitmapCamera = null;

                    if (null != data) {
                        //获取图片
                        ByteArrayOutputStream baos;
                        byte[] rawImage;
                        BitmapFactory.Options newOpts = new BitmapFactory.Options();
                        newOpts.inJustDecodeBounds = true;
                        YuvImage yuvimage = new YuvImage(
                                data,
                                ImageFormat.NV21,
                                DEFAULT_CAMERA_IMAGE_WIDTH,
                                DEFAULT_CAMERA_IMAGE_HEIGHT,
                                null);
                        baos = new ByteArrayOutputStream();
                        yuvimage.compressToJpeg(new Rect(0, 0, DEFAULT_CAMERA_IMAGE_WIDTH, DEFAULT_CAMERA_IMAGE_HEIGHT), 100, baos);// 80--JPG图片的质量[0-100],100最高
                        rawImage = baos.toByteArray();
                        //将rawImage转换成bitmap
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        //获取到图片
                        Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);

                        //摄像头拍出来的图片，默认是横着的，所以需要对图片进行旋转
                        if (bitmap != null) {
                            Matrix matrix = new Matrix();
                            matrix.postRotate((float) 90);
                            bitmapCamera = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                        }

                        Log.d(TAG, "dealData 获取到的图片 bitmapCamera --> " + bitmapCamera);

                    }

                    if (bitmapCamera != null) {

                        // TODO 获取到图片
                        Message message = Message.obtain();
                        message.what = 12341;
                        message.obj = bitmapCamera;
                        handler.sendMessage(message);
                        
                    }
                }
            }

            return null;
        }

    }
    
    

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 12341) {

                Bitmap bitmap = (Bitmap) msg.obj;

                if (mOnPreviewChangedListener != null) {
                    
                    mOnPreviewChangedListener.onPreviewChanged(bitmap);
                }
            }
        }
    };

}
