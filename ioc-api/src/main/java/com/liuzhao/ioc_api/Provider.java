package com.liuzhao.ioc_api;

import android.view.View;

/**
 *
 * @author liuzhao
 */
public interface Provider {
    View findView(Object source, int id);
}
