package com.liuzhao.muzik.app;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;

import com.liuzhao.muzik.database.AppDatabase;
import com.liuzhao.muzik.tinker.log.MyLogImp;
import com.liuzhao.muzik.tinker.util.SampleApplicationContext;
import com.liuzhao.muzik.tinker.util.TinkerManager;
import com.liuzhao.muzik.utils.SharedPreferencesUtil;
import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.entry.DefaultApplicationLike;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 * @authur : liu zhao
 * @time : 2020/9/24 下午 4:30
 * @Des :
 */
@SuppressWarnings("unused")
@DefaultLifeCycle(application = "com.liuzhao.muzik.app.MyApp",// application类名。只能用字符串，这个MyApplication文件是不存在的，但可以在AndroidManifest.xml的application标签上使用（name）
        flags = ShareConstants.TINKER_ENABLE_ALL,// tinkerFlags
        loaderClass = "com.tencent.tinker.loader.TinkerLoader",//loaderClassName, 我们这里使用默认即可!（可不写）
        loadVerifyFlag = false)//tinkerLoadVerifyFlag
public class TinkerApplicationLike extends DefaultApplicationLike {

    private static Context instance;
    private AppDatabase appDatabase;
    private SharedPreferencesUtil spUtil = new SharedPreferencesUtil();


    public TinkerApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag,
                                 long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }

    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        instance = base;

        MultiDex.install(base);

        SampleApplicationContext.application = getApplication();
        SampleApplicationContext.context = getApplication();
        TinkerManager.setTinkerApplicationLike(this);

        TinkerManager.initFastCrashProtect();
        //should set before tinker is installed
        TinkerManager.setUpgradeRetryEnable(true);

        //optional set logIml, or you can use default debug log
        TinkerInstaller.setLogIml(new MyLogImp());

        //installTinker after load multiDex
        //or you can put com.tencent.tinker.** to main dex
        TinkerManager.installTinker(this);
        Tinker tinker = Tinker.with(getApplication());

        spUtil.init(base, "muzik");
        appDatabase = Room.databaseBuilder(base, AppDatabase.class, Constants.DATA_PATH)
                .allowMainThreadQueries()
                .build();
    }

    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        getApplication().registerActivityLifecycleCallbacks(callback);
    }
    public static Context getContext(){
        return instance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }

}
