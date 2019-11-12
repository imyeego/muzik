package com.liuzhao.muzik.common.http;

public interface UploadListener<T> extends ICallback<T>{
    void onStart();

    void onProgress(int progress);

    void onFinish(T t);

    void onFail(Throwable t);
}
