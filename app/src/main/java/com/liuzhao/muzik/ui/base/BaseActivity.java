package com.liuzhao.muzik.ui.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 *
 * @author liuzhao
 */
public abstract class BaseActivity<P extends BasePresenter> extends Activity {

    protected P presenter;
    protected Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
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
