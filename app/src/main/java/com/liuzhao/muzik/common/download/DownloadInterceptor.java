package com.liuzhao.muzik.common.download;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by zhongyu on 2018/11/8.
 *
 * @author liuzhao
 */
public class DownloadInterceptor implements Interceptor {

    private DownloadListener listener;

    public DownloadInterceptor(DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        return response.newBuilder()
                .body(new DownLoadResponse(response.body(), listener))
                .build();
    }
}
