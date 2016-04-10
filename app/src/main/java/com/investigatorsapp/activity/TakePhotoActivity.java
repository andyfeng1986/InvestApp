package com.investigatorsapp.activity;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.investigatorsapp.R;
import com.investigatorsapp.logger.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fenglei on 16/1/23.
 */
public class TakePhotoActivity extends Activity implements View.OnClickListener, SurfaceHolder.Callback{

    private static final String TAG = "TakePhotoActivity";

    private ViewGroup surfaceViewVG;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private ViewGroup afterPhotoVG;
    private Button takeBtn;
    private Button commitBtn;
    private Button exitBtn;
    private Button retakeBtn;

    private Camera mCamera;

    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

    private String mSalerNo;
    private String mEnterTime;

    private ExecutorService mExector;

    private volatile boolean isPreviewCapturing = false;

    private Camera.PictureCallback picture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            mExector.execute(new Runnable() {
                @Override
                public void run() {
                    saveImage(data);
                }
            });
        }

    };

    private void saveImage(byte[] data) {
//        String fileName = Environment.getExternalStorageDirectory().getAbsolutePath()
//                + "/img.jpeg";
//        String fileName = Util.getPhotoFilePath(mSalerNo, mEnterTime);
//        Logger.i(TAG, "fileName = " + fileName + ", data = " + data.length);
//        File file = new File(fileName);
//        try {
//            OutputStream os = new FileOutputStream(file);
//            os.write(data, 0, data.length);
//            os.close();
//            setPictureDegree(file.getPath(), String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        isPreviewCapturing = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        surfaceViewVG = (ViewGroup) findViewById(R.id.vg_surface_view);
        mSurfaceView = (SurfaceView)findViewById(R.id.surface_view);
        afterPhotoVG = (ViewGroup) findViewById(R.id.vg_after_photo);
        takeBtn = (Button) findViewById(R.id.btn_take);
        commitBtn = (Button) findViewById(R.id.btn_commit);
        exitBtn = (Button) findViewById(R.id.btn_exit);
        retakeBtn = (Button) findViewById(R.id.btn_retake);
        takeBtn.setOnClickListener(this);
        commitBtn.setOnClickListener(this);
        exitBtn.setOnClickListener(this);
        retakeBtn.setOnClickListener(this);
        afterPhotoVG.setVisibility(View.GONE);
        takeBtn.setVisibility(View.VISIBLE);
        initSurfaceView();
        mExector = Executors.newSingleThreadExecutor();
        mSalerNo = getIntent().getStringExtra("saler_no");
        mEnterTime = getIntent().getStringExtra("enter_time");
    }

    private void initSurfaceView() {
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            Logger.i(TAG, "surfaceCreated");
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            Logger.i(TAG, "surfaceChanged, w = " + width + ", h = " + height);
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
            Camera.Size picSize = findNearestSize(pictureSizes);
            if(picSize != null) {
                parameters.setPictureSize(picSize.width, picSize.height);
            }
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            Camera.Size previewSize = findNearestSize(previewSizes);
            if(previewSize != null) {
                parameters.setPreviewSize(previewSize.width, previewSize.height);
            }
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setFocusMode("auto");
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Camera.Size findNearestSize(List<Camera.Size> sizeList) {
        Camera.Size currentSize = null;
        if(sizeList != null) {
            for(int i = 0; i < sizeList.size(); i++) {
                Camera.Size size = sizeList.get(i);
                if(size != null) {
                    if(currentSize == null) {
                        currentSize = size;
                    }
                    if(Math.abs(size.width - DEFAULT_WIDTH) <
                            Math.abs(currentSize.width - DEFAULT_WIDTH)) {
                        currentSize = size;
                    }
                }
            }
        }
        if(currentSize != null) {
            Logger.i(TAG, "currentSize w = " + currentSize.width + ", h = " + currentSize.height);
        }else {
            Logger.i(TAG, "currentSize is null");
        }
        return currentSize;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logger.i(TAG, "surfaceDestroyed");
        if(mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
//        if(v.equals(commitBtn)) {
//            finish();
//        }else if(v.equals(exitBtn)) {
//            String fileName = Util.getPhotoFilePath(mSalerNo, mEnterTime);
//            File file = new File(fileName);
//            if(file.exists()) {
//                file.delete();
//            }
//            finish();
//        }else if(v.equals(retakeBtn)) {
//            if(!isPreviewCapturing) {
//                String fileName = Util.getPhotoFilePath(mSalerNo, mEnterTime);
//                File file = new File(fileName);
//                if(file.exists()) {
//                    file.delete();
//                }
//                afterPhotoVG.setVisibility(View.GONE);
//                takeBtn.setVisibility(View.VISIBLE);
//                mCamera.startPreview();
//            }
////            Log.i("Test", "retakeBtn, mCamear = " + mCamera);
////
//        }else if(v.equals(takeBtn)) {
//            isPreviewCapturing = true;
//            afterPhotoVG.setVisibility(View.VISIBLE);
//            takeBtn.setVisibility(View.GONE);
//            mCamera.takePicture(null, null, picture);
//            //mCamera.stopPreview();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.i(TAG, "onPause, mCamera = " + mCamera);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.i(TAG, "onDestroy");
    }

    private  int readPictureDegree(String path) {
        int degree  = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);

            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    degree = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    private void setPictureDegree(String path, String degree) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, degree);
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
