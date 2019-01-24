package com.liuzhao.muzik.common;

import com.liuzhao.muzik.app.Constants;
import com.liuzhao.muzik.model.bean.News;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 *
 * @author liuzhao
 */
public class RetrofitClient {

    private static String TAG = "network";
    private static volatile RetrofitClient retrofitClient = null;
    private static ApiService api;
    private static Retrofit retrofit;


    public static RetrofitClient getInstance(){
        if (retrofit == null) {
            synchronized (RetrofitClient.class){
                if (retrofit == null) {
                    getClient();
                }
            }
        }

        return retrofitClient;
    }

    private static void getClient(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .addNetworkInterceptor(new LoggingInterceptor())
                .build();
        retrofitClient = new RetrofitClient();
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.DOUBAN_TOP250)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .build();

        api = retrofit.create(ApiService.class);
    }

    public <T> void getNews(ICallback<T> callback){
        Call<ResponseBody> enqueue = api.getNews("top", Constants.JUHE_APIKEY);
        enqueue.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                News<T> news = Convert.parseResponse(response.body(), callback);
                List<T> list = news.getResult().getData();
                callback.onSuccess(list);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure();
            }
        });

    }

    public <T> Subscription getMovies(ICallback<T> callback){
        return api.getMovie("0", "5").subscribeOn(Schedulers.io())
                .map(responseBody -> Convert.parseResponseBody(responseBody, callback))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<T>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onFailure();
                    }

                    @Override
                    public void onNext(List<T> ts) {
                        callback.onSuccess(ts);
                    }
                });
    }


}
