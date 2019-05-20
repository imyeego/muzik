package com.liuzhao.muzik.common;

public interface EntityCallback<T> {
    void onSuccess(T result);
    void onFailure();
}
