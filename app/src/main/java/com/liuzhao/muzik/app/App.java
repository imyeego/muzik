package com.liuzhao.muzik.app;

import android.app.Application;
import android.content.Context;

import org.xutils.x;

/**
 * Created by zhongyu on 2018/10/22.
 *
 * @author liuzhao
 */
public class App extends Application {

    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        x.Ext.init(this);
    }

    public static Context getContext(){
        return instance;
    }
}
