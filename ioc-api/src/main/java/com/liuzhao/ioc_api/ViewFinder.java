package com.liuzhao.ioc_api;

import android.app.Activity;
import android.view.View;

/**
 *
 * @author liuzhao
 */
public class ViewFinder {
    private static final ActivityProvider PROVIDER_ACTIVITY = new ActivityProvider();
    private static final ViewProvider PROVIDER_VIEW = new ViewProvider();

    /**
     * @param activity
     */
    public static void inject(Activity activity) {
        inject(activity, activity, PROVIDER_ACTIVITY);
    }
    /**
     * @param host
     * @param view
     */
    public static void inject(Object host, View view) {
        // for fragment
        inject(host, view, PROVIDER_VIEW);
    }
    private static void inject(Object host, Object source, Provider provider) {
        Class<?> clazz = host.getClass();
        String proxyClassFullName = clazz.getName()+"$$Finder";
        Class<?> proxyClazz = null;
        try {
            proxyClazz = Class.forName(proxyClassFullName);
            Finder viewInjector = (Finder) proxyClazz.newInstance();
            viewInjector.inject(host, source, provider);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
