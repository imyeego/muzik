package com.liuzhao.muzik.ui.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.liuzhao.muzik.R;

/**
 *
 * @author liuzhao
 */
public abstract class BaseActivity<P extends BasePresenter> extends Activity {

    protected P presenter;
    protected Context context;
    protected TextView tvTitle;
    protected FrameLayout flContent;
    protected ImageView ivLeft;
    protected TextView tvNetworkStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        tvTitle = findViewById(R.id.tv_title);
        ivLeft = findViewById(R.id.iv_left);
        flContent = findViewById(R.id.content);
        LayoutInflater.from(this).inflate(getLayoutId(), flContent);
        context = this;
        if (presenter == null) getPresenter();
        initView();
    }

    protected abstract void initView();

    protected abstract P getPresenter();

    protected abstract int getLayoutId();


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
