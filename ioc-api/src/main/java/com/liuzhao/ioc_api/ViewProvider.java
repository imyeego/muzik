package com.liuzhao.ioc_api;

import android.view.View;

/**
 *
 * @author liuzhao
 */
public class ViewProvider implements Provider {
    @Override
    public View findView(Object source, int id) {
        return ((View) source).findViewById(id);
    }
}
