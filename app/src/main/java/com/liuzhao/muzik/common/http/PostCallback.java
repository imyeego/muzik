package com.liuzhao.muzik.common.http;

public interface PostCallback<T> extends ICallback<T>{
    void success(T t);
    void fail(String err);
}
