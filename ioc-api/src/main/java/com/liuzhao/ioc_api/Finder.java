package com.liuzhao.ioc_api;

/**
 * Created by zhongyu on 2018/11/13.
 *
 * @author liuzhao
 */
public interface Finder<T> {
    void inject(T host, Object source, Provider provider);
}
