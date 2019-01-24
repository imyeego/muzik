package com.liuzhao.muzik.app;

import android.app.Application;
import android.content.Context;

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
    }

    public static Context getContext(){
        return instance;
    }
}
