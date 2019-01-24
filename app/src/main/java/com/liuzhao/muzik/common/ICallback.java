package com.liuzhao.muzik.common;


import java.util.List;

/**
 * Created by zhongyu on 2018/10/23.
 *
 * @author liuzhao
 */
public interface ICallback<T> {

    void onSuccess(List<T> result);
    void onFailure();
}
