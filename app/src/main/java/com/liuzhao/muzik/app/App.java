package com.liuzhao.muzik.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDexApplication;

//import com.tencent.tinker.loader.app.ApplicationLike;
//import com.tinkerpatch.sdk.TinkerPatch;
//import com.tinkerpatch.sdk.loader.TinkerPatchApplicationLike;

import org.xutils.x;

/**
 * Created by zhongyu on 2018/10/22.
 *
 * @author liuzhao
 */
public class App extends Application {

    private static App instance;
//    private ApplicationLike mApplicationLike;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        x.Ext.init(this);
//        mApplicationLike = TinkerPatchApplicationLike.getTinkerPatchApplicationLike();
//        // 初始化TinkerPatch SDK, 更多配置可参照API章节中的,初始化SDK
//        TinkerPatch.init(mApplicationLike)
//                .reflectPatchLibrary()
//                .fetchPatchUpdate(true)
//                // 强制更新
//                .setPatchRollbackOnScreenOff(true)
//                .setPatchRestartOnSrceenOff(true)
//                .setFetchPatchIntervalByHours(3);
//
//        // 每隔3个小时(通过setFetchPatchIntervalByHours设置)去访问后台时候有更新,
//        //通过handler实现轮训的效果
//        TinkerPatch.with().fetchPatchUpdateAndPollWithInterval();
    }

    public static Context getContext(){
        return instance;
    }
}
