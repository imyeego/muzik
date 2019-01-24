package com.liuzhao.muzik.common;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 *
 * @author liuzhao
 */
public interface ApiService {

    @GET("/toutiao/index")
    Call<ResponseBody> getNews(@Query("type")String type, @Query("key")String apiKey);

    @GET("top250")
    Observable<ResponseBody> getMovie(@Query("start")String start, @Query("count")String count);

    @Streaming
    @GET
    Observable<ResponseBody> download(@Header("RANGE") String start, @Url String url);
}
