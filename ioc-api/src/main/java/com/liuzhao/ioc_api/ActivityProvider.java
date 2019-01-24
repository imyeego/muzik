package com.liuzhao.ioc_api;

import android.app.Activity;
import android.view.View;

/**
 *
 * @author liuzhao
 */
public class ActivityProvider implements Provider {

    @Override
    public View findView(Object source, int id) {
        return ((Activity) source).findViewById(id);
    }


}
