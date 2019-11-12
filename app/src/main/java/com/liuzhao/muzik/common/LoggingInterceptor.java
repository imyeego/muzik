package com.liuzhao.muzik.common;

import com.liuzhao.muzik.common.download.DownloadListener;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.net.URLDecoder;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by zhongyu on 2018/11/8.
 *
 * @author Ann
 */
public class LoggingInterceptor implements Interceptor {
    final static String CONTENT_DISPOSITION = "Content-Disposition";

    private String fileName;

    private DownloadListener listener;

    public LoggingInterceptor(){}

    public LoggingInterceptor(DownloadListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();

        //1.请求前--打印请求信息
        long t1 = System.nanoTime();
        Logger.i(String.format("Sending request %s%n%s%n%s",
                request.url(), request.headers(), request.body().toString()));

        //2.网络请求
        Response response = chain.proceed(request);
        MediaType mediaType = response.body().contentType();
//        String content= response.body().string();

        //3.网络响应后--打印响应信息
        long t2 = System.nanoTime();

        Logger.i(String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));

//        String result = response.headers().get(CONTENT_DISPOSITION);
//        if (result != null && fileName == null) {
//            fileName = URLDecoder.decode(result.split(";")[1].trim().substring(9), "UTF-8");
//            listener.setFileName(fileName);
//        }
        return response;

    }

}
