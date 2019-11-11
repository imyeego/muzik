package com.liuzhao.muzik.common.http;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.liuzhao.muzik.app.Constants;
import com.liuzhao.muzik.common.ApiService;
import com.liuzhao.muzik.common.ICallback;
import com.liuzhao.muzik.common.LoggingInterceptor;
import com.liuzhao.muzik.common.RetrofitClient;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Http {

    private static final String TAG = "http";
    private static final int SUCCESS = 0x4f01;
    private static final int FAIL = 0x4f00;
    private static final int PROGRESS = 0x4f02;
    private static Retrofit retrofit;
    private static APIService service;
    private static Http http;
    private Handler handler;

    public static Http instance() {
        if (http == null) {
            synchronized (Http.class) {
                if (http == null) {
                    http = new Http();
                }
            }
        }
        return http;
    }
    private Http() {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SUCCESS:
                        break;
                    case FAIL:
                        break;
                    case PROGRESS:
                        break;
                }
            }
        };
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .readTimeout(3000, TimeUnit.SECONDS)
                .connectTimeout(2000, TimeUnit.SECONDS)
                .addNetworkInterceptor(new LoggingInterceptor())
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.MY_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .build();

        service = retrofit.create(APIService.class);
    }

    public <T> void postMap(String url, Map<String, Object> map, ICallback<T> callback) {
        Call<ResponseBody> call = service.postMap(url, map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
