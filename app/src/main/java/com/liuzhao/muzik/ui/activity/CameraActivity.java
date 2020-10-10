package com.liuzhao.muzik.ui.activity;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.liuzhao.ioc_annotations.BindView;
import com.liuzhao.ioc_annotations.OnClick;
import com.liuzhao.ioc_api.ViewFinder;
import com.liuzhao.muzik.R;
import com.liuzhao.muzik.gl.GLRenderer;
import com.liuzhao.muzik.presenter.NewsPresenter;
import com.liuzhao.muzik.ui.base.BaseActivity;
import com.liuzhao.muzik.utils.BitmapUtil;

/**
 * @authur : liuzhao
 * @time : 2020/10/9 2:47 PM
 * @Des :
 */
public class CameraActivity extends BaseActivity<NewsPresenter> implements CameraV1.PreviewCallback{

    @BindView(R.id.texture_view)
    TextureView textureView;
    @BindView(R.id.iv)
    ImageView imageView;
    @BindView(R.id.btn_capture)
    Button btnCapture;

    private SurfaceTexture surfaceTexture;
    private CameraV1 mCamera;
    private GLRenderer mRenderer;

    @Override
    protected void initView() {
        ViewFinder.inject(this);
        textureView.setSurfaceTextureListener(mTextureListener);
    }

    @Override
    protected NewsPresenter getPresenter() {
        return null;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_camera;
    }

    public TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mRenderer = new GLRenderer();

            mRenderer.init(textureView, CameraActivity.this);
            surfaceTexture = mRenderer.createOESTexture();

            mCamera = new CameraV1(CameraActivity.this);
            if (!mCamera.openCamera()) {
                return;
            }

            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.setPreviewCallback(CameraActivity.this);
            mCamera.startPreview();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.releaseCamera();
                mCamera = null;
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @OnClick(R.id.btn_capture)
    void onTakenPic(View v) {
        isTaken = true;
    }

    boolean isTaken;

    @Override
    public void onPreviewFrame(byte[] data, int width, int height) {
        if (isTaken) {
            isTaken = false;
            display(data, width, height);
        }
    }

    private void display(byte[] data, int width, int height) {
        long start = System.currentTimeMillis();
        Bitmap bitmap = BitmapUtil.nv21ToBitmap(data, width, height, 0);
        Log.e("convert cost", "" + (System.currentTimeMillis() - start) + "");
        imageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.releaseCamera();
            mCamera = null;
        }
    }
}
