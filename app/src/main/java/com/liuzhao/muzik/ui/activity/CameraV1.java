package com.liuzhao.muzik.ui.activity;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;

/**
 * Created by lb6905 on 2017/6/27.
 */

public class CameraV1 implements Camera.PreviewCallback{
    private Activity mActivity;
    private int mCameraId;
    private Camera mCamera;
    private PreviewCallback mPreviewCallback;
    private int previewWidth, previewHeight;

    public CameraV1(Activity activity) {
        mActivity = activity;
    }

    public boolean openCamera(int screenWidth, int screenHeight, int cameraId) {
        try {
            mCameraId = cameraId;
            mCamera = Camera.open(mCameraId);
            Camera.Parameters parameters = mCamera.getParameters();
            Log.e("numbers of cameras", "" + Camera.getNumberOfCameras());
            parameters.set("orientation", "portrait");
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            parameters.setPreviewSize(800, 600);
            setCameraDisplayOrientation(mActivity, mCameraId, mCamera);
            mCamera.setParameters(parameters);
            previewWidth = mCamera.getParameters().getPreviewSize().width;
            previewHeight = mCamera.getParameters().getPreviewSize().height;
            mCamera.setPreviewCallbackWithBuffer(this);
            byte[] buffer = new byte[800 * 600 * ImageFormat.getBitsPerPixel(ImageFormat.NV21) * 2 / 3];
            mCamera.addCallbackBuffer(buffer);
            Log.i("lb6905", "open camera");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        Log.e("camera orientation", " " + info.orientation);
        Log.e("screen rotation", " " + rotation);

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        Log.e("result", " " + result);
        camera.setDisplayOrientation(result);

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mCamera != null && mPreviewCallback != null) {
            mCamera.addCallbackBuffer(data);
            mPreviewCallback.onPreviewFrame(data, previewWidth, previewHeight);
        }
    }

    public void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public void setPreviewTexture(SurfaceTexture surfaceTexture) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPreviewCallback(PreviewCallback mPreviewCallback) {
        this.mPreviewCallback = mPreviewCallback;
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public interface PreviewCallback {
        void onPreviewFrame(byte[] data, int width, int height);
    }
}
