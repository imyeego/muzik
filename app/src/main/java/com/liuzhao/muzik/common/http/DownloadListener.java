package com.liuzhao.muzik.common.http;

public interface DownloadListener{
    void onStart();

    void onProgress(int progress);

    void onFinish();

    void onFail(Throwable t);

}
